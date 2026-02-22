package com.hms.api.dto;

import java.time.Instant;

public class AdminComplaintActionResponse {
  private final Instant actionAt;
  private final String actionType;
  private final String actorUserId;
  private final String fromStatus;
  private final String toStatus;
  private final String assignedStaffMember;
  private final String assignedDepartment;
  private final String actionDetails;

  public AdminComplaintActionResponse(
      Instant actionAt,
      String actionType,
      String actorUserId,
      String fromStatus,
      String toStatus,
      String assignedStaffMember,
      String assignedDepartment,
      String actionDetails
  ) {
    this.actionAt = actionAt;
    this.actionType = actionType;
    this.actorUserId = actorUserId;
    this.fromStatus = fromStatus;
    this.toStatus = toStatus;
    this.assignedStaffMember = assignedStaffMember;
    this.assignedDepartment = assignedDepartment;
    this.actionDetails = actionDetails;
  }

  public Instant getActionAt() {
    return actionAt;
  }

  public String getActionType() {
    return actionType;
  }

  public String getActorUserId() {
    return actorUserId;
  }

  public String getFromStatus() {
    return fromStatus;
  }

  public String getToStatus() {
    return toStatus;
  }

  public String getAssignedStaffMember() {
    return assignedStaffMember;
  }

  public String getAssignedDepartment() {
    return assignedDepartment;
  }

  public String getActionDetails() {
    return actionDetails;
  }
}

