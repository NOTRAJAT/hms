package com.hms.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "service_requests",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "requestId"),
        @UniqueConstraint(columnNames = "transactionId")
    }
)
public class ServiceRequest {
  public enum ServiceType {
    Cab,
    Salon,
    Dining
  }

  public enum Status {
    Requested,
    Confirmed,
    Completed,
    Cancelled
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String requestId;

  @Column(nullable = false)
  private String bookingId;

  @Column(nullable = false)
  private String customerUserId;

  @Column(nullable = false)
  private String customerName;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ServiceType serviceType;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Status status;

  @Column(nullable = false)
  private int amount;

  @Column(nullable = false)
  private String paymentStatus;

  @Column(nullable = false)
  private String paymentMethod;

  @Column(nullable = false)
  private String transactionId;

  @Column(nullable = false)
  private LocalDateTime serviceDateTime;

  @Column(nullable = false, length = 200)
  private String serviceSummary;

  @Column(nullable = false, length = 1000)
  private String serviceDetails;

  @Column(nullable = false, length = 5000)
  private String detailsJson;

  @Column(nullable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant updatedAt;

  @PrePersist
  void onCreate() {
    Instant now = Instant.now();
    if (createdAt == null) {
      createdAt = now;
    }
    if (updatedAt == null) {
      updatedAt = now;
    }
  }

  @PreUpdate
  void onUpdate() {
    updatedAt = Instant.now();
  }

  public Long getId() {
    return id;
  }

  public String getRequestId() {
    return requestId;
  }

  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

  public String getBookingId() {
    return bookingId;
  }

  public void setBookingId(String bookingId) {
    this.bookingId = bookingId;
  }

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

  public ServiceType getServiceType() {
    return serviceType;
  }

  public void setServiceType(ServiceType serviceType) {
    this.serviceType = serviceType;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public int getAmount() {
    return amount;
  }

  public void setAmount(int amount) {
    this.amount = amount;
  }

  public String getPaymentStatus() {
    return paymentStatus;
  }

  public void setPaymentStatus(String paymentStatus) {
    this.paymentStatus = paymentStatus;
  }

  public String getPaymentMethod() {
    return paymentMethod;
  }

  public void setPaymentMethod(String paymentMethod) {
    this.paymentMethod = paymentMethod;
  }

  public String getTransactionId() {
    return transactionId;
  }

  public void setTransactionId(String transactionId) {
    this.transactionId = transactionId;
  }

  public LocalDateTime getServiceDateTime() {
    return serviceDateTime;
  }

  public void setServiceDateTime(LocalDateTime serviceDateTime) {
    this.serviceDateTime = serviceDateTime;
  }

  public String getServiceSummary() {
    return serviceSummary;
  }

  public void setServiceSummary(String serviceSummary) {
    this.serviceSummary = serviceSummary;
  }

  public String getServiceDetails() {
    return serviceDetails;
  }

  public void setServiceDetails(String serviceDetails) {
    this.serviceDetails = serviceDetails;
  }

  public String getDetailsJson() {
    return detailsJson;
  }

  public void setDetailsJson(String detailsJson) {
    this.detailsJson = detailsJson;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }
}
