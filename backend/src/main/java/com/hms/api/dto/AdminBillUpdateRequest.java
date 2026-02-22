package com.hms.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class AdminBillUpdateRequest {
  @NotNull
  @Min(0)
  private Integer roomCharges;

  @NotNull
  @Min(0)
  @Max(100)
  private Integer additionalFees;

  @NotNull
  @Min(0)
  @Max(100)
  private Integer taxes;

  @NotNull
  @Min(0)
  @Max(100)
  private Integer discounts;

  @NotNull
  @Min(0)
  private Integer totalAmountDue;

  private List<AdminBillServiceItem> serviceItems;

  public Integer getRoomCharges() {
    return roomCharges;
  }

  public void setRoomCharges(Integer roomCharges) {
    this.roomCharges = roomCharges;
  }

  public Integer getAdditionalFees() {
    return additionalFees;
  }

  public void setAdditionalFees(Integer additionalFees) {
    this.additionalFees = additionalFees;
  }

  public Integer getTaxes() {
    return taxes;
  }

  public void setTaxes(Integer taxes) {
    this.taxes = taxes;
  }

  public Integer getDiscounts() {
    return discounts;
  }

  public void setDiscounts(Integer discounts) {
    this.discounts = discounts;
  }

  public Integer getTotalAmountDue() {
    return totalAmountDue;
  }

  public void setTotalAmountDue(Integer totalAmountDue) {
    this.totalAmountDue = totalAmountDue;
  }

  public List<AdminBillServiceItem> getServiceItems() {
    return serviceItems;
  }

  public void setServiceItems(List<AdminBillServiceItem> serviceItems) {
    this.serviceItems = serviceItems;
  }
}
