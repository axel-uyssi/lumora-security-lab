package com.lumora.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "hotels")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String longDescription; // Descrição detalhada para página de detalhes

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    private String country;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    private String city;

    @Size(max = 100)
    private String address; // Endereço completo

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude; // Para mapas

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude; // Para mapas

    @Column(name = "price_per_night", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerNight;

    @DecimalMin("1.0") @DecimalMax("5.0")
    @Column(nullable = false, precision = 2, scale = 1)
    private BigDecimal stars;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Region region;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private Category category; // BOUTIQUE, RESORT, ECO_LODGE, VILLA, etc

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @ElementCollection
    @CollectionTable(name = "hotel_gallery",
            joinColumns = @JoinColumn(name = "hotel_id"))
    @Column(name = "image_url", length = 500)
    private List<String> galleryImages; // Galeria de imagens

    @Builder.Default
    @Column(name = "is_featured")
    private boolean featured = false;

    @Column(name = "average_rating", precision = 3, scale = 2)
    private BigDecimal averageRating;

    @Builder.Default
    @Column(name = "total_reviews")
    private Integer totalReviews = 0;

    @Column(name = "total_rooms")
    private Integer totalRooms;

    @Column(name = "check_in_time")
    private String checkInTime; // "15:00"

    @Column(name = "check_out_time")
    private String checkOutTime; // "12:00"

    @Column(name = "minimum_stay_nights")
    private Integer minimumStayNights;

    @Column(name = "cancellation_policy", columnDefinition = "TEXT")
    private String cancellationPolicy;

    @Column(name = "best_season")
    private String bestSeason; // "May - October"

    @Column(name = "nearby_attractions", columnDefinition = "TEXT")
    private String nearbyAttractions;

    @Column(name = "languages_spoken")
    private String languagesSpoken; // "English, Greek, French"

    @Column(name = "dress_code")
    private String dressCode; // "Smart Casual"

    @Column(name = "sustainability_rating")
    private Integer sustainabilityRating; // 1-5

    @Builder.Default
    @Column(name = "accepts_pets")
    private boolean acceptsPets = false;

    @Builder.Default
    @Column(name = "child_friendly")
    private boolean childFriendly = true;

    @Builder.Default
    @Column(name = "wheelchair_accessible")
    private boolean wheelchairAccessible = false;

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Room> rooms;

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Review> reviews;

    @ElementCollection
    @CollectionTable(name = "hotel_amenities",
            joinColumns = @JoinColumn(name = "hotel_id"))
    @Column(name = "amenity", length = 100)
    private Set<String> amenities;

    @ElementCollection
    @CollectionTable(name = "hotel_dining_options",
            joinColumns = @JoinColumn(name = "hotel_id"))
    @Column(name = "dining_option", length = 100)
    private Set<String> diningOptions; // Restaurant, Bar, Room Service, etc

    @ElementCollection
    @CollectionTable(name = "hotel_activities",
            joinColumns = @JoinColumn(name = "hotel_id"))
    @Column(name = "activity", length = 100)
    private Set<String> activities; // Yoga, Diving, Hiking, etc

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum Region {
        MEDITERRANEAN,
        ARCTIC_NORDIC,
        TROPICAL_ISLANDS,
        MOUNTAIN_RETREATS,
        DESERT_SANCTUARIES,
        ASIA_PACIFIC,
        SOUTH_AMERICA,
        NORTH_AMERICA,
        MIDDLE_EAST,
        OCEANIA
    }

    public enum Category {
        BOUTIQUE_HOTEL,
        LUXURY_RESORT,
        ECO_LODGE,
        PRIVATE_VILLA,
        OVERWATER_BUNGALOW,
        MOUNTAIN_RETREAT,
        DESERT_CAMP,
        PALACE_HOTEL,
        RYOKAN,
        GLAMPING
    }
}