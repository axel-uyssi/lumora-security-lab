package com.lumora.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewResponse(
        UUID          id,
        UUID          hotelId,
        UUID          userId,
        String        userFullName,
        int           rating,
        String        comment,
        boolean       verified,      // true = hóspede real confirmado
        LocalDateTime createdAt
) {}