package com.hms.service;

import com.hms.api.ApiException;
import com.hms.api.dto.ComplaintRequest;
import com.hms.api.dto.ComplaintResponse;
import com.hms.domain.Complaint;
import com.hms.repo.BookingRepository;
import com.hms.repo.ComplaintRepository;
import java.security.SecureRandom;
import java.time.format.DateTimeFormatter;
import java.util.List;
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

  public ComplaintService(ComplaintRepository repository, BookingRepository bookingRepository) {
    this.repository = repository;
    this.bookingRepository = bookingRepository;
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

    boolean bookingBelongsToUser = bookingRepository.findByBookingIdAndCustomerUserId(
        request.getBookingId().trim(),
        request.getUserId().trim()
    ).isPresent();
    if (!bookingBelongsToUser) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Booking ID must belong to the logged-in customer.");
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
    String statusLabel = toStatusLabel(complaint.getStatus());
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
        complaint.getResolutionNotes() == null ? "Resolution notes will appear once the complaint is resolved." : complaint.getResolutionNotes()
    );
  }

  private String toStatusLabel(Complaint.Status status) {
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
}
