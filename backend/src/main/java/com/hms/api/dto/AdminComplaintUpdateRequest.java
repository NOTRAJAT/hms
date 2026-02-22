package com.hms.api.dto;

import jakarta.validation.constraints.Size;

public class AdminComplaintUpdateRequest {
  @Size(max = 120)
  private String assignedStaffMember;

  @Size(max = 120)
  private String assignedDepartment;

  @Size(max = 40)
  private String status;

  @Size(max = 1000)
  private String supportResponse;

  @Size(max = 1000)
  private String resolutionNotes;

  @Size(max = 2000)
  private String actionDetails;

  public String getAssignedStaffMember() {
    return assignedStaffMember;
  }

  public void setAssignedStaffMember(String assignedStaffMember) {
    this.assignedStaffMember = assignedStaffMember;
  }

  public String getAssignedDepartment() {
    return assignedDepartment;
  }

  public void setAssignedDepartment(String assignedDepartment) {
    this.assignedDepartment = assignedDepartment;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
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

  public String getActionDetails() {
    return actionDetails;
  }

  public void setActionDetails(String actionDetails) {
    this.actionDetails = actionDetails;
  }
}

