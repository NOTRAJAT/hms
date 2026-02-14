package com.hms.repo;

import com.hms.domain.Booking;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {
  List<Booking> findByCustomerUserIdOrderByCreatedAtDesc(String customerUserId);
  Optional<Booking> findByBookingId(String bookingId);
  Optional<Booking> findByBookingIdAndCustomerUserId(String bookingId, String customerUserId);

  List<Booking> findByRoomCodeAndStatusAndCheckInDateLessThanAndCheckOutDateGreaterThan(
      String roomCode,
      Booking.Status status,
      LocalDate checkOutDate,
      LocalDate checkInDate
  );

  boolean existsByRoomCodeAndCustomerUserIdAndCheckInDateAndCheckOutDateAndStatus(
      String roomCode,
      String customerUserId,
      LocalDate checkInDate,
      LocalDate checkOutDate,
      Booking.Status status
  );
}
