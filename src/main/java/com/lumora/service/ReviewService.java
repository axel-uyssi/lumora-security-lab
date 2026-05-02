package com.lumora.service;

import com.lumora.dto.PagedResponse;
import com.lumora.dto.ReviewRequest;
import com.lumora.dto.ReviewResponse;
import com.lumora.exception.ConflictException;
import com.lumora.exception.ResourceNotFoundException;
import com.lumora.dto.*;
import com.lumora.exception.*;
import com.lumora.model.*;
import com.lumora.repository.*;
import com.lumora.model.Hotel;
import com.lumora.model.Reservation;
import com.lumora.model.Review;
import com.lumora.model.User;
import com.lumora.repository.HotelRepository;
import com.lumora.repository.ReservationRepository;
import com.lumora.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

// ─────────────────────────────────────────────────────────────────────────────
// SERVICE/REVIEWSERVICE.JAVA — Lógica de avaliações de hotéis
//
// Regras:
//   1. Usuário só pode avaliar um hotel UMA VEZ
//   2. Avaliação é "verified" se o usuário tem estadia CHECKED_OUT no hotel
//   3. Após salvar, o rating médio do hotel é recalculado no banco
// ─────────────────────────────────────────────────────────────────────────────

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final HotelRepository hotelRepository;
    private final ReservationRepository reservationRepository;
    private final HotelService          hotelService;
    private final UserService           userService;

    // ── Criar avaliação ───────────────────────────────────────────────────────

    @Transactional
    public ReviewResponse create(UUID hotelId, ReviewRequest req) {
        User user = userService.getCurrentUser();

        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", "id", hotelId));

        if (reviewRepository.existsByUserIdAndHotelId(user.getId(), hotelId)) {
            throw new ConflictException("Você já avaliou este hotel");
        }

        // verified = true se o usuário realmente ficou no hotel
        List<Reservation> stays = reservationRepository
                .findCompletedStays(user.getId(), hotelId);
        boolean verified = !stays.isEmpty();

        Review review = Review.builder()
                .hotel(hotel)
                .user(user)
                .rating(req.rating())
                .comment(req.comment())
                .verified(verified)
                .build();

        Review saved = reviewRepository.save(review);

        // Recalcula o rating médio do hotel no banco (desnormalização para performance)
        hotelService.refreshRating(hotelId);

        log.info("Review criada: hotel={} user={} rating={} verified={}",
                hotelId, user.getEmail(), req.rating(), verified);

        return toResponse(saved);
    }

    // ── Listar avaliações de um hotel (público) ───────────────────────────────

    public PagedResponse<ReviewResponse> getByHotel(UUID hotelId, Pageable pageable) {
        return PagedResponse.from(
                reviewRepository.findByHotelIdOrderByCreatedAtDesc(hotelId, pageable)
                        .map(this::toResponse)
        );
    }

    // ── Apenas avaliações verificadas ────────────────────────────────────────

    public PagedResponse<ReviewResponse> getVerifiedByHotel(UUID hotelId, Pageable pageable) {
        return PagedResponse.from(
                reviewRepository.findByHotelIdAndVerifiedTrueOrderByCreatedAtDesc(hotelId, pageable)
                        .map(this::toResponse)
        );
    }

    // ── Minhas avaliações ────────────────────────────────────────────────────

    public PagedResponse<ReviewResponse> getMyReviews(Pageable pageable) {
        User user = userService.getCurrentUser();
        return PagedResponse.from(
                reviewRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable)
                        .map(this::toResponse)
        );
    }

    // ── Distribuição de notas de um hotel ────────────────────────────────────

    public List<Object[]> getRatingDistribution(UUID hotelId) {
        return reviewRepository.getRatingDistribution(hotelId);
    }

    // ── Moderação (admin) ────────────────────────────────────────────────────

    @Transactional
    public void updateVerified(UUID id, boolean status) {
        if (!reviewRepository.existsById(id)) {
            throw new ResourceNotFoundException("Review", "id", id);
        }
        reviewRepository.updateVerifiedStatus(id, status);
    }

    @Transactional
    public void delete(UUID id) {
        if (!reviewRepository.existsById(id)) {
            throw new ResourceNotFoundException("Review", "id", id);
        }
        Review r = reviewRepository.findById(id).get();
        UUID hotelId = r.getHotel().getId();
        reviewRepository.deleteById(id);
        hotelService.refreshRating(hotelId); // recalcula o rating após deletar
    }

    // ── Privado ───────────────────────────────────────────────────────────────

    private ReviewResponse toResponse(Review r) {
        return new ReviewResponse(
                r.getId(),
                r.getHotel().getId(),
                r.getUser().getId(),
                r.getUser().getFullName(),
                r.getRating(),
                r.getComment(),
                r.isVerified(),
                r.getCreatedAt()
        );
    }
}