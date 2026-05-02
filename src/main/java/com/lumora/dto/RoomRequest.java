package com.lumora.dto;

import com.lumora.model.Room;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public record RoomRequest(

        @NotBlank(message = "Número do quarto é obrigatório")
        @Size(max = 50)
        String roomNumber,

        @NotNull(message = "Tipo do quarto é obrigatório")
        Room.RoomType type,

        @NotNull(message = "Preço é obrigatório")
        @DecimalMin(value = "0.01", message = "Preço deve ser maior que zero")
        BigDecimal price,

        @Min(value = 1, message = "Capacidade mínima é 1")
        int capacity,

        Integer floorNumber,

        @Size(max = 2000)
        String description,

        List<String> imageUrls

) {}