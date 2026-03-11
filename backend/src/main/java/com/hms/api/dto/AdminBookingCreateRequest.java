package com.hms.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class AdminBookingCreateRequest {
  private String customerUserId;

  @NotBlank
  private String customerName;

  @NotBlank
  @Email
  private String customerEmail;

  @NotBlank
  @Pattern(regexp = "^\\+91[789]\\d{9}$")
  private String customerMobile;

  @NotNull
  private LocalDate checkInDate;

  @NotNull
  private LocalDate checkOutDate;

  @NotNull
  @Min(1)
  private Integer adults;

  @NotNull
  @Min(0)
  private Integer children;

  private String roomCode;

  @NotBlank
  private String roomType;

  @NotBlank
  @Pattern(regexp = "^(?i)card$")
  private String paymentMethod;

  @Min(0)
  private Integer depositAmount;

  @Size(max = 1000)
  private String specialRequests;

  public String getCustomerUserId() {
    return customerUserId;
  }

  public void setCustomerUserId(String customerUserId) {
    this.customerUserId = customerUserId;
  }

  public String getCustomerName() {
    return customerName;
  }

  public void setCustomerName(String customerName) {
    this.customerName = customerName;
  }

  public String getCustomerEmail() {
    return customerEmail;
  }

  public void setCustomerEmail(String customerEmail) {
    this.customerEmail = customerEmail;
  }

  public String getCustomerMobile() {
    return customerMobile;
  }

  public void setCustomerMobile(String customerMobile) {
    this.customerMobile = customerMobile;
  }

  public LocalDate getCheckInDate() {
    return checkInDate;
  }

  public void setCheckInDate(LocalDate checkInDate) {
    this.checkInDate = checkInDate;
  }

  public LocalDate getCheckOutDate() {
    return checkOutDate;
  }

  public void setCheckOutDate(LocalDate checkOutDate) {
    this.checkOutDate = checkOutDate;
  }

  public Integer getAdults() {
    return adults;
  }

  public void setAdults(Integer adults) {
    this.adults = adults;
  }

  public Integer getChildren() {
    return children;
  }

  public void setChildren(Integer children) {
    this.children = children;
  }

  public String getRoomCode() {
    return roomCode;
  }

  public void setRoomCode(String roomCode) {
    this.roomCode = roomCode;
  }

  public String getRoomType() {
    return roomType;
  }

  public void setRoomType(String roomType) {
    this.roomType = roomType;
  }

  public String getPaymentMethod() {
    return paymentMethod;
  }

  public void setPaymentMethod(String paymentMethod) {
    this.paymentMethod = paymentMethod;
  }

  public Integer getDepositAmount() {
    return depositAmount;
  }

  public void setDepositAmount(Integer depositAmount) {
    this.depositAmount = depositAmount;
  }

  public String getSpecialRequests() {
    return specialRequests;
  }

  public void setSpecialRequests(String specialRequests) {
    this.specialRequests = specialRequests;
  }
}
