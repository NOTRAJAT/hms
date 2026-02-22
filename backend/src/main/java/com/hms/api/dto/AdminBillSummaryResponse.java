package com.hms.api.dto;

public class AdminBillSummaryResponse {
  private final int totalRevenue;
  private final int invoiceRevenue;
  private final int manualBillRevenue;
  private final int billRoomRevenue;
  private final int roomRevenue;
  private final int serviceRevenue;
  private final int otherRevenue;
  private final int taxRevenue;
  private final int discountTotal;
  private final long billCount;

  public AdminBillSummaryResponse(
      int totalRevenue,
      int invoiceRevenue,
      int manualBillRevenue,
      int billRoomRevenue,
      int roomRevenue,
      int serviceRevenue,
      int otherRevenue,
      int taxRevenue,
      int discountTotal,
      long billCount
  ) {
    this.totalRevenue = totalRevenue;
    this.invoiceRevenue = invoiceRevenue;
    this.manualBillRevenue = manualBillRevenue;
    this.billRoomRevenue = billRoomRevenue;
    this.roomRevenue = roomRevenue;
    this.serviceRevenue = serviceRevenue;
    this.otherRevenue = otherRevenue;
    this.taxRevenue = taxRevenue;
    this.discountTotal = discountTotal;
    this.billCount = billCount;
  }

  public int getTotalRevenue() {
    return totalRevenue;
  }

  public int getInvoiceRevenue() {
    return invoiceRevenue;
  }

  public int getManualBillRevenue() {
    return manualBillRevenue;
  }

  public int getBillRoomRevenue() {
    return billRoomRevenue;
  }

  public int getRoomRevenue() {
    return roomRevenue;
  }

  public int getServiceRevenue() {
    return serviceRevenue;
  }

  public int getOtherRevenue() {
    return otherRevenue;
  }

  public int getTaxRevenue() {
    return taxRevenue;
  }

  public int getDiscountTotal() {
    return discountTotal;
  }

  public long getBillCount() {
    return billCount;
  }
}
