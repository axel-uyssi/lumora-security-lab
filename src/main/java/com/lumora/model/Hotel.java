package com.lumora.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

// ─────────────────────────────────────────────────────────────────────────────
// MODEL/HOTEL.JAVA — Entidade central do sistema Lumora Hotels
//
// Relacionamentos:
//   @OneToMany rooms    → um hotel tem vários quartos
//   @OneToMany reviews  → um hotel tem várias avaliações
//   @ElementCollection  → amenidades (WiFi, Piscina...) em tabela separada
//
// fetch = LAZY → só carrega a lista quando você chamar hotel.getRooms()
//                Evita carregar dados desnecessários em toda consulta
// ─────────────────────────────────────────────────────────────────────────────

@Entity
@Table(
        name = "hotels",
        indexes = {
                @Index(name = "idx_hotel_country", columnList = "country"),
                @Index(name = "idx_hotel_region",  columnList = "region"),
                @Index(name = "idx_hotel_price",   columnList = "price_per_night")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    private String country;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    private String city;

    // BigDecimal para valores monetários — nunca use double para dinheiro!
    @Column(name = "price_per_night", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerNight;

    @DecimalMin("1.0") @DecimalMax("5.0")
    @Column(nullable = false, precision = 2, scale = 1)
    private BigDecimal stars;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Region region;

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @Builder.Default
    @Column(name = "is_featured")
    private boolean featured = false;

    @Column(name = "average_rating", precision = 3, scale = 2)
    private BigDecimal averageRating;

    @Builder.Default
    @Column(name = "total_reviews")
    private Integer totalReviews = 0;

    // ── Relacionamentos ──────────────────────────────────────────────────────

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Room> rooms;

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Review> reviews;

    // Amenidades armazenadas em tabela separada (hotel_amenities)
    @ElementCollection
    @CollectionTable(name = "hotel_amenities",
            joinColumns = @JoinColumn(name = "hotel_id"))
    @Column(name = "amenity", length = 100)
    private Set<String> amenities;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ── Regiões disponíveis ──────────────────────────────────────────────────
    public enum Region {
        MEDITERRANEAN, ARCTIC_NORDIC, TROPICAL_ISLANDS,
        MOUNTAIN_RETREATS, DESERT_SANCTUARIES, ASIA_PACIFIC, SOUTH_AMERICA
    }
}
