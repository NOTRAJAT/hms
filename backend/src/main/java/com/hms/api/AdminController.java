package com.hms.api;

import com.hms.api.dto.AdminBookingResponse;
import com.hms.api.dto.AdminBookingPageResponse;
import com.hms.api.dto.AdminBookingCreateRequest;
import com.hms.api.dto.AdminBookingUpdateRequest;
import com.hms.api.dto.AdminBillPageResponse;
import com.hms.api.dto.AdminBillResponse;
import com.hms.api.dto.AdminBillCreateRequest;
import com.hms.api.dto.AdminBillUpdateRequest;
import com.hms.api.dto.AdminBillSummaryResponse;
import com.hms.api.dto.AdminComplaintPageResponse;
import com.hms.api.dto.AdminComplaintResponse;
import com.hms.api.dto.AdminComplaintUpdateRequest;
import com.hms.api.dto.AdminDashboardResponse;
import com.hms.api.dto.AdminBulkImportResponse;
import com.hms.api.dto.AdminRoomCreateRequest;
import com.hms.api.dto.AdminRoomPageResponse;
import com.hms.api.dto.AdminRoomOccupancyGridResponse;
import com.hms.api.dto.AdminRoomOccupancyResponse;
import com.hms.api.dto.AdminRoomResponse;
import com.hms.api.dto.AdminRoomUpdateRequest;
import com.hms.api.dto.AdminUserCreateRequest;
import com.hms.api.dto.AdminUserCreateResponse;
import com.hms.api.dto.AdminUserPageResponse;
import com.hms.api.dto.AdminUserResponse;
import com.hms.api.dto.AdminUserUpdateRequest;
import com.hms.api.dto.AdminPasswordResetResponse;
import com.hms.api.dto.AdminServiceItemResponse;
import com.hms.api.dto.AdminServicePageResponse;
import com.hms.api.dto.AdminServiceStatusUpdateRequest;
import jakarta.validation.Valid;
import java.time.LocalDate;
import com.hms.service.AdminService;
import com.hms.service.ServiceRequestService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
  private final AdminService adminService;
  private final ServiceRequestService serviceRequestService;

  public AdminController(AdminService adminService, ServiceRequestService serviceRequestService) {
    this.adminService = adminService;
    this.serviceRequestService = serviceRequestService;
  }

  @GetMapping("/dashboard-summary")
  public ResponseEntity<AdminDashboardResponse> dashboardSummary() {
    return ResponseEntity.ok(adminService.getDashboardSummary());
  }

  @GetMapping("/rooms")
  public ResponseEntity<AdminRoomPageResponse> rooms(
      @RequestParam(name = "q", required = false) String q,
      @RequestParam(name = "roomType", required = false) String roomType,
      @RequestParam(name = "priceMin", required = false) Integer priceMin,
      @RequestParam(name = "priceMax", required = false) Integer priceMax,
      @RequestParam(name = "availability", required = false) String availability,
      @RequestParam(name = "amenity", required = false) String amenity,
      @RequestParam(name = "maxOccupancy", required = false) Integer maxOccupancy,
      @RequestParam(name = "date", required = false) LocalDate date,
      @RequestParam(name = "sortBy", required = false) String sortBy,
      @RequestParam(name = "sortDir", required = false) String sortDir,
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "10") int size
  ) {
    return ResponseEntity.ok(adminService.searchRooms(
        q, roomType, priceMin, priceMax, availability, amenity, maxOccupancy, date, sortBy, sortDir, page, size
    ));
  }

  @PutMapping("/rooms/{roomCode}")
  public ResponseEntity<AdminRoomResponse> updateRoom(
      @PathVariable String roomCode,
      @Valid @RequestBody AdminRoomUpdateRequest request
  ) {
    return ResponseEntity.ok(adminService.updateRoom(roomCode, request));
  }

  @PostMapping("/rooms")
  public ResponseEntity<AdminRoomResponse> addRoom(@Valid @RequestBody AdminRoomCreateRequest request) {
    return ResponseEntity.ok(adminService.addRoom(request));
  }

  @PostMapping(value = "/rooms/bulk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<AdminBulkImportResponse> bulkImport(@RequestPart("file") MultipartFile file) {
    return ResponseEntity.ok(adminService.bulkImportRooms(file));
  }

  @GetMapping("/rooms/template")
  public ResponseEntity<String> template() {
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=room-import-template.csv")
        .contentType(MediaType.parseMediaType("text/csv"))
        .body(adminService.csvTemplate());
  }

  @GetMapping("/bookings")
  public ResponseEntity<AdminBookingPageResponse> bookings(
      @RequestParam(name = "q", required = false) String q,
      @RequestParam(name = "roomCode", required = false) String roomCode,
      @RequestParam(name = "status", required = false) String status,
      @RequestParam(name = "roomType", required = false) String roomType,
      @RequestParam(name = "bookingDate", required = false) LocalDate bookingDate,
      @RequestParam(name = "fromDate", required = false) LocalDate fromDate,
      @RequestParam(name = "toDate", required = false) LocalDate toDate,
      @RequestParam(name = "sortBy", required = false) String sortBy,
      @RequestParam(name = "sortDir", required = false) String sortDir,
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "10") int size
  ) {
    return ResponseEntity.ok(
        adminService.searchBookings(q, roomCode, status, roomType, bookingDate, fromDate, toDate, sortBy, sortDir, page, size)
    );
  }

  @PostMapping("/bookings")
  public ResponseEntity<AdminBookingResponse> createBooking(@Valid @RequestBody AdminBookingCreateRequest request) {
    return ResponseEntity.ok(adminService.createBooking(request));
  }

  @PutMapping("/bookings/{bookingId}")
  public ResponseEntity<AdminBookingResponse> updateBooking(
      @PathVariable String bookingId,
      @Valid @RequestBody AdminBookingUpdateRequest request
  ) {
    return ResponseEntity.ok(adminService.updateBooking(bookingId, request));
  }

  @PostMapping("/bookings/{bookingId}/cancel")
  public ResponseEntity<AdminBookingResponse> cancelBooking(@PathVariable String bookingId) {
    return ResponseEntity.ok(adminService.cancelBooking(bookingId));
  }

  @GetMapping("/bills")
  public ResponseEntity<AdminBillPageResponse> bills(
      @RequestParam(name = "q", required = false) String q,
      @RequestParam(name = "paymentStatus", required = false) String paymentStatus,
      @RequestParam(name = "fromDate", required = false) LocalDate fromDate,
      @RequestParam(name = "toDate", required = false) LocalDate toDate,
      @RequestParam(name = "sortBy", required = false) String sortBy,
      @RequestParam(name = "sortDir", required = false) String sortDir,
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "10") int size
  ) {
    return ResponseEntity.ok(adminService.searchBills(q, paymentStatus, fromDate, toDate, sortBy, sortDir, page, size));
  }

  @GetMapping("/bills/summary")
  public ResponseEntity<AdminBillSummaryResponse> billSummary(
      @RequestParam(name = "q", required = false) String q,
      @RequestParam(name = "paymentStatus", required = false) String paymentStatus,
      @RequestParam(name = "fromDate", required = false) LocalDate fromDate,
      @RequestParam(name = "toDate", required = false) LocalDate toDate
  ) {
    return ResponseEntity.ok(adminService.summarizeBills(q, paymentStatus, fromDate, toDate));
  }

  @GetMapping("/bills/export")
  public ResponseEntity<String> exportBills(
      @RequestParam(name = "q", required = false) String q,
      @RequestParam(name = "paymentStatus", required = false) String paymentStatus,
      @RequestParam(name = "fromDate", required = false) LocalDate fromDate,
      @RequestParam(name = "toDate", required = false) LocalDate toDate,
      @RequestParam(name = "sortBy", required = false) String sortBy,
      @RequestParam(name = "sortDir", required = false) String sortDir
  ) {
    String suffix = fromDate == null && toDate == null
        ? "all"
        : String.format("%s_to_%s", fromDate == null ? "start" : fromDate, toDate == null ? "today" : toDate);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=bills-" + suffix + ".csv")
        .contentType(MediaType.parseMediaType("text/csv"))
        .body(adminService.exportBillsCsv(q, paymentStatus, fromDate, toDate, sortBy, sortDir));
  }

  @GetMapping("/complaints")
  public ResponseEntity<AdminComplaintPageResponse> complaints(
      @RequestParam(name = "q", required = false) String q,
      @RequestParam(name = "category", required = false) String category,
      @RequestParam(name = "priority", required = false) String priority,
      @RequestParam(name = "status", required = false) String status,
      @RequestParam(name = "assignedTo", required = false) String assignedTo,
      @RequestParam(name = "fromDate", required = false) LocalDate fromDate,
      @RequestParam(name = "toDate", required = false) LocalDate toDate,
      @RequestParam(name = "sortBy", required = false) String sortBy,
      @RequestParam(name = "sortDir", required = false) String sortDir,
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "10") int size,
      Authentication authentication
  ) {
    return ResponseEntity.ok(adminService.searchComplaints(
        q,
        category,
        priority,
        status,
        assignedTo,
        fromDate,
        toDate,
        sortBy,
        sortDir,
        authentication.getName(),
        isAdmin(authentication),
        page,
        size
    ));
  }

  @GetMapping("/complaints/{complaintId}")
  public ResponseEntity<AdminComplaintResponse> complaintDetail(@PathVariable String complaintId, Authentication authentication) {
    return ResponseEntity.ok(adminService.complaintDetail(complaintId, authentication.getName(), isAdmin(authentication)));
  }

  @PatchMapping("/complaints/{complaintId}")
  public ResponseEntity<AdminComplaintResponse> updateComplaint(
      @PathVariable String complaintId,
      @Valid @RequestBody AdminComplaintUpdateRequest request,
      Authentication authentication
  ) {
    return ResponseEntity.ok(adminService.updateComplaint(complaintId, request, authentication.getName(), isAdmin(authentication)));
  }

  private boolean isAdmin(Authentication authentication) {
    return authentication != null
        && authentication.getAuthorities() != null
        && authentication.getAuthorities().stream()
        .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
  }

  @PostMapping("/bills")
  public ResponseEntity<AdminBillResponse> createBill(@Valid @RequestBody AdminBillCreateRequest request) {
    return ResponseEntity.ok(adminService.createBill(request));
  }

  @PutMapping("/bills/{billId}")
  public ResponseEntity<AdminBillResponse> updateBill(
      @PathVariable String billId,
      @Valid @RequestBody AdminBillUpdateRequest request
  ) {
    return ResponseEntity.ok(adminService.updateBill(billId, request));
  }

  @PostMapping("/bills/{billId}/mark-paid")
  public ResponseEntity<AdminBillResponse> markBillPaid(@PathVariable String billId) {
    return ResponseEntity.ok(adminService.markBillPaid(billId));
  }

  @GetMapping("/users")
  public ResponseEntity<AdminUserPageResponse> users(
      @RequestParam(name = "q", required = false) String q,
      @RequestParam(name = "role", required = false) String role,
      @RequestParam(name = "status", required = false) String status,
      @RequestParam(name = "sortBy", required = false) String sortBy,
      @RequestParam(name = "sortDir", required = false) String sortDir,
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "10") int size
  ) {
    return ResponseEntity.ok(adminService.searchUsers(q, role, status, sortBy, sortDir, page, size));
  }

  @PostMapping("/users")
  public ResponseEntity<AdminUserCreateResponse> createUser(@Valid @RequestBody AdminUserCreateRequest request) {
    return ResponseEntity.ok(adminService.createUser(request));
  }

  @PutMapping("/users/{userId}")
  public ResponseEntity<AdminUserResponse> updateUser(
      @PathVariable String userId,
      @Valid @RequestBody AdminUserUpdateRequest request
  ) {
    return ResponseEntity.ok(adminService.updateUser(userId, request));
  }

  @PostMapping("/users/{userId}/status")
  public ResponseEntity<AdminUserResponse> updateUserStatus(
      @PathVariable String userId,
      @RequestParam(name = "status") String status
  ) {
    return ResponseEntity.ok(adminService.updateUserStatus(userId, status));
  }

  @PostMapping("/users/{userId}/reset-password")
  public ResponseEntity<AdminPasswordResetResponse> resetUserPassword(@PathVariable String userId) {
    return ResponseEntity.ok(adminService.resetUserPassword(userId));
  }

  @GetMapping("/rooms/occupancy")
  public ResponseEntity<AdminRoomOccupancyResponse> roomOccupancy(
      @RequestParam(name = "roomCode") String roomCode
  ) {
    return ResponseEntity.ok(adminService.roomOccupancy30Days(roomCode));
  }

  @GetMapping("/rooms/occupancy-grid")
  public ResponseEntity<AdminRoomOccupancyGridResponse> roomOccupancyGrid(
      @RequestParam(name = "roomType") String roomType,
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "pageSize", defaultValue = "2") int pageSize
  ) {
    return ResponseEntity.ok(adminService.roomOccupancyGrid(roomType, page, pageSize));
  }

  @GetMapping("/services")
  public ResponseEntity<AdminServicePageResponse> services(
      @RequestParam(name = "q", required = false) String q,
      @RequestParam(name = "serviceType", required = false) String serviceType,
      @RequestParam(name = "status", required = false) String status,
      @RequestParam(name = "bookingId", required = false) String bookingId,
      @RequestParam(name = "customer", required = false) String customer,
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "10") int size
  ) {
    return ResponseEntity.ok(serviceRequestService.searchAdmin(q, serviceType, status, bookingId, customer, page, size));
  }

  @PatchMapping("/services/{requestId}/status")
  public ResponseEntity<AdminServiceItemResponse> updateServiceStatus(
      @PathVariable String requestId,
      @Valid @RequestBody AdminServiceStatusUpdateRequest request
  ) {
    return ResponseEntity.ok(serviceRequestService.updateStatus(requestId, request.getStatus()));
  }
}
