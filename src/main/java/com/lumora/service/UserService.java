package com.lumora.service;

import com.lumora.auth.AuthResponse;
import com.lumora.auth.LoginRequest;
import com.lumora.dto.UserRequest;
import com.lumora.dto.UserResponse;
import com.lumora.exception.ConflictException;
import com.lumora.exception.ResourceNotFoundException;
import com.lumora.model.User;
import com.lumora.repository.UserRepository;
import com.lumora.security.JwtService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@Transactional(readOnly = true)
public class UserService {

    private static final int MAX_ATTEMPTS = 5;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            @Lazy AuthenticationManager authenticationManager
    ) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    // =========================================================
    // REGISTER
    // =========================================================
    @Transactional
    public AuthResponse register(UserRequest request) {

        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email já cadastrado");
        }

        User user = User.builder()
                .fullName(request.fullName().trim())
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .role(User.Role.GUEST)
                .enabled(true)
                .accountNonLocked(true)
                .failedAttempts(0)
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user);

        log.info("Usuário registrado: {}", email);

        return new AuthResponse(
                token,
                3600000L,
                user.getRole().name()
        );
    }

    // =========================================================
    // LOGIN
    // =========================================================
    @Transactional
    public AuthResponse login(LoginRequest request) {

        String email = request.email().trim().toLowerCase();

        try {

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            email,
                            request.password()
                    )
            );

        } catch (BadCredentialsException ex) {

            User user = userRepository.findByEmail(email).orElse(null);

            if (user != null) {
                handleFailedLogin(user);
            }

            throw new BadCredentialsException("Credenciais inválidas");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Usuário não encontrado"));

        if (!user.isAccountNonLocked()) {
            throw new LockedException(
                    "Conta bloqueada por excesso de tentativas."
            );
        }

        userRepository.resetFailedAttempts(email);

        String token = jwtService.generateToken(user);

        log.info("Login realizado: {}", email);

        return new AuthResponse(
                token,
                3600000L,
                user.getRole().name()
        );
    }

    // =========================================================
    // CURRENT USER
    // =========================================================
    public User getCurrentUser() {

        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth.getName() == null) {
            throw new UsernameNotFoundException(
                    "Usuário não autenticado"
            );
        }

        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User",
                                "email",
                                auth.getName()
                        )
                );
    }

    public UserResponse getProfile() {
        return UserResponse.from(getCurrentUser());
    }

    // =========================================================
    // ADMIN
    // =========================================================
    public UserResponse getById(UUID id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User",
                                "id",
                                id
                        )
                );

        return UserResponse.from(user);
    }

    public Page<UserResponse> listAll(Pageable pageable) {
        return userRepository
                .findAll(pageable)
                .map(UserResponse::from);
    }

    @Transactional
    public void updateRole(UUID id, User.Role role) {

        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "User",
                    "id",
                    id
            );
        }

        userRepository.updateRole(id, role);
    }

    @Transactional
    public void toggleEnabled(UUID id, boolean status) {

        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "User",
                    "id",
                    id
            );
        }

        userRepository.updateEnabledStatus(id, status);
    }

    // =========================================================
    // FAILED LOGIN
    // =========================================================
    private void handleFailedLogin(User user) {

        int attempts = user.getFailedAttempts() + 1;

        userRepository.incrementFailedAttempts(user.getEmail());

        if (attempts >= MAX_ATTEMPTS) {

            userRepository.lockAccount(user.getEmail());

            log.warn("Conta bloqueada: {}", user.getEmail());

            throw new LockedException(
                    "Conta bloqueada por excesso de tentativas."
            );
        }
    }
}