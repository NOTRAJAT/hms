package com.hms.api.dto;

public class LoginResponse {
  private String userId;
  private String name;
  private String email;
  private String mobile;
  private String address;
  private String role;
  private boolean passwordChangeRequired;

  public LoginResponse(
      String userId,
      String name,
      String email,
      String mobile,
      String address,
      String role,
      boolean passwordChangeRequired
  ) {
    this.userId = userId;
    this.name = name;
    this.email = email;
    this.mobile = mobile;
    this.address = address;
    this.role = role;
    this.passwordChangeRequired = passwordChangeRequired;
  }

  public String getUserId() {
    return userId;
  }

  public String getName() {
    return name;
  }

  public String getEmail() {
    return email;
  }

  public String getMobile() {
    return mobile;
  }

  public String getAddress() {
    return address;
  }

  public String getRole() {
    return role;
  }

  public boolean isPasswordChangeRequired() {
    return passwordChangeRequired;
  }
}
