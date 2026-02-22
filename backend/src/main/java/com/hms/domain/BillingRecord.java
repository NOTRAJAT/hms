package com.hms.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

@Entity
@Table(
    name = "billing_records",
    uniqueConstraints = { @UniqueConstraint(columnNames = "billId") }
)
public class BillingRecord {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String billId;

  @Column(nullable = false)
  private String customerUserId;

  @Column(nullable = false)
  private String customerName;

  @Column(nullable = false)
  private int roomCharges;

  @Column(nullable = false)
  private int serviceCharges;

  @Column(nullable = false)
  private int additionalFees;

  @Column(nullable = false)
  private int taxes;

  @Column(nullable = false)
  private int discounts;

  @Column(nullable = false)
  private int totalAmount;

  @Column(nullable = false)
  private String paymentStatus;

  @Column(nullable = false, length = 5000)
  private String serviceItemsJson;

  @Column(nullable = false)
  private Instant issueDate;

  @PrePersist
  void onCreate() {
    if (issueDate == null) {
      issueDate = Instant.now();
    }
  }

  public Long getId() {
    return id;
  }

  public String getBillId() {
    return billId;
  }

  public void setBillId(String billId) {
    this.billId = billId;
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

  public int getRoomCharges() {
    return roomCharges;
  }

  public void setRoomCharges(int roomCharges) {
    this.roomCharges = roomCharges;
  }

  public int getServiceCharges() {
    return serviceCharges;
  }

  public void setServiceCharges(int serviceCharges) {
    this.serviceCharges = serviceCharges;
  }

  public int getAdditionalFees() {
    return additionalFees;
  }

  public void setAdditionalFees(int additionalFees) {
    this.additionalFees = additionalFees;
  }

  public int getTaxes() {
    return taxes;
  }

  public void setTaxes(int taxes) {
    this.taxes = taxes;
  }

  public int getDiscounts() {
    return discounts;
  }

  public void setDiscounts(int discounts) {
    this.discounts = discounts;
  }

  public int getTotalAmount() {
    return totalAmount;
  }

  public void setTotalAmount(int totalAmount) {
    this.totalAmount = totalAmount;
  }

  public String getPaymentStatus() {
    return paymentStatus;
  }

  public void setPaymentStatus(String paymentStatus) {
    this.paymentStatus = paymentStatus;
  }

  public String getServiceItemsJson() {
    return serviceItemsJson;
  }

  public void setServiceItemsJson(String serviceItemsJson) {
    this.serviceItemsJson = serviceItemsJson;
  }

  public Instant getIssueDate() {
    return issueDate;
  }

  public void setIssueDate(Instant issueDate) {
    this.issueDate = issueDate;
  }
}

