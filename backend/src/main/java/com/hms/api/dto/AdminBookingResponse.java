package com.hms.api.dto;

import java.time.Instant;
import java.time.LocalDate;

public class AdminBookingResponse {
  private final String bookingId;
  private final String customerName;
  private final String customerEmail;
  private final String customerMobile;
  private final String customerUserId;
  private final String roomCode;
  private final String roomType;
  private final LocalDate checkInDate;
  private final LocalDate checkOutDate;
  private final int adults;
  private final int children;
  private final String status;
  private final int totalAmount;
  private final String paymentMethod;
  private final String specialRequests;
  private final Instant createdAt;

  public AdminBookingResponse(
      String bookingId,
      String customerName,
      String customerEmail,
      String customerMobile,
      String customerUserId,
      String roomCode,
      String roomType,
      LocalDate checkInDate,
      LocalDate checkOutDate,
      int adults,
      int children,
      String status,
      int totalAmount,
      String paymentMethod,
      String specialRequests,
      Instant createdAt
  ) {
    this.bookingId = bookingId;
    this.customerName = customerName;
    this.customerEmail = customerEmail;
    this.customerMobile = customerMobile;
    this.customerUserId = customerUserId;
    this.roomCode = roomCode;
    this.roomType = roomType;
    this.checkInDate = checkInDate;
    this.checkOutDate = checkOutDate;
    this.adults = adults;
    this.children = children;
    this.status = status;
    this.totalAmount = totalAmount;
    this.paymentMethod = paymentMethod;
    this.specialRequests = specialRequests;
    this.createdAt = createdAt;
  }

  public String getBookingId() {
    return bookingId;
  }

  public String getCustomerName() {
    return customerName;
  }

  public String getCustomerEmail() {
    return customerEmail;
  }

  public String getCustomerMobile() {
    return customerMobile;
  }

  public String getCustomerUserId() {
    return customerUserId;
  }

  public String getRoomCode() {
    return roomCode;
  }

  public String getRoomType() {
    return roomType;
  }

  public LocalDate getCheckInDate() {
    return checkInDate;
  }

  public LocalDate getCheckOutDate() {
    return checkOutDate;
  }

  public int getAdults() {
    return adults;
  }

  public int getChildren() {
    return children;
  }

  public String getStatus() {
    return status;
  }

  public int getTotalAmount() {
    return totalAmount;
  }

  public String getPaymentMethod() {
    return paymentMethod;
  }

  public String getSpecialRequests() {
    return specialRequests;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
