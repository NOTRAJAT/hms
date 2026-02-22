package com.hms.api.dto;

public class AdminPasswordResetResponse {
  private final String userId;
  private final String username;
  private final String temporaryPassword;
  private final String message;

  public AdminPasswordResetResponse(String userId, String username, String temporaryPassword, String message) {
    this.userId = userId;
    this.username = username;
    this.temporaryPassword = temporaryPassword;
    this.message = message;
  }

  public String getUserId() {
    return userId;
  }

  public String getUsername() {
    return username;
  }

  public String getTemporaryPassword() {
    return temporaryPassword;
  }

  public String getMessage() {
    return message;
  }
}

