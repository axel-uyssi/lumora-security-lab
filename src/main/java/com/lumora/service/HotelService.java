package com.lumora.service;

// ─────────────────────────────────────────────────────────────────────────────
// CORREÇÃO: imports explícitos por classe ao invés de wildcard (dto.*)
// O IntelliJ às vezes não resolve PagedResponse com import com.com.com.com.lumora.lumora.com.com.lumora.lumora.dto.*
// ─────────────────────────────────────────────────────────────────────────────

import com.lumora.dto.HotelRequest;
import com.lumora.dto.HotelResponse;
import com.lumora.dto.PagedResponse;
import com.lumora.exception.ConflictException;
import com.lumora.exception.ResourceNotFoundException;
import com.lumora.model.Hotel;
import com.lumora.repository.HotelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class HotelService {

    private final HotelRepository hotelRepository;

    // ── Leitura pública ───────────────────────────────────────────────────────

    public List<HotelResponse> getFeatured() {
        return hotelRepository.findByFeaturedTrueOrderByAverageRatingDesc()
                .stream()
                .map(HotelResponse::from)
                .toList();
    }

    public HotelResponse getById(UUID id) {
        return HotelResponse.from(findOrThrow(id));
    }

    public PagedResponse<HotelResponse> search(
            String country,
            Hotel.Region region,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            BigDecimal minStars,
            Pageable pageable
    ) {
        return PagedResponse.from(
                hotelRepository.search(country, region, minPrice, maxPrice, minStars, pageable)
                        .map(HotelResponse::from)
        );
    }

    // ── Escrita (somente ADMIN ou CURATOR) ────────────────────────────────────

    @PreAuthorize("hasAnyRole('ADMIN', 'CURATOR')")
    @Transactional
    public HotelResponse create(HotelRequest request) {
        if (hotelRepository.existsByNameIgnoreCaseAndCountryIgnoreCase(
                request.name(), request.country())) {
            throw new ConflictException("Hotel com este nome já existe neste país");
        }

        Hotel hotel = Hotel.builder()
                .name(request.name())
                .description(request.description())
                .country(request.country())
                .city(request.city())
                .stars(request.stars())
                .pricePerNight(request.pricePerNight())
                .region(request.region())
                .coverImageUrl(request.coverImageUrl())
                .featured(request.featured())
                .amenities(request.amenities())
                .build();

        Hotel saved = hotelRepository.save(hotel);
        log.info("Hotel criado: {} ({})", saved.getName(), saved.getId());
        return HotelResponse.from(saved);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CURATOR')")
    @Transactional
    public HotelResponse update(UUID id, HotelRequest request) {
        Hotel hotel = findOrThrow(id);
        hotel.setName(request.name());
        hotel.setDescription(request.description());
        hotel.setPricePerNight(request.pricePerNight());
        hotel.setFeatured(request.featured());
        hotel.setAmenities(request.amenities());
        hotel.setCoverImageUrl(request.coverImageUrl());
        return HotelResponse.from(hotelRepository.save(hotel));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void delete(UUID id) {
        if (!hotelRepository.existsById(id)) {
            throw new ResourceNotFoundException("Hotel", "id", id);
        }
        hotelRepository.deleteById(id);
        log.info("Hotel deletado: {}", id);
    }

    @Transactional
    public void refreshRating(UUID hotelId) {
        hotelRepository.refreshRatingStats(hotelId);
    }

    // ── Utilitário privado ────────────────────────────────────────────────────

    private Hotel findOrThrow(UUID id) {
        return hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", "id", id));
    }
}