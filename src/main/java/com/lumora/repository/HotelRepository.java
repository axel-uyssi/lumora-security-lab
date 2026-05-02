package com.lumora.repository;

import com.lumora.model.Hotel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, UUID> {

    // ── Buscas públicas ───────────────────────────────────────────────────────

    List<Hotel> findByFeaturedTrueOrderByAverageRatingDesc();
    Page<Hotel> findByCountryIgnoreCase(String country, Pageable pageable);
    Page<Hotel> findByRegion(Hotel.Region region, Pageable pageable);
    Page<Hotel> findByNameContainingIgnoreCase(String nome, Pageable pageable);
    Page<Hotel> findByPricePerNightBetween(BigDecimal min, BigDecimal max, Pageable pageable);
    boolean existsByNameIgnoreCaseAndCountryIgnoreCase(String nome, String country);

    // ── Busca com filtros opcionais combinados ────────────────────────────────
    // Parâmetro null = filtro ignorado
    @Query("""
        SELECT h FROM Hotel h
        WHERE (:country  IS NULL OR LOWER(h.country) = LOWER(:country))
          AND (:region   IS NULL OR h.region = :region)
          AND (:minPrice IS NULL OR h.pricePerNight >= :minPrice)
          AND (:maxPrice IS NULL OR h.pricePerNight <= :maxPrice)
          AND (:minStars IS NULL OR h.stars >= :minStars)
        ORDER BY h.averageRating DESC NULLS LAST
        """)
    Page<Hotel> search(
            @Param("country")  String country,
            @Param("region")   Hotel.Region region,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("minStars") BigDecimal minStars,
            Pageable pageable
    );

    // ── Atualiza rating após nova avaliação ───────────────────────────────────
    @Modifying @Transactional
    @Query("""
        UPDATE Hotel h
        SET h.averageRating = (
            SELECT AVG(CAST(r.rating AS double)) FROM Review r WHERE r.hotel.id = h.id
        ),
        h.totalReviews = (SELECT COUNT(r) FROM Review r WHERE r.hotel.id = h.id)
        WHERE h.id = :hotelId
        """)
    void refreshRatingStats(@Param("hotelId") UUID hotelId);

    // ── Relatórios ────────────────────────────────────────────────────────────

    @Query("SELECT h.region, COUNT(h) FROM Hotel h GROUP BY h.region ORDER BY COUNT(h) DESC")
    List<Object[]> countByRegion();

    @Query("SELECT h FROM Hotel h WHERE h.totalReviews > 0 ORDER BY h.averageRating DESC")
    List<Hotel> findTopRated(Pageable pageable);
}