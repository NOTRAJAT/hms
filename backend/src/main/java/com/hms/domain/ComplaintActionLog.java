package com.hms.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "complaint_action_logs")
public class ComplaintActionLog {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String complaintId;

  @Column(nullable = false)
  private Instant actionAt;

  @Column(nullable = false)
  private String actionType;

  @Column(nullable = false)
  private String actorUserId;

  @Column(nullable = true)
  private String fromStatus;

  @Column(nullable = true)
  private String toStatus;

  @Column(nullable = true)
  private String assignedStaffMember;

  @Column(nullable = true)
  private String assignedDepartment;

  @Column(length = 2000)
  private String actionDetails;

  @PrePersist
  void onCreate() {
    if (actionAt == null) {
      actionAt = Instant.now();
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

  public Instant getActionAt() {
    return actionAt;
  }

  public void setActionAt(Instant actionAt) {
    this.actionAt = actionAt;
  }

  public String getActionType() {
    return actionType;
  }

  public void setActionType(String actionType) {
    this.actionType = actionType;
  }

  public String getActorUserId() {
    return actorUserId;
  }

  public void setActorUserId(String actorUserId) {
    this.actorUserId = actorUserId;
  }

  public String getFromStatus() {
    return fromStatus;
  }

  public void setFromStatus(String fromStatus) {
    this.fromStatus = fromStatus;
  }

  public String getToStatus() {
    return toStatus;
  }

  public void setToStatus(String toStatus) {
    this.toStatus = toStatus;
  }

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

  public String getActionDetails() {
    return actionDetails;
  }

  public void setActionDetails(String actionDetails) {
    this.actionDetails = actionDetails;
  }
}

