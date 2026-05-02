package com.lumora.dto;

import com.lumora.model.User;
import java.time.LocalDateTime;
import java.util.UUID;

// ─────────────────────────────────────────────────────────────────────────────
// DTO/USERRESPONSE.JAVA — Dados do usuário retornados pela API
//
// NUNCA inclua: password, failedAttempts, lockTime, accountNonLocked
// Esses campos são internos e não devem vazar para o cliente.
// ─────────────────────────────────────────────────────────────────────────────

public record UserResponse(
        UUID          id,
        String        fullName,
        String        email,
        String        role,
        LocalDateTime createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().name(),
                user.getCreatedAt()
        );
    }
}