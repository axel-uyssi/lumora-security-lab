package com.lumora.auth;

import com.lumora.model.User;
import com.lumora.repository.UserRepository;
import com.lumora.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    // ───────────────── REGISTER ─────────────────

    public AuthResponse register(RegisterRequest request) {

        if (repository.existsByEmail(request.email())) {
            throw new RuntimeException("Email já cadastrado");
        }

        User user = User.builder()
                .fullName(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(User.Role.GUEST)
                .enabled(true)
                .accountNonLocked(true)
                .failedAttempts(0)
                .build();

        repository.save(user);

        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .build();
    }

    // ───────────────── LOGIN ─────────────────

    public AuthResponse login(LoginRequest request) {

        User user = repository.findByEmail(request.email())
                .orElseThrow(() ->
                        new BadCredentialsException("Credenciais inválidas"));

        if (!user.isEnabled()) {
            throw new DisabledException("Conta desativada");
        }

        if (!user.isAccountNonLocked()) {
            throw new LockedException("Conta bloqueada");
        }

        try {

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.password()
                    )
            );

            repository.resetFailedAttempts(user.getEmail());

            String token = jwtService.generateToken(user);

            return AuthResponse.builder()
                    .token(token)
                    .build();

        } catch (BadCredentialsException ex) {

            repository.incrementFailedAttempts(user.getEmail());

            int newAttempts = user.getFailedAttempts() + 1;

            if (newAttempts >= MAX_FAILED_ATTEMPTS) {
                repository.lockAccount(user.getEmail());

                throw new LockedException(
                        "Conta bloqueada por excesso de tentativas"
                );
            }

            throw new BadCredentialsException(
                    "Email ou senha inválidos"
            );
        }
    }
}
