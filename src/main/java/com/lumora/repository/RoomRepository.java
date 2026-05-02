package com.lumora.repository;

import com.lumora.model.Room;
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
import java.util.List;
import java.util.UUID;

@Repository
public interface RoomRepository extends JpaRepository<Room, UUID> {

    // ── Buscas por hotel ──────────────────────────────────────────────────────

    List<Room> findByHotelId(UUID hotelId);
    List<Room> findByHotelIdAndAvailableTrue(UUID hotelId);
    Page<Room> findByHotelId(UUID hotelId, Pageable pageable);
    List<Room> findByHotelIdAndType(UUID hotelId, Room.RoomType type);
    List<Room> findByHotelIdAndCapacityGreaterThanEqual(UUID hotelId, int minCapacity);
    boolean existsByHotelIdAndRoomNumber(UUID hotelId, String roomNumber);
    long countByHotelId(UUID hotelId);

    // ── Disponibilidade por período ───────────────────────────────────────────
    // Lógica de conflito:
    //   SEM conflito: checkOut <= :checkIn  OU  checkIn >= :checkOut
    //   COM conflito: NOT (sem conflito)
    @Query("""
        SELECT r FROM Room r
        WHERE r.hotel.id = :hotelId
          AND r.available = true
          AND r.capacity >= :guests
          AND r.id NOT IN (
              SELECT res.room.id FROM Reservation res
              WHERE res.status IN ('PENDING', 'CONFIRMED', 'CHECKED_IN')
                AND NOT (res.checkOutDate <= :checkIn OR res.checkInDate >= :checkOut)
          )
        ORDER BY r.price ASC
        """)
    List<Room> findAvailableRooms(
            @Param("hotelId")  UUID hotelId,
            @Param("checkIn")  LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            @Param("guests")   int guests
    );

    // Disponibilidade filtrada por tipo de quarto
    @Query("""
        SELECT r FROM Room r
        WHERE r.hotel.id = :hotelId
          AND r.type = :tipo
          AND r.available = true
          AND r.capacity >= :guests
          AND r.id NOT IN (
              SELECT res.room.id FROM Reservation res
              WHERE res.status IN ('PENDING', 'CONFIRMED', 'CHECKED_IN')
                AND NOT (res.checkOutDate <= :checkIn OR res.checkInDate >= :checkOut)
          )
        ORDER BY r.price ASC
        """)
    List<Room> findAvailableRoomsByType(
            @Param("hotelId")  UUID hotelId,
            @Param("tipo")     Room.RoomType tipo,
            @Param("checkIn")  LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            @Param("guests")   int guests
    );

    // ── Gerenciamento ─────────────────────────────────────────────────────────

    @Modifying @Transactional
    @Query("UPDATE Room r SET r.available = :status WHERE r.id = :id")
    void updateAvailability(@Param("id") UUID id, @Param("status") boolean status);

    @Modifying @Transactional
    @Query("UPDATE Room r SET r.available = false WHERE r.hotel.id = :hotelId")
    int blockAllRooms(@Param("hotelId") UUID hotelId);

    @Modifying @Transactional
    @Query("UPDATE Room r SET r.price = :preco WHERE r.hotel.id = :hotelId AND r.type = :tipo")
    int updatePriceByType(
            @Param("hotelId") UUID hotelId,
            @Param("tipo")    Room.RoomType tipo,
            @Param("preco")   BigDecimal preco
    );

    // ── Relatórios ────────────────────────────────────────────────────────────

    @Query("SELECT r.type, COUNT(r) FROM Room r WHERE r.hotel.id = :hotelId GROUP BY r.type")
    List<Object[]> countByType(@Param("hotelId") UUID hotelId);

    @Query("SELECT COUNT(r) FROM Room r WHERE r.hotel.id = :hotelId AND r.available = true")
    long countAvailable(@Param("hotelId") UUID hotelId);
}