package com.lumora.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// ─────────────────────────────────────────────────────────────────────────────
// SECURITY/LOGINREQUEST.JAVA — Dados de entrada para autenticação
// ─────────────────────────────────────────────────────────────────────────────

public record LoginRequest(

        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Formato de email inválido")
        String email,

        @NotBlank(message = "Senha é obrigatória")
        String password
) {}