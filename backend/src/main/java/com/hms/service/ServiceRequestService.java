package com.hms.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hms.api.ApiException;
import com.hms.api.dto.AdminBillServiceItem;
import com.hms.api.dto.AdminServiceItemResponse;
import com.hms.api.dto.AdminServicePageResponse;
import com.hms.api.dto.CabServiceCreateRequest;
import com.hms.api.dto.DiningServiceCreateRequest;
import com.hms.api.dto.DiningServiceItemRequest;
import com.hms.api.dto.SalonServiceCreateRequest;
import com.hms.api.dto.ServiceCatalogResponse;
import com.hms.api.dto.ServiceRequestResponse;
import com.hms.domain.BillingRecord;
import com.hms.domain.Booking;
import com.hms.domain.ServiceRequest;
import com.hms.repo.BillingRecordRepository;
import com.hms.repo.BookingRepository;
import com.hms.repo.ServiceRequestRepository;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServiceRequestService {
  private static final SecureRandom RANDOM = new SecureRandom();

  private static final Map<String, Integer> CAB_FARES = new LinkedHashMap<>();
  private static final Map<String, SalonPackage> SALON_PACKAGES = new LinkedHashMap<>();
  private static final Map<String, DiningMenuItem> DINING_MENU = new LinkedHashMap<>();

  static {
    CAB_FARES.put("Airport", 1200);
    CAB_FARES.put("Railway Station", 650);
    CAB_FARES.put("Bus Stand", 450);
    CAB_FARES.put("City Center", 500);
    CAB_FARES.put("Mall", 550);

    SALON_PACKAGES.put("SALON-HERBAL", new SalonPackage("SALON-HERBAL", "Herbal Glow Facial", 1800, 60));
    SALON_PACKAGES.put("SALON-PREMIUM", new SalonPackage("SALON-PREMIUM", "Premium Grooming", 2500, 75));
    SALON_PACKAGES.put("SALON-BRIDAL", new SalonPackage("SALON-BRIDAL", "Bridal Luxe Prep", 4200, 120));

    DINING_MENU.put("DNG-BREAKFAST", new DiningMenuItem("DNG-BREAKFAST", "Breakfast Platter", 450));
    DINING_MENU.put("DNG-LUNCH", new DiningMenuItem("DNG-LUNCH", "Signature Lunch Thali", 620));
    DINING_MENU.put("DNG-DINNER", new DiningMenuItem("DNG-DINNER", "Chef's Dinner Buffet", 900));
    DINING_MENU.put("DNG-COFFEE", new DiningMenuItem("DNG-COFFEE", "Coffee & Bites", 220));
    DINING_MENU.put("DNG-DESSERT", new DiningMenuItem("DNG-DESSERT", "Dessert Tasting", 300));
  }

  private final BookingRepository bookingRepository;
  private final ServiceRequestRepository serviceRequestRepository;
  private final BillingRecordRepository billingRecordRepository;
  private final ObjectMapper objectMapper;

  public ServiceRequestService(
      BookingRepository bookingRepository,
      ServiceRequestRepository serviceRequestRepository,
      BillingRecordRepository billingRecordRepository,
      ObjectMapper objectMapper
  ) {
    this.bookingRepository = bookingRepository;
    this.serviceRequestRepository = serviceRequestRepository;
    this.billingRecordRepository = billingRecordRepository;
    this.objectMapper = objectMapper;
  }

  @Transactional(readOnly = true)
  public ServiceCatalogResponse catalog() {
    List<ServiceCatalogResponse.CabDestinationOption> cab = CAB_FARES.entrySet().stream()
        .map(entry -> new ServiceCatalogResponse.CabDestinationOption(entry.getKey(), entry.getValue()))
        .toList();
    List<ServiceCatalogResponse.SalonPackageOption> salon = SALON_PACKAGES.values().stream()
        .map(item -> new ServiceCatalogResponse.SalonPackageOption(item.code(), item.name(), item.price(), item.durationMinutes()))
        .toList();
    List<ServiceCatalogResponse.DiningMenuOption> dining = DINING_MENU.values().stream()
        .map(item -> new ServiceCatalogResponse.DiningMenuOption(item.code(), item.name(), item.price()))
        .toList();
    return new ServiceCatalogResponse(cab, salon, dining);
  }

  @Transactional
  public ServiceRequestResponse createCab(String userId, CabServiceCreateRequest request) {
    LocalDateTime pickup = request.getPickupDateTime();
    if (pickup == null) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "pickupDateTime", "Pickup date/time is required.");
    }
    Booking booking = validateEligibleBooking(userId, request.getBookingId(), pickup.toLocalDate(), "pickupDateTime");
    String destination = normalizeCabDestination(request.getDestination());
    int amount = CAB_FARES.get(destination);

    validatePaymentDetails(
        request.getPaymentMethod(),
        request.getCardNumber(),
        request.getExpiryDate(),
        request.getCvv(),
        request.getOtp(),
        request.getBillingAddress()
    );

    Map<String, Object> rawDetails = Map.of(
        "destination", destination,
        "pickupDateTime", pickup.toString(),
        "fare", amount
    );
    String summary = "Cab to " + destination;
    String details = String.format("Pickup at %s for destination %s.", pickup, destination);
    List<AdminBillServiceItem> billItems = List.of(buildBillItem(
        pickup,
        "Cab",
        "Cab drop to " + destination,
        1,
        amount
    ));

    ServiceRequest record = createAndPersist(
        booking,
        ServiceRequest.ServiceType.Cab,
        pickup,
        amount,
        summary,
        details,
        rawDetails,
        billItems
    );
    return toResponse(record);
  }

  @Transactional
  public ServiceRequestResponse createSalon(String userId, SalonServiceCreateRequest request) {
    LocalDateTime slot = request.getSlotDateTime();
    if (slot == null) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "slotDateTime", "Slot date/time is required.");
    }
    Booking booking = validateEligibleBooking(userId, request.getBookingId(), slot.toLocalDate(), "slotDateTime");
    SalonPackage selected = normalizeSalonPackage(request.getPackageCode());

    validatePaymentDetails(
        request.getPaymentMethod(),
        request.getCardNumber(),
        request.getExpiryDate(),
        request.getCvv(),
        request.getOtp(),
        request.getBillingAddress()
    );

    Map<String, Object> rawDetails = Map.of(
        "packageCode", selected.code(),
        "packageName", selected.name(),
        "slotDateTime", slot.toString(),
        "durationMinutes", selected.durationMinutes(),
        "price", selected.price()
    );
    String summary = "Salon package " + selected.name();
    String details = String.format("%s at %s (%d mins).", selected.name(), slot, selected.durationMinutes());
    List<AdminBillServiceItem> billItems = List.of(buildBillItem(
        slot,
        "Salon",
        selected.name(),
        1,
        selected.price()
    ));

    ServiceRequest record = createAndPersist(
        booking,
        ServiceRequest.ServiceType.Salon,
        slot,
        selected.price(),
        summary,
        details,
        rawDetails,
        billItems
    );
    return toResponse(record);
  }

  @Transactional
  public ServiceRequestResponse createDining(String userId, DiningServiceCreateRequest request) {
    LocalDateTime delivery = request.getDeliveryDateTime();
    if (delivery == null) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "deliveryDateTime", "Delivery date/time is required.");
    }
    Booking booking = validateEligibleBooking(userId, request.getBookingId(), delivery.toLocalDate(), "deliveryDateTime");

    validatePaymentDetails(
        request.getPaymentMethod(),
        request.getCardNumber(),
        request.getExpiryDate(),
        request.getCvv(),
        request.getOtp(),
        request.getBillingAddress()
    );

    List<DiningServiceItemRequest> requestItems = request.getItems() == null ? List.of() : request.getItems();
    if (requestItems.isEmpty()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "items", "At least one dining item is required.");
    }

    List<Map<String, Object>> rawItems = new ArrayList<>();
    List<AdminBillServiceItem> billItems = new ArrayList<>();
    int total = 0;
    List<String> detailParts = new ArrayList<>();

    for (DiningServiceItemRequest item : requestItems) {
      DiningMenuItem menuItem = normalizeDiningItem(item.getItemCode());
      int quantity = item.getQuantity();
      int lineTotal = menuItem.price() * quantity;
      total += lineTotal;
      detailParts.add(menuItem.name() + " x" + quantity);
      rawItems.add(Map.of(
          "itemCode", menuItem.code(),
          "itemName", menuItem.name(),
          "quantity", quantity,
          "unitPrice", menuItem.price(),
          "lineTotal", lineTotal
      ));
      billItems.add(buildBillItem(
          delivery,
          "Dining",
          menuItem.name(),
          quantity,
          menuItem.price()
      ));
    }

    String instructions = request.getSpecialInstructions() == null ? "" : request.getSpecialInstructions().trim();
    Map<String, Object> rawDetails = new LinkedHashMap<>();
    rawDetails.put("items", rawItems);
    rawDetails.put("deliveryDateTime", delivery.toString());
    rawDetails.put("specialInstructions", instructions);
    String summary = "Dining order";
    String details = detailParts.stream().collect(Collectors.joining(", "))
        + " · Delivery at " + delivery
        + (instructions.isBlank() ? "" : " · Notes: " + instructions);

    ServiceRequest record = createAndPersist(
        booking,
        ServiceRequest.ServiceType.Dining,
        delivery,
        total,
        summary,
        details,
        rawDetails,
        billItems
    );
    return toResponse(record);
  }

  @Transactional(readOnly = true)
  public List<ServiceRequestResponse> listMy(String userId, String bookingId) {
    if (userId == null || userId.isBlank()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "User is required.");
    }
    List<ServiceRequest> items;
    if (bookingId == null || bookingId.isBlank()) {
      items = serviceRequestRepository.findByCustomerUserIdOrderByCreatedAtDesc(userId);
    } else {
      items = serviceRequestRepository.findByCustomerUserIdAndBookingIdOrderByCreatedAtDesc(userId, bookingId.trim());
    }
    return items.stream().map(this::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public AdminServicePageResponse searchAdmin(
      String q,
      String serviceType,
      String status,
      String bookingId,
      String customer,
      int page,
      int size
  ) {
    if (page < 0) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "page must be >= 0.");
    }
    if (size < 1 || size > 100) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "size must be between 1 and 100.");
    }

    String query = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
    String typeFilter = serviceType == null ? "" : serviceType.trim().toLowerCase(Locale.ROOT);
    String statusFilter = status == null ? "" : status.trim().toLowerCase(Locale.ROOT);
    String bookingFilter = bookingId == null ? "" : bookingId.trim().toLowerCase(Locale.ROOT);
    String customerFilter = customer == null ? "" : customer.trim().toLowerCase(Locale.ROOT);

    List<ServiceRequest> filtered = serviceRequestRepository.findAllByOrderByCreatedAtDesc().stream()
        .filter(item -> query.isBlank()
            || item.getRequestId().toLowerCase(Locale.ROOT).contains(query)
            || item.getBookingId().toLowerCase(Locale.ROOT).contains(query)
            || item.getCustomerUserId().toLowerCase(Locale.ROOT).contains(query)
            || item.getCustomerName().toLowerCase(Locale.ROOT).contains(query)
            || item.getServiceSummary().toLowerCase(Locale.ROOT).contains(query))
        .filter(item -> typeFilter.isBlank() || item.getServiceType().name().toLowerCase(Locale.ROOT).equals(typeFilter))
        .filter(item -> statusFilter.isBlank() || item.getStatus().name().toLowerCase(Locale.ROOT).equals(statusFilter))
        .filter(item -> bookingFilter.isBlank() || item.getBookingId().toLowerCase(Locale.ROOT).contains(bookingFilter))
        .filter(item -> customerFilter.isBlank()
            || item.getCustomerUserId().toLowerCase(Locale.ROOT).contains(customerFilter)
            || item.getCustomerName().toLowerCase(Locale.ROOT).contains(customerFilter))
        .toList();

    long totalItems = filtered.size();
    int totalPages = totalItems == 0 ? 0 : (int) Math.ceil((double) totalItems / size);
    int from = page * size;
    int to = Math.min(from + size, filtered.size());
    List<AdminServiceItemResponse> items = from >= to
        ? List.of()
        : filtered.subList(from, to).stream().map(this::toAdminResponse).toList();

    return new AdminServicePageResponse(items, page, size, totalItems, totalPages);
  }

  @Transactional
  public AdminServiceItemResponse updateStatus(String requestId, String targetStatusRaw) {
    ServiceRequest item = serviceRequestRepository.findByRequestId(requestId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Service request not found."));

    ServiceRequest.Status target = parseStatus(targetStatusRaw);
    ServiceRequest.Status current = item.getStatus();
    if (current == target) {
      return toAdminResponse(item);
    }

    if (!isValidTransition(current, target)) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid status transition.");
    }

    item.setStatus(target);
    if (target == ServiceRequest.Status.Cancelled) {
      String refundNote = String.format(
          "Refund amount INR %d initiated to bank and will be processed in 2 business days.",
          item.getAmount()
      );
      String currentDetails = item.getServiceDetails() == null ? "" : item.getServiceDetails().trim();
      if (!currentDetails.toLowerCase(Locale.ROOT).contains("refund initiated")) {
        item.setServiceDetails(currentDetails.isBlank() ? refundNote : currentDetails + " " + refundNote);
      }
    }
    serviceRequestRepository.save(item);
    return toAdminResponse(item);
  }

  private ServiceRequest createAndPersist(
      Booking booking,
      ServiceRequest.ServiceType type,
      LocalDateTime serviceDateTime,
      int amount,
      String summary,
      String details,
      Map<String, Object> rawDetails,
      List<AdminBillServiceItem> billItems
  ) {
    ServiceRequest request = new ServiceRequest();
    request.setRequestId(generateUniqueId("SRV", serviceRequestRepository::existsByRequestId));
    request.setBookingId(booking.getBookingId());
    request.setCustomerUserId(booking.getCustomerUserId());
    request.setCustomerName(booking.getCustomerName());
    request.setServiceType(type);
    request.setStatus(ServiceRequest.Status.Requested);
    request.setAmount(amount);
    request.setPaymentStatus("PAID");
    request.setPaymentMethod("Card");
    request.setTransactionId(generateUniqueId("STX", serviceRequestRepository::existsByTransactionId));
    request.setServiceDateTime(serviceDateTime);
    request.setServiceSummary(summary);
    request.setServiceDetails(details);
    request.setDetailsJson(toJson(rawDetails));

    serviceRequestRepository.save(request);
    createBillingRecord(booking, request, billItems);
    return request;
  }

  private void createBillingRecord(Booking booking, ServiceRequest serviceRequest, List<AdminBillServiceItem> billItems) {
    BillingRecord record = new BillingRecord();
    record.setBillId(generateUniqueId("BIL", billingRecordRepository::existsByBillId));
    record.setCustomerUserId(booking.getCustomerUserId());
    record.setCustomerName(booking.getCustomerName());
    record.setBookingId(booking.getBookingId());
    record.setRoomCharges(0);
    record.setServiceCharges(serviceRequest.getAmount());
    record.setAdditionalFees(0);
    record.setTaxes(0);
    record.setDiscounts(0);
    record.setTotalAmount(serviceRequest.getAmount());
    record.setPaymentStatus("PAID");
    record.setServiceItemsJson(toJson(billItems));
    billingRecordRepository.save(record);
  }

  private Booking validateEligibleBooking(String userId, String bookingId, LocalDate serviceDate, String field) {
    if (userId == null || userId.isBlank()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "User is required.");
    }
    if (bookingId == null || bookingId.isBlank()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "bookingId", "Booking ID is required.");
    }

    Booking booking = bookingRepository.findByBookingIdAndCustomerUserId(bookingId.trim(), userId)
        .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Booking not found for the logged-in customer."));

    if (booking.getStatus() != Booking.Status.Confirmed) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Only confirmed bookings can request services.");
    }

    LocalDate today = LocalDate.now();
    if (booking.getCheckOutDate().isBefore(today)) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Only active/upcoming bookings can use services.");
    }

    if (serviceDate.isBefore(today)) {
      throw new ApiException(HttpStatus.BAD_REQUEST, field, "Service date/time cannot be in the past.");
    }

    boolean withinStayWindow = !serviceDate.isBefore(booking.getCheckInDate())
        && serviceDate.isBefore(booking.getCheckOutDate());
    if (!withinStayWindow) {
      throw new ApiException(HttpStatus.BAD_REQUEST, field, "Service date/time must be within the booking stay window.");
    }

    return booking;
  }

  private String normalizeCabDestination(String value) {
    if (value == null || value.isBlank()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "destination", "Destination is required.");
    }
    String normalized = value.trim().toLowerCase(Locale.ROOT);
    return CAB_FARES.keySet().stream()
        .filter(key -> key.toLowerCase(Locale.ROOT).equals(normalized))
        .findFirst()
        .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Invalid cab destination selected."));
  }

  private SalonPackage normalizeSalonPackage(String value) {
    if (value == null || value.isBlank()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "packageCode", "Salon package is required.");
    }
    SalonPackage selected = SALON_PACKAGES.get(value.trim().toUpperCase(Locale.ROOT));
    if (selected == null) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid salon package selected.");
    }
    return selected;
  }

  private DiningMenuItem normalizeDiningItem(String code) {
    if (code == null || code.isBlank()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "itemCode", "Dining item code is required.");
    }
    DiningMenuItem selected = DINING_MENU.get(code.trim().toUpperCase(Locale.ROOT));
    if (selected == null) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid dining item selected.");
    }
    return selected;
  }

  private void validatePaymentDetails(
      String paymentMethod,
      String cardNumber,
      String expiryDate,
      String cvv,
      String otp,
      String billingAddress
  ) {
    if (paymentMethod == null || !"card".equalsIgnoreCase(paymentMethod.trim())) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "paymentMethod", "Only Card payment method is supported.");
    }
    if (!isCardNumberValid(cardNumber)) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid card number");
    }
    if (isExpiryInvalidOrPast(expiryDate)) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Card expiry date must be in the future.");
    }
    if (!isCvvValid(cardNumber, cvv)) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid CVV. Please check again.");
    }
    validateOtp(otp);
    if (billingAddress != null && !billingAddress.isBlank() && billingAddress.trim().length() < 5) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "billingAddress", "Billing address must be at least 5 characters.");
    }
  }

  private boolean isCardNumberValid(String value) {
    if (value == null || !value.matches("^\\d{16}$")) {
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
    if (value == null || !value.matches("^(0[1-9]|1[0-2])\\/\\d{2}$")) {
      return true;
    }
    int month = Integer.parseInt(value.substring(0, 2));
    int year = 2000 + Integer.parseInt(value.substring(3, 5));
    LocalDate now = LocalDate.now();
    return year < now.getYear() || (year == now.getYear() && month <= now.getMonthValue());
  }

  private boolean isCvvValid(String cardNumber, String cvv) {
    if (cardNumber == null || cvv == null) {
      return false;
    }
    boolean amex = cardNumber.matches("^3[47]\\d{13}$");
    if (amex) {
      return cvv.matches("^\\d{4}$");
    }
    return cvv.matches("^\\d{3}$");
  }

  private void validateOtp(String otp) {
    if ("123456".equals(otp)) {
      return;
    }
    if ("000000".equals(otp)) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Your OTP session has expired. Please try again.");
    }
    throw new ApiException(HttpStatus.BAD_REQUEST, "Transaction failed. Invalid OTP.");
  }

  private ServiceRequest.Status parseStatus(String value) {
    if (value == null || value.isBlank()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "status", "Status is required.");
    }
    String normalized = value.trim().toLowerCase(Locale.ROOT).replace("_", "").replace(" ", "");
    return switch (normalized) {
      case "requested" -> ServiceRequest.Status.Requested;
      case "confirmed" -> ServiceRequest.Status.Confirmed;
      case "completed" -> ServiceRequest.Status.Completed;
      case "cancelled", "canceled" -> ServiceRequest.Status.Cancelled;
      default -> throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid status value.");
    };
  }

  private boolean isValidTransition(ServiceRequest.Status from, ServiceRequest.Status to) {
    return switch (from) {
      case Requested -> to == ServiceRequest.Status.Confirmed || to == ServiceRequest.Status.Cancelled;
      case Confirmed -> to == ServiceRequest.Status.Completed || to == ServiceRequest.Status.Cancelled;
      case Completed, Cancelled -> false;
    };
  }

  private ServiceRequestResponse toResponse(ServiceRequest record) {
    return new ServiceRequestResponse(
        record.getRequestId(),
        record.getBookingId(),
        record.getCustomerUserId(),
        record.getCustomerName(),
        record.getServiceType().name(),
        record.getStatus().name(),
        record.getAmount(),
        record.getPaymentStatus(),
        record.getPaymentMethod(),
        record.getTransactionId(),
        record.getServiceDateTime(),
        record.getServiceSummary(),
        record.getServiceDetails(),
        record.getCreatedAt(),
        record.getUpdatedAt()
    );
  }

  private AdminServiceItemResponse toAdminResponse(ServiceRequest record) {
    return new AdminServiceItemResponse(
        record.getRequestId(),
        record.getBookingId(),
        record.getCustomerUserId(),
        record.getCustomerName(),
        record.getServiceType().name(),
        record.getStatus().name(),
        record.getAmount(),
        record.getPaymentStatus(),
        record.getPaymentMethod(),
        record.getTransactionId(),
        record.getServiceDateTime(),
        record.getServiceSummary(),
        record.getServiceDetails(),
        record.getCreatedAt(),
        record.getUpdatedAt()
    );
  }

  private String generateUniqueId(String prefix, Predicate<String> existsFn) {
    for (int attempt = 0; attempt < 24; attempt += 1) {
      String candidate = String.format("%s-%06d", prefix, RANDOM.nextInt(1_000_000));
      if (!existsFn.test(candidate)) {
        return candidate;
      }
    }
    throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to generate unique identifier.");
  }

  private String toJson(Object value) {
    try {
      return objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException ignored) {
      return "{}";
    }
  }

  private AdminBillServiceItem buildBillItem(
      LocalDateTime dateTime,
      String serviceType,
      String description,
      int quantity,
      int unitPrice
  ) {
    AdminBillServiceItem item = new AdminBillServiceItem();
    item.setServiceDateTime(dateTime);
    item.setServiceType(serviceType);
    item.setDescription(description);
    item.setQuantity(quantity);
    item.setUnitPrice(unitPrice);
    item.setTaxPercent(0);
    item.setDiscountPercent(0);
    return item;
  }

  private record SalonPackage(String code, String name, int price, int durationMinutes) {
  }

  private record DiningMenuItem(String code, String name, int price) {
  }
}
