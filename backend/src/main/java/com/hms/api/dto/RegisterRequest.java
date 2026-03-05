package com.hms.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
  @NotBlank
  @Size(min = 3)
  @Pattern(regexp = "^[A-Za-z ]+$")
  private String name;

  @NotBlank
  @Email
  private String email;

  @NotBlank
  @Pattern(regexp = "^\\+91$")
  private String countryCode;

  @NotBlank
  @Pattern(regexp = "^[789]\\d{9}$")
  private String mobileNumber;

  @NotBlank
  @Size(min = 10)
  private String address;

  @NotBlank
  @Size(min = 5)
  @Pattern(regexp = "^\\S+$")
  private String username;

  @NotBlank
  @Size(min = 8)
  private String password;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getCountryCode() {
    return countryCode;
  }

  public void setCountryCode(String countryCode) {
    this.countryCode = countryCode;
  }

  public String getMobileNumber() {
    return mobileNumber;
  }

  public void setMobileNumber(String mobileNumber) {
    this.mobileNumber = mobileNumber;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}
