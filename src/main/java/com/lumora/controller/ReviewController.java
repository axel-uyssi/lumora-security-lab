package com.lumora.controller;

import com.lumora.dto.ApiResponse;
import com.lumora.dto.PagedResponse;
import com.lumora.dto.ReviewRequest;
import com.lumora.dto.ReviewResponse;
import com.lumora.dto.*;
import com.lumora.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// ─────────────────────────────────────────────────────────────────────────────
// CONTROLLER/REVIEWCONTROLLER.JAVA
//
// Rota aninhada: /api/v1/hotels/{hotelId}/reviews
//   Semântica REST correta: avaliações PERTENCEM a um hotel.
//   O hotelId vem do path — o body tem apenas rating e comment.
//
// Rotas públicas:
//   GET /api/v1/hotels/{hotelId}/reviews           → todas as avaliações
//   GET /api/v1/hotels/{hotelId}/reviews/verified  → só avaliações verificadas
//   GET /api/v1/hotels/{hotelId}/reviews/stats     → distribuição de notas
//
// Rotas autenticadas:
//   POST   /api/v1/hotels/{hotelId}/reviews        → avaliar hotel
//   GET    /api/v1/reviews/me                      → minhas avaliações
//   DELETE /api/v1/hotels/{hotelId}/reviews/{id}   → deletar (ADMIN)
// ─────────────────────────────────────────────────────────────────────────────

@RestController
@RequiredArgsConstructor
@Tag(name = "Avaliações", description = "Reviews e ratings de hotéis")
public class ReviewController {

    private final ReviewService reviewService;

    // GET /api/v1/hotels/{hotelId}/reviews?page=0&size=10
    @GetMapping("/api/v1/hotels/{hotelId}/reviews")
    @Operation(summary = "Listar avaliações de um hotel (público)")
    public ResponseEntity<ApiResponse<PagedResponse<ReviewResponse>>> list(
            @PathVariable UUID hotelId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        return ResponseEntity.ok(
                ApiResponse.ok(reviewService.getByHotel(hotelId, pageable))
        );
    }

    // GET /api/v1/hotels/{hotelId}/reviews/verified
    @GetMapping("/api/v1/hotels/{hotelId}/reviews/verified")
    @Operation(summary = "Listar apenas avaliações verificadas (público)")
    public ResponseEntity<ApiResponse<PagedResponse<ReviewResponse>>> listVerified(
            @PathVariable UUID hotelId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        return ResponseEntity.ok(
                ApiResponse.ok(reviewService.getVerifiedByHotel(hotelId, pageable))
        );
    }

    // GET /api/v1/hotels/{hotelId}/reviews/stats
    @GetMapping("/api/v1/hotels/{hotelId}/reviews/stats")
    @Operation(summary = "Distribuição de notas por estrela (público)")
    public ResponseEntity<ApiResponse<List<Object[]>>> stats(@PathVariable UUID hotelId) {
        return ResponseEntity.ok(
                ApiResponse.ok(reviewService.getRatingDistribution(hotelId))
        );
    }

    // POST /api/v1/hotels/{hotelId}/reviews
    @PostMapping("/api/v1/hotels/{hotelId}/reviews")
    @Operation(summary = "Avaliar um hotel (autenticado)",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<ReviewResponse>> create(
            @PathVariable UUID hotelId,
            @Valid @RequestBody ReviewRequest req
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Avaliação enviada", reviewService.create(hotelId, req)));
    }

    // GET /api/v1/reviews/me
    @GetMapping("/api/v1/reviews/me")
    @Operation(summary = "Minhas avaliações",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<PagedResponse<ReviewResponse>>> myReviews(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        return ResponseEntity.ok(
                ApiResponse.ok(reviewService.getMyReviews(pageable))
        );
    }

    // DELETE /api/v1/hotels/{hotelId}/reviews/{id}
    @DeleteMapping("/api/v1/hotels/{hotelId}/reviews/{id}")
    @Operation(summary = "Deletar avaliação (ADMIN)",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> delete(
            @PathVariable UUID hotelId,
            @PathVariable UUID id
    ) {
        reviewService.delete(id);
        return ResponseEntity.noContent().build();
    }
}