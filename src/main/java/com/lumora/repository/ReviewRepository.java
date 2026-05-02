package com.lumora.repository;

import com.lumora.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    // ── Listagem pública ──────────────────────────────────────────────────────

    Page<Review> findByHotelIdOrderByCreatedAtDesc(UUID hotelId, Pageable pageable);
    Page<Review> findByHotelIdOrderByRatingDesc(UUID hotelId, Pageable pageable);
    Page<Review> findByHotelIdAndVerifiedTrueOrderByCreatedAtDesc(UUID hotelId, Pageable pageable);
    Page<Review> findByHotelIdAndRating(UUID hotelId, int rating, Pageable pageable);

    // ── Avaliações do usuário ─────────────────────────────────────────────────

    Page<Review> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    Optional<Review> findByUserIdAndHotelId(UUID userId, UUID hotelId);

    // ── Validação — impede review duplicada ───────────────────────────────────

    boolean existsByUserIdAndHotelId(UUID userId, UUID hotelId);

    // ── Cálculos de rating ────────────────────────────────────────────────────

    @Query("SELECT AVG(CAST(r.rating AS double)) FROM Review r WHERE r.hotel.id = :hotelId")
    Optional<Double> getAverageRating(@Param("hotelId") UUID hotelId);

    long countByHotelId(UUID hotelId);
    long countByHotelIdAndVerifiedTrue(UUID hotelId);

    // Distribuição de notas → barras de estrelas no frontend
    // Ex: [[1, 3], [2, 8], [3, 15], [4, 42], [5, 97]]
    @Query("""
        SELECT r.rating, COUNT(r) FROM Review r
        WHERE r.hotel.id = :hotelId
        GROUP BY r.rating ORDER BY r.rating ASC
        """)
    List<Object[]> getRatingDistribution(@Param("hotelId") UUID hotelId);

    // ── Busca textual ─────────────────────────────────────────────────────────

    @Query("""
        SELECT r FROM Review r
        WHERE r.hotel.id = :hotelId
          AND LOWER(r.comment) LIKE LOWER(CONCAT('%', :termo, '%'))
        ORDER BY r.createdAt DESC
        """)
    Page<Review> searchByComment(
            @Param("hotelId") UUID hotelId,
            @Param("termo")   String termo,
            Pageable pageable
    );

    // ── Moderação ─────────────────────────────────────────────────────────────

    @Modifying @Transactional
    @Query("UPDATE Review r SET r.verified = :status WHERE r.id = :id")
    void updateVerifiedStatus(@Param("id") UUID id, @Param("status") boolean status);

    // LGPD — direito ao esquecimento
    @Modifying @Transactional
    @Query("DELETE FROM Review r WHERE r.user.id = :userId")
    int deleteAllByUserId(@Param("userId") UUID userId);

    // ── Relatórios ────────────────────────────────────────────────────────────

    @Query("SELECT AVG(CAST(r.rating AS double)) FROM Review r")
    Optional<Double> getGlobalAverageRating();

    @Query("SELECT r FROM Review r ORDER BY r.createdAt DESC")
    Page<Review> findAllRecent(Pageable pageable);
}