package com.lumora.dto;

import com.lumora.model.Room;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record RoomResponse(
        UUID         id,
        UUID         hotelId,
        String       roomNumber,
        String       type,
        BigDecimal   price,
        int          capacity,
        boolean      available,
        Integer      floorNumber,
        String       description,
        List<String> imageUrls
) {
    public static RoomResponse from(Room room) {
        return new RoomResponse(
                room.getId(),
                room.getHotel().getId(),
                room.getRoomNumber(),
                room.getType().name(),
                room.getPrice(),
                room.getCapacity(),
                room.isAvailable(),
                room.getFloorNumber(),
                room.getDescription(),
                room.getImageUrls()
        );
    }
}