package com.lumora.exception;

// ─────────────────────────────────────────────────────────────────────────────
// EXCEPTION/BUSINESSEXCEPTION.JAVA — Regra de negócio violada (422)
//
// Usada quando:
//   - Check-out antes do check-in
//   - Número de hóspedes excede capacidade do quarto
//   - Tentativa de cancelar reserva já finalizada
//   - Check-in em reserva não confirmada
//
// Diferença de 400 vs 422:
//   400 Bad Request  → formato/sintaxe inválida (campo em branco, email sem @)
//   422 Unprocessable → formato válido, mas a LÓGICA de negócio falhou
// ─────────────────────────────────────────────────────────────────────────────

public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}