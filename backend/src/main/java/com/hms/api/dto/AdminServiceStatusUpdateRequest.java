package com.hms.api.dto;

import jakarta.validation.constraints.NotBlank;

public class AdminServiceStatusUpdateRequest {
  @NotBlank
  private String status;

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
