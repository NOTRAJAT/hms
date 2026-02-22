package com.hms.api.dto;

import java.time.Instant;

public class ComplaintCheckpointResponse {
  private final Instant updatedAt;
  private final String status;
  private final String message;
  private final String actorName;

  public ComplaintCheckpointResponse(Instant updatedAt, String status, String message, String actorName) {
    this.updatedAt = updatedAt;
    this.status = status;
    this.message = message;
    this.actorName = actorName;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public String getStatus() {
    return status;
  }

  public String getMessage() {
    return message;
  }

  public String getActorName() {
    return actorName;
  }
}
