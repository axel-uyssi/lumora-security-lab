package com.lumora.service;

import com.lumora.dto.RoomRequest;
import com.lumora.dto.RoomResponse;
import com.lumora.exception.BusinessException;
import com.lumora.exception.ConflictException;
import com.lumora.exception.ResourceNotFoundException;
import com.lumora.dto.*;
import com.lumora.exception.*;
import com.lumora.model.*;
import com.lumora.repository.*;
import com.lumora.model.Hotel;
import com.lumora.model.Room;
import com.lumora.repository.HotelRepository;
import com.lumora.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RoomService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;

    // ── Leitura ───────────────────────────────────────────────────────────────

    public List<RoomResponse> getByHotel(UUID hotelId) {
        return roomRepository.findByHotelId(hotelId)
                .stream().map(RoomResponse::from).toList();
    }

    public List<RoomResponse> getAvailable(UUID hotelId, LocalDate checkIn,
                                           LocalDate checkOut, int guests) {
        validateDates(checkIn, checkOut);
        return roomRepository.findAvailableRooms(hotelId, checkIn, checkOut, guests)
                .stream().map(RoomResponse::from).toList();
    }

    public RoomResponse getById(UUID id) {
        return RoomResponse.from(findOrThrow(id));
    }

    // ── Escrita (somente ADMIN ou CURATOR) ───────────────────────────────────

    @PreAuthorize("hasAnyRole('ADMIN','CURATOR')")
    @Transactional
    public RoomResponse create(UUID hotelId, RoomRequest request) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", "id", hotelId));

        if (roomRepository.existsByHotelIdAndRoomNumber(hotelId, request.roomNumber())) {
            throw new ConflictException("Número de quarto já existe neste hotel");
        }

        Room room = Room.builder()
                .hotel(hotel)
                .roomNumber(request.roomNumber())
                .type(request.type())
                .price(request.price())
                .capacity(request.capacity())
                .floorNumber(request.floorNumber())
                .description(request.description())
                .imageUrls(request.imageUrls())
                .build();

        Room saved = roomRepository.save(room);
        log.info("Quarto criado: {} no hotel {}", saved.getRoomNumber(), hotelId);
        return RoomResponse.from(saved);
    }

    @PreAuthorize("hasAnyRole('ADMIN','CURATOR')")
    @Transactional
    public void updateAvailability(UUID id, boolean status) {
        if (!roomRepository.existsById(id)) {
            throw new ResourceNotFoundException("Room", "id", id);
        }
        roomRepository.updateAvailability(id, status);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void delete(UUID id) {
        if (!roomRepository.existsById(id)) {
            throw new ResourceNotFoundException("Room", "id", id);
        }
        roomRepository.deleteById(id);
    }

    // ── Privado ───────────────────────────────────────────────────────────────

    private Room findOrThrow(UUID id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", id));
    }

    private void validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (!checkIn.isBefore(checkOut))
            throw new BusinessException("Check-out deve ser posterior ao check-in");
        if (checkIn.isBefore(LocalDate.now()))
            throw new BusinessException("Check-in não pode ser no passado");
    }
}