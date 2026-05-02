package com.lumora.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

// ─────────────────────────────────────────────────────────────────────────────
// MODEL/RESERVATION.JAVA — Reserva de quarto de hotel
//
// Relacionamentos com 3 entidades:
//   User  → quem fez a reserva
//   Hotel → onde é a reserva
//   Room  → qual quarto específico
//
// totalPrice é calculado e FIXADO no momento da reserva.
// Se o preço do quarto mudar depois, a reserva mantém o valor original.
// ─────────────────────────────────────────────────────────────────────────────

@Entity
@Table(
        name = "reservations",
        indexes = {
                @Index(name = "idx_res_user",  columnList = "user_id"),
                @Index(name = "idx_res_hotel", columnList = "hotel_id"),
                @Index(name = "idx_res_code",  columnList = "confirmation_code"),
                @Index(name = "idx_res_dates", columnList = "room_id, check_in_date, check_out_date")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    // LocalDate → apenas data (sem hora), ideal para check-in/out
    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;

    @Column(name = "check_out_date", nullable = false)
    private LocalDate checkOutDate;

    // Valor fixado no momento da reserva — imutável
    @Column(name = "total_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.PENDING;

    @Size(max = 1000)
    @Column(name = "special_requests", columnDefinition = "TEXT")
    private String specialRequests;

    // Código amigável para o hóspede (ex: LMR-AB3X7K2P9Q)
    @Column(name = "confirmation_code", unique = true, nullable = false, length = 20)
    private String confirmationCode;

    @Min(1)
    @Column(name = "number_of_guests", nullable = false)
    private int numberOfGuests;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ── Ciclo de vida da reserva ─────────────────────────────────────────────
    public enum Status {
        PENDING,     // Aguardando confirmação/pagamento
        CONFIRMED,   // Confirmada
        CHECKED_IN,  // Hóspede fez check-in
        CHECKED_OUT, // Hóspede saiu
        CANCELLED,   // Cancelada
        REFUNDED     // Reembolsada
    }
}
