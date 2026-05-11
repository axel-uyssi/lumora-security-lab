package com.lumora.dto;

import com.lumora.model.Hotel;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public record HotelRequest(

        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 200)
        String name,

        @NotBlank(message = "Descrição é obrigatória")
        String description,

        String longDescription,

        @NotBlank(message = "País é obrigatório")
        @Size(max = 100)
        String country,

        @NotBlank(message = "Cidade é obrigatória")
        @Size(max = 100)
        String city,

        String address,

        @DecimalMin("-90.0") @DecimalMax("90.0")
        BigDecimal latitude,

        @DecimalMin("-180.0") @DecimalMax("180.0")
        BigDecimal longitude,

        @NotNull
        @DecimalMin(value = "1.0")
        @DecimalMax(value = "5.0")
        BigDecimal stars,

        @NotNull
        @DecimalMin(value = "0.01")
        BigDecimal pricePerNight,

        @NotNull
        Hotel.Region region,

        Hotel.Category category,

        String coverImageUrl,

        List<String> galleryImages,

        boolean featured,

        Integer totalRooms,

        String checkInTime,

        String checkOutTime,

        @Min(1)
        Integer minimumStayNights,

        String cancellationPolicy,

        String bestSeason,

        String nearbyAttractions,

        String languagesSpoken,

        String dressCode,

        @Min(1) @Max(5)
        Integer sustainabilityRating,

        boolean acceptsPets,

        boolean childFriendly,

        boolean wheelchairAccessible,

        Set<String> amenities,

        Set<String> diningOptions,

        Set<String> activities

) {}