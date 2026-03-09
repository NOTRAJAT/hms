package com.hms.api.dto;

import java.time.Instant;
import java.time.LocalDateTime;

public class ServiceRequestResponse {
  private final String requestId;
  private final String bookingId;
  private final String customerUserId;
  private final String customerName;
  private final String serviceType;
  private final String status;
  private final int amount;
  private final String paymentStatus;
  private final String paymentMethod;
  private final String transactionId;
  private final LocalDateTime serviceDateTime;
  private final String serviceSummary;
  private final String serviceDetails;
  private final Instant createdAt;
  private final Instant updatedAt;

  public ServiceRequestResponse(
      String requestId,
      String bookingId,
      String customerUserId,
      String customerName,
      String serviceType,
      String status,
      int amount,
      String paymentStatus,
      String paymentMethod,
      String transactionId,
      LocalDateTime serviceDateTime,
      String serviceSummary,
      String serviceDetails,
      Instant createdAt,
      Instant updatedAt
  ) {
    this.requestId = requestId;
    this.bookingId = bookingId;
    this.customerUserId = customerUserId;
    this.customerName = customerName;
    this.serviceType = serviceType;
    this.status = status;
    this.amount = amount;
    this.paymentStatus = paymentStatus;
    this.paymentMethod = paymentMethod;
    this.transactionId = transactionId;
    this.serviceDateTime = serviceDateTime;
    this.serviceSummary = serviceSummary;
    this.serviceDetails = serviceDetails;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public String getRequestId() {
    return requestId;
  }

  public String getBookingId() {
    return bookingId;
  }

  public String getCustomerUserId() {
    return customerUserId;
  }

  public String getCustomerName() {
    return customerName;
  }

  public String getServiceType() {
    return serviceType;
  }

  public String getStatus() {
    return status;
  }

  public int getAmount() {
    return amount;
  }

  public String getPaymentStatus() {
    return paymentStatus;
  }

  public String getPaymentMethod() {
    return paymentMethod;
  }

  public String getTransactionId() {
    return transactionId;
  }

  public LocalDateTime getServiceDateTime() {
    return serviceDateTime;
  }

  public String getServiceSummary() {
    return serviceSummary;
  }

  public String getServiceDetails() {
    return serviceDetails;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
