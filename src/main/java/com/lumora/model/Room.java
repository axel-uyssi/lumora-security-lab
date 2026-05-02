package com.lumora.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

// ─────────────────────────────────────────────────────────────────────────────
// MODEL/ROOM.JAVA — Quarto de hotel
//
// @ManyToOne → muitos quartos pertencem a um hotel
//   @JoinColumn → define a FK hotel_id nesta tabela
//
// O lado "dono" da relação é sempre quem tem o @JoinColumn (Room).
// Hotel tem @OneToMany(mappedBy = "hotel") — apenas referência inversa.
// ─────────────────────────────────────────────────────────────────────────────

@Entity
@Table(
        name = "rooms",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_room_hotel",
                        columnNames = {"hotel_id", "room_number"})
        },
        indexes = {
                @Index(name = "idx_room_hotel",     columnList = "hotel_id"),
                @Index(name = "idx_room_available", columnList = "hotel_id, available")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    // FK para a tabela hotels
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @NotBlank
    @Size(max = 50)
    @Column(name = "room_number", nullable = false)
    private String roomNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RoomType type;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Min(1)
    @Column(nullable = false)
    private int capacity;

    @Builder.Default
    @Column(nullable = false)
    private boolean available = true;

    @Column(name = "floor_number")
    private Integer floorNumber;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ElementCollection
    @CollectionTable(name = "room_images",
            joinColumns = @JoinColumn(name = "room_id"))
    @Column(name = "image_url", length = 500)
    private List<String> imageUrls;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum RoomType {
        STANDARD, DELUXE, SUITE, VILLA, PENTHOUSE, OVERWATER_BUNGALOW
    }
}