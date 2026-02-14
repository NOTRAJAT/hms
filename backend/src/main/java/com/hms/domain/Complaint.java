package com.hms.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
    name = "complaints",
    uniqueConstraints = { @UniqueConstraint(columnNames = "complaintId") }
)
public class Complaint {
  public enum Status {
    Pending,
    Open,
    In_Progress,
    Resolved,
    Closed
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String complaintId;

  @Column(nullable = false)
  private String userId;

  @Column(nullable = false)
  private String category;

  @Column(nullable = false)
  private String bookingId;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false, length = 500)
  private String description;

  @Column(nullable = false)
  private String contactPreference;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Status status;

  @Column(nullable = true)
  private LocalDate expectedResolutionDate;

  @Column(length = 1000)
  private String supportResponse;

  @Column(length = 1000)
  private String resolutionNotes;

  @Column(nullable = false)
  private Instant createdAt;

  @PrePersist
  void onCreate() {
    if (status == null) {
      status = Status.Open;
    }
    if (createdAt == null) {
      createdAt = Instant.now();
    }
    if (expectedResolutionDate == null) {
      expectedResolutionDate = LocalDate.now().plusDays(2);
    }
  }

  public Long getId() {
    return id;
  }

  public String getComplaintId() {
    return complaintId;
  }

  public void setComplaintId(String complaintId) {
    this.complaintId = complaintId;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getBookingId() {
    return bookingId;
  }

  public void setBookingId(String bookingId) {
    this.bookingId = bookingId;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getContactPreference() {
    return contactPreference;
  }

  public void setContactPreference(String contactPreference) {
    this.contactPreference = contactPreference;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public LocalDate getExpectedResolutionDate() {
    return expectedResolutionDate;
  }

  public void setExpectedResolutionDate(LocalDate expectedResolutionDate) {
    this.expectedResolutionDate = expectedResolutionDate;
  }

  public String getSupportResponse() {
    return supportResponse;
  }

  public void setSupportResponse(String supportResponse) {
    this.supportResponse = supportResponse;
  }

  public String getResolutionNotes() {
    return resolutionNotes;
  }

  public void setResolutionNotes(String resolutionNotes) {
    this.resolutionNotes = resolutionNotes;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }
}
