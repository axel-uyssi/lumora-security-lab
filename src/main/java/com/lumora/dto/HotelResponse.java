package com.lumora.dto;

import com.lumora.model.Hotel;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

// ─────────────────────────────────────────────────────────────────────────────
// DTO/HOTELRESPONSE.JAVA — Dados do hotel retornados pela API
//
// Por que não retornar a entidade Hotel diretamente?
//   A entidade tem listas lazy (rooms, reviews) que causam LazyInitException
//   se acessadas fora de uma transação. O DTO é seguro e controlado.
// ─────────────────────────────────────────────────────────────────────────────

public record HotelResponse(
        UUID          id,
        String        name,
        String        description,
        String        country,
        String        city,
        BigDecimal    stars,
        BigDecimal    pricePerNight,
        String        region,
        String        coverImageUrl,
        boolean       featured,
        BigDecimal    averageRating,
        Integer       totalReviews,
        Set<String>   amenities,
        LocalDateTime createdAt
) {
    // Factory method: converte entidade → DTO
    public static HotelResponse from(Hotel hotel) {
        return new HotelResponse(
                hotel.getId(),
                hotel.getName(),
                hotel.getDescription(),
                hotel.getCountry(),
                hotel.getCity(),
                hotel.getStars(),
                hotel.getPricePerNight(),
                hotel.getRegion().name(),
                hotel.getCoverImageUrl(),
                hotel.isFeatured(),
                hotel.getAverageRating(),
                hotel.getTotalReviews(),
                hotel.getAmenities(),
                hotel.getCreatedAt()
        );
    }
}