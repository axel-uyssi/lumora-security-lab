package com.lumora.controller;

import com.lumora.dto.ApiResponse;
import com.lumora.dto.RoomRequest;
import com.lumora.dto.RoomResponse;
import com.lumora.dto.*;
import com.lumora.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

// ─────────────────────────────────────────────────────────────────────────────
// CONTROLLER/ROOMCONTROLLER.JAVA
//
// Rota aninhada: /api/v1/hotels/{hotelId}/rooms
//   Deixa claro que quartos PERTENCEM a um hotel (REST semântico correto).
//
// Rotas públicas:
//   GET /api/v1/hotels/{hotelId}/rooms              → todos os quartos do hotel
//   GET /api/v1/hotels/{hotelId}/rooms/available    → disponíveis no período
//   GET /api/v1/hotels/{hotelId}/rooms/{id}         → detalhes de um quarto
//
// Rotas protegidas:
//   POST   /api/v1/hotels/{hotelId}/rooms           → criar quarto (ADMIN/CURATOR)
//   PATCH  /api/v1/hotels/{hotelId}/rooms/{id}/availability → disponibilidade
//   DELETE /api/v1/hotels/{hotelId}/rooms/{id}      → deletar (ADMIN)
// ─────────────────────────────────────────────────────────────────────────────

@RestController
@RequestMapping("/api/v1/hotels/{hotelId}/rooms")
@RequiredArgsConstructor
@Tag(name = "Quartos", description = "Gerenciamento de quartos por hotel")
public class RoomController {

    private final RoomService roomService;

    // GET /api/v1/hotels/{hotelId}/rooms
    @GetMapping
    @Operation(summary = "Listar todos os quartos de um hotel")
    public ResponseEntity<ApiResponse<List<RoomResponse>>> getByHotel(
            @PathVariable UUID hotelId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(roomService.getByHotel(hotelId)));
    }

    // GET /api/v1/hotels/{hotelId}/rooms/available?checkIn=2025-07-01&checkOut=2025-07-05&guests=2
    @GetMapping("/available")
    @Operation(summary = "Quartos disponíveis para um período e número de hóspedes")
    public ResponseEntity<ApiResponse<List<RoomResponse>>> getAvailable(
            @PathVariable UUID hotelId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(defaultValue = "1") int guests
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                roomService.getAvailable(hotelId, checkIn, checkOut, guests)
        ));
    }

    // GET /api/v1/hotels/{hotelId}/rooms/{id}
    @GetMapping("/{id}")
    @Operation(summary = "Detalhes de um quarto")
    public ResponseEntity<ApiResponse<RoomResponse>> getById(
            @PathVariable UUID hotelId,
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(ApiResponse.ok(roomService.getById(id)));
    }

    // POST /api/v1/hotels/{hotelId}/rooms
    @PostMapping
    @Operation(summary = "Criar quarto em um hotel (ADMIN/CURATOR)",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<RoomResponse>> create(
            @PathVariable UUID hotelId,
            @Valid @RequestBody RoomRequest req
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Quarto criado", roomService.create(hotelId, req)));
    }

    // PATCH /api/v1/hotels/{hotelId}/rooms/{id}/availability?available=false
    @PatchMapping("/{id}/availability")
    @Operation(summary = "Atualizar disponibilidade de um quarto (ADMIN/CURATOR)",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> updateAvailability(
            @PathVariable UUID hotelId,
            @PathVariable UUID id,
            @RequestParam boolean available
    ) {
        roomService.updateAvailability(id, available);
        return ResponseEntity.ok(ApiResponse.ok(
                available ? "Quarto disponibilizado" : "Quarto bloqueado", null
        ));
    }

    // DELETE /api/v1/hotels/{hotelId}/rooms/{id}
    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar quarto (ADMIN)",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> delete(
            @PathVariable UUID hotelId,
            @PathVariable UUID id
    ) {
        roomService.delete(id);
        return ResponseEntity.noContent().build();
    }
}