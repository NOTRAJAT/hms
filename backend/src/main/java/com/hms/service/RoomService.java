package com.hms.service;

import com.hms.api.ApiException;
import com.hms.api.dto.RoomSearchResponse;
import com.hms.domain.Booking;
import com.hms.domain.Room;
import com.hms.repo.BookingRepository;
import com.hms.repo.RoomRepository;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoomService {
  private final RoomRepository roomRepository;
  private final BookingRepository bookingRepository;

  public RoomService(RoomRepository roomRepository, BookingRepository bookingRepository) {
    this.roomRepository = roomRepository;
    this.bookingRepository = bookingRepository;
  }

  @Transactional(readOnly = true)
  public List<RoomSearchResponse> search(
      LocalDate checkInDate,
      LocalDate checkOutDate,
      int adults,
      int children,
      String roomType
  ) {
    validate(checkInDate, checkOutDate, adults, children, roomType);

    List<Room> rooms = roomRepository.findByActiveTrueAndRoomType(roomType.trim());
    return rooms.stream()
        .filter(room -> adults <= room.getOccupancyAdults() && children <= room.getOccupancyChildren())
        .map(room -> new RoomSearchResponse(
            room.getRoomCode(),
            room.getRoomType(),
            room.getPricePerNight(),
            room.getOccupancyAdults(),
            room.getOccupancyChildren(),
            Arrays.stream(room.getAmenitiesCsv().split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList(),
            room.getRoomSizeSqFt(),
            room.getImageUrl(),
            isAvailable(room.getRoomCode(), checkInDate, checkOutDate)
        ))
        .toList();
  }

  private boolean isAvailable(String roomCode, LocalDate checkInDate, LocalDate checkOutDate) {
    List<Booking> overlaps = bookingRepository.findByRoomCodeAndStatusAndCheckInDateLessThanAndCheckOutDateGreaterThan(
        roomCode,
        Booking.Status.Confirmed,
        checkOutDate,
        checkInDate
    );
    return overlaps.isEmpty();
  }

  private void validate(LocalDate checkInDate, LocalDate checkOutDate, int adults, int children, String roomType) {
    if (checkInDate == null || checkOutDate == null) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Check-in and check-out dates are required.");
    }
    if (checkInDate.isBefore(LocalDate.now())) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "checkInDate", "Check-in date cannot be in the past.");
    }
    if (!checkOutDate.isAfter(checkInDate)) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "checkOutDate", "Check-out date must be after the check-in date.");
    }
    if (adults < 1 || adults > 10) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "adults", "At least one adult must be selected.");
    }
    if (children < 0 || children > 5) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "children", "Number of children cannot be negative.");
    }
    if (roomType == null || roomType.isBlank()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "roomType", "Please select a room type.");
    }
  }
}
