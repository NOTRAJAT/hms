package com.hms.service;

import com.hms.api.ApiException;
import com.hms.api.dto.BookingResponse;
import com.hms.api.dto.CancellationPreviewResponse;
import com.hms.api.dto.InvoiceResponse;
import com.hms.api.dto.ModifyBookingConfirmRequest;
import com.hms.api.dto.ModifyBookingPreviewResponse;
import com.hms.api.dto.ModifyBookingRequest;
import com.hms.api.dto.PaymentRequest;
import com.hms.domain.Booking;
import com.hms.domain.Room;
import com.hms.repo.BookingRepository;
import com.hms.repo.RoomRepository;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Comparator;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {
  private static final SecureRandom RANDOM = new SecureRandom();
  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
  private static final DateTimeFormatter TIMESTAMP_FORMAT = new DateTimeFormatterBuilder()
      .appendPattern("dd-MM-yyyy HH:mm:ss")
      .toFormatter();

  private final BookingRepository bookingRepository;
  private final RoomRepository roomRepository;

  public BookingService(BookingRepository bookingRepository, RoomRepository roomRepository) {
    this.bookingRepository = bookingRepository;
    this.roomRepository = roomRepository;
  }

  @Transactional
  public BookingResponse payAndCreateBooking(PaymentRequest request) {
    Room room = roomRepository.findByRoomCode(request.getRoomId())
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Room not found."));

    validatePaymentRequest(request, room);

    if (bookingRepository.existsByRoomCodeAndCustomerUserIdAndCheckInDateAndCheckOutDateAndStatus(
        room.getRoomCode(),
        request.getUserId(),
        request.getCheckInDate(),
        request.getCheckOutDate(),
        Booking.Status.Confirmed
    )) {
      throw new ApiException(HttpStatus.CONFLICT, "Duplicate booking for the same room and customer within the same dates.");
    }

    boolean available = bookingRepository.findByRoomCodeAndStatusAndCheckInDateLessThanAndCheckOutDateGreaterThan(
        room.getRoomCode(),
        Booking.Status.Confirmed,
        request.getCheckOutDate(),
        request.getCheckInDate()
    ).isEmpty();
    if (!available) {
      throw new ApiException(HttpStatus.CONFLICT, "No rooms available for the selected dates. Please try different dates.");
    }

    if (!isCardNumberValid(request.getCardNumber())) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid card number");
    }
    if (isExpiryInvalidOrPast(request.getExpiryDate())) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Card expiry date must be in the future.");
    }
    if (!isCvvValid(request.getCardNumber(), request.getCvv())) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid CVV. Please check again.");
    }

    int nights = (int) (request.getCheckOutDate().toEpochDay() - request.getCheckInDate().toEpochDay());
    int basePrice = room.getPricePerNight() * nights;
    int gst = Math.round(basePrice * 0.10f);
    int service = Math.round(basePrice * 0.02f);
    int total = basePrice + gst + service;

    Booking booking = new Booking();
    booking.setBookingId(generateId("BK"));
    booking.setInvoiceId(generateId("INV"));
    booking.setTransactionId(generateId("TXN"));
    booking.setCustomerUserId(request.getUserId());
    booking.setCustomerName(request.getCustomerName().trim());
    booking.setCustomerEmail(request.getCustomerEmail().trim().toLowerCase());
    booking.setCustomerMobile(request.getCustomerMobile().trim());
    booking.setRoomCode(room.getRoomCode());
    booking.setRoomType(room.getRoomType());
    booking.setOccupancyAdults(room.getOccupancyAdults());
    booking.setOccupancyChildren(room.getOccupancyChildren());
    booking.setPricePerNight(room.getPricePerNight());
    booking.setCheckInDate(request.getCheckInDate());
    booking.setCheckOutDate(request.getCheckOutDate());
    booking.setNights(nights);
    booking.setAdults(request.getAdults());
    booking.setChildren(request.getChildren());
    booking.setBasePrice(basePrice);
    booking.setGstAmount(gst);
    booking.setServiceChargeAmount(service);
    booking.setTotalAmount(total);
    booking.setPaymentMethod(request.getPaymentMethod().trim());
    booking.setSpecialRequests(request.getSpecialRequests() == null ? "" : request.getSpecialRequests().trim());
    booking.setStatus(Booking.Status.Confirmed);

    bookingRepository.save(booking);
    return toResponse(booking);
  }

  @Transactional(readOnly = true)
  public List<BookingResponse> listByUser(String userId) {
    if (userId == null || userId.isBlank()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "userId is required.");
    }
    return bookingRepository.findByCustomerUserIdOrderByCreatedAtDesc(userId).stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional
  public BookingResponse cancel(String bookingId, String userId) {
    CancellationPolicyResult policy = evaluateCancellation(bookingId, userId);
    Booking booking = policy.booking;
    booking.setStatus(Booking.Status.Cancelled);
    booking.setCancelledAt(Instant.now());
    booking.setCancellationRefundAmount(policy.refundAmount);
    booking.setCancellationNote(policy.finalMessage);
    bookingRepository.save(booking);
    return toResponse(booking);
  }

  @Transactional(readOnly = true)
  public CancellationPreviewResponse cancellationPreview(String bookingId, String userId) {
    CancellationPolicyResult policy = evaluateCancellation(bookingId, userId);
    return new CancellationPreviewResponse(true, policy.refundAmount, policy.previewMessage);
  }

  @Transactional(readOnly = true)
  public InvoiceResponse getInvoice(String bookingId, String userId) {
    if (userId == null || userId.isBlank()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "userId is required.");
    }
    Booking booking = bookingRepository.findByBookingIdAndCustomerUserId(bookingId, userId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Booking not found for this user."));
    if (booking.getStatus() != Booking.Status.Confirmed) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Invoice can only be generated for paid bookings.");
    }
    return new InvoiceResponse(
        booking.getInvoiceId(),
        booking.getBookingId(),
        booking.getTransactionId(),
        booking.getCustomerName(),
        booking.getCustomerEmail(),
        booking.getCustomerMobile(),
        booking.getRoomType(),
        booking.getOccupancyAdults(),
        booking.getOccupancyChildren(),
        booking.getPricePerNight(),
        booking.getCheckInDate().format(DATE_FORMAT),
        booking.getCheckOutDate().format(DATE_FORMAT),
        booking.getNights(),
        booking.getAdults(),
        booking.getChildren(),
        booking.getBasePrice(),
        booking.getGstAmount(),
        booking.getServiceChargeAmount(),
        booking.getTotalAmount(),
        booking.getPaymentMethod(),
        LocalDateTime.ofInstant(booking.getCreatedAt(), ZoneId.systemDefault()).format(TIMESTAMP_FORMAT),
        "Hotel Management System",
        "12 Garden Lane, City Center",
        "support@hotel.example",
        "+91 90000 12345"
    );
  }

  @Transactional(readOnly = true)
  public ModifyBookingPreviewResponse previewModification(String bookingId, ModifyBookingRequest request) {
    ModificationData data = buildModificationData(bookingId, request);
    String message;
    if (data.additional > 0) {
      message = "Additional payment is required to confirm this modification.";
    } else if (data.refund > 0) {
      message = "Your updated booking costs less. Refund (if applicable) will be processed as per the cancellation policy.";
    } else {
      message = "No price change for this modification.";
    }
    return new ModifyBookingPreviewResponse(
        data.booking.getBookingId(),
        data.room.getRoomCode(),
        data.room.getRoomType(),
        data.room.getOccupancyAdults(),
        data.room.getOccupancyChildren(),
        data.room.getPricePerNight(),
        data.booking.getTotalAmount(),
        data.newTotal,
        data.additional,
        data.refund,
        data.additional > 0,
        message
    );
  }

  @Transactional
  public BookingResponse confirmModification(String bookingId, ModifyBookingConfirmRequest request) {
    if (request.getPaymentCompleted() == null) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "paymentCompleted is required.");
    }

    ModifyBookingRequest normalized = new ModifyBookingRequest();
    normalized.setUserId(request.getUserId());
    normalized.setCheckInDate(request.getCheckInDate());
    normalized.setCheckOutDate(request.getCheckOutDate());
    normalized.setAdults(request.getAdults());
    normalized.setChildren(request.getChildren());
    normalized.setRoomType(request.getRoomType());

    ModificationData data = buildModificationData(bookingId, normalized);
    if (data.additional > 0 && !request.getPaymentCompleted()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Modification failed due to payment error. Please try again.");
    }

    Booking booking = data.booking;
    Room room = data.room;
    booking.setRoomCode(room.getRoomCode());
    booking.setRoomType(room.getRoomType());
    booking.setOccupancyAdults(room.getOccupancyAdults());
    booking.setOccupancyChildren(room.getOccupancyChildren());
    booking.setPricePerNight(room.getPricePerNight());
    booking.setCheckInDate(data.checkInDate);
    booking.setCheckOutDate(data.checkOutDate);
    booking.setNights(data.newNights);
    booking.setAdults(data.adults);
    booking.setChildren(data.children);
    booking.setBasePrice(data.newBase);
    booking.setGstAmount(data.newGst);
    booking.setServiceChargeAmount(data.newService);
    booking.setTotalAmount(data.newTotal);
    bookingRepository.save(booking);
    return toResponse(booking);
  }

  private BookingResponse toResponse(Booking booking) {
    return new BookingResponse(
        booking.getBookingId(),
        booking.getInvoiceId(),
        booking.getTransactionId(),
        booking.getRoomCode(),
        booking.getRoomType(),
        booking.getOccupancyAdults(),
        booking.getOccupancyChildren(),
        booking.getPricePerNight(),
        booking.getCheckInDate(),
        booking.getCheckOutDate(),
        booking.getNights(),
        booking.getAdults(),
        booking.getChildren(),
        booking.getBasePrice(),
        booking.getGstAmount(),
        booking.getServiceChargeAmount(),
        booking.getTotalAmount(),
        booking.getPaymentMethod(),
        booking.getStatus().name(),
        booking.getCreatedAt(),
        booking.getCancelledAt(),
        booking.getCancellationRefundAmount(),
        booking.getCancellationNote()
    );
  }

  private CancellationPolicyResult evaluateCancellation(String bookingId, String userId) {
    if (userId == null || userId.isBlank()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "userId is required.");
    }
    Booking booking = bookingRepository.findByBookingIdAndCustomerUserId(bookingId, userId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Booking not found for this user."));
    if (booking.getStatus() == Booking.Status.Cancelled) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Booking is already canceled.");
    }
    LocalDate today = LocalDate.now();
    if (!booking.getCheckInDate().isAfter(today)) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "This booking cannot be canceled as it is past the allowed cancellation window.");
    }

    long hoursUntilCheckIn = ChronoUnit.HOURS.between(LocalDateTime.now(), booking.getCheckInDate().atStartOfDay());
    int refundAmount;
    String previewMessage;
    if (hoursUntilCheckIn >= 48) {
      refundAmount = booking.getTotalAmount();
      previewMessage = "Canceling now will result in a 100% refund as per the hotel's cancellation policy. Do you want to proceed?";
    } else if (hoursUntilCheckIn >= 24) {
      refundAmount = Math.round(booking.getTotalAmount() * 0.5f);
      previewMessage = "Canceling now will result in a 50% refund as per the hotel's cancellation policy. Do you want to proceed?";
    } else {
      refundAmount = 0;
      previewMessage = "As per the hotel's policy, this booking is non-refundable.";
    }

    String finalMessage = refundAmount > 0
        ? String.format("Your booking has been canceled. A refund of INR %d will be processed within 3-5 business days.", refundAmount)
        : "As per the hotel's policy, this booking is non-refundable.";

    return new CancellationPolicyResult(booking, refundAmount, previewMessage, finalMessage);
  }

  private ModificationData buildModificationData(String bookingId, ModifyBookingRequest request) {
    Booking booking = bookingRepository.findByBookingIdAndCustomerUserId(bookingId, request.getUserId())
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Booking not found for this user."));
    if (booking.getStatus() != Booking.Status.Confirmed) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Only confirmed upcoming bookings can be modified.");
    }
    LocalDate today = LocalDate.now();
    if (!booking.getCheckInDate().isAfter(today)) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Only upcoming bookings can be modified.");
    }
    LocalDateTime cutOff = booking.getCheckInDate().atStartOfDay().minusHours(24);
    if (!LocalDateTime.now().isBefore(cutOff)) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Modifications are not allowed within 24 hours of check-in. Please contact support.");
    }
    if (request.getCheckInDate().isBefore(today)) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "checkInDate", "Check-in date cannot be in the past.");
    }
    if (!request.getCheckOutDate().isAfter(request.getCheckInDate())) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "checkOutDate", "Check-out date must be after the check-in date.");
    }
    if (request.getRoomType() == null || request.getRoomType().isBlank()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "roomType", "Please select a room type.");
    }

    Room selectedRoom = findAvailableRoomForModification(booking, request);
    if (request.getAdults() > selectedRoom.getOccupancyAdults()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "adults", "Selected adults exceed room capacity.");
    }
    if (request.getChildren() > selectedRoom.getOccupancyChildren()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "children", "Selected children exceed room capacity.");
    }

    int nights = (int) (request.getCheckOutDate().toEpochDay() - request.getCheckInDate().toEpochDay());
    int base = selectedRoom.getPricePerNight() * nights;
    int gst = Math.round(base * 0.10f);
    int service = Math.round(base * 0.02f);
    int total = base + gst + service;
    int additional = Math.max(0, total - booking.getTotalAmount());
    int refund = Math.max(0, booking.getTotalAmount() - total);

    return new ModificationData(
        booking,
        selectedRoom,
        request.getCheckInDate(),
        request.getCheckOutDate(),
        request.getAdults(),
        request.getChildren(),
        nights,
        base,
        gst,
        service,
        total,
        additional,
        refund
    );
  }

  private Room findAvailableRoomForModification(Booking currentBooking, ModifyBookingRequest request) {
    List<Room> candidates = roomRepository.findByActiveTrueAndRoomType(request.getRoomType().trim()).stream()
        .filter(room -> request.getAdults() <= room.getOccupancyAdults())
        .filter(room -> request.getChildren() <= room.getOccupancyChildren())
        .sorted(Comparator.comparingInt(Room::getPricePerNight))
        .toList();

    for (Room room : candidates) {
      List<Booking> overlaps = bookingRepository.findByRoomCodeAndStatusAndCheckInDateLessThanAndCheckOutDateGreaterThan(
          room.getRoomCode(),
          Booking.Status.Confirmed,
          request.getCheckOutDate(),
          request.getCheckInDate()
      );
      boolean unavailable = overlaps.stream().anyMatch(booking -> !booking.getBookingId().equals(currentBooking.getBookingId()));
      if (!unavailable) {
        return room;
      }
    }
    throw new ApiException(HttpStatus.CONFLICT, "The selected room type is fully booked for these dates. Please choose another option.");
  }

  private void validatePaymentRequest(PaymentRequest request, Room room) {
    LocalDate today = LocalDate.now();
    if (request.getCheckInDate().isBefore(today)) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "checkInDate", "Check-in date cannot be in the past.");
    }
    if (!request.getCheckOutDate().isAfter(request.getCheckInDate())) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "checkOutDate", "Check-out date must be after the check-in date.");
    }
    if (request.getAdults() > room.getOccupancyAdults() || request.getChildren() > room.getOccupancyChildren()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Selected guests exceed room occupancy limit.");
    }
    if (!room.getRoomType().equalsIgnoreCase(request.getRoomType())) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Selected room details are invalid.");
    }
    String billingAddress = request.getBillingAddress();
    if (billingAddress != null && !billingAddress.isBlank() && billingAddress.trim().length() < 5) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "billingAddress", "Billing address must be at least 5 characters.");
    }
  }

  private boolean isCardNumberValid(String value) {
    if (!value.matches("^\\d{16}$")) {
      return false;
    }
    int sum = 0;
    boolean doubleIt = false;
    for (int i = value.length() - 1; i >= 0; i -= 1) {
      int digit = Character.digit(value.charAt(i), 10);
      if (doubleIt) {
        digit *= 2;
        if (digit > 9) {
          digit -= 9;
        }
      }
      sum += digit;
      doubleIt = !doubleIt;
    }
    return sum % 10 == 0;
  }

  private boolean isExpiryInvalidOrPast(String value) {
    if (!value.matches("^(0[1-9]|1[0-2])\\/\\d{2}$")) {
      return true;
    }
    int month = Integer.parseInt(value.substring(0, 2));
    int year = 2000 + Integer.parseInt(value.substring(3, 5));
    LocalDate now = LocalDate.now();
    return year < now.getYear() || (year == now.getYear() && month <= now.getMonthValue());
  }

  private boolean isCvvValid(String cardNumber, String cvv) {
    boolean amex = cardNumber.matches("^3[47]\\d{13}$");
    if (amex) {
      return cvv.matches("^\\d{4}$");
    }
    return cvv.matches("^\\d{3}$");
  }

  private String generateId(String prefix) {
    return String.format("%s-%06d", prefix, RANDOM.nextInt(1_000_000));
  }

  private static class CancellationPolicyResult {
    private final Booking booking;
    private final int refundAmount;
    private final String previewMessage;
    private final String finalMessage;

    CancellationPolicyResult(Booking booking, int refundAmount, String previewMessage, String finalMessage) {
      this.booking = booking;
      this.refundAmount = refundAmount;
      this.previewMessage = previewMessage;
      this.finalMessage = finalMessage;
    }
  }

  private static class ModificationData {
    private final Booking booking;
    private final Room room;
    private final LocalDate checkInDate;
    private final LocalDate checkOutDate;
    private final int adults;
    private final int children;
    private final int newNights;
    private final int newBase;
    private final int newGst;
    private final int newService;
    private final int newTotal;
    private final int additional;
    private final int refund;

    ModificationData(
        Booking booking,
        Room room,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        int adults,
        int children,
        int newNights,
        int newBase,
        int newGst,
        int newService,
        int newTotal,
        int additional,
        int refund
    ) {
      this.booking = booking;
      this.room = room;
      this.checkInDate = checkInDate;
      this.checkOutDate = checkOutDate;
      this.adults = adults;
      this.children = children;
      this.newNights = newNights;
      this.newBase = newBase;
      this.newGst = newGst;
      this.newService = newService;
      this.newTotal = newTotal;
      this.additional = additional;
      this.refund = refund;
    }
  }
}
