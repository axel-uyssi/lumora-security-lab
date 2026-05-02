package com.lumora.dto;

import jakarta.validation.constraints.*;

// ─────────────────────────────────────────────────────────────────────────────
// DTO/USERREQUEST.JAVA — Dados para criar conta de usuário
//
// Regex da senha explicada:
//   (?=.*[a-z])  → pelo menos 1 letra minúscula
//   (?=.*[A-Z])  → pelo menos 1 letra maiúscula
//   (?=.*\d)     → pelo menos 1 número
//   (?=.*[...])  → pelo menos 1 caractere especial
//   .{8,}        → mínimo 8 caracteres
// ─────────────────────────────────────────────────────────────────────────────

public record UserRequest(

        @NotBlank(message = "Nome completo é obrigatório")
        @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
        String fullName,

        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Formato de email inválido")
        @Size(max = 254)
        String email,

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 8, max = 128, message = "Senha deve ter entre 8 e 128 caracteres")
        @Pattern(
                regexp  = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#^()_+=\\-]).{8,}$",
                message = "Senha deve conter: maiúscula, minúscula, número e caractere especial"
        )
        String password

) {}