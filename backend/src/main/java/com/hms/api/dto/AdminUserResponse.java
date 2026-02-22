package com.hms.api.dto;

public class AdminUserResponse {
  private final String userId;
  private final String name;
  private final String username;
  private final String email;
  private final String mobile;
  private final boolean locked;
  private final String role;
  private final String status;
  private final String department;

  public AdminUserResponse(
      String userId,
      String name,
      String username,
      String email,
      String mobile,
      boolean locked,
      String role,
      String status,
      String department
  ) {
    this.userId = userId;
    this.name = name;
    this.username = username;
    this.email = email;
    this.mobile = mobile;
    this.locked = locked;
    this.role = role;
    this.status = status;
    this.department = department;
  }

  public String getUserId() {
    return userId;
  }

  public String getName() {
    return name;
  }

  public String getUsername() {
    return username;
  }

  public String getEmail() {
    return email;
  }

  public String getMobile() {
    return mobile;
  }

  public boolean isLocked() {
    return locked;
  }

  public String getRole() {
    return role;
  }

  public String getStatus() {
    return status;
  }

  public String getDepartment() {
    return department;
  }
}
