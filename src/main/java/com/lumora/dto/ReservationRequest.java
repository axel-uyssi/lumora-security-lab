package com.lumora.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.UUID;

// ─────────────────────────────────────────────────────────────────────────────
// DTO/RESERVATIONREQUEST.JAVA — Dados para criar uma reserva
//
// hotelId e roomId → o cliente envia apenas os IDs
//   O servidor busca e valida os objetos no banco (segurança)
//   Nunca confie no cliente para fornecer objetos completos
//
// @FutureOrPresent → data deve ser hoje ou no futuro (Jakarta Validation)
// @Future          → data deve ser estritamente no futuro
// ─────────────────────────────────────────────────────────────────────────────

public record ReservationRequest(

        @NotNull(message = "ID do hotel é obrigatório")
        UUID hotelId,

        @NotNull(message = "ID do quarto é obrigatório")
        UUID roomId,

        @NotNull(message = "Data de check-in é obrigatória")
        @FutureOrPresent(message = "Check-in deve ser hoje ou no futuro")
        LocalDate checkIn,

        @NotNull(message = "Data de check-out é obrigatória")
        @Future(message = "Check-out deve ser data futura")
        LocalDate checkOut,

        @Min(value = 1, message = "Mínimo 1 hóspede")
        @Max(value = 20, message = "Máximo 20 hóspedes")
        int numberOfGuests,

        @Size(max = 1000, message = "Pedidos especiais: máximo 1000 caracteres")
        String specialRequests

) {}