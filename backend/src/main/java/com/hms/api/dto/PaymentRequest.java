package com.hms.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

public class PaymentRequest {
  @NotBlank
  private String userId;

  @NotBlank
  private String customerName;

  @NotBlank
  @Pattern(regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
  private String customerEmail;

  @NotBlank
  @Pattern(regexp = "^\\+91[789]\\d{9}$")
  private String customerMobile;

  @NotBlank
  private String roomId;

  @NotBlank
  private String roomType;

  @NotNull
  private LocalDate checkInDate;

  @NotNull
  private LocalDate checkOutDate;

  @NotNull
  @Min(1)
  @Max(10)
  private Integer adults;

  @NotNull
  @Min(0)
  @Max(5)
  private Integer children;

  @NotBlank
  private String paymentMethod;

  private String specialRequests;

  @NotBlank
  @Pattern(regexp = "^[A-Za-z ]{3,50}$")
  private String cardholderName;

  @NotBlank
  @Pattern(regexp = "^\\d{16}$")
  private String cardNumber;

  @NotBlank
  @Pattern(regexp = "^(0[1-9]|1[0-2])\\/\\d{2}$")
  private String expiryDate;

  @NotBlank
  @Pattern(regexp = "^\\d{3,4}$")
  private String cvv;

  @NotBlank
  @Pattern(regexp = "^\\d{6}$")
  private String otp;

  private String billingAddress;

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
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

  public String getRoomId() {
    return roomId;
  }

  public void setRoomId(String roomId) {
    this.roomId = roomId;
  }

  public String getRoomType() {
    return roomType;
  }

  public void setRoomType(String roomType) {
    this.roomType = roomType;
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

  public String getPaymentMethod() {
    return paymentMethod;
  }

  public void setPaymentMethod(String paymentMethod) {
    this.paymentMethod = paymentMethod;
  }

  public String getSpecialRequests() {
    return specialRequests;
  }

  public void setSpecialRequests(String specialRequests) {
    this.specialRequests = specialRequests;
  }

  public String getCardholderName() {
    return cardholderName;
  }

  public void setCardholderName(String cardholderName) {
    this.cardholderName = cardholderName;
  }

  public String getCardNumber() {
    return cardNumber;
  }

  public void setCardNumber(String cardNumber) {
    this.cardNumber = cardNumber;
  }

  public String getExpiryDate() {
    return expiryDate;
  }

  public void setExpiryDate(String expiryDate) {
    this.expiryDate = expiryDate;
  }

  public String getCvv() {
    return cvv;
  }

  public void setCvv(String cvv) {
    this.cvv = cvv;
  }

  public String getBillingAddress() {
    return billingAddress;
  }

  public void setBillingAddress(String billingAddress) {
    this.billingAddress = billingAddress;
  }

  public String getOtp() {
    return otp;
  }

  public void setOtp(String otp) {
    this.otp = otp;
  }
}
