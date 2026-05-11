package com.lumora.controller;

import com.lumora.dto.*;
import com.lumora.model.User;
import com.lumora.auth.AuthResponse;
import com.lumora.auth.LoginRequest;
import com.lumora.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Autenticação e gerenciamento de usuários")
public class UserController {

    private final UserService userService;

    // ─────────────────────────────────────────────────────────────
    // 🔓 AUTENTICAÇÃO (PÚBLICO)
    // ─────────────────────────────────────────────────────────────

    @PostMapping("/register")
    @Operation(summary = "Criar nova conta")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody UserRequest req
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(
                        "Conta criada com sucesso",
                        userService.register(req)
                ));
    }

    @PostMapping("/login")
    @Operation(summary = "Login — retorna token JWT")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest req
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(userService.login(req))
        );
    }

    // ─────────────────────────────────────────────────────────────
    // 🔐 USUÁRIO AUTENTICADO
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/me")
    @Operation(
            summary = "Perfil do usuário autenticado",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<UserResponse>> me() {
        return ResponseEntity.ok(
                ApiResponse.ok(userService.getProfile())
        );
    }

    // ─────────────────────────────────────────────────────────────
    // 🛠️ ADMIN
    // ─────────────────────────────────────────────────────────────

    @GetMapping
    @Operation(
            summary = "Listar usuários (ADMIN)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> listAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        Pageable pageable = PageRequest.of(
                page,
                Math.min(size, 50),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return ResponseEntity.ok(
                ApiResponse.ok(
                        PagedResponse.from(userService.listAll(pageable))
                )
        );
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar usuário por ID (ADMIN)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<UserResponse>> getById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(userService.getById(id))
        );
    }

    @PatchMapping("/{id}/role")
    @Operation(
            summary = "Alterar role do usuário (ADMIN)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<Void>> updateRole(
            @PathVariable UUID id,
            @RequestParam User.Role role
    ) {
        userService.updateRole(id, role);

        return ResponseEntity.ok(
                ApiResponse.ok("Role atualizada", null)
        );
    }

    @PatchMapping("/{id}/toggle")
    @Operation(
            summary = "Ativar/desativar conta (ADMIN)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<Void>> toggleEnabled(
            @PathVariable UUID id,
            @RequestParam boolean enabled
    ) {
        userService.toggleEnabled(id, enabled);

        return ResponseEntity.ok(
                ApiResponse.ok(
                        enabled ? "Conta ativada" : "Conta desativada",
                        null
                )
        );
    }
}