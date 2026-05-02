package com.lumora.dto;

import jakarta.validation.constraints.*;

// ─────────────────────────────────────────────────────────────────────────────
// DTO/REVIEWREQUEST.JAVA — Dados para criar uma avaliação
//
// Simples: apenas nota (1-5) e comentário opcional.
// O hotelId vem do PATH da URL → /api/v1/hotels/{hotelId}/reviews
// O userId é extraído do TOKEN JWT → nunca do body (segurança!)
// ─────────────────────────────────────────────────────────────────────────────

public record ReviewRequest(

        @Min(value = 1, message = "Nota mínima é 1")
        @Max(value = 5, message = "Nota máxima é 5")
        int rating,

        @Size(max = 2000, message = "Comentário deve ter no máximo 2000 caracteres")
        String comment

) {}