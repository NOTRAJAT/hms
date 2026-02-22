package com.hms.api.dto;

public class AdminUserCreateResponse {
  private final AdminUserResponse user;
  private final String temporaryPassword;
  private final String message;

  public AdminUserCreateResponse(AdminUserResponse user, String temporaryPassword, String message) {
    this.user = user;
    this.temporaryPassword = temporaryPassword;
    this.message = message;
  }

  public AdminUserResponse getUser() {
    return user;
  }

  public String getTemporaryPassword() {
    return temporaryPassword;
  }

  public String getMessage() {
    return message;
  }
}

