package com.hms.service;

import com.hms.api.ApiException;
import com.hms.api.dto.ComplaintRequest;
import com.hms.api.dto.ComplaintResponse;
import com.hms.api.dto.ComplaintCheckpointResponse;
import com.hms.domain.Booking;
import com.hms.domain.Complaint;
import com.hms.repo.CustomerRepository;
import com.hms.repo.ComplaintActionLogRepository;
import com.hms.repo.BookingRepository;
import com.hms.repo.ComplaintRepository;
import com.hms.repo.StaffRepository;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ComplaintService {
  private static final SecureRandom RANDOM = new SecureRandom();
  private static final Logger LOG = LoggerFactory.getLogger(ComplaintService.class);
  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

  private final ComplaintRepository repository;
  private final BookingRepository bookingRepository;
  private final ComplaintActionLogRepository complaintActionLogRepository;
  private final CustomerRepository customerRepository;
  private final StaffRepository staffRepository;

  public ComplaintService(
      ComplaintRepository repository,
      BookingRepository bookingRepository,
      ComplaintActionLogRepository complaintActionLogRepository,
      CustomerRepository customerRepository,
      StaffRepository staffRepository
  ) {
    this.repository = repository;
    this.bookingRepository = bookingRepository;
    this.complaintActionLogRepository = complaintActionLogRepository;
    this.customerRepository = customerRepository;
    this.staffRepository = staffRepository;
  }

  @Transactional(readOnly = true)
  public List<ComplaintResponse> listByUser(String userId) {
    if (userId == null || userId.isBlank()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "userId is required.");
    }
    return repository.findByUserIdOrderByCreatedAtDesc(userId).stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public ComplaintResponse detail(String complaintId, String userId) {
    if (userId == null || userId.isBlank()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "userId is required.");
    }
    Complaint complaint = repository.findByComplaintIdAndUserId(complaintId, userId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "The complaint you are looking for does not exist or has been deleted."));
    return toResponse(complaint);
  }

  @Transactional
  public ComplaintResponse create(ComplaintRequest request) {
    validateComplaint(request);

    Complaint complaint = new Complaint();
    complaint.setComplaintId(generateComplaintId());
    complaint.setUserId(request.getUserId().trim());
    complaint.setCategory(request.getCategory().trim());
    complaint.setBookingId(request.getBookingId().trim());
    complaint.setTitle(request.getTitle().trim());
    complaint.setDescription(request.getDescription().trim());
    complaint.setContactPreference(request.getContactPreference().trim());
    complaint.setStatus(Complaint.Status.Open);
    complaint.setEscalated(false);
    complaint.setPriorityLevel(priorityForCategory(complaint.getCategory()));

    repository.save(complaint);
    logAcknowledgement(complaint);
    return toResponse(complaint);
  }

  @Transactional
  public ComplaintResponse update(String complaintId, ComplaintRequest request) {
    validateComplaint(request);
    Complaint complaint = repository.findByComplaintIdAndUserId(complaintId, request.getUserId().trim())
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "The complaint you are looking for does not exist or has been deleted."));
    if (!isOpenLikeStatus(complaint.getStatus())) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Only Open complaints can be edited.");
    }

    complaint.setCategory(request.getCategory().trim());
    complaint.setBookingId(request.getBookingId().trim());
    complaint.setTitle(request.getTitle().trim());
    complaint.setDescription(request.getDescription().trim());
    complaint.setContactPreference(request.getContactPreference().trim());
    complaint.setPriorityLevel(priorityForCategory(complaint.getCategory()));

    repository.save(complaint);
    return toResponse(complaint);
  }

  @Transactional
  public ComplaintResponse confirmResolution(String complaintId, String userId) {
    Complaint complaint = repository.findByComplaintIdAndUserId(complaintId, userId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "The complaint you are looking for does not exist or has been deleted."));
    if (complaint.getStatus() != Complaint.Status.Resolved) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Only resolved complaints can be confirmed.");
    }
    complaint.setStatus(Complaint.Status.Closed);
    repository.save(complaint);
    return toResponse(complaint);
  }

  @Transactional
  public ComplaintResponse reopen(String complaintId, String userId) {
    Complaint complaint = repository.findByComplaintIdAndUserId(complaintId, userId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "The complaint you are looking for does not exist or has been deleted."));
    if (complaint.getStatus() != Complaint.Status.Resolved) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Only resolved complaints can be reopened.");
    }
    complaint.setStatus(Complaint.Status.Open);
    repository.save(complaint);
    return toResponse(complaint);
  }

  private void validateComplaint(ComplaintRequest request) {
    if (request.getUserId() == null || request.getUserId().isBlank()
        || request.getCategory() == null || request.getCategory().isBlank()
        || request.getBookingId() == null || request.getBookingId().isBlank()
        || request.getTitle() == null || request.getTitle().isBlank()
        || request.getDescription() == null || request.getDescription().isBlank()
        || request.getContactPreference() == null || request.getContactPreference().isBlank()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Please fill in all required fields.");
    }

    String title = request.getTitle().trim();
    String description = request.getDescription().trim();
    if (title.length() < 10 || description.length() < 20 || title.length() > 100 || description.length() > 500) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Please provide more details to help us resolve your issue.");
    }

    Booking booking = bookingRepository.findByBookingIdAndCustomerUserId(
        request.getBookingId().trim(),
        request.getUserId().trim()
    ).orElse(null);
    if (booking == null) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Booking ID must belong to the logged-in customer.");
    }
    if (LocalDate.now().isBefore(booking.getCheckInDate())) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Complaint can be registered only on or after check-in date.");
    }

    String category = request.getCategory().trim();
    boolean validCategory = category.equals("Room Issue")
        || category.equals("Service Issue")
        || category.equals("Billing Issue")
        || category.equals("Other");
    if (!validCategory) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid complaint category.");
    }

    String contact = request.getContactPreference().trim();
    boolean validContact = contact.equals("Call") || contact.equals("Email");
    if (!validContact) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid contact preference.");
    }
  }

  private ComplaintResponse toResponse(Complaint complaint) {
    String statusLabel = toStatusLabel(complaint);
    Map<String, String> actorNames = loadActorNames();
    List<ComplaintCheckpointResponse> updates = complaintActionLogRepository
        .findByComplaintIdOrderByActionAtDesc(complaint.getComplaintId()).stream()
        .filter(log -> "CHECKPOINT_UPDATED".equalsIgnoreCase(log.getActionType()) || startsWithCheckpoint(log.getActionDetails()))
        .filter(log -> log.getActionDetails() != null && !log.getActionDetails().isBlank())
        .map(log -> new ComplaintCheckpointResponse(
            log.getActionAt(),
            log.getToStatus() == null || log.getToStatus().isBlank() ? statusLabel : log.getToStatus(),
            checkpointMessage(log.getActionDetails()),
            actorDisplayName(log.getActorUserId(), actorNames)
        ))
        .toList();
    String latestSupport = complaint.getSupportResponse() == null ? "" : complaint.getSupportResponse().trim();
    if (updates.isEmpty() && !latestSupport.isBlank() && !"No response from support team yet.".equalsIgnoreCase(latestSupport)) {
      List<ComplaintCheckpointResponse> fallback = new ArrayList<>();
      fallback.add(new ComplaintCheckpointResponse(
          complaint.getUpdatedAt() == null ? complaint.getCreatedAt() : complaint.getUpdatedAt(),
          statusLabel,
          latestSupport,
          "Support Team"
      ));
      updates = fallback;
    }
    return new ComplaintResponse(
        complaint.getComplaintId(),
        complaint.getUserId(),
        complaint.getCategory(),
        complaint.getBookingId(),
        complaint.getTitle(),
        complaint.getDescription(),
        complaint.getContactPreference(),
        statusLabel,
        complaint.getCreatedAt(),
        complaint.getExpectedResolutionDate() == null
            ? ""
            : complaint.getExpectedResolutionDate().format(DATE_FORMAT),
        "Open".equals(statusLabel),
        String.format(
            "Acknowledgment sent via %s. Expected resolution within 48 hours.",
            complaint.getContactPreference()
        ),
        complaint.getSupportResponse() == null ? "No response from support team yet." : complaint.getSupportResponse(),
        "",
        updates
    );
  }

  private String toStatusLabel(Complaint complaint) {
    if (complaint.isEscalated() && complaint.getStatus() == Complaint.Status.In_Progress) {
      return "Escalated";
    }
    Complaint.Status status = complaint.getStatus();
    return switch (status) {
      case Pending -> "Open";
      case Open -> "Open";
      case In_Progress -> "In Progress";
      case Resolved -> "Resolved";
      case Closed -> "Closed";
    };
  }

  private boolean isOpenLikeStatus(Complaint.Status status) {
    return status == Complaint.Status.Open || status == Complaint.Status.Pending;
  }

  private String generateComplaintId() {
    return String.format("CMP-%06d", RANDOM.nextInt(1_000_000));
  }

  private void logAcknowledgement(Complaint complaint) {
    LOG.info(
        "Complaint acknowledgement sent via {} for complaintId={} with expected resolution in 48 hours.",
        complaint.getContactPreference(),
        complaint.getComplaintId()
    );
  }

  private String checkpointMessage(String raw) {
    if (raw == null) {
      return "";
    }
    String value = raw.trim();
    String prefix = "checkpoint update:";
    if (value.toLowerCase(Locale.ROOT).startsWith(prefix)) {
      return value.substring(prefix.length()).trim();
    }
    return value;
  }

  private boolean startsWithCheckpoint(String raw) {
    if (raw == null) {
      return false;
    }
    return raw.trim().toLowerCase(Locale.ROOT).startsWith("checkpoint update:");
  }

  private Map<String, String> loadActorNames() {
    Map<String, String> names = new HashMap<>();
    customerRepository.findAll().forEach(customer -> names.put(customer.getUserId(), customer.getName()));
    staffRepository.findAll().forEach(staff -> names.put(staff.getUserId(), staff.getName()));
    return names;
  }

  private String actorDisplayName(String actorUserId, Map<String, String> actorNames) {
    if (actorUserId == null || actorUserId.isBlank()) {
      return "Support Team";
    }
    String userId = actorUserId.trim();
    return actorNames.getOrDefault(userId, userId);
  }

  private String priorityForCategory(String category) {
    String normalized = category == null ? "" : category.trim().toLowerCase(Locale.ROOT);
    return switch (normalized) {
      case "billing issue", "room issue" -> "High";
      case "service issue" -> "Medium";
      default -> "Low";
    };
  }
}
