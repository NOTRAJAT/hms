package com.hms.api.dto;

import java.time.Instant;
import java.util.List;

public class AdminBillResponse {
  private final String billId;
  private final String bookingId;
  private final String customerUserId;
  private final String customerName;
  private final Instant issueDate;
  private final int roomCharges;
  private final int serviceCharges;
  private final int additionalFees;
  private final int taxes;
  private final int discounts;
  private final int totalAmount;
  private final String paymentStatus;
  private final boolean editable;
  private final List<AdminBillServiceItem> serviceItems;

  public AdminBillResponse(
      String billId,
      String bookingId,
      String customerUserId,
      String customerName,
      Instant issueDate,
      int roomCharges,
      int serviceCharges,
      int additionalFees,
      int taxes,
      int discounts,
      int totalAmount,
      String paymentStatus,
      boolean editable,
      List<AdminBillServiceItem> serviceItems
  ) {
    this.billId = billId;
    this.bookingId = bookingId;
    this.customerUserId = customerUserId;
    this.customerName = customerName;
    this.issueDate = issueDate;
    this.roomCharges = roomCharges;
    this.serviceCharges = serviceCharges;
    this.additionalFees = additionalFees;
    this.taxes = taxes;
    this.discounts = discounts;
    this.totalAmount = totalAmount;
    this.paymentStatus = paymentStatus;
    this.editable = editable;
    this.serviceItems = serviceItems;
  }

  public String getBillId() {
    return billId;
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

  public Instant getIssueDate() {
    return issueDate;
  }

  public int getRoomCharges() {
    return roomCharges;
  }

  public int getServiceCharges() {
    return serviceCharges;
  }

  public int getAdditionalFees() {
    return additionalFees;
  }

  public int getTaxes() {
    return taxes;
  }

  public int getDiscounts() {
    return discounts;
  }

  public int getTotalAmount() {
    return totalAmount;
  }

  public String getPaymentStatus() {
    return paymentStatus;
  }

  public boolean isEditable() {
    return editable;
  }

  public List<AdminBillServiceItem> getServiceItems() {
    return serviceItems;
  }
}
