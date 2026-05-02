package com.lumora.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ReservationResponse(
        UUID          id,
        UUID          hotelId,
        String        hotelName,
        UUID          roomId,
        String        roomNumber,
        LocalDate     checkInDate,
        LocalDate     checkOutDate,
        BigDecimal    totalPrice,
        String        status,
        String        confirmationCode,
        int           numberOfGuests,
        String        specialRequests,
        LocalDateTime createdAt
) {}