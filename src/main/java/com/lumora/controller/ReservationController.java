package com.lumora.controller;

import com.lumora.dto.ApiResponse;
import com.lumora.dto.PagedResponse;
import com.lumora.dto.ReservationRequest;
import com.lumora.dto.ReservationResponse;
import com.lumora.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Reservas", description = "Criação e gerenciamento de reservas")
public class ReservationController {

    private final ReservationService reservationService;

    // POST /api/v1/reservations
    @PostMapping
    @Operation(summary = "Criar nova reserva")
    public ResponseEntity<ApiResponse<ReservationResponse>> create(
            @Valid @RequestBody ReservationRequest req
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Reserva confirmada", reservationService.create(req)));
    }

    // GET /api/v1/reservations?page=0&size=10
    @GetMapping
    @Operation(summary = "Listar minhas reservas")
    public ResponseEntity<ApiResponse<PagedResponse<ReservationResponse>>> myReservations(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(
                page,
                Math.min(size, 50),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        return ResponseEntity.ok(
                ApiResponse.ok(reservationService.getMyReservations(pageable))
        );
    }

    // GET /api/v1/reservations/{id}
    @GetMapping("/{id}")
    @Operation(summary = "Buscar reserva por ID")
    public ResponseEntity<ApiResponse<ReservationResponse>> getById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(ApiResponse.ok(reservationService.getById(id)));
    }

    // GET /api/v1/reservations/code/{code}
    @GetMapping("/code/{code}")
    @Operation(summary = "Buscar reserva pelo código de confirmação")
    public ResponseEntity<ApiResponse<ReservationResponse>> getByCode(
            @PathVariable String code
    ) {
        return ResponseEntity.ok(ApiResponse.ok(reservationService.getByCode(code)));
    }

    // PATCH /api/v1/reservations/{id}/cancel
    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancelar uma reserva")
    public ResponseEntity<ApiResponse<Void>> cancel(@PathVariable UUID id) {
        reservationService.cancel(id);
        return ResponseEntity.ok(ApiResponse.okMessage("Reserva cancelada"));
    }

    // PATCH /api/v1/reservations/{id}/checkin
    @PatchMapping("/{id}/checkin")
    @Operation(summary = "Realizar check-in (ADMIN/CURATOR)")
    public ResponseEntity<ApiResponse<Void>> checkIn(@PathVariable UUID id) {
        reservationService.checkIn(id);
        return ResponseEntity.ok(ApiResponse.okMessage("Check-in realizado"));
    }

    // PATCH /api/v1/reservations/{id}/checkout
    @PatchMapping("/{id}/checkout")
    @Operation(summary = "Realizar check-out (ADMIN/CURATOR)")
    public ResponseEntity<ApiResponse<Void>> checkOut(@PathVariable UUID id) {
        reservationService.checkOut(id);
        return ResponseEntity.ok(ApiResponse.okMessage("Check-out realizado"));
    }
}
