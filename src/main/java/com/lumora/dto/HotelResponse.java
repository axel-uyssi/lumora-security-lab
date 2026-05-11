package com.lumora.dto;

import com.lumora.model.Hotel;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record HotelResponse(
        UUID          id,
        String        name,
        String        description,
        String        longDescription,
        String        country,
        String        city,
        String        address,
        BigDecimal    latitude,
        BigDecimal    longitude,
        BigDecimal    stars,
        BigDecimal    pricePerNight,
        String        region,
        String        category,
        String        coverImageUrl,
        List<String>  galleryImages,
        boolean       featured,
        BigDecimal    averageRating,
        Integer       totalReviews,
        Integer       totalRooms,
        String        checkInTime,
        String        checkOutTime,
        Integer       minimumStayNights,
        String        cancellationPolicy,
        String        bestSeason,
        String        nearbyAttractions,
        String        languagesSpoken,
        String        dressCode,
        Integer       sustainabilityRating,
        boolean       acceptsPets,
        boolean       childFriendly,
        boolean       wheelchairAccessible,
        Set<String>   amenities,
        Set<String>   diningOptions,
        Set<String>   activities,
        LocalDateTime createdAt
) {
    public static HotelResponse from(Hotel hotel) {
        return new HotelResponse(
                hotel.getId(),
                hotel.getName(),
                hotel.getDescription(),
                hotel.getLongDescription(),
                hotel.getCountry(),
                hotel.getCity(),
                hotel.getAddress(),
                hotel.getLatitude(),
                hotel.getLongitude(),
                hotel.getStars(),
                hotel.getPricePerNight(),
                hotel.getRegion() != null ? hotel.getRegion().name() : null,
                hotel.getCategory() != null ? hotel.getCategory().name() : null,
                hotel.getCoverImageUrl(),
                hotel.getGalleryImages(),
                hotel.isFeatured(),
                hotel.getAverageRating(),
                hotel.getTotalReviews(),
                hotel.getTotalRooms(),
                hotel.getCheckInTime(),
                hotel.getCheckOutTime(),
                hotel.getMinimumStayNights(),
                hotel.getCancellationPolicy(),
                hotel.getBestSeason(),
                hotel.getNearbyAttractions(),
                hotel.getLanguagesSpoken(),
                hotel.getDressCode(),
                hotel.getSustainabilityRating(),
                hotel.isAcceptsPets(),
                hotel.isChildFriendly(),
                hotel.isWheelchairAccessible(),
                hotel.getAmenities(),
                hotel.getDiningOptions(),
                hotel.getActivities(),
                hotel.getCreatedAt()
        );
    }
}
