package com.lumora.exception;

// ─────────────────────────────────────────────────────────────────────────────
// EXCEPTION/CONFLICTEXCEPTION.JAVA — Conflito de dados (409)
//
// Usada quando:
//   - Email já cadastrado
//   - Quarto já reservado no período solicitado
//   - Usuário tentando avaliar hotel que já avaliou
//   - Hotel com mesmo nome já existe no país
// ─────────────────────────────────────────────────────────────────────────────

public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
