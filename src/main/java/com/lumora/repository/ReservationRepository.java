package com.lumora.repository;

import com.lumora.model.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    // ── Buscas do hóspede ─────────────────────────────────────────────────────

    Page<Reservation> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    List<Reservation> findByUserIdAndStatus(UUID userId, Reservation.Status status);
    Optional<Reservation> findByConfirmationCode(String code);

    @Query("""
        SELECT r FROM Reservation r
        WHERE r.user.id = :userId
          AND r.checkInDate >= :hoje
          AND r.status IN ('PENDING', 'CONFIRMED')
        ORDER BY r.checkInDate ASC
        """)
    List<Reservation> findUpcoming(
            @Param("userId") UUID userId,
            @Param("hoje")   LocalDate hoje
    );

    // ── Buscas administrativas ────────────────────────────────────────────────

    Page<Reservation> findByHotelIdOrderByCreatedAtDesc(UUID hotelId, Pageable pageable);
    Page<Reservation> findByHotelIdAndStatus(UUID hotelId, Reservation.Status status, Pageable pageable);

    // ── Anti double-booking ───────────────────────────────────────────────────
    // Retorna true se o quarto já está ocupado no período solicitado
    @Query("""
        SELECT COUNT(r) > 0 FROM Reservation r
        WHERE r.room.id = :roomId
          AND r.status IN ('PENDING', 'CONFIRMED', 'CHECKED_IN')
          AND NOT (r.checkOutDate <= :checkIn OR r.checkInDate >= :checkOut)
        """)
    boolean hasConflict(
            @Param("roomId")   UUID roomId,
            @Param("checkIn")  LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut
    );

    // ── Estadias concluídas (para validar review) ─────────────────────────────
    @Query("""
        SELECT r FROM Reservation r
        WHERE r.user.id = :userId
          AND r.hotel.id = :hotelId
          AND r.status = 'CHECKED_OUT'
        """)
    List<Reservation> findCompletedStays(
            @Param("userId")  UUID userId,
            @Param("hotelId") UUID hotelId
    );

    // ── Operações de status ───────────────────────────────────────────────────

    @Modifying @Transactional
    @Query("UPDATE Reservation r SET r.status = :status WHERE r.id = :id")
    void updateStatus(@Param("id") UUID id, @Param("status") Reservation.Status status);

    // Cancela reservas PENDING antigas automaticamente (via @Scheduled)
    @Modifying @Transactional
    @Query("""
        UPDATE Reservation r SET r.status = 'CANCELLED'
        WHERE r.status = 'PENDING' AND r.createdAt < :antes
        """)
    int cancelExpiredPending(@Param("antes") LocalDateTime antes);

    // ── Relatórios ────────────────────────────────────────────────────────────

    @Query("""
        SELECT SUM(r.totalPrice) FROM Reservation r
        WHERE r.hotel.id = :hotelId
          AND r.status IN ('CONFIRMED', 'CHECKED_IN', 'CHECKED_OUT')
          AND r.checkInDate >= :inicio AND r.checkOutDate <= :fim
        """)
    Optional<BigDecimal> totalRevenue(
            @Param("hotelId") UUID hotelId,
            @Param("inicio")  LocalDate inicio,
            @Param("fim")     LocalDate fim
    );

    @Query("SELECT r FROM Reservation r WHERE r.checkInDate = :hoje AND r.status = 'CONFIRMED'")
    List<Reservation> findCheckInsToday(@Param("hoje") LocalDate hoje);

    @Query("SELECT r FROM Reservation r WHERE r.checkOutDate = :hoje AND r.status = 'CHECKED_IN'")
    List<Reservation> findCheckOutsToday(@Param("hoje") LocalDate hoje);

    @Query("SELECT r.status, COUNT(r) FROM Reservation r GROUP BY r.status")
    List<Object[]> countByStatus();
}