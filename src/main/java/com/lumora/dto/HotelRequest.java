package com.lumora.dto;

import com.lumora.model.Hotel;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.Set;

// ─────────────────────────────────────────────────────────────────────────────
// DTO/HOTELREQUEST.JAVA — Dados de entrada para criar ou atualizar um hotel
//
// Record do Java 16+: imutável, gera construtor/getters/equals automaticamente
// @Valid no Controller ativa as validações abaixo antes de executar o método
// ─────────────────────────────────────────────────────────────────────────────

public record HotelRequest(

        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 200, message = "Nome deve ter no máximo 200 caracteres")
        String name,

        @NotBlank(message = "Descrição é obrigatória")
        String description,

        @NotBlank(message = "País é obrigatório")
        @Size(max = 100)
        String country,

        @NotBlank(message = "Cidade é obrigatória")
        @Size(max = 100)
        String city,

        @NotNull(message = "Estrelas é obrigatório")
        @DecimalMin(value = "1.0", message = "Mínimo 1 estrela")
        @DecimalMax(value = "5.0", message = "Máximo 5 estrelas")
        BigDecimal stars,

        @NotNull(message = "Preço por noite é obrigatório")
        @DecimalMin(value = "0.01", message = "Preço deve ser maior que zero")
        BigDecimal pricePerNight,

        @NotNull(message = "Região é obrigatória")
        Hotel.Region region,

        String coverImageUrl,

        boolean featured,

        Set<String> amenities

) {}