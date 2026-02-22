package com.hms.repo;

import com.hms.domain.Booking;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {
  List<Booking> findByCustomerUserIdOrderByCreatedAtDesc(String customerUserId);
  Optional<Booking> findByBookingId(String bookingId);
  Optional<Booking> findByInvoiceId(String invoiceId);
  Optional<Booking> findByBookingIdAndCustomerUserId(String bookingId, String customerUserId);

  List<Booking> findByRoomCodeAndStatusAndCheckInDateLessThanAndCheckOutDateGreaterThan(
      String roomCode,
      Booking.Status status,
      LocalDate checkOutDate,
      LocalDate checkInDate
  );

  boolean existsByRoomCodeAndStatusAndCheckInDateLessThanAndCheckOutDateGreaterThan(
      String roomCode,
      Booking.Status status,
      LocalDate checkOutDate,
      LocalDate checkInDate
  );

  boolean existsByRoomCodeAndStatusAndCheckOutDateGreaterThan(
      String roomCode,
      Booking.Status status,
      LocalDate date
  );

  boolean existsByRoomCodeAndCustomerUserIdAndCheckInDateAndCheckOutDateAndStatus(
      String roomCode,
      String customerUserId,
      LocalDate checkInDate,
      LocalDate checkOutDate,
      Booking.Status status
  );

  long countByCreatedAtBetween(Instant from, Instant to);

  long countByStatusAndCheckInDateLessThanEqualAndCheckOutDateGreaterThan(
      Booking.Status status,
      LocalDate checkInDate,
      LocalDate checkOutDate
  );

  List<Booking> findByBookingIdContainingIgnoreCaseOrCustomerNameContainingIgnoreCaseOrderByCreatedAtDesc(
      String bookingId,
      String customerName
  );

  List<Booking> findAllByOrderByCreatedAtDesc();
}
