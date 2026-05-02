package com.lumora.controller;

import com.lumora.dto.ApiResponse;
import com.lumora.dto.HotelRequest;
import com.lumora.dto.HotelResponse;
import com.lumora.dto.PagedResponse;
import com.lumora.dto.*;
import com.lumora.model.Hotel;
import com.lumora.service.HotelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

// ─────────────────────────────────────────────────────────────────────────────
// CONTROLLER/HOTELCONTROLLER.JAVA
//
// Endpoints REST para gerenciamento de hotéis.
//
// Rotas públicas (sem token):
//   GET /api/v1/hotels          → busca com filtros
//   GET /api/v1/hotels/featured → destaques
//   GET /api/v1/hotels/{id}     → detalhes
//
// Rotas protegidas:
//   POST   /api/v1/hotels       → criar (ADMIN/CURATOR)
//   PUT    /api/v1/hotels/{id}  → atualizar (ADMIN/CURATOR)
//   DELETE /api/v1/hotels/{id}  → deletar (ADMIN)
// ─────────────────────────────────────────────────────────────────────────────

@RestController
@RequestMapping("/api/v1/hotels")
@RequiredArgsConstructor
@Tag(name = "Hotéis", description = "Busca, detalhes e gerenciamento de hotéis")
public class HotelController {

    private final HotelService hotelService;

    // GET /api/v1/hotels/featured
    @GetMapping("/featured")
    @Operation(summary = "Hotéis em destaque ordenados por avaliação")
    public ResponseEntity<ApiResponse<List<HotelResponse>>> featured() {
        return ResponseEntity.ok(ApiResponse.ok(hotelService.getFeatured()));
    }

    // GET /api/v1/hotels?country=greece&region=MEDITERRANEAN&minPrice=500&page=0&size=12
    @GetMapping
    @Operation(summary = "Buscar hotéis com filtros opcionais e paginação")
    public ResponseEntity<ApiResponse<PagedResponse<HotelResponse>>> search(
            @RequestParam(required = false) String country,
            @RequestParam(required = false) Hotel.Region region,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) BigDecimal minStars,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "averageRating") String sort
    ) {
        Pageable pageable = PageRequest.of(
                page,
                Math.min(size, 50),
                Sort.by(Sort.Direction.DESC, sort)
        );
        return ResponseEntity.ok(ApiResponse.ok(
                hotelService.search(country, region, minPrice, maxPrice, minStars, pageable)
        ));
    }

    // GET /api/v1/hotels/{id}
    @GetMapping("/{id}")
    @Operation(summary = "Detalhes de um hotel por ID")
    public ResponseEntity<ApiResponse<HotelResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(hotelService.getById(id)));
    }

    // POST /api/v1/hotels
    @PostMapping
    @Operation(summary = "Criar hotel (ADMIN/CURATOR)",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<HotelResponse>> create(
            @Valid @RequestBody HotelRequest req
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Hotel criado com sucesso", hotelService.create(req)));
    }

    // PUT /api/v1/hotels/{id}
    @PutMapping("/{id}")
    @Operation(summary = "Atualizar hotel (ADMIN/CURATOR)",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<HotelResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody HotelRequest req
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok("Hotel atualizado", hotelService.update(id, req))
        );
    }

    // DELETE /api/v1/hotels/{id}
    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar hotel (ADMIN)",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        hotelService.delete(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}