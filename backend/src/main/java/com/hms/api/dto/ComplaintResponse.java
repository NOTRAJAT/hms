package com.hms.api.dto;

import java.time.Instant;

public class ComplaintResponse {
  private String id;
  private String userId;
  private String category;
  private String bookingId;
  private String title;
  private String description;
  private String contactPreference;
  private String status;
  private Instant createdAt;
  private String expectedResolutionDate;
  private boolean editable;
  private String acknowledgementMessage;
  private String supportResponse;
  private String resolutionNotes;

  public ComplaintResponse(
      String id,
      String userId,
      String category,
      String bookingId,
      String title,
      String description,
      String contactPreference,
      String status,
      Instant createdAt,
      String expectedResolutionDate,
      boolean editable,
      String acknowledgementMessage,
      String supportResponse,
      String resolutionNotes
  ) {
    this.id = id;
    this.userId = userId;
    this.category = category;
    this.bookingId = bookingId;
    this.title = title;
    this.description = description;
    this.contactPreference = contactPreference;
    this.status = status;
    this.createdAt = createdAt;
    this.expectedResolutionDate = expectedResolutionDate;
    this.editable = editable;
    this.acknowledgementMessage = acknowledgementMessage;
    this.supportResponse = supportResponse;
    this.resolutionNotes = resolutionNotes;
  }

  public String getId() {
    return id;
  }

  public String getUserId() {
    return userId;
  }

  public String getCategory() {
    return category;
  }

  public String getBookingId() {
    return bookingId;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public String getContactPreference() {
    return contactPreference;
  }

  public String getStatus() {
    return status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public String getExpectedResolutionDate() {
    return expectedResolutionDate;
  }

  public boolean isEditable() {
    return editable;
  }

  public String getAcknowledgementMessage() {
    return acknowledgementMessage;
  }

  public String getSupportResponse() {
    return supportResponse;
  }

  public String getResolutionNotes() {
    return resolutionNotes;
  }
}
