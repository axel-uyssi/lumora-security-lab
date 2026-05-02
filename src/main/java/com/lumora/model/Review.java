package com.lumora.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

// ─────────────────────────────────────────────────────────────────────────────
// MODEL/REVIEW.JAVA — Avaliação de hotel pelo hóspede
//
// Regras importantes:
//   - Um usuário só pode avaliar um hotel UMA VEZ
//     → garantido pela UniqueConstraint (user_id, hotel_id) no banco
//   - verified = true → usuário realmente ficou no hotel (CHECKED_OUT)
//   - verified = false → avaliação sem estadia comprovada
// ─────────────────────────────────────────────────────────────────────────────

@Entity
@Table(
        name = "reviews",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_review_user_hotel",
                        columnNames = {"user_id", "hotel_id"})
        },
        indexes = {
                @Index(name = "idx_review_hotel", columnList = "hotel_id"),
                @Index(name = "idx_review_user",  columnList = "user_id")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Reserva vinculada (opcional — só existe se o usuário ficou no hotel)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    @Min(1) @Max(5)
    @Column(nullable = false)
    private int rating;

    @Size(max = 2000)
    @Column(columnDefinition = "TEXT")
    private String comment;

    // true = hóspede real com estadia confirmada (CHECKED_OUT)
    @Builder.Default
    @Column(nullable = false)
    private boolean verified = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}