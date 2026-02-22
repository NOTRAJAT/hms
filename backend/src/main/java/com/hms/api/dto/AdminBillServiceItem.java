package com.hms.api.dto;

import java.time.LocalDateTime;

public class AdminBillServiceItem {
  private LocalDateTime serviceDateTime;
  private String serviceType;
  private String description;
  private Integer quantity;
  private Integer unitPrice;
  private Integer taxPercent;
  private Integer discountPercent;

  public LocalDateTime getServiceDateTime() {
    return serviceDateTime;
  }

  public void setServiceDateTime(LocalDateTime serviceDateTime) {
    this.serviceDateTime = serviceDateTime;
  }

  public String getServiceType() {
    return serviceType;
  }

  public void setServiceType(String serviceType) {
    this.serviceType = serviceType;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Integer getQuantity() {
    return quantity;
  }

  public void setQuantity(Integer quantity) {
    this.quantity = quantity;
  }

  public Integer getUnitPrice() {
    return unitPrice;
  }

  public void setUnitPrice(Integer unitPrice) {
    this.unitPrice = unitPrice;
  }

  public Integer getTaxPercent() {
    return taxPercent;
  }

  public void setTaxPercent(Integer taxPercent) {
    this.taxPercent = taxPercent;
  }

  public Integer getDiscountPercent() {
    return discountPercent;
  }

  public void setDiscountPercent(Integer discountPercent) {
    this.discountPercent = discountPercent;
  }
}
