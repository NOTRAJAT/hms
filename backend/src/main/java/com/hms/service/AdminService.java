package com.hms.service;

import com.hms.api.dto.AdminBookingResponse;
import com.hms.api.dto.AdminBookingPageResponse;
import com.hms.api.dto.AdminBookingCreateRequest;
import com.hms.api.dto.AdminBookingUpdateRequest;
import com.hms.api.dto.AdminBillPageResponse;
import com.hms.api.dto.AdminBillResponse;
import com.hms.api.dto.AdminBillCreateRequest;
import com.hms.api.dto.AdminBillUpdateRequest;
import com.hms.api.dto.AdminBillServiceItem;
import com.hms.api.dto.AdminBillSummaryResponse;
import com.hms.api.dto.AdminComplaintActionResponse;
import com.hms.api.dto.AdminComplaintPageResponse;
import com.hms.api.dto.AdminComplaintResponse;
import com.hms.api.dto.AdminComplaintUpdateRequest;
import com.hms.api.dto.AdminBulkImportResponse;
import com.hms.api.dto.AdminDashboardResponse;
import com.hms.api.dto.AdminRoomCreateRequest;
import com.hms.api.dto.AdminRoomPageResponse;
import com.hms.api.dto.AdminRoomOccupancyGridResponse;
import com.hms.api.dto.AdminRoomOccupancyPoint;
import com.hms.api.dto.AdminRoomOccupancyResponse;
import com.hms.api.dto.AdminRoomResponse;
import com.hms.api.dto.AdminRoomUpdateRequest;
import com.hms.api.dto.AdminUserResponse;
import com.hms.api.dto.AdminUserPageResponse;
import com.hms.api.dto.AdminUserCreateRequest;
import com.hms.api.dto.AdminUserUpdateRequest;
import com.hms.api.dto.AdminUserCreateResponse;
import com.hms.api.dto.AdminPasswordResetResponse;
import com.hms.api.ApiException;
import com.hms.domain.Booking;
import com.hms.domain.BillingRecord;
import com.hms.domain.Complaint;
import com.hms.domain.ComplaintActionLog;
import com.hms.domain.Customer;
import com.hms.domain.Room;
import com.hms.domain.Staff;
import com.hms.repo.BillingRecordRepository;
import com.hms.repo.BookingRepository;
import com.hms.repo.ComplaintActionLogRepository;
import com.hms.repo.ComplaintRepository;
import com.hms.repo.CustomerRepository;
import com.hms.repo.RoomRepository;
import com.hms.repo.StaffRepository;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AdminService {
  private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.ofPattern("dd-MM");
  private static final Pattern AUTO_ROOM_CODE_PATTERN = Pattern.compile("^(ST|DL|SU|SP)(\\d{4})$");
  private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]{5,30}$");
  private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
  private static final Pattern MOBILE_PATTERN = Pattern.compile("^\\+91[789]\\d{9}$");
  private static final SecureRandom RANDOM = new SecureRandom();
  private static final Map<String, String> ALLOWED_AMENITIES = new LinkedHashMap<>();
  static {
    ALLOWED_AMENITIES.put("wifi", "WiFi");
    ALLOWED_AMENITIES.put("tv", "TV");
    ALLOWED_AMENITIES.put("mini-bar", "Mini-bar");
    ALLOWED_AMENITIES.put("ac", "AC");
    ALLOWED_AMENITIES.put("air conditioning", "AC");
    ALLOWED_AMENITIES.put("balcony", "Balcony");
    ALLOWED_AMENITIES.put("breakfast", "Breakfast");
  }
  private final RoomRepository roomRepository;
  private final BillingRecordRepository billingRecordRepository;
  private final BookingRepository bookingRepository;
  private final ComplaintRepository complaintRepository;
  private final ComplaintActionLogRepository complaintActionLogRepository;
  private final CustomerRepository customerRepository;
  private final StaffRepository staffRepository;
  private final PasswordEncoder passwordEncoder;
  private final ObjectMapper objectMapper;

  public AdminService(
      RoomRepository roomRepository,
      BillingRecordRepository billingRecordRepository,
      BookingRepository bookingRepository,
      ComplaintRepository complaintRepository,
      ComplaintActionLogRepository complaintActionLogRepository,
      CustomerRepository customerRepository,
      StaffRepository staffRepository,
      PasswordEncoder passwordEncoder,
      ObjectMapper objectMapper
  ) {
    this.roomRepository = roomRepository;
    this.billingRecordRepository = billingRecordRepository;
    this.bookingRepository = bookingRepository;
    this.complaintRepository = complaintRepository;
    this.complaintActionLogRepository = complaintActionLogRepository;
    this.customerRepository = customerRepository;
    this.staffRepository = staffRepository;
    this.passwordEncoder = passwordEncoder;
    this.objectMapper = objectMapper;
  }

  @Transactional(readOnly = true)
  public AdminDashboardResponse getDashboardSummary() {
    Instant now = Instant.now();
    return new AdminDashboardResponse(
        bookingRepository.countByCreatedAtBetween(now.minus(1, ChronoUnit.DAYS), now),
        bookingRepository.countByCreatedAtBetween(now.minus(7, ChronoUnit.DAYS), now),
        bookingRepository.countByCreatedAtBetween(now.minus(30, ChronoUnit.DAYS), now),
        roomRepository.countByActiveTrue()
    );
  }

  @Transactional(readOnly = true)
  public AdminRoomPageResponse searchRooms(
      String q,
      String roomType,
      Integer priceMin,
      Integer priceMax,
      String availability,
      String amenity,
      Integer maxOccupancy,
      LocalDate date,
      String sortBy,
      String sortDir,
      int page,
      int size
  ) {
    if (page < 0) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "page must be >= 0.");
    }
    if (size < 1 || size > 100) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "size must be between 1 and 100.");
    }
    if (priceMin != null && priceMin < 0) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "priceMin must be >= 0.");
    }
    if (priceMax != null && priceMax < 0) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "priceMax must be >= 0.");
    }
    if (priceMin != null && priceMax != null && priceMin > priceMax) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "priceMin cannot be greater than priceMax.");
    }

    LocalDate selectedDate = date == null ? LocalDate.now() : date;
    String query = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
    String roomTypeFilter = roomType == null ? "" : roomType.trim().toLowerCase(Locale.ROOT);
    String amenityFilter = amenity == null ? "" : amenity.trim().toLowerCase(Locale.ROOT);
    String availabilityFilter = availability == null ? "" : availability.trim().toUpperCase(Locale.ROOT);

    List<RoomWithAvailability> filtered = roomRepository.findAll().stream()
        .map(room -> new RoomWithAvailability(room, availabilityForDate(room, selectedDate)))
        .filter(item -> query.isBlank()
            || item.room.getRoomCode().toLowerCase(Locale.ROOT).contains(query)
            || item.room.getRoomType().toLowerCase(Locale.ROOT).contains(query))
        .filter(item -> roomTypeFilter.isBlank()
            || item.room.getRoomType().toLowerCase(Locale.ROOT).equals(roomTypeFilter))
        .filter(item -> priceMin == null || item.room.getPricePerNight() >= priceMin)
        .filter(item -> priceMax == null || item.room.getPricePerNight() <= priceMax)
        .filter(item -> amenityFilter.isBlank()
            || item.room.getAmenitiesCsv().toLowerCase(Locale.ROOT).contains(amenityFilter))
        .filter(item -> maxOccupancy == null
            || (item.room.getOccupancyAdults() + item.room.getOccupancyChildren()) >= maxOccupancy)
        .filter(item -> availabilityFilter.isBlank() || item.availabilityStatus.equals(availabilityFilter))
        .toList();

    Comparator<RoomWithAvailability> comparator = roomComparator(sortBy);
    if ("desc".equalsIgnoreCase(sortDir)) {
      comparator = comparator.reversed();
    }
    List<RoomWithAvailability> sorted = filtered.stream().sorted(comparator).toList();

    long totalItems = sorted.size();
    int totalPages = totalItems == 0 ? 0 : (int) Math.ceil((double) totalItems / size);
    int from = page * size;
    int to = Math.min(from + size, sorted.size());
    List<AdminRoomResponse> items = from >= to ? List.of() : sorted.subList(from, to).stream()
        .map(item -> toAdminRoomResponse(item.room, item.availabilityStatus))
        .toList();

    return new AdminRoomPageResponse(items, page, size, totalItems, totalPages);
  }

  @Transactional
  public AdminRoomResponse updateRoom(String roomCode, AdminRoomUpdateRequest request) {
    Room room = roomRepository.findByRoomCode(roomCode)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Room not found."));

    if (request.getRoomType() != null
        && !request.getRoomType().isBlank()
        && !room.getRoomType().equalsIgnoreCase(request.getRoomType().trim())) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Room type is immutable and cannot be changed.");
    }

    if ("OCCUPIED".equalsIgnoreCase(room.getRoomStatus())) {
      throw new ApiException(HttpStatus.CONFLICT, "Rooms marked as 'Occupied' cannot be updated.");
    }
    if (bookingRepository.existsByRoomCodeAndStatusAndCheckOutDateGreaterThan(
        roomCode,
        Booking.Status.Confirmed,
        LocalDate.now()
    )) {
      throw new ApiException(HttpStatus.CONFLICT,
          "Room has active or upcoming reservations. Update is blocked.");
    }

    String normalizedStatus = normalizeRoomStatus(request.getRoomStatus());
    if (request.getPricePerNight() == null || request.getPricePerNight() < 1000) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Price per night must be at least 1000.");
    }
    if (request.getOccupancyAdults() == null || request.getOccupancyAdults() < 1 || request.getOccupancyAdults() > 10) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Adults capacity must be between 1 and 10.");
    }
    if (request.getOccupancyChildren() == null || request.getOccupancyChildren() < 0 || request.getOccupancyChildren() > 5) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Children capacity must be between 0 and 5.");
    }
    String normalizedAmenities = normalizeAmenities(request.getAmenitiesCsv());
    if (normalizedAmenities.isBlank()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Select at least one valid amenity.");
    }

    room.setBedType(request.getBedType().trim());
    room.setPricePerNight(request.getPricePerNight());
    room.setRoomStatus(normalizedStatus);
    room.setAmenitiesCsv(normalizedAmenities);
    room.setOccupancyAdults(request.getOccupancyAdults());
    room.setOccupancyChildren(request.getOccupancyChildren());
    room.setActive(!"UNDER_MAINTENANCE".equals(normalizedStatus) && !"DEPRECATED".equals(normalizedStatus));
    roomRepository.save(room);

    return toAdminRoomResponse(room, availabilityForDate(room, LocalDate.now()));
  }

  @Transactional
  public AdminRoomResponse addRoom(AdminRoomCreateRequest request) {
    if (request.getPricePerNight() == null || request.getPricePerNight() < 1000) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Price must be at least 1000.");
    }
    int adults;
    int children;
    if (request.getOccupancyAdults() != null || request.getOccupancyChildren() != null) {
      adults = request.getOccupancyAdults() == null ? 1 : request.getOccupancyAdults();
      children = request.getOccupancyChildren() == null ? 0 : request.getOccupancyChildren();
    } else {
      if (request.getMaxOccupancy() == null) {
        throw new ApiException(HttpStatus.BAD_REQUEST, "Provide occupancy adults/children or max occupancy.");
      }
      adults = request.getMaxOccupancy();
      children = 0;
    }
    if (adults < 1 || adults > 10) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Adults occupancy must be between 1 and 10.");
    }
    if (children < 0 || children > 5) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Children occupancy must be between 0 and 5.");
    }
    if (adults + children > 10) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Total occupancy cannot exceed 10.");
    }
    String normalizedType = normalizeRoomType(request.getRoomType());
    String normalizedAvailability = normalizeAvailability(request.getAvailability());
    String normalizedAmenities = normalizeAmenities(request.getAmenitiesCsv());
    String description = request.getDescription() == null ? "" : request.getDescription().trim();
    if (description.length() > 500) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Description cannot exceed 500 characters.");
    }

    Room room = new Room();
    room.setRoomCode(generateRoomCode(normalizedType));
    room.setRoomType(normalizedType);
    room.setBedType((request.getBedType() == null || request.getBedType().isBlank()) ? "Queen" : request.getBedType().trim());
    room.setPricePerNight(request.getPricePerNight());
    room.setAmenitiesCsv(normalizedAmenities);
    room.setOccupancyAdults(adults);
    room.setOccupancyChildren(children);
    room.setRoomSizeSqFt(defaultSizeByType(normalizedType));
    room.setImageUrl(defaultImageByType(normalizedType));
    room.setDescription(description);
    room.setActive("AVAILABLE".equals(normalizedAvailability));
    room.setRoomStatus("AVAILABLE".equals(normalizedAvailability) ? "AVAILABLE" : "UNDER_MAINTENANCE");
    roomRepository.save(room);
    return toAdminRoomResponse(room, availabilityForDate(room, LocalDate.now()));
  }

  @Transactional
  public AdminBulkImportResponse bulkImportRooms(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Please upload a CSV file.");
    }
    String content;
    try {
      content = new String(file.getBytes());
    } catch (IOException e) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Unable to read uploaded CSV.");
    }

    try (BufferedReader reader = new BufferedReader(new StringReader(content))) {
      String header = reader.readLine();
      if (header == null) {
        throw new ApiException(HttpStatus.BAD_REQUEST, "CSV file is empty.");
      }
      String normalizedHeader = header.trim().toLowerCase(Locale.ROOT);
      String expected = "roomtype,price,amenities,availability,occupancyadults,occupancychildren,description,bedtype";
      if (!normalizedHeader.equals(expected)) {
        throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid CSV header. Download template and retry.");
      }

      int imported = 0;
      int lineNo = 1;
      String line;
      while ((line = reader.readLine()) != null) {
        lineNo += 1;
        String trimmed = line.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
          continue;
        }
        List<String> cols = parseCsvLine(line);
        if (cols.size() < 8) {
          throw new ApiException(HttpStatus.BAD_REQUEST, "CSV line " + lineNo + " is incomplete.");
        }
        AdminRoomCreateRequest req = new AdminRoomCreateRequest();
        req.setRoomType(cols.get(0).trim());
        req.setPricePerNight(parseInteger(cols.get(1).trim(), "price", lineNo));
        req.setAmenitiesCsv(cols.get(2).trim());
        req.setAvailability(cols.get(3).trim());
        req.setOccupancyAdults(parseInteger(cols.get(4).trim(), "occupancyAdults", lineNo));
        req.setOccupancyChildren(parseInteger(cols.get(5).trim(), "occupancyChildren", lineNo));
        req.setDescription(cols.get(6).trim());
        req.setBedType(cols.get(7).trim());
        addRoom(req);
        imported += 1;
      }
      return new AdminBulkImportResponse(imported, "Bulk upload is successful");
    } catch (IOException e) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Unable to process CSV file.");
    }
  }

  public String csvTemplate() {
    return "# Constraints:\n"
        + "# roomType: Mandatory. Allowed values = Standard | Deluxe | Suite | Supreme\n"
        + "# price: Mandatory. Integer >= 1000 (INR)\n"
        + "# amenities: Optional. Comma-separated from allowed list = WiFi | TV | Mini-bar | AC | Balcony | Breakfast\n"
        + "# availability: Mandatory. Allowed values = Available | Not Available\n"
        + "# occupancyAdults: Mandatory. Integer between 1 and 10\n"
        + "# occupancyChildren: Mandatory. Integer between 0 and 5\n"
        + "# total occupancy rule: occupancyAdults + occupancyChildren must be <= 10\n"
        + "# description: Optional. Max length 500 characters\n"
        + "# bedType: Optional. Allowed values = King | Queen\n"
        + "roomType,price,amenities,availability,occupancyAdults,occupancyChildren,description,bedType\n"
        + "Deluxe,8000,\"WiFi, TV, AC\",Available,2,1,\"City facing room\",King\n";
  }

  private String normalizeAmenities(String amenitiesCsv) {
    if (amenitiesCsv == null) {
      return "";
    }
    List<String> normalized = List.of(amenitiesCsv.split(",")).stream()
        .map(String::trim)
        .filter(value -> !value.isBlank())
        .map(value -> value.toLowerCase(Locale.ROOT))
        .map(ALLOWED_AMENITIES::get)
        .filter(value -> value != null && !value.isBlank())
        .distinct()
        .collect(Collectors.toList());
    return String.join(", ", normalized);
  }

  private String normalizeRoomType(String roomType) {
    String type = roomType == null ? "" : roomType.trim();
    if (type.equalsIgnoreCase("Standard") || type.equalsIgnoreCase("Deluxe")
        || type.equalsIgnoreCase("Suite") || type.equalsIgnoreCase("Supreme")) {
      return Character.toUpperCase(type.charAt(0)) + type.substring(1).toLowerCase(Locale.ROOT);
    }
    throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid room type.");
  }

  private String normalizeAvailability(String availability) {
    String value = availability == null ? "" : availability.trim().toUpperCase(Locale.ROOT).replace(' ', '_');
    if ("AVAILABLE".equals(value) || "NOT_AVAILABLE".equals(value)) {
      return value;
    }
    throw new ApiException(HttpStatus.BAD_REQUEST, "Availability must be Available or Not Available.");
  }

  private int parseInteger(String value, String field, int lineNo) {
    try {
      return Integer.parseInt(value);
    } catch (Exception ex) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid " + field + " at line " + lineNo + ".");
    }
  }

  private List<String> parseCsvLine(String line) {
    List<String> values = new ArrayList<>();
    StringBuilder current = new StringBuilder();
    boolean inQuotes = false;
    for (int i = 0; i < line.length(); i++) {
      char ch = line.charAt(i);
      if (ch == '"') {
        if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
          current.append('"');
          i += 1;
        } else {
          inQuotes = !inQuotes;
        }
      } else if (ch == ',' && !inQuotes) {
        values.add(current.toString());
        current.setLength(0);
      } else {
        current.append(ch);
      }
    }
    values.add(current.toString());
    return values;
  }

  private String generateRoomCode(String roomType) {
    String prefix = switch (roomType.toLowerCase(Locale.ROOT)) {
      case "standard" -> "ST";
      case "deluxe" -> "DL";
      case "suite" -> "SU";
      case "supreme" -> "SP";
      default -> "RM";
    };

    int max = roomRepository.findAll().stream()
        .map(Room::getRoomCode)
        .filter(code -> code != null)
        .map(String::trim)
        .map(AUTO_ROOM_CODE_PATTERN::matcher)
        .filter(Matcher::matches)
        .filter(matcher -> prefix.equals(matcher.group(1)))
        .mapToInt(matcher -> Integer.parseInt(matcher.group(2)))
        .max()
        .orElse(0);

    String candidate;
    do {
      max += 1;
      candidate = String.format("%s%04d", prefix, max);
    } while (roomRepository.findByRoomCode(candidate).isPresent());
    return candidate;
  }

  private int defaultSizeByType(String roomType) {
    return switch (roomType.toLowerCase(Locale.ROOT)) {
      case "supreme" -> 420;
      case "deluxe" -> 360;
      case "suite" -> 540;
      default -> 280;
    };
  }

  private String defaultImageByType(String roomType) {
    return switch (roomType.toLowerCase(Locale.ROOT)) {
      case "deluxe" -> "/assets/Deluxe.png";
      case "suite" -> "/assets/Suite.png";
      case "supreme" -> "/assets/Supreme.png";
      default -> "/assets/Standard.png";
    };
  }

  @Transactional(readOnly = true)
  public AdminBookingPageResponse searchBookings(
      String q,
      String roomCode,
      String status,
      String roomType,
      LocalDate bookingDate,
      LocalDate fromDate,
      LocalDate toDate,
      String sortBy,
      String sortDir,
      int page,
      int size
  ) {
    if (page < 0) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "page must be >= 0.");
    }
    if (size < 1 || size > 100) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "size must be between 1 and 100.");
    }
    if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "fromDate cannot be after toDate.");
    }

    String query = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
    String roomCodeFilter = roomCode == null ? "" : roomCode.trim().toLowerCase(Locale.ROOT);
    String statusFilter = status == null ? "" : status.trim().toLowerCase(Locale.ROOT);
    String roomTypeFilter = roomType == null ? "" : roomType.trim().toLowerCase(Locale.ROOT);

    List<Booking> filtered = bookingRepository.findAllByOrderByCreatedAtDesc().stream()
        .filter(booking -> query.isBlank()
            || booking.getBookingId().toLowerCase(Locale.ROOT).contains(query)
            || booking.getCustomerName().toLowerCase(Locale.ROOT).contains(query)
            || booking.getRoomCode().toLowerCase(Locale.ROOT).contains(query))
        .filter(booking -> roomCodeFilter.isBlank()
            || booking.getRoomCode().toLowerCase(Locale.ROOT).contains(roomCodeFilter))
        .filter(booking -> roomTypeFilter.isBlank()
            || booking.getRoomType().toLowerCase(Locale.ROOT).equals(roomTypeFilter))
        .filter(booking -> bookingDate == null
            || LocalDate.ofInstant(booking.getCreatedAt(), ZoneId.systemDefault()).equals(bookingDate))
        .filter(booking -> fromDate == null || !booking.getCheckInDate().isBefore(fromDate))
        .filter(booking -> toDate == null || !booking.getCheckOutDate().isAfter(toDate))
        .filter(booking -> statusFilter.isBlank()
            || computeReservationStatus(booking).toLowerCase(Locale.ROOT).equals(statusFilter))
        .toList();

    Comparator<Booking> comparator = bookingComparator(sortBy);
    if ("desc".equalsIgnoreCase(sortDir)) {
      comparator = comparator.reversed();
    }
    List<Booking> sorted = filtered.stream().sorted(comparator).toList();

    long totalItems = sorted.size();
    int totalPages = totalItems == 0 ? 0 : (int) Math.ceil((double) totalItems / size);
    int from = page * size;
    int to = Math.min(from + size, sorted.size());
    List<AdminBookingResponse> items = from >= to ? List.of() : sorted.subList(from, to).stream()
        .map(this::toAdminBookingResponse)
        .toList();

    return new AdminBookingPageResponse(items, page, size, totalItems, totalPages);
  }

  @Transactional(readOnly = true)
  public AdminBillPageResponse searchBills(
      String q,
      String paymentStatus,
      LocalDate fromDate,
      LocalDate toDate,
      String sortBy,
      String sortDir,
      int page,
      int size
  ) {
    if (page < 0) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "page must be >= 0.");
    }
    if (size < 1 || size > 100) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "size must be between 1 and 100.");
    }
    if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "fromDate cannot be after toDate.");
    }

    List<AdminBillResponse> filtered = filteredBills(q, paymentStatus, fromDate, toDate);

    Comparator<AdminBillResponse> comparator = billComparator(sortBy);
    if ("desc".equalsIgnoreCase(sortDir)) {
      comparator = comparator.reversed();
    }
    List<AdminBillResponse> sorted = filtered.stream().sorted(comparator).toList();

    long totalItems = sorted.size();
    int totalPages = totalItems == 0 ? 0 : (int) Math.ceil((double) totalItems / size);
    int from = page * size;
    int to = Math.min(from + size, sorted.size());
    List<AdminBillResponse> items = from >= to ? List.of() : sorted.subList(from, to);

    return new AdminBillPageResponse(items, page, size, totalItems, totalPages);
  }

  @Transactional(readOnly = true)
  public String exportBillsCsv(
      String q,
      String paymentStatus,
      LocalDate fromDate,
      LocalDate toDate,
      String sortBy,
      String sortDir
  ) {
    if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "fromDate cannot be after toDate.");
    }

    List<AdminBillResponse> bills = filteredBills(q, paymentStatus, fromDate, toDate);
    Comparator<AdminBillResponse> comparator = billComparator(sortBy);
    if ("desc".equalsIgnoreCase(sortDir)) {
      comparator = comparator.reversed();
    }
    List<AdminBillResponse> sorted = bills.stream().sorted(comparator).toList();

    StringBuilder csv = new StringBuilder();
    csv.append("billType,billId,bookingId,customerUserId,customerName,issueDate,paymentStatus,")
        .append("roomCharges,serviceCharges,additionalRaw,additionalMode,additionalAmount,")
        .append("taxRaw,taxMode,taxAmount,discountRaw,discountMode,discountAmount,totalAmount,")
        .append("serviceItemCount,serviceItems\n");

    for (AdminBillResponse bill : sorted) {
      String billType = bill.isEditable() ? "MANUAL_BILL" : "INVOICE";
      String additionalMode = bill.isEditable() ? (bill.getAdditionalFees() > 100 ? "AMOUNT" : "PERCENT") : "AMOUNT";
      String taxMode = bill.isEditable() ? (bill.getTaxes() > 100 ? "AMOUNT" : "PERCENT") : "AMOUNT";
      String discountMode = bill.isEditable() ? (bill.getDiscounts() > 100 ? "AMOUNT" : "PERCENT") : "AMOUNT";
      int additionalAmount = effectiveAdditionalAmount(bill);
      int taxAmount = effectiveTaxAmount(bill, additionalAmount);
      int discountAmount = effectiveDiscountAmount(bill, additionalAmount);
      List<AdminBillServiceItem> serviceItems = bill.getServiceItems() == null ? List.of() : bill.getServiceItems();

      csv.append(csvField(billType)).append(',')
          .append(csvField(bill.getBillId())).append(',')
          .append(csvField(bill.getBookingId())).append(',')
          .append(csvField(bill.getCustomerUserId())).append(',')
          .append(csvField(bill.getCustomerName())).append(',')
          .append(csvField(String.valueOf(LocalDate.ofInstant(bill.getIssueDate(), ZoneId.systemDefault())))).append(',')
          .append(csvField(bill.getPaymentStatus())).append(',')
          .append(bill.getRoomCharges()).append(',')
          .append(bill.getServiceCharges()).append(',')
          .append(bill.getAdditionalFees()).append(',')
          .append(csvField(additionalMode)).append(',')
          .append(additionalAmount).append(',')
          .append(bill.getTaxes()).append(',')
          .append(csvField(taxMode)).append(',')
          .append(taxAmount).append(',')
          .append(bill.getDiscounts()).append(',')
          .append(csvField(discountMode)).append(',')
          .append(discountAmount).append(',')
          .append(bill.getTotalAmount()).append(',')
          .append(serviceItems.size()).append(',')
          .append(csvField(flattenServiceItems(serviceItems)))
          .append('\n');
    }

    return csv.toString();
  }

  @Transactional(readOnly = true)
  public AdminBillSummaryResponse summarizeBills(
      String q,
      String paymentStatus,
      LocalDate fromDate,
      LocalDate toDate
  ) {
    if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "fromDate cannot be after toDate.");
    }

    List<AdminBillResponse> bills = filteredBills(q, paymentStatus, fromDate, toDate);
    int totalRevenue = 0;
    int invoiceRevenue = 0;
    int manualBillRevenue = 0;
    int billRoomRevenue = 0;
    int roomRevenue = 0;
    int serviceRevenue = 0;
    int otherRevenue = 0;
    int taxRevenue = 0;
    int discountTotal = 0;

    for (AdminBillResponse bill : bills) {
      roomRevenue += bill.getRoomCharges();
      serviceRevenue += bill.getServiceCharges();

      if (bill.isEditable()) {
        manualBillRevenue += bill.getTotalAmount();
        billRoomRevenue += bill.getRoomCharges();
        int baseSubtotal = bill.getRoomCharges() + bill.getServiceCharges();
        // Backward compatibility: old rows may store absolute values, newer rows store percentages.
        int additionalAmount = bill.getAdditionalFees() > 100
            ? bill.getAdditionalFees()
            : Math.round(baseSubtotal * (bill.getAdditionalFees() / 100f));
        int subtotal = baseSubtotal + additionalAmount;
        int taxAmount = bill.getTaxes() > 100
            ? bill.getTaxes()
            : Math.round(subtotal * (bill.getTaxes() / 100f));
        int discountAmount = bill.getDiscounts() > 100
            ? bill.getDiscounts()
            : Math.round(subtotal * (bill.getDiscounts() / 100f));
        otherRevenue += additionalAmount;
        taxRevenue += taxAmount;
        discountTotal += discountAmount;
      } else {
        // Invoice contribution is treated as room cost only (as requested),
        // while manual bill contribution uses full bill total.
        invoiceRevenue += bill.getRoomCharges();
        otherRevenue += bill.getAdditionalFees();
        taxRevenue += bill.getTaxes();
        discountTotal += bill.getDiscounts();
      }
    }

    totalRevenue = invoiceRevenue + manualBillRevenue;

    return new AdminBillSummaryResponse(
        totalRevenue,
        invoiceRevenue,
        manualBillRevenue,
        billRoomRevenue,
        roomRevenue,
        serviceRevenue,
        otherRevenue,
        taxRevenue,
        discountTotal,
        bills.size()
    );
  }

  private List<AdminBillResponse> filteredBills(
      String q,
      String paymentStatus,
      LocalDate fromDate,
      LocalDate toDate
  ) {
    String query = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
    String paymentFilter = paymentStatus == null ? "" : paymentStatus.trim().toUpperCase(Locale.ROOT);

    List<AdminBillResponse> all = new ArrayList<>();
    all.addAll(bookingRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toAdminBillResponse).toList());
    all.addAll(billingRecordRepository.findAllByOrderByIssueDateDesc().stream().map(this::toAdminBillResponse).toList());

    return all.stream()
        .filter(bill -> query.isBlank()
            || bill.getBillId().toLowerCase(Locale.ROOT).contains(query)
            || bill.getCustomerName().toLowerCase(Locale.ROOT).contains(query)
            || bill.getCustomerUserId().toLowerCase(Locale.ROOT).contains(query))
        .filter(bill -> fromDate == null
            || !LocalDate.ofInstant(bill.getIssueDate(), ZoneId.systemDefault()).isBefore(fromDate))
        .filter(bill -> toDate == null
            || !LocalDate.ofInstant(bill.getIssueDate(), ZoneId.systemDefault()).isAfter(toDate))
        .filter(bill -> paymentFilter.isBlank() || bill.getPaymentStatus().equals(paymentFilter))
        .toList();
  }

  private int effectiveAdditionalAmount(AdminBillResponse bill) {
    if (!bill.isEditable()) {
      return bill.getAdditionalFees();
    }
    if (bill.getAdditionalFees() > 100) {
      return bill.getAdditionalFees();
    }
    int baseSubtotal = bill.getRoomCharges() + bill.getServiceCharges();
    return Math.round(baseSubtotal * (bill.getAdditionalFees() / 100f));
  }

  private int effectiveTaxAmount(AdminBillResponse bill, int additionalAmount) {
    if (!bill.isEditable()) {
      return bill.getTaxes();
    }
    if (bill.getTaxes() > 100) {
      return bill.getTaxes();
    }
    int subtotal = bill.getRoomCharges() + bill.getServiceCharges() + additionalAmount;
    return Math.round(subtotal * (bill.getTaxes() / 100f));
  }

  private int effectiveDiscountAmount(AdminBillResponse bill, int additionalAmount) {
    if (!bill.isEditable()) {
      return bill.getDiscounts();
    }
    if (bill.getDiscounts() > 100) {
      return bill.getDiscounts();
    }
    int subtotal = bill.getRoomCharges() + bill.getServiceCharges() + additionalAmount;
    return Math.round(subtotal * (bill.getDiscounts() / 100f));
  }

  private String flattenServiceItems(List<AdminBillServiceItem> serviceItems) {
    if (serviceItems == null || serviceItems.isEmpty()) {
      return "";
    }
    return serviceItems.stream()
        .map(item -> String.format(
            Locale.ROOT,
            "%s @ %s (qty=%d, unit=%d, tax%%=%d, discount%%=%d, desc=%s)",
            safeCsvText(item.getServiceType()),
            item.getServiceDateTime() == null ? "-" : item.getServiceDateTime().toString(),
            item.getQuantity() == null ? 0 : item.getQuantity(),
            item.getUnitPrice() == null ? 0 : item.getUnitPrice(),
            item.getTaxPercent() == null ? 0 : item.getTaxPercent(),
            item.getDiscountPercent() == null ? 0 : item.getDiscountPercent(),
            safeCsvText(item.getDescription())
        ))
        .collect(Collectors.joining(" | "));
  }

  private String safeCsvText(String value) {
    return value == null ? "" : value.replace('\n', ' ').replace('\r', ' ').trim();
  }

  private String csvField(String value) {
    String text = value == null ? "" : value;
    String escaped = text.replace("\"", "\"\"");
    return "\"" + escaped + "\"";
  }

  @Transactional
  public AdminBillResponse markBillPaid(String billId) {
    BillingRecord record = billingRecordRepository.findByBillId(billId).orElse(null);
    if (record != null) {
      record.setPaymentStatus("PAID");
      billingRecordRepository.save(record);
      return toAdminBillResponse(record);
    }
    Booking booking = bookingRepository.findByInvoiceId(billId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Bill not found."));
    booking.setPaymentStatus("PAID");
    bookingRepository.save(booking);
    return toAdminBillResponse(booking);
  }

  @Transactional(readOnly = true)
  public AdminComplaintPageResponse searchComplaints(
      String q,
      String category,
      String priority,
      String status,
      String assignedTo,
      LocalDate fromDate,
      LocalDate toDate,
      String sortBy,
      String sortDir,
      String actorUserId,
      boolean isAdmin,
      int page,
      int size
  ) {
    if (page < 0) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "page must be >= 0.");
    }
    if (size < 1 || size > 100) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "size must be between 1 and 100.");
    }
    if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "fromDate cannot be after toDate.");
    }

    String query = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
    String categoryFilter = category == null ? "" : category.trim().toLowerCase(Locale.ROOT);
    String priorityFilter = priority == null ? "" : priority.trim().toLowerCase(Locale.ROOT);
    String statusFilter = status == null ? "" : status.trim().toLowerCase(Locale.ROOT);
    String assignedFilter = assignedTo == null ? "" : assignedTo.trim().toLowerCase(Locale.ROOT);
    Map<String, String> customerNames = customerNameMap();

    List<Complaint> filtered = complaintRepository.findAll().stream()
        .filter(c -> query.isBlank()
            || c.getComplaintId().toLowerCase(Locale.ROOT).contains(query)
            || c.getUserId().toLowerCase(Locale.ROOT).contains(query)
            || c.getTitle().toLowerCase(Locale.ROOT).contains(query)
            || c.getBookingId().toLowerCase(Locale.ROOT).contains(query)
            || customerNames.getOrDefault(c.getUserId(), "").toLowerCase(Locale.ROOT).contains(query))
        .filter(c -> categoryFilter.isBlank() || c.getCategory().toLowerCase(Locale.ROOT).equals(categoryFilter))
        .filter(c -> priorityFilter.isBlank() || complaintPriorityLabel(c).toLowerCase(Locale.ROOT).equals(priorityFilter))
        .filter(c -> statusFilter.isBlank() || complaintStatusMatches(c, statusFilter))
        .filter(c -> assignedFilter.isBlank()
            || (c.getAssignedStaffMember() != null && c.getAssignedStaffMember().toLowerCase(Locale.ROOT).contains(assignedFilter))
            || (c.getAssignedDepartment() != null && c.getAssignedDepartment().toLowerCase(Locale.ROOT).contains(assignedFilter)))
        .filter(c -> fromDate == null || !LocalDate.ofInstant(c.getCreatedAt(), ZoneId.systemDefault()).isBefore(fromDate))
        .filter(c -> toDate == null || !LocalDate.ofInstant(c.getCreatedAt(), ZoneId.systemDefault()).isAfter(toDate))
        .filter(c -> isAdmin || isComplaintAssignedTo(c, actorUserId))
        .toList();

    Comparator<Complaint> comparator = complaintComparator(sortBy);
    if ("desc".equalsIgnoreCase(sortDir)) {
      comparator = comparator.reversed();
    }
    List<Complaint> sorted = filtered.stream().sorted(comparator).toList();

    long totalItems = sorted.size();
    int totalPages = totalItems == 0 ? 0 : (int) Math.ceil((double) totalItems / size);
    int from = page * size;
    int to = Math.min(from + size, sorted.size());
    List<AdminComplaintResponse> items = from >= to ? List.of() : sorted.subList(from, to).stream()
        .map(c -> toAdminComplaintResponse(c, customerNames.getOrDefault(c.getUserId(), c.getUserId()), actorUserId, isAdmin, false))
        .toList();

    return new AdminComplaintPageResponse(items, page, size, totalItems, totalPages);
  }

  @Transactional(readOnly = true)
  public AdminComplaintResponse complaintDetail(String complaintId) {
    Complaint complaint = complaintRepository.findByComplaintId(complaintId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Complaint not found."));
    return complaintDetail(complaintId, null, true);
  }

  @Transactional(readOnly = true)
  public AdminComplaintResponse complaintDetail(String complaintId, String actorUserId, boolean isAdmin) {
    Complaint complaint = complaintRepository.findByComplaintId(complaintId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Complaint not found."));
    if (!isAdmin && !isComplaintAssignedTo(complaint, actorUserId)) {
      throw new ApiException(HttpStatus.FORBIDDEN, "You are not authorized to view this complaint.");
    }
    String customerName = customerNameMap().getOrDefault(complaint.getUserId(), complaint.getUserId());
    return toAdminComplaintResponse(complaint, customerName, actorUserId, isAdmin, true);
  }

  @Transactional
  public AdminComplaintResponse updateComplaint(
      String complaintId,
      AdminComplaintUpdateRequest request,
      String actorUserId,
      boolean isAdmin
  ) {
    Complaint complaint = complaintRepository.findByComplaintId(complaintId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Complaint not found."));
    if (actorUserId == null || actorUserId.isBlank()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Actor userId is required.");
    }
    if (!isAdmin && !isComplaintAssignedTo(complaint, actorUserId)) {
      throw new ApiException(HttpStatus.FORBIDDEN, "You are not authorized to update this complaint.");
    }

    String assignedStaff = request.getAssignedStaffMember() == null ? null : request.getAssignedStaffMember().trim();
    String assignedDepartment = request.getAssignedDepartment() == null ? null : request.getAssignedDepartment().trim();
    String statusInput = request.getStatus() == null ? null : request.getStatus().trim();
    String supportResponse = request.getSupportResponse();
    String resolutionNotes = request.getResolutionNotes();
    String actionDetails = request.getActionDetails();
    String actionDetailsTrimmed = actionDetails == null ? "" : actionDetails.trim();
    if (assignedStaff != null && !assignedStaff.isBlank()) {
      Staff staff = staffRepository.findByUserId(assignedStaff)
          .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Assigned staff member does not exist."));
      if (assignedDepartment == null || assignedDepartment.isBlank()) {
        assignedDepartment = staff.getDepartment();
      }
    }

    if (!isAdmin) {
      if ((request.getAssignedStaffMember() != null && !request.getAssignedStaffMember().isBlank())
          || (request.getAssignedDepartment() != null && !request.getAssignedDepartment().isBlank())) {
        throw new ApiException(HttpStatus.FORBIDDEN, "Staff cannot reassign complaints.");
      }
      if (request.getResolutionNotes() != null) {
        throw new ApiException(HttpStatus.FORBIDDEN, "Admin only note can be updated by admin.");
      }
    }

    boolean hasAnyChangeRequest =
        request.getAssignedStaffMember() != null
            || request.getAssignedDepartment() != null
            || (statusInput != null && !statusInput.isBlank())
            || supportResponse != null
            || resolutionNotes != null
            || !actionDetailsTrimmed.isBlank();
    if (!hasAnyChangeRequest) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Provide assignment, status, or action update fields.");
    }

    String fromStatusLabel = complaintStatusLabel(complaint);
    Complaint.Status toStatus = complaint.getStatus();
    boolean assignmentChanged = false;
    boolean statusChanged = false;
    boolean supportChanged = false;

    if (request.getAssignedStaffMember() != null) {
      String normalized = assignedStaff == null || assignedStaff.isBlank() ? null : assignedStaff;
      if (!java.util.Objects.equals(normalized, complaint.getAssignedStaffMember())) {
        complaint.setAssignedStaffMember(normalized);
        assignmentChanged = true;
      }
    }
    if (request.getAssignedDepartment() != null) {
      String normalized = assignedDepartment == null || assignedDepartment.isBlank() ? null : assignedDepartment;
      if (!java.util.Objects.equals(normalized, complaint.getAssignedDepartment())) {
        complaint.setAssignedDepartment(normalized);
        assignmentChanged = true;
      }
    }
    if (statusInput != null && !statusInput.isBlank()) {
      String normalizedStatus = normalizeComplaintStatus(statusInput);
      if ("ESCALATED".equals(normalizedStatus)) {
        boolean nextChanged = complaint.getStatus() != Complaint.Status.In_Progress || !complaint.isEscalated();
        complaint.setStatus(Complaint.Status.In_Progress);
        complaint.setEscalated(true);
        toStatus = complaint.getStatus();
        statusChanged = nextChanged;
      } else {
        toStatus = parseComplaintStatus(statusInput);
        boolean nextChanged = toStatus != complaint.getStatus() || complaint.isEscalated();
        complaint.setStatus(toStatus);
        complaint.setEscalated(false);
        statusChanged = nextChanged;
      }
    } else if (assignmentChanged && (complaint.getStatus() == Complaint.Status.Open || complaint.getStatus() == Complaint.Status.Pending)) {
      toStatus = Complaint.Status.In_Progress;
      complaint.setStatus(toStatus);
      statusChanged = true;
    }

    if (supportResponse != null) {
      String next = supportResponse.trim();
      String prev = complaint.getSupportResponse() == null ? "" : complaint.getSupportResponse().trim();
      if (!next.equals(prev)) {
        supportChanged = true;
      }
      complaint.setSupportResponse(next);
    }
    if (resolutionNotes != null && isAdmin) {
      complaint.setResolutionNotes(resolutionNotes.trim());
    }

    complaintRepository.save(complaint);

    String actionType = statusChanged ? "STATUS_UPDATED" : (assignmentChanged ? "ASSIGNED" : (supportChanged ? "CHECKPOINT_UPDATED" : "NOTE"));
    String details = actionDetailsTrimmed;
    if (details.isBlank()) {
      if (statusChanged) {
        details = String.format("Status changed from %s to %s.", fromStatusLabel, complaintStatusLabel(complaint));
      } else if (assignmentChanged) {
        details = "Complaint assignment updated.";
      } else if (supportChanged) {
        details = complaint.getSupportResponse() == null || complaint.getSupportResponse().isBlank()
            ? "Checkpoint update shared with customer."
            : "Checkpoint update: " + complaint.getSupportResponse();
      } else {
        details = "Complaint record updated.";
      }
    }

    saveComplaintAction(complaint, actorUserId, fromStatusLabel, complaintStatusLabel(complaint), actionType, details);
    if (shouldWriteCustomerCheckpoint(supportChanged, actionDetailsTrimmed, actionType)) {
      String checkpointDetails = resolveCheckpointDetails(complaint, supportChanged, actionDetailsTrimmed);
      saveComplaintAction(complaint, actorUserId, fromStatusLabel, complaintStatusLabel(complaint), "CHECKPOINT_UPDATED", checkpointDetails);
    }

    String customerName = customerNameMap().getOrDefault(complaint.getUserId(), complaint.getUserId());
    return toAdminComplaintResponse(complaint, customerName, actorUserId, isAdmin, true);
  }

  @Transactional
  public AdminBillResponse createBill(AdminBillCreateRequest request) {
    Customer customer = customerRepository.findByUserId(request.getCustomerUserId().trim())
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Customer not found."));
    int serviceCharges = calculateServiceCharges(request.getServiceItems());
    int total = calculateBillTotal(
        request.getRoomCharges(),
        serviceCharges,
        request.getAdditionalFees(),
        request.getTaxes(),
        request.getDiscounts()
    );
    if (total != request.getTotalAmountDue()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Total Amount Due does not match itemized calculation.");
    }

    BillingRecord record = new BillingRecord();
    record.setBillId(generateId("BILL"));
    record.setCustomerUserId(customer.getUserId());
    record.setCustomerName(customer.getName());
    record.setBookingId("");
    record.setRoomCharges(request.getRoomCharges());
    record.setServiceCharges(serviceCharges);
    record.setAdditionalFees(request.getAdditionalFees());
    record.setTaxes(request.getTaxes());
    record.setDiscounts(request.getDiscounts());
    record.setTotalAmount(total);
    record.setPaymentStatus("PENDING");
    record.setServiceItemsJson(writeServiceItems(request.getServiceItems()));
    billingRecordRepository.save(record);
    return toAdminBillResponse(record);
  }

  @Transactional
  public AdminBillResponse updateBill(String billId, AdminBillUpdateRequest request) {
    BillingRecord record = billingRecordRepository.findByBillId(billId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Editable bill not found."));
    int serviceCharges = calculateServiceCharges(request.getServiceItems());
    int total = calculateBillTotal(
        request.getRoomCharges(),
        serviceCharges,
        request.getAdditionalFees(),
        request.getTaxes(),
        request.getDiscounts()
    );
    if (total != request.getTotalAmountDue()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Total Amount Due does not match itemized calculation.");
    }

    record.setRoomCharges(request.getRoomCharges());
    record.setServiceCharges(serviceCharges);
    record.setAdditionalFees(request.getAdditionalFees());
    record.setTaxes(request.getTaxes());
    record.setDiscounts(request.getDiscounts());
    record.setTotalAmount(total);
    record.setServiceItemsJson(writeServiceItems(request.getServiceItems()));
    billingRecordRepository.save(record);
    return toAdminBillResponse(record);
  }

  @Transactional
  public AdminBookingResponse createBooking(AdminBookingCreateRequest request) {
    validateBookingDates(request.getCheckInDate(), request.getCheckOutDate());
    validateGuestCounts(request.getAdults(), request.getChildren());

    Room room = selectRoomForCreate(
        request.getRoomCode(),
        request.getRoomType(),
        request.getCheckInDate(),
        request.getCheckOutDate()
    );

    if (request.getAdults() > room.getOccupancyAdults() || request.getChildren() > room.getOccupancyChildren()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Guest count exceeds room occupancy limit.");
    }

    int nights = (int) (request.getCheckOutDate().toEpochDay() - request.getCheckInDate().toEpochDay());
    int basePrice = room.getPricePerNight() * nights;
    int gst = Math.round(basePrice * 0.10f);
    int service = Math.round(basePrice * 0.02f);
    int total = basePrice + gst + service;
    String paymentMethod = request.getPaymentMethod() == null ? "" : request.getPaymentMethod().trim();
    if (!"card".equalsIgnoreCase(paymentMethod)) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "paymentMethod", "Only Card payment method is supported.");
    }

    Booking booking = new Booking();
    booking.setBookingId(generateId("BK"));
    booking.setInvoiceId(generateId("INV"));
    booking.setTransactionId(generateId("TXN"));
    String customerUserId = request.getCustomerUserId() == null ? "" : request.getCustomerUserId().trim();
    booking.setCustomerUserId(customerUserId.isBlank() ? generateId("CUS") : customerUserId);
    booking.setCustomerName(request.getCustomerName().trim());
    booking.setCustomerEmail(request.getCustomerEmail().trim().toLowerCase(Locale.ROOT));
    booking.setCustomerMobile(normalizeMobile(request.getCustomerMobile()));
    booking.setRoomCode(room.getRoomCode());
    booking.setRoomType(room.getRoomType());
    booking.setOccupancyAdults(room.getOccupancyAdults());
    booking.setOccupancyChildren(room.getOccupancyChildren());
    booking.setPricePerNight(room.getPricePerNight());
    booking.setCheckInDate(request.getCheckInDate());
    booking.setOriginalCheckInDate(request.getCheckInDate());
    booking.setCheckOutDate(request.getCheckOutDate());
    booking.setNights(nights);
    booking.setAdults(request.getAdults());
    booking.setChildren(request.getChildren());
    booking.setBasePrice(basePrice);
    booking.setGstAmount(gst);
    booking.setServiceChargeAmount(service);
    booking.setTotalAmount(total);
    booking.setPaymentMethod("Card");
    booking.setPaymentStatus("PENDING");
    booking.setSpecialRequests(request.getSpecialRequests() == null ? "" : request.getSpecialRequests().trim());
    booking.setStatus(Booking.Status.Confirmed);
    bookingRepository.save(booking);
    return toAdminBookingResponse(booking);
  }

  @Transactional
  public AdminBookingResponse updateBooking(String bookingId, AdminBookingUpdateRequest request) {
    Booking booking = bookingRepository.findByBookingId(bookingId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Reservation not found."));

    if (booking.getStatus() == Booking.Status.Cancelled) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Cancelled reservations cannot be edited.");
    }
    if (!booking.getCheckInDate().isAfter(LocalDate.now())) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Checked-in or completed reservations cannot be edited.");
    }

    validateBookingDates(request.getCheckInDate(), request.getCheckOutDate());
    validateGuestCounts(request.getAdults(), request.getChildren());

    Room room = roomRepository.findByRoomCode(request.getRoomCode().trim())
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Assigned room not found."));
    if (!room.isActive() || "UNDER_MAINTENANCE".equalsIgnoreCase(room.getRoomStatus())) {
      throw new ApiException(HttpStatus.CONFLICT, "Assigned room is not available.");
    }
    if (request.getAdults() > room.getOccupancyAdults() || request.getChildren() > room.getOccupancyChildren()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Guest count exceeds room occupancy limit.");
    }

    if (!isRoomFree(room.getRoomCode(), request.getCheckInDate(), request.getCheckOutDate(), bookingId)) {
      throw new ApiException(HttpStatus.CONFLICT, "Selected room is not available for the full reservation period.");
    }

    int nights = (int) (request.getCheckOutDate().toEpochDay() - request.getCheckInDate().toEpochDay());
    int basePrice = room.getPricePerNight() * nights;
    int gst = Math.round(basePrice * 0.10f);
    int service = Math.round(basePrice * 0.02f);
    int total = basePrice + gst + service;

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
    booking.setSpecialRequests(request.getSpecialRequests() == null ? "" : request.getSpecialRequests().trim());
    bookingRepository.save(booking);
    return toAdminBookingResponse(booking);
  }

  @Transactional
  public AdminBookingResponse cancelBooking(String bookingId) {
    Booking booking = bookingRepository.findByBookingId(bookingId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Reservation not found."));

    if (booking.getStatus() == Booking.Status.Cancelled) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Reservation is already cancelled.");
    }
    if (!booking.getCheckInDate().isAfter(LocalDate.now())) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Checked-in or completed reservations cannot be canceled.");
    }

    booking.setStatus(Booking.Status.Cancelled);
    booking.setCancelledAt(Instant.now());
    booking.setCancellationRefundAmount(booking.getTotalAmount());
    booking.setCancellationNote("Cancelled by admin.");
    bookingRepository.save(booking);
    return toAdminBookingResponse(booking);
  }

  private void validateBookingDates(LocalDate checkInDate, LocalDate checkOutDate) {
    if (checkInDate == null || checkOutDate == null) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Check-in and check-out dates are required.");
    }
    if (checkInDate.isBefore(LocalDate.now())) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Check-in date cannot be in the past.");
    }
    if (!checkOutDate.isAfter(checkInDate)) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Check-out date must be after check-in date.");
    }
  }

  private void validateGuestCounts(Integer adults, Integer children) {
    if (adults == null || adults < 1 || adults > 10) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Adults must be between 1 and 10.");
    }
    if (children == null || children < 0 || children > 5) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Children must be between 0 and 5.");
    }
    if (adults + children > 10) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Adults + children cannot exceed 10.");
    }
  }

  private Room selectRoomForCreate(String requestedRoomCode, String roomType, LocalDate checkInDate, LocalDate checkOutDate) {
    if (roomType == null || roomType.isBlank()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Room type is required.");
    }
    String normalizedType = normalizeRoomType(roomType);
    if (requestedRoomCode != null && !requestedRoomCode.isBlank()) {
      Room room = roomRepository.findByRoomCode(requestedRoomCode.trim())
          .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Requested room was not found."));
      if (!normalizedType.equalsIgnoreCase(room.getRoomType())) {
        throw new ApiException(HttpStatus.BAD_REQUEST, "Requested room does not match selected room type.");
      }
      if (!room.isActive() || "UNDER_MAINTENANCE".equalsIgnoreCase(room.getRoomStatus())) {
        throw new ApiException(HttpStatus.CONFLICT, "Requested room is not available.");
      }
      if (!isRoomFree(room.getRoomCode(), checkInDate, checkOutDate, null)) {
        throw new ApiException(HttpStatus.CONFLICT, "Requested room is not available for selected dates.");
      }
      return room;
    }

    List<Room> candidates = roomRepository.findByActiveTrueAndRoomType(normalizedType).stream()
        .filter(room -> !"UNDER_MAINTENANCE".equalsIgnoreCase(room.getRoomStatus()))
        .sorted(Comparator.comparing(Room::getRoomCode))
        .toList();
    for (Room candidate : candidates) {
      if (isRoomFree(candidate.getRoomCode(), checkInDate, checkOutDate, null)) {
        return candidate;
      }
    }
    throw new ApiException(HttpStatus.CONFLICT, "No rooms available for the selected dates. Please try different dates.");
  }

  private boolean isRoomFree(String roomCode, LocalDate checkInDate, LocalDate checkOutDate, String excludeBookingId) {
    if (excludeBookingId == null || excludeBookingId.isBlank()) {
      return !bookingRepository.existsByRoomCodeAndStatusAndCheckInDateLessThanAndCheckOutDateGreaterThan(
          roomCode,
          Booking.Status.Confirmed,
          checkOutDate,
          checkInDate
      );
    }
    List<Booking> overlaps = bookingRepository.findByRoomCodeAndStatusAndCheckInDateLessThanAndCheckOutDateGreaterThan(
        roomCode,
        Booking.Status.Confirmed,
        checkOutDate,
        checkInDate
    );
    return overlaps.stream().noneMatch(booking -> !booking.getBookingId().equals(excludeBookingId));
  }

  private Comparator<Booking> bookingComparator(String sortBy) {
    String key = sortBy == null ? "" : sortBy.trim().toLowerCase(Locale.ROOT);
    return switch (key) {
      case "reservationid", "bookingid" -> Comparator.comparing(Booking::getBookingId, String.CASE_INSENSITIVE_ORDER);
      case "customername" -> Comparator.comparing(Booking::getCustomerName, String.CASE_INSENSITIVE_ORDER);
      case "roomnumber", "roomcode" -> Comparator.comparing(Booking::getRoomCode, String.CASE_INSENSITIVE_ORDER);
      case "roomtype" -> Comparator.comparing(Booking::getRoomType, String.CASE_INSENSITIVE_ORDER);
      case "checkindate" -> Comparator.comparing(Booking::getCheckInDate);
      case "checkoutdate" -> Comparator.comparing(Booking::getCheckOutDate);
      case "totalamount" -> Comparator.comparingInt(Booking::getTotalAmount);
      case "status" -> Comparator.comparing(this::computeReservationStatus, String.CASE_INSENSITIVE_ORDER);
      default -> Comparator.comparing(Booking::getCreatedAt).reversed();
    };
  }

  private String computeReservationStatus(Booking booking) {
    if (booking.getStatus() == Booking.Status.Cancelled) {
      return "Cancelled";
    }
    LocalDate today = LocalDate.now();
    if (!today.isBefore(booking.getCheckOutDate())) {
      return "Checked-out";
    }
    if (!today.isBefore(booking.getCheckInDate())) {
      return "Checked-in";
    }
    return "Confirmed";
  }

  private AdminBookingResponse toAdminBookingResponse(Booking booking) {
    return new AdminBookingResponse(
        booking.getBookingId(),
        booking.getCustomerName(),
        booking.getCustomerEmail(),
        booking.getCustomerMobile(),
        booking.getCustomerUserId(),
        booking.getRoomCode(),
        booking.getRoomType(),
        booking.getCheckInDate(),
        booking.getCheckOutDate(),
        booking.getAdults(),
        booking.getChildren(),
        computeReservationStatus(booking),
        booking.getTotalAmount(),
        booking.getPaymentMethod(),
        booking.getSpecialRequests() == null ? "" : booking.getSpecialRequests(),
        booking.getCreatedAt()
    );
  }

  private AdminBillResponse toAdminBillResponse(Booking booking) {
    return new AdminBillResponse(
        booking.getInvoiceId(),
        booking.getBookingId(),
        booking.getCustomerUserId(),
        booking.getCustomerName(),
        booking.getCreatedAt(),
        booking.getBasePrice(),
        booking.getServiceChargeAmount(),
        0,
        booking.getGstAmount(),
        0,
        booking.getTotalAmount(),
        paymentStatusOf(booking),
        false,
        List.of()
    );
  }

  private AdminBillResponse toAdminBillResponse(BillingRecord record) {
    return new AdminBillResponse(
        record.getBillId(),
        record.getBookingId() == null ? "" : record.getBookingId(),
        record.getCustomerUserId(),
        record.getCustomerName(),
        record.getIssueDate(),
        record.getRoomCharges(),
        record.getServiceCharges(),
        record.getAdditionalFees(),
        record.getTaxes(),
        record.getDiscounts(),
        record.getTotalAmount(),
        record.getPaymentStatus(),
        true,
        readServiceItems(record.getServiceItemsJson())
    );
  }

  private String paymentStatusOf(Booking booking) {
    String status = booking.getPaymentStatus() == null ? "" : booking.getPaymentStatus().trim().toUpperCase(Locale.ROOT);
    if ("PAID".equals(status) || "PENDING".equals(status)) {
      return status;
    }
    return booking.getStatus() == Booking.Status.Confirmed ? "PAID" : "PENDING";
  }

  private Comparator<AdminBillResponse> billComparator(String sortBy) {
    String key = sortBy == null ? "" : sortBy.trim().toLowerCase(Locale.ROOT);
    return switch (key) {
      case "billid", "invoiceid" -> Comparator.comparing(AdminBillResponse::getBillId, String.CASE_INSENSITIVE_ORDER);
      case "customername" -> Comparator.comparing(AdminBillResponse::getCustomerName, String.CASE_INSENSITIVE_ORDER);
      case "customerid", "customeruserid" -> Comparator.comparing(AdminBillResponse::getCustomerUserId, String.CASE_INSENSITIVE_ORDER);
      case "date", "issuedate" -> Comparator.comparing(AdminBillResponse::getIssueDate);
      case "totalamount", "amount" -> Comparator.comparingInt(AdminBillResponse::getTotalAmount);
      case "paymentstatus" -> Comparator.comparing(AdminBillResponse::getPaymentStatus, String.CASE_INSENSITIVE_ORDER);
      default -> Comparator.comparing(AdminBillResponse::getIssueDate).reversed();
    };
  }

  private Map<String, String> customerNameMap() {
    return customerRepository.findAll().stream()
        .collect(Collectors.toMap(Customer::getUserId, Customer::getName, (a, b) -> a));
  }

  private AdminComplaintResponse toAdminComplaintResponse(
      Complaint complaint,
      String customerName,
      String actorUserId,
      boolean isAdmin,
      boolean includeActions
  ) {
    List<AdminComplaintActionResponse> actions = includeActions
        ? complaintActionLogRepository.findByComplaintIdOrderByActionAtDesc(complaint.getComplaintId()).stream()
            .map(action -> new AdminComplaintActionResponse(
                action.getActionAt(),
                action.getActionType(),
                action.getActorUserId(),
                action.getFromStatus(),
                action.getToStatus(),
                action.getAssignedStaffMember(),
                action.getAssignedDepartment(),
                action.getActionDetails() == null ? "" : action.getActionDetails()
            ))
            .toList()
        : List.of();

    return new AdminComplaintResponse(
        complaint.getComplaintId(),
        complaint.getUserId(),
        customerName,
        complaint.getBookingId(),
        complaint.getCreatedAt(),
        complaint.getCategory(),
        complaint.getTitle(),
        complaint.getDescription(),
        complaint.getContactPreference(),
        complaintPriorityLabel(complaint),
        complaintStatusLabel(complaint),
        isNewAssignmentForStaff(complaint, actorUserId, isAdmin),
        complaint.getAssignedStaffMember() == null ? "" : complaint.getAssignedStaffMember(),
        complaint.getAssignedDepartment() == null ? "" : complaint.getAssignedDepartment(),
        complaint.getExpectedResolutionDate() == null ? "" : complaint.getExpectedResolutionDate().toString(),
        complaint.getSupportResponse() == null ? "" : complaint.getSupportResponse(),
        complaint.getResolutionNotes() == null ? "" : complaint.getResolutionNotes(),
        actions
    );
  }

  private boolean complaintStatusMatches(Complaint complaint, String filter) {
    if (complaint == null || filter == null || filter.isBlank()) {
      return false;
    }
    String normalized = filter.replace('-', ' ').replace('_', ' ').trim().toLowerCase(Locale.ROOT);
    if (normalized.equals("escalated")) {
      return complaint.isEscalated() && complaint.getStatus() == Complaint.Status.In_Progress;
    }
    Complaint.Status status = complaint.getStatus();
    if (normalized.equals("in progress")) {
      return status == Complaint.Status.In_Progress && !complaint.isEscalated();
    }
    if (normalized.equals("open")) {
      return status == Complaint.Status.Open || status == Complaint.Status.Pending;
    }
    if (normalized.equals("pending")) {
      return status == Complaint.Status.Pending;
    }
    if (normalized.equals("resolved")) {
      return status == Complaint.Status.Resolved;
    }
    if (normalized.equals("closed")) {
      return status == Complaint.Status.Closed;
    }
    return false;
  }

  private Complaint.Status parseComplaintStatus(String status) {
    String normalized = normalizeComplaintStatus(status);
    return switch (normalized) {
      case "OPEN" -> Complaint.Status.Open;
      case "PENDING" -> Complaint.Status.Pending;
      case "IN_PROGRESS" -> Complaint.Status.In_Progress;
      case "RESOLVED" -> Complaint.Status.Resolved;
      case "CLOSED" -> Complaint.Status.Closed;
      default -> throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid complaint status.");
    };
  }

  private String normalizeComplaintStatus(String status) {
    return status.trim().replace('-', '_').replace(' ', '_').toUpperCase(Locale.ROOT);
  }

  private String complaintStatusLabel(Complaint complaint) {
    if (complaint != null && complaint.isEscalated() && complaint.getStatus() == Complaint.Status.In_Progress) {
      return "Escalated";
    }
    return complaintStatusLabel(complaint == null ? Complaint.Status.Open : complaint.getStatus());
  }

  private String complaintStatusLabel(Complaint.Status status) {
    return switch (status) {
      case Pending, Open -> "Open";
      case In_Progress -> "In Progress";
      case Resolved -> "Resolved";
      case Closed -> "Closed";
    };
  }

  private String complaintPriorityLabel(Complaint complaint) {
    String priority = complaint == null || complaint.getPriorityLevel() == null
        ? ""
        : complaint.getPriorityLevel().trim();
    if (priority.isBlank()) {
      return "Medium";
    }
    String normalized = priority.toLowerCase(Locale.ROOT);
    if ("high".equals(normalized)) {
      return "High";
    }
    if ("low".equals(normalized)) {
      return "Low";
    }
    return "Medium";
  }

  private boolean isNewAssignmentForStaff(Complaint complaint, String actorUserId, boolean isAdmin) {
    if (isAdmin || actorUserId == null || actorUserId.isBlank() || complaint == null) {
      return false;
    }
    if (!isComplaintAssignedTo(complaint, actorUserId)) {
      return false;
    }
    return complaintActionLogRepository.findFirstByComplaintIdOrderByActionAtDesc(complaint.getComplaintId())
        .map(action -> "ASSIGNED".equalsIgnoreCase(action.getActionType())
            && action.getAssignedStaffMember() != null
            && action.getAssignedStaffMember().equalsIgnoreCase(actorUserId.trim())
            && action.getActionAt() != null
            && action.getActionAt().isAfter(Instant.now().minus(3, ChronoUnit.DAYS)))
        .orElse(false);
  }

  private Comparator<Complaint> complaintComparator(String sortBy) {
    String key = sortBy == null ? "" : sortBy.trim().toLowerCase(Locale.ROOT);
    return switch (key) {
      case "complaintid" -> Comparator.comparing(Complaint::getComplaintId, String.CASE_INSENSITIVE_ORDER);
      case "customerid" -> Comparator.comparing(Complaint::getUserId, String.CASE_INSENSITIVE_ORDER);
      case "category" -> Comparator.comparing(Complaint::getCategory, String.CASE_INSENSITIVE_ORDER);
      case "status" -> Comparator.comparing(this::complaintStatusLabel, String.CASE_INSENSITIVE_ORDER);
      case "priority", "prioritylevel" -> Comparator.comparing(this::complaintPriorityLabel, String.CASE_INSENSITIVE_ORDER);
      case "assignedstaff", "assignedstaffmember" -> Comparator.comparing(
          c -> c.getAssignedStaffMember() == null ? "" : c.getAssignedStaffMember(),
          String.CASE_INSENSITIVE_ORDER
      );
      default -> Comparator.comparing(Complaint::getCreatedAt).reversed();
    };
  }

  private boolean isComplaintAssignedTo(Complaint complaint, String staffUserId) {
    if (staffUserId == null || staffUserId.isBlank()) {
      return false;
    }
    String assigned = complaint.getAssignedStaffMember();
    return assigned != null && assigned.equalsIgnoreCase(staffUserId.trim());
  }

  private void saveComplaintAction(
      Complaint complaint,
      String actorUserId,
      String fromStatus,
      String toStatus,
      String actionType,
      String details
  ) {
    ComplaintActionLog log = new ComplaintActionLog();
    log.setComplaintId(complaint.getComplaintId());
    log.setActionType(actionType);
    log.setActorUserId(actorUserId);
    log.setFromStatus(fromStatus == null ? "" : fromStatus);
    log.setToStatus(toStatus == null ? "" : toStatus);
    log.setAssignedStaffMember(complaint.getAssignedStaffMember());
    log.setAssignedDepartment(complaint.getAssignedDepartment());
    log.setActionDetails(details == null ? "" : details);
    complaintActionLogRepository.save(log);
  }

  private boolean shouldWriteCustomerCheckpoint(
      boolean supportChanged,
      String actionDetailsTrimmed,
      String actionType
  ) {
    if ("CHECKPOINT_UPDATED".equals(actionType)) {
      return false;
    }
    if (supportChanged) {
      return true;
    }
    return startsWithCheckpointPrefix(actionDetailsTrimmed);
  }

  private String resolveCheckpointDetails(
      Complaint complaint,
      boolean supportChanged,
      String actionDetailsTrimmed
  ) {
    if (supportChanged) {
      return complaint.getSupportResponse() == null || complaint.getSupportResponse().isBlank()
          ? "Checkpoint update shared with customer."
          : "Checkpoint update: " + complaint.getSupportResponse().trim();
    }
    return "Checkpoint update: " + actionDetailsTrimmed;
  }

  private boolean startsWithCheckpointPrefix(String raw) {
    if (raw == null || raw.isBlank()) {
      return false;
    }
    return raw.trim().toLowerCase(Locale.ROOT).startsWith("checkpoint update:");
  }

  private int calculateServiceCharges(List<AdminBillServiceItem> serviceItems) {
    List<AdminBillServiceItem> items = serviceItems == null ? List.of() : serviceItems;
    int total = 0;
    for (AdminBillServiceItem item : items) {
      if (item == null) {
        continue;
      }
      String type = item.getServiceType() == null ? "" : item.getServiceType().trim();
      if (type.isBlank()) {
        throw new ApiException(HttpStatus.BAD_REQUEST, "Service type is required for each service item.");
      }
      if (item.getServiceDateTime() == null) {
        throw new ApiException(HttpStatus.BAD_REQUEST, "Service date/time is required for each service item.");
      }
      int qty = item.getQuantity() == null ? 0 : item.getQuantity();
      int unitPrice = item.getUnitPrice() == null ? 0 : item.getUnitPrice();
      int taxPercent = item.getTaxPercent() == null ? 0 : item.getTaxPercent();
      int discountPercent = item.getDiscountPercent() == null ? 0 : item.getDiscountPercent();
      if (qty < 1 || unitPrice < 0 || taxPercent < 0 || taxPercent > 100 || discountPercent < 0 || discountPercent > 100) {
        throw new ApiException(HttpStatus.BAD_REQUEST, "Service item quantity/price/tax%/discount% values are invalid.");
      }
      int lineBase = qty * unitPrice;
      int lineTax = Math.round(lineBase * (taxPercent / 100f));
      int lineDiscount = Math.round(lineBase * (discountPercent / 100f));
      total += lineBase + lineTax - lineDiscount;
    }
    return total;
  }

  private int calculateBillTotal(Integer roomCharges, int serviceCharges, Integer additionalFees, Integer taxes, Integer discounts) {
    int room = roomCharges == null ? 0 : roomCharges;
    int addnPercent = additionalFees == null ? 0 : additionalFees;
    int taxPercent = taxes == null ? 0 : taxes;
    int discountPercent = discounts == null ? 0 : discounts;
    if (room < 0 || addnPercent < 0 || addnPercent > 100 || taxPercent < 0 || taxPercent > 100 || discountPercent < 0 || discountPercent > 100) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Bill amounts/percentages are invalid.");
    }
    int baseSubtotal = room + serviceCharges;
    int additionalAmount = Math.round(baseSubtotal * (addnPercent / 100f));
    int subtotal = baseSubtotal + additionalAmount;
    int taxAmount = Math.round(subtotal * (taxPercent / 100f));
    int discountAmount = Math.round(subtotal * (discountPercent / 100f));
    return subtotal + taxAmount - discountAmount;
  }

  private String writeServiceItems(List<AdminBillServiceItem> items) {
    List<AdminBillServiceItem> safe = items == null ? List.of() : items;
    try {
      return objectMapper.writeValueAsString(safe);
    } catch (Exception ex) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Unable to process service items.");
    }
  }

  private List<AdminBillServiceItem> readServiceItems(String raw) {
    if (raw == null || raw.isBlank()) {
      return List.of();
    }
    try {
      return objectMapper.readValue(raw, new TypeReference<List<AdminBillServiceItem>>() {});
    } catch (Exception ex) {
      return List.of();
    }
  }

  private String generateId(String prefix) {
    return prefix + "-" + (100000 + RANDOM.nextInt(900000));
  }

  @Transactional(readOnly = true)
  public AdminUserPageResponse searchUsers(
      String q,
      String role,
      String status,
      String sortBy,
      String sortDir,
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
    String roleFilter = role == null ? "" : role.trim().toUpperCase(Locale.ROOT);
    String statusFilter = status == null ? "" : status.trim().toUpperCase(Locale.ROOT);

    List<AdminUserResponse> filtered = allUsers().stream()
        .filter(u -> query.isBlank()
            || u.getUsername().toLowerCase(Locale.ROOT).contains(query)
            || u.getEmail().toLowerCase(Locale.ROOT).contains(query)
            || u.getUserId().toLowerCase(Locale.ROOT).contains(query)
            || u.getName().toLowerCase(Locale.ROOT).contains(query))
        .filter(u -> roleFilter.isBlank() || u.getRole().equals(roleFilter))
        .filter(u -> statusFilter.isBlank() || u.getStatus().equals(statusFilter))
        .toList();

    Comparator<AdminUserResponse> comparator = userComparator(sortBy);
    if ("desc".equalsIgnoreCase(sortDir)) {
      comparator = comparator.reversed();
    }
    List<AdminUserResponse> sorted = filtered.stream().sorted(comparator).toList();

    long totalItems = sorted.size();
    int totalPages = totalItems == 0 ? 0 : (int) Math.ceil((double) totalItems / size);
    int from = page * size;
    int to = Math.min(from + size, sorted.size());
    List<AdminUserResponse> items = from >= to ? List.of() : sorted.subList(from, to);
    return new AdminUserPageResponse(items, page, size, totalItems, totalPages);
  }

  @Transactional
  public AdminUserCreateResponse createUser(AdminUserCreateRequest request) {
    String username = normalizeUsername(request.getUsername());
    String email = normalizeEmail(request.getEmail());
    String mobile = normalizeMobile(request.getMobile());
    String role = normalizeCreatableRole(request.getRole());
    String name = request.getName() == null || request.getName().isBlank() ? username : request.getName().trim();
    String department = request.getDepartment() == null ? "" : request.getDepartment().trim();

    if (customerRepository.existsByUsername(username) || staffRepository.existsByUsername(username)) {
      throw new ApiException(HttpStatus.CONFLICT, "Username already exists.");
    }
    if (customerRepository.existsByEmail(email) || staffRepository.existsByEmail(email)) {
      throw new ApiException(HttpStatus.CONFLICT, "Email already exists.");
    }
    if (customerRepository.existsByMobile(mobile) || staffRepository.existsByMobile(mobile)) {
      throw new ApiException(HttpStatus.CONFLICT, "Mobile already exists.");
    }

    String tempPassword = generateTemporaryPassword();
    AdminUserResponse created;
    if ("STAFF".equals(role)) {
      if (department.isBlank()) {
        throw new ApiException(HttpStatus.BAD_REQUEST, "Department is required for STAFF.");
      }
      Staff staff = new Staff();
      staff.setUserId(generateStaffId());
      staff.setName(name);
      staff.setUsername(username);
      staff.setEmail(email);
      staff.setMobile(mobile);
      staff.setDepartment(department);
      staff.setAddress("Office address");
      staff.setPasswordHash(passwordEncoder.encode(tempPassword));
      staff.setFailedAttempts(0);
      staff.setLocked(false);
      staff.setPasswordChangeRequired(true);
      staffRepository.save(staff);
      created = toAdminUserResponse(staff);
    } else {
      Customer customer = new Customer();
      customer.setUserId(generateId("CUST"));
      customer.setName(name);
      customer.setUsername(username);
      customer.setEmail(email);
      customer.setMobile(mobile);
      customer.setAddress("Address not set");
      customer.setPasswordHash(passwordEncoder.encode(tempPassword));
      customer.setFailedAttempts(0);
      customer.setLocked(false);
      customer.setAdmin(false);
      customer.setPasswordChangeRequired(true);
      customerRepository.save(customer);
      created = toAdminUserResponse(customer);
    }

    return new AdminUserCreateResponse(
        created,
        tempPassword,
        "User created successfully. Share temporary password and instruct user to change it on first login."
    );
  }

  @Transactional
  public AdminUserResponse updateUser(String userId, AdminUserUpdateRequest request) {
    String role = normalizeRole(request.getRole());
    String email = normalizeEmail(request.getEmail());
    String mobile = normalizeMobile(request.getMobile());
    String status = normalizeStatus(request.getStatus());
    String department = request.getDepartment() == null ? "" : request.getDepartment().trim();

    Customer customer = customerRepository.findByUserId(userId).orElse(null);
    if (customer != null) {
      String currentRole = customer.isAdmin() ? "ADMIN" : "CUSTOMER";
      if (!currentRole.equals(role)) {
        throw new ApiException(HttpStatus.BAD_REQUEST, "Role is immutable. Create a new user for a different role.");
      }
      if (customer.isAdmin() && !customer.isLocked() && "INACTIVE".equals(status)) {
        throw new ApiException(HttpStatus.BAD_REQUEST, "Admin account cannot be deactivated.");
      }
      if (customerRepository.existsByEmailAndUserIdNot(email, userId) || staffRepository.existsByEmail(email)) {
        throw new ApiException(HttpStatus.CONFLICT, "Email already exists.");
      }
      if (customerRepository.existsByMobileAndUserIdNot(mobile, userId) || staffRepository.existsByMobile(mobile)) {
        throw new ApiException(HttpStatus.CONFLICT, "Mobile already exists.");
      }

      customer.setEmail(email);
      customer.setMobile(mobile);
      customer.setLocked("INACTIVE".equals(status));
      if ("ACTIVE".equals(status)) {
        customer.setFailedAttempts(0);
      }
      customerRepository.save(customer);
      return toAdminUserResponse(customer);
    }

    Staff staff = staffRepository.findByUserId(userId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found."));
    if (!"STAFF".equals(role)) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Role is immutable. Create a new user for a different role.");
    }
    if (department.isBlank()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Department is required for STAFF.");
    }
    if (staffRepository.existsByEmailAndUserIdNot(email, userId) || customerRepository.existsByEmail(email)) {
      throw new ApiException(HttpStatus.CONFLICT, "Email already exists.");
    }
    if (staffRepository.existsByMobileAndUserIdNot(mobile, userId) || customerRepository.existsByMobile(mobile)) {
      throw new ApiException(HttpStatus.CONFLICT, "Mobile already exists.");
    }
    staff.setEmail(email);
    staff.setMobile(mobile);
    staff.setDepartment(department);
    staff.setLocked("INACTIVE".equals(status));
    if ("ACTIVE".equals(status)) {
      staff.setFailedAttempts(0);
    }
    staffRepository.save(staff);
    return toAdminUserResponse(staff);
  }

  @Transactional
  public AdminUserResponse updateUserStatus(String userId, String status) {
    String normalized = normalizeStatus(status);
    Customer customer = customerRepository.findByUserId(userId).orElse(null);
    if (customer != null) {
      if (customer.isAdmin() && !customer.isLocked() && "INACTIVE".equals(normalized)) {
        throw new ApiException(HttpStatus.BAD_REQUEST, "Admin account cannot be deactivated.");
      }
      customer.setLocked("INACTIVE".equals(normalized));
      if ("ACTIVE".equals(normalized)) {
        customer.setFailedAttempts(0);
      }
      customerRepository.save(customer);
      return toAdminUserResponse(customer);
    }
    Staff staff = staffRepository.findByUserId(userId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found."));
    staff.setLocked("INACTIVE".equals(normalized));
    if ("ACTIVE".equals(normalized)) {
      staff.setFailedAttempts(0);
    }
    staffRepository.save(staff);
    return toAdminUserResponse(staff);
  }

  @Transactional
  public AdminPasswordResetResponse resetUserPassword(String userId) {
    String tempPassword = generateTemporaryPassword();
    Customer customer = customerRepository.findByUserId(userId).orElse(null);
    if (customer != null) {
      if (customer.isAdmin()) {
        throw new ApiException(HttpStatus.BAD_REQUEST, "Admin password reset is not allowed from user management.");
      }
      customer.setPasswordHash(passwordEncoder.encode(tempPassword));
      customer.setPasswordChangeRequired(true);
      customer.setFailedAttempts(0);
      customerRepository.save(customer);
      return new AdminPasswordResetResponse(
          customer.getUserId(),
          customer.getUsername(),
          tempPassword,
          "Password reset successful. User must change password on first login."
      );
    }
    Staff staff = staffRepository.findByUserId(userId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found."));
    staff.setPasswordHash(passwordEncoder.encode(tempPassword));
    staff.setPasswordChangeRequired(true);
    staff.setFailedAttempts(0);
    staffRepository.save(staff);
    return new AdminPasswordResetResponse(
        staff.getUserId(),
        staff.getUsername(),
        tempPassword,
        "Password reset successful. User must change password on first login."
    );
  }

  @Transactional(readOnly = true)
  public AdminRoomOccupancyResponse roomOccupancy30Days(String roomCode) {
    Room room = roomRepository.findByRoomCode(roomCode)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Room not found."));

    LocalDate start = LocalDate.now();
    LocalDate end = start.plusDays(30);

    List<Booking> overlaps = bookingRepository.findByRoomCodeAndStatusAndCheckInDateLessThanAndCheckOutDateGreaterThan(
        room.getRoomCode(),
        Booking.Status.Confirmed,
        end,
        start
    );

    List<AdminRoomOccupancyPoint> points = start.datesUntil(end)
        .map(day -> {
          boolean occupied = overlaps.stream()
              .anyMatch(booking -> !day.isBefore(booking.getCheckInDate()) && day.isBefore(booking.getCheckOutDate()));
          return new AdminRoomOccupancyPoint(day.format(DAY_FORMAT), occupied);
        })
        .toList();

    return new AdminRoomOccupancyResponse(room.getRoomCode(), points);
  }

  @Transactional(readOnly = true)
  public AdminRoomOccupancyGridResponse roomOccupancyGrid(String roomType, int page, int pageSize) {
    if (roomType == null || roomType.isBlank()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "roomType is required.");
    }
    if (page < 0) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "page must be >= 0.");
    }
    if (pageSize < 1 || pageSize > 20) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "pageSize must be between 1 and 20.");
    }

    List<Room> rooms = roomRepository.findByActiveTrueAndRoomType(roomType.trim()).stream()
        .sorted(Comparator.comparing(Room::getRoomCode))
        .toList();

    int totalRooms = rooms.size();
    int totalPages = totalRooms == 0 ? 0 : (int) Math.ceil((double) totalRooms / pageSize);
    if (totalPages > 0 && page >= totalPages) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "page out of range.");
    }

    int from = page * pageSize;
    int to = Math.min(from + pageSize, totalRooms);
    List<Room> pageRooms = from >= to ? List.of() : rooms.subList(from, to);

    LocalDate start = LocalDate.now();
    LocalDate end = start.plusDays(30);
    List<LocalDate> dayRange = start.datesUntil(end).toList();
    List<String> dateLabels = dayRange.stream().map(day -> day.format(DAY_FORMAT)).toList();

    List<AdminRoomOccupancyGridResponse.Row> rows = pageRooms.stream().map(room -> {
      List<Booking> overlaps = bookingRepository.findByRoomCodeAndStatusAndCheckInDateLessThanAndCheckOutDateGreaterThan(
          room.getRoomCode(),
          Booking.Status.Confirmed,
          end,
          start
      );
      List<Boolean> occupancy = dayRange.stream()
          .map(day -> overlaps.stream()
              .anyMatch(booking -> !day.isBefore(booking.getCheckInDate()) && day.isBefore(booking.getCheckOutDate())))
          .toList();
      return new AdminRoomOccupancyGridResponse.Row(room.getRoomCode(), occupancy);
    }).toList();

    return new AdminRoomOccupancyGridResponse(roomType.trim(), page, pageSize, totalRooms, totalPages, dateLabels, rows);
  }

  private String normalizeRoomStatus(String value) {
    String status = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    return switch (status) {
      case "AVAILABLE", "OCCUPIED", "UNDER_MAINTENANCE", "DEPRECATED" -> status;
      default -> throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid room status.");
    };
  }

  private String availabilityForDate(Room room, LocalDate date) {
    if (!room.isActive()
        || "UNDER_MAINTENANCE".equalsIgnoreCase(room.getRoomStatus())
        || "DEPRECATED".equalsIgnoreCase(room.getRoomStatus())) {
      return "NOT_AVAILABLE";
    }
    boolean overlap = !bookingRepository.findByRoomCodeAndStatusAndCheckInDateLessThanAndCheckOutDateGreaterThan(
        room.getRoomCode(),
        Booking.Status.Confirmed,
        date.plusDays(1),
        date
    ).isEmpty();
    return overlap ? "NOT_AVAILABLE" : "AVAILABLE";
  }

  private Comparator<RoomWithAvailability> roomComparator(String sortBy) {
    String key = sortBy == null ? "" : sortBy.trim().toLowerCase(Locale.ROOT);
    return switch (key) {
      case "roomtype" -> Comparator.comparing(item -> item.room.getRoomType(), String.CASE_INSENSITIVE_ORDER);
      case "price" -> Comparator.comparingInt(item -> item.room.getPricePerNight());
      case "availability" -> Comparator.comparing(item -> item.availabilityStatus);
      case "maxoccupancy" -> Comparator.comparingInt(item -> item.room.getOccupancyAdults() + item.room.getOccupancyChildren());
      case "amenities" -> Comparator.comparing(item -> item.room.getAmenitiesCsv(), String.CASE_INSENSITIVE_ORDER);
      default -> Comparator.comparing(item -> item.room.getRoomCode(), String.CASE_INSENSITIVE_ORDER);
    };
  }

  private AdminRoomResponse toAdminRoomResponse(Room room, String availabilityStatus) {
    String roomType = room.getRoomType() == null || room.getRoomType().isBlank() ? "Standard" : room.getRoomType();
    String bedType = room.getBedType() == null || room.getBedType().isBlank() ? "Queen" : room.getBedType();
    String roomStatus = room.getRoomStatus() == null || room.getRoomStatus().isBlank() ? "AVAILABLE" : room.getRoomStatus();
    String amenities = room.getAmenitiesCsv() == null ? "" : room.getAmenitiesCsv();
    return new AdminRoomResponse(
        room.getRoomCode(),
        roomType,
        bedType,
        room.getPricePerNight(),
        room.getOccupancyAdults(),
        room.getOccupancyChildren(),
        room.getOccupancyAdults() + room.getOccupancyChildren(),
        amenities,
        availabilityStatus,
        roomStatus,
        room.getDescription() == null ? "" : room.getDescription(),
        room.isActive()
    );
  }

  private AdminUserResponse toAdminUserResponse(Customer customer) {
    return new AdminUserResponse(
        customer.getUserId(),
        customer.getName(),
        customer.getUsername(),
        customer.getEmail(),
        customer.getMobile(),
        customer.isLocked(),
        roleOf(customer),
        statusOf(customer),
        ""
    );
  }

  private AdminUserResponse toAdminUserResponse(Staff staff) {
    return new AdminUserResponse(
        staff.getUserId(),
        staff.getName(),
        staff.getUsername(),
        staff.getEmail(),
        staff.getMobile(),
        staff.isLocked(),
        "STAFF",
        staff.isLocked() ? "INACTIVE" : "ACTIVE",
        staff.getDepartment() == null ? "" : staff.getDepartment()
    );
  }

  private List<AdminUserResponse> allUsers() {
    List<AdminUserResponse> result = new ArrayList<>();
    result.addAll(customerRepository.findAll().stream().map(this::toAdminUserResponse).toList());
    result.addAll(staffRepository.findAll().stream().map(this::toAdminUserResponse).toList());
    return result;
  }

  private Comparator<AdminUserResponse> userComparator(String sortBy) {
    String key = sortBy == null ? "" : sortBy.trim().toLowerCase(Locale.ROOT);
    return switch (key) {
      case "userid" -> Comparator.comparing(AdminUserResponse::getUserId, String.CASE_INSENSITIVE_ORDER);
      case "username" -> Comparator.comparing(AdminUserResponse::getUsername, String.CASE_INSENSITIVE_ORDER);
      case "email" -> Comparator.comparing(AdminUserResponse::getEmail, String.CASE_INSENSITIVE_ORDER);
      case "role" -> Comparator.comparing(AdminUserResponse::getRole, String.CASE_INSENSITIVE_ORDER);
      case "status" -> Comparator.comparing(AdminUserResponse::getStatus, String.CASE_INSENSITIVE_ORDER);
      case "department" -> Comparator.comparing(
          user -> user.getDepartment() == null ? "" : user.getDepartment(),
          String.CASE_INSENSITIVE_ORDER
      );
      default -> Comparator.comparing(AdminUserResponse::getUsername, String.CASE_INSENSITIVE_ORDER);
    };
  }

  private String roleOf(Customer customer) {
    return customer.isAdmin() ? "ADMIN" : "CUSTOMER";
  }

  private String statusOf(Customer customer) {
    return customer.isLocked() ? "INACTIVE" : "ACTIVE";
  }

  private String normalizeUsername(String username) {
    String value = username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    if (!USERNAME_PATTERN.matcher(value).matches()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Username must be 5-30 characters and contain only letters, numbers, ., _, -");
    }
    return value;
  }

  private String normalizeEmail(String email) {
    String value = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    if (!EMAIL_PATTERN.matcher(value).matches()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Enter a valid email address.");
    }
    return value;
  }

  private String normalizeMobile(String mobile) {
    String value = mobile == null ? "" : mobile.trim();
    if (!MOBILE_PATTERN.matcher(value).matches()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Enter a valid +91 mobile number (10 digits, starts with 7, 8, or 9).");
    }
    return value;
  }

  private String normalizeRole(String role) {
    String value = role == null ? "" : role.trim().toUpperCase(Locale.ROOT);
    return switch (value) {
      case "ADMIN", "CUSTOMER", "STAFF" -> value;
      default -> throw new ApiException(HttpStatus.BAD_REQUEST, "Role must be ADMIN, CUSTOMER or STAFF.");
    };
  }

  private String normalizeCreatableRole(String role) {
    String value = normalizeRole(role);
    if ("ADMIN".equals(value)) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Role must be CUSTOMER or STAFF for user creation.");
    }
    return value;
  }

  private String generateStaffId() {
    String candidate;
    do {
      candidate = String.format("STA-%06d", RANDOM.nextInt(1_000_000));
    } while (staffRepository.existsByUserId(candidate) || customerRepository.existsByUserId(candidate));
    return candidate;
  }

  private String normalizeStatus(String status) {
    String value = status == null ? "" : status.trim().toUpperCase(Locale.ROOT);
    return switch (value) {
      case "ACTIVE", "INACTIVE" -> value;
      default -> throw new ApiException(HttpStatus.BAD_REQUEST, "Status must be ACTIVE or INACTIVE.");
    };
  }

  private String generateTemporaryPassword() {
    String upper = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    String lower = "abcdefghijkmnpqrstuvwxyz";
    String digits = "23456789";
    String specials = "@#$%&*!";
    String all = upper + lower + digits + specials;
    char[] pwd = new char[10];
    pwd[0] = upper.charAt(RANDOM.nextInt(upper.length()));
    pwd[1] = lower.charAt(RANDOM.nextInt(lower.length()));
    pwd[2] = digits.charAt(RANDOM.nextInt(digits.length()));
    pwd[3] = specials.charAt(RANDOM.nextInt(specials.length()));
    for (int i = 4; i < pwd.length; i++) {
      pwd[i] = all.charAt(RANDOM.nextInt(all.length()));
    }
    for (int i = pwd.length - 1; i > 0; i--) {
      int j = RANDOM.nextInt(i + 1);
      char t = pwd[i];
      pwd[i] = pwd[j];
      pwd[j] = t;
    }
    return new String(pwd);
  }

  private static class RoomWithAvailability {
    private final Room room;
    private final String availabilityStatus;

    private RoomWithAvailability(Room room, String availabilityStatus) {
      this.room = room;
      this.availabilityStatus = availabilityStatus;
    }
  }
}
