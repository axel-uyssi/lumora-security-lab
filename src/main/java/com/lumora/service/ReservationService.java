package com.lumora.service;

import com.lumora.dto.PagedResponse;
import com.lumora.dto.ReservationRequest;
import com.lumora.dto.ReservationResponse;
import com.lumora.exception.BusinessException;
import com.lumora.exception.ConflictException;
import com.lumora.exception.ResourceNotFoundException;
import com.lumora.dto.*;
import com.lumora.exception.*;
import com.lumora.model.*;
import com.lumora.repository.*;
import com.lumora.model.Hotel;
import com.lumora.model.Reservation;
import com.lumora.model.Room;
import com.lumora.model.User;
import com.lumora.repository.HotelRepository;
import com.lumora.repository.ReservationRepository;
import com.lumora.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

// ─────────────────────────────────────────────────────────────────────────────
// SERVICE/RESERVATIONSERVICE.JAVA — Lógica de negócio de reservas
//
// Fluxo de criação:
//   1. Busca usuário autenticado no SecurityContext
//   2. Valida datas (checkOut > checkIn, não no passado)
//   3. Verifica conflito de datas (anti double-booking)
//   4. Verifica capacidade do quarto
//   5. Calcula preço total (noites × tarifa)
//   6. Gera código de confirmação único
//   7. Salva e retorna
// ─────────────────────────────────────────────────────────────────────────────

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReservationService {

    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final UserService           userService;

    // ── Criar reserva ─────────────────────────────────────────────────────────

    @Transactional
    public ReservationResponse create(ReservationRequest req) {
        User user = userService.getCurrentUser();

        Hotel hotel = hotelRepository.findById(req.hotelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", "id", req.hotelId()));

        Room room = roomRepository.findById(req.roomId())
                .orElseThrow(() -> new ResourceNotFoundException("Quarto", "id", req.roomId()));

        // Validações de negócio
        validateDates(req.checkIn(), req.checkOut());

        if (reservationRepository.hasConflict(room.getId(), req.checkIn(), req.checkOut())) {
            throw new ConflictException("Quarto indisponível para o período selecionado");
        }

        if (req.numberOfGuests() > room.getCapacity()) {
            throw new BusinessException(
                    "Quarto comporta no máximo " + room.getCapacity() + " hóspedes"
            );
        }

        // Preço total fixado no momento da reserva
        long nights = ChronoUnit.DAYS.between(req.checkIn(), req.checkOut());
        BigDecimal totalPrice = room.getPrice().multiply(BigDecimal.valueOf(nights));

        Reservation reservation = Reservation.builder()
                .user(user)
                .hotel(hotel)
                .room(room)
                .checkInDate(req.checkIn())
                .checkOutDate(req.checkOut())
                .totalPrice(totalPrice)
                .numberOfGuests(req.numberOfGuests())
                .specialRequests(req.specialRequests())
                .confirmationCode(generateCode())
                .status(Reservation.Status.PENDING)
                .build();

        Reservation saved = reservationRepository.save(reservation);
        log.info("Reserva criada: {} para {}", saved.getConfirmationCode(), user.getEmail());
        return toResponse(saved);
    }

    // ── Listar minhas reservas ────────────────────────────────────────────────

    public PagedResponse<ReservationResponse> getMyReservations(Pageable pageable) {
        User user = userService.getCurrentUser();
        return PagedResponse.from(
                reservationRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable)
                        .map(this::toResponse)
        );
    }

    // ── Buscar por código de confirmação ──────────────────────────────────────

    public ReservationResponse getByCode(String code) {
        Reservation r = reservationRepository.findByConfirmationCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", "código", code));
        checkOwnership(r);
        return toResponse(r);
    }

    // ── Buscar por ID ─────────────────────────────────────────────────────────

    public ReservationResponse getById(UUID id) {
        Reservation r = findOrThrow(id);
        checkOwnership(r);
        return toResponse(r);
    }

    // ── Cancelar ──────────────────────────────────────────────────────────────

    @Transactional
    public void cancel(UUID id) {
        Reservation r = findOrThrow(id);
        checkOwnership(r);

        if (r.getStatus() != Reservation.Status.PENDING &&
                r.getStatus() != Reservation.Status.CONFIRMED) {
            throw new BusinessException(
                    "Não é possível cancelar reserva com status: " + r.getStatus()
            );
        }

        r.setStatus(Reservation.Status.CANCELLED);
        reservationRepository.save(r);
        log.info("Reserva cancelada: {}", r.getConfirmationCode());
    }

    // ── Check-in / Check-out (admin/curator) ──────────────────────────────────

    @Transactional
    public void checkIn(UUID id) {
        Reservation r = findOrThrow(id);
        if (r.getStatus() != Reservation.Status.CONFIRMED) {
            throw new BusinessException("Reserva deve estar CONFIRMED para check-in");
        }
        reservationRepository.updateStatus(id, Reservation.Status.CHECKED_IN);
        log.info("Check-in realizado: {}", r.getConfirmationCode());
    }

    @Transactional
    public void checkOut(UUID id) {
        Reservation r = findOrThrow(id);
        if (r.getStatus() != Reservation.Status.CHECKED_IN) {
            throw new BusinessException("Reserva deve estar CHECKED_IN para check-out");
        }
        reservationRepository.updateStatus(id, Reservation.Status.CHECKED_OUT);
        log.info("Check-out realizado: {}", r.getConfirmationCode());
    }

    // ── Privados ──────────────────────────────────────────────────────────────

    private void validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (!checkIn.isBefore(checkOut))
            throw new BusinessException("Check-out deve ser posterior ao check-in");
        if (checkIn.isBefore(LocalDate.now()))
            throw new BusinessException("Check-in não pode ser no passado");
    }

    private void checkOwnership(Reservation r) {
        User current = userService.getCurrentUser();
        boolean isAdmin = current.getRole() == User.Role.ADMIN;
        if (!isAdmin && !r.getUser().getId().equals(current.getId())) {
            throw new AccessDeniedException("Sem permissão para acessar esta reserva");
        }
    }

    private Reservation findOrThrow(UUID id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", "id", id));
    }

    // Gera código legível: LMR-XXXXXXXXXX (sem I, O, 0, 1 — ambíguos)
    private String generateCode() {
        StringBuilder sb = new StringBuilder("LMR-");
        for (int i = 0; i < 10; i++) {
            sb.append(CODE_CHARS.charAt(RANDOM.nextInt(CODE_CHARS.length())));
        }
        return sb.toString();
    }

    private ReservationResponse toResponse(Reservation r) {
        return new ReservationResponse(
                r.getId(),
                r.getHotel().getId(),
                r.getHotel().getName(),
                r.getRoom().getId(),
                r.getRoom().getRoomNumber(),
                r.getCheckInDate(),
                r.getCheckOutDate(),
                r.getTotalPrice(),
                r.getStatus().name(),
                r.getConfirmationCode(),
                r.getNumberOfGuests(),
                r.getSpecialRequests(),
                r.getCreatedAt()
        );
    }
}