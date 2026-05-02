package com.lumora.controller;

import com.lumora.dto.ApiResponse;
import com.lumora.dto.PagedResponse;
import com.lumora.dto.UserRequest;
import com.lumora.dto.UserResponse;
import com.lumora.dto.*;
import com.lumora.model.User;
import com.lumora.security.*;
import com.lumora.security.AuthResponse;
import com.lumora.security.LoginRequest;
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

// ─────────────────────────────────────────────────────────────────────────────
// CONTROLLER/USERCONTROLLER.JAVA
//
// Responsável pelos endpoints de:
//   - Registro e login (públicos)
//   - Perfil do usuário autenticado
//   - Gerenciamento de usuários (admin)
//
// @RestController = @Controller + @ResponseBody
//   Todos os métodos retornam JSON automaticamente via Jackson
//
// @Valid → ativa as validações do DTO antes de executar o método
//   Se falhar → MethodArgumentNotValidException → GlobalExceptionHandler → 400
// ─────────────────────────────────────────────────────────────────────────────

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Registro, login e gerenciamento de usuários")
public class UserController {

    private final UserService userService;

    // ── Endpoints públicos ────────────────────────────────────────────────────

    // POST /api/v1/users/register
    @PostMapping("/register")
    @Operation(summary = "Criar nova conta de hóspede")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody UserRequest req
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Conta criada com sucesso", userService.register(req)));
    }

    // POST /api/v1/users/login
    @PostMapping("/login")
    @Operation(summary = "Login — retorna token JWT")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest req
    ) {
        return ResponseEntity.ok(ApiResponse.ok(userService.login(req)));
    }

    // ── Endpoints autenticados ────────────────────────────────────────────────

    // GET /api/v1/users/me
    @GetMapping("/me")
    @Operation(summary = "Perfil do usuário autenticado",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<UserResponse>> me() {
        return ResponseEntity.ok(ApiResponse.ok(userService.getProfile()));
    }

    // ── Endpoints administrativos (ADMIN) ────────────────────────────────────

    // GET /api/v1/users?page=0&size=20
    @GetMapping
    @Operation(summary = "Listar todos os usuários (ADMIN)",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> listAll(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(
                ApiResponse.ok(
                        PagedResponse.from(userService.listAll(pageable))
                )
        );
    }

    // GET /api/v1/users/{id}
    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuário por ID (ADMIN)",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<UserResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getById(id)));
    }

    // PATCH /api/v1/users/{id}/role?role=CURATOR
    @PatchMapping("/{id}/role")
    @Operation(summary = "Alterar role do usuário (ADMIN)",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> updateRole(
            @PathVariable UUID id,
            @RequestParam User.Role role
    ) {
        userService.updateRole(id, role);
        return ResponseEntity.ok(ApiResponse.ok("Role atualizado", null));
    }

    // PATCH /api/v1/users/{id}/toggle?enabled=false
    @PatchMapping("/{id}/toggle")
    @Operation(summary = "Ativar/desativar conta (ADMIN)",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> toggleEnabled(
            @PathVariable UUID id,
            @RequestParam boolean enabled
    ) {
        userService.toggleEnabled(id, enabled);
        return ResponseEntity.ok(ApiResponse.ok(
                enabled ? "Conta ativada" : "Conta desativada", null
        ));
    }
}