package com.hms.api.dto;

public class LoginResponse {
  private String userId;
  private String name;
  private String email;
  private String mobile;
  private String address;

  public LoginResponse(String userId, String name, String email, String mobile, String address) {
    this.userId = userId;
    this.name = name;
    this.email = email;
    this.mobile = mobile;
    this.address = address;
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
}
