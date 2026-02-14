package com.hms.api.dto;

import java.time.Instant;
import java.time.LocalDate;

public class BookingResponse {
  private String bookingId;
  private String invoiceId;
  private String transactionId;
  private String roomId;
  private String roomType;
  private int occupancyAdults;
  private int occupancyChildren;
  private int price;
  private LocalDate checkInDate;
  private LocalDate checkOutDate;
  private int nights;
  private int adults;
  private int children;
  private int basePrice;
  private int gstAmount;
  private int serviceChargeAmount;
  private int totalAmount;
  private String paymentMethod;
  private String status;
  private Instant createdAt;
  private Instant cancelledAt;
  private Integer cancellationRefundAmount;
  private String cancellationNote;

  public BookingResponse(
      String bookingId,
      String invoiceId,
      String transactionId,
      String roomId,
      String roomType,
      int occupancyAdults,
      int occupancyChildren,
      int price,
      LocalDate checkInDate,
      LocalDate checkOutDate,
      int nights,
      int adults,
      int children,
      int basePrice,
      int gstAmount,
      int serviceChargeAmount,
      int totalAmount,
      String paymentMethod,
      String status,
      Instant createdAt,
      Instant cancelledAt,
      Integer cancellationRefundAmount,
      String cancellationNote
  ) {
    this.bookingId = bookingId;
    this.invoiceId = invoiceId;
    this.transactionId = transactionId;
    this.roomId = roomId;
    this.roomType = roomType;
    this.occupancyAdults = occupancyAdults;
    this.occupancyChildren = occupancyChildren;
    this.price = price;
    this.checkInDate = checkInDate;
    this.checkOutDate = checkOutDate;
    this.nights = nights;
    this.adults = adults;
    this.children = children;
    this.basePrice = basePrice;
    this.gstAmount = gstAmount;
    this.serviceChargeAmount = serviceChargeAmount;
    this.totalAmount = totalAmount;
    this.paymentMethod = paymentMethod;
    this.status = status;
    this.createdAt = createdAt;
    this.cancelledAt = cancelledAt;
    this.cancellationRefundAmount = cancellationRefundAmount;
    this.cancellationNote = cancellationNote;
  }

  public String getBookingId() {
    return bookingId;
  }

  public String getInvoiceId() {
    return invoiceId;
  }

  public String getTransactionId() {
    return transactionId;
  }

  public String getRoomId() {
    return roomId;
  }

  public String getRoomType() {
    return roomType;
  }

  public int getOccupancyAdults() {
    return occupancyAdults;
  }

  public int getOccupancyChildren() {
    return occupancyChildren;
  }

  public int getPrice() {
    return price;
  }

  public LocalDate getCheckInDate() {
    return checkInDate;
  }

  public LocalDate getCheckOutDate() {
    return checkOutDate;
  }

  public int getNights() {
    return nights;
  }

  public int getAdults() {
    return adults;
  }

  public int getChildren() {
    return children;
  }

  public int getBasePrice() {
    return basePrice;
  }

  public int getGstAmount() {
    return gstAmount;
  }

  public int getServiceChargeAmount() {
    return serviceChargeAmount;
  }

  public int getTotalAmount() {
    return totalAmount;
  }

  public String getPaymentMethod() {
    return paymentMethod;
  }

  public String getStatus() {
    return status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getCancelledAt() {
    return cancelledAt;
  }

  public Integer getCancellationRefundAmount() {
    return cancellationRefundAmount;
  }

  public String getCancellationNote() {
    return cancellationNote;
  }
}
