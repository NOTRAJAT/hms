package com.hms.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;

public class SalonServiceCreateRequest {
  @NotBlank
  private String bookingId;

  @NotBlank
  private String packageCode;

  @NotNull
  private LocalDateTime slotDateTime;

  @NotBlank
  @Pattern(regexp = "^(?i)card$")
  private String paymentMethod;

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

  public String getBookingId() {
    return bookingId;
  }

  public void setBookingId(String bookingId) {
    this.bookingId = bookingId;
  }

  public String getPackageCode() {
    return packageCode;
  }

  public void setPackageCode(String packageCode) {
    this.packageCode = packageCode;
  }

  public LocalDateTime getSlotDateTime() {
    return slotDateTime;
  }

  public void setSlotDateTime(LocalDateTime slotDateTime) {
    this.slotDateTime = slotDateTime;
  }

  public String getPaymentMethod() {
    return paymentMethod;
  }

  public void setPaymentMethod(String paymentMethod) {
    this.paymentMethod = paymentMethod;
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

  public String getOtp() {
    return otp;
  }

  public void setOtp(String otp) {
    this.otp = otp;
  }

  public String getBillingAddress() {
    return billingAddress;
  }

  public void setBillingAddress(String billingAddress) {
    this.billingAddress = billingAddress;
  }
}
