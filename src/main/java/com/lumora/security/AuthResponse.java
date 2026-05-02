package com.lumora.security;

// ─────────────────────────────────────────────────────────────────────────────
// SECURITY/AUTHRESPONSE.JAVA — Resposta após login ou registro
//
// accessToken → JWT que o cliente deve enviar em toda requisição protegida
//               Header: Authorization: Bearer <accessToken>
// expiresInMs → quando o token expira (cliente usa para renovar)
// role        → papel do usuário (GUEST, CURATOR, ADMIN)
// ─────────────────────────────────────────────────────────────────────────────

public record AuthResponse(
        String accessToken,
        long   expiresInMs,
        String role
) {}