package com.hms.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
    name = "bookings",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "bookingId"),
        @UniqueConstraint(columnNames = "transactionId"),
        @UniqueConstraint(columnNames = "invoiceId")
    }
)
public class Booking {
  public enum Status {
    Confirmed,
    Cancelled
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String bookingId;

  @Column(nullable = false)
  private String invoiceId;

  @Column(nullable = false)
  private String transactionId;

  @Column(nullable = false)
  private String customerUserId;

  @Column(nullable = false)
  private String customerName;

  @Column(nullable = false)
  private String customerEmail;

  @Column(nullable = false)
  private String customerMobile;

  @Column(nullable = false)
  private String roomCode;

  @Column(nullable = false)
  private String roomType;

  @Column(nullable = false)
  private int occupancyAdults;

  @Column(nullable = false)
  private int occupancyChildren;

  @Column(nullable = false)
  private int pricePerNight;

  @Column(nullable = false)
  private LocalDate checkInDate;

  @Column(nullable = true)
  private LocalDate originalCheckInDate;

  @Column(nullable = false)
  private LocalDate checkOutDate;

  @Column(nullable = false)
  private int nights;

  @Column(nullable = false)
  private int adults;

  @Column(nullable = false)
  private int children;

  @Column(nullable = false)
  private int basePrice;

  @Column(nullable = false)
  private int gstAmount;

  @Column(nullable = false)
  private int serviceChargeAmount;

  @Column(nullable = false)
  private int totalAmount;

  @Column(nullable = false)
  private String paymentMethod;

  @Column(nullable = false, columnDefinition = "varchar(20) default 'PAID'")
  private String paymentStatus;

  @Column(length = 1000)
  private String specialRequests;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Status status;

  @Column(nullable = false)
  private Instant createdAt;

  private Instant cancelledAt;

  private Integer cancellationRefundAmount;

  @Column(length = 500)
  private String cancellationNote;

  @PrePersist
  void onCreate() {
    if (createdAt == null) {
      createdAt = Instant.now();
    }
    if (originalCheckInDate == null) {
      originalCheckInDate = checkInDate;
    }
  }

  public Long getId() {
    return id;
  }

  public String getBookingId() {
    return bookingId;
  }

  public void setBookingId(String bookingId) {
    this.bookingId = bookingId;
  }

  public String getInvoiceId() {
    return invoiceId;
  }

  public void setInvoiceId(String invoiceId) {
    this.invoiceId = invoiceId;
  }

  public String getTransactionId() {
    return transactionId;
  }

  public void setTransactionId(String transactionId) {
    this.transactionId = transactionId;
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

  public int getOccupancyAdults() {
    return occupancyAdults;
  }

  public void setOccupancyAdults(int occupancyAdults) {
    this.occupancyAdults = occupancyAdults;
  }

  public int getOccupancyChildren() {
    return occupancyChildren;
  }

  public void setOccupancyChildren(int occupancyChildren) {
    this.occupancyChildren = occupancyChildren;
  }

  public int getPricePerNight() {
    return pricePerNight;
  }

  public void setPricePerNight(int pricePerNight) {
    this.pricePerNight = pricePerNight;
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

  public LocalDate getOriginalCheckInDate() {
    return originalCheckInDate;
  }

  public void setOriginalCheckInDate(LocalDate originalCheckInDate) {
    this.originalCheckInDate = originalCheckInDate;
  }

  public int getNights() {
    return nights;
  }

  public void setNights(int nights) {
    this.nights = nights;
  }

  public int getAdults() {
    return adults;
  }

  public void setAdults(int adults) {
    this.adults = adults;
  }

  public int getChildren() {
    return children;
  }

  public void setChildren(int children) {
    this.children = children;
  }

  public int getBasePrice() {
    return basePrice;
  }

  public void setBasePrice(int basePrice) {
    this.basePrice = basePrice;
  }

  public int getGstAmount() {
    return gstAmount;
  }

  public void setGstAmount(int gstAmount) {
    this.gstAmount = gstAmount;
  }

  public int getServiceChargeAmount() {
    return serviceChargeAmount;
  }

  public void setServiceChargeAmount(int serviceChargeAmount) {
    this.serviceChargeAmount = serviceChargeAmount;
  }

  public int getTotalAmount() {
    return totalAmount;
  }

  public void setTotalAmount(int totalAmount) {
    this.totalAmount = totalAmount;
  }

  public String getPaymentMethod() {
    return paymentMethod;
  }

  public void setPaymentMethod(String paymentMethod) {
    this.paymentMethod = paymentMethod;
  }

  public String getPaymentStatus() {
    return paymentStatus;
  }

  public void setPaymentStatus(String paymentStatus) {
    this.paymentStatus = paymentStatus;
  }

  public String getSpecialRequests() {
    return specialRequests;
  }

  public void setSpecialRequests(String specialRequests) {
    this.specialRequests = specialRequests;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getCancelledAt() {
    return cancelledAt;
  }

  public void setCancelledAt(Instant cancelledAt) {
    this.cancelledAt = cancelledAt;
  }

  public Integer getCancellationRefundAmount() {
    return cancellationRefundAmount;
  }

  public void setCancellationRefundAmount(Integer cancellationRefundAmount) {
    this.cancellationRefundAmount = cancellationRefundAmount;
  }

  public String getCancellationNote() {
    return cancellationNote;
  }

  public void setCancellationNote(String cancellationNote) {
    this.cancellationNote = cancellationNote;
  }
}
