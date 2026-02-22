package com.hms.api.dto;

import java.time.Instant;
import java.util.List;

public class AdminComplaintResponse {
  private final String complaintId;
  private final String customerUserId;
  private final String customerName;
  private final String bookingId;
  private final Instant submissionDate;
  private final String category;
  private final String title;
  private final String description;
  private final String contactPreference;
  private final String priorityLevel;
  private final String currentStatus;
  private final boolean newAssignment;
  private final String assignedStaffMember;
  private final String assignedDepartment;
  private final String expectedResolutionDate;
  private final String supportResponse;
  private final String resolutionNotes;
  private final List<AdminComplaintActionResponse> actions;

  public AdminComplaintResponse(
      String complaintId,
      String customerUserId,
      String customerName,
      String bookingId,
      Instant submissionDate,
      String category,
      String title,
      String description,
      String contactPreference,
      String priorityLevel,
      String currentStatus,
      boolean newAssignment,
      String assignedStaffMember,
      String assignedDepartment,
      String expectedResolutionDate,
      String supportResponse,
      String resolutionNotes,
      List<AdminComplaintActionResponse> actions
  ) {
    this.complaintId = complaintId;
    this.customerUserId = customerUserId;
    this.customerName = customerName;
    this.bookingId = bookingId;
    this.submissionDate = submissionDate;
    this.category = category;
    this.title = title;
    this.description = description;
    this.contactPreference = contactPreference;
    this.priorityLevel = priorityLevel;
    this.currentStatus = currentStatus;
    this.newAssignment = newAssignment;
    this.assignedStaffMember = assignedStaffMember;
    this.assignedDepartment = assignedDepartment;
    this.expectedResolutionDate = expectedResolutionDate;
    this.supportResponse = supportResponse;
    this.resolutionNotes = resolutionNotes;
    this.actions = actions;
  }

  public String getComplaintId() {
    return complaintId;
  }

  public String getCustomerUserId() {
    return customerUserId;
  }

  public String getCustomerName() {
    return customerName;
  }

  public String getBookingId() {
    return bookingId;
  }

  public Instant getSubmissionDate() {
    return submissionDate;
  }

  public String getCategory() {
    return category;
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

  public String getPriorityLevel() {
    return priorityLevel;
  }

  public String getCurrentStatus() {
    return currentStatus;
  }

  public boolean isNewAssignment() {
    return newAssignment;
  }

  public String getAssignedStaffMember() {
    return assignedStaffMember;
  }

  public String getAssignedDepartment() {
    return assignedDepartment;
  }

  public String getExpectedResolutionDate() {
    return expectedResolutionDate;
  }

  public String getSupportResponse() {
    return supportResponse;
  }

  public String getResolutionNotes() {
    return resolutionNotes;
  }

  public List<AdminComplaintActionResponse> getActions() {
    return actions;
  }
}
