package com.hms.api.dto;

public class InvoiceServiceChargeDetail {
  private final String requestId;
  private final String serviceType;
  private final String status;
  private final int amount;
  private final String serviceDateTime;
  private final String serviceSummary;
  private final String serviceDetails;
  private final int refundInitiatedAmount;
  private final String refundNote;

  public InvoiceServiceChargeDetail(
      String requestId,
      String serviceType,
      String status,
      int amount,
      String serviceDateTime,
      String serviceSummary,
      String serviceDetails,
      int refundInitiatedAmount,
      String refundNote
  ) {
    this.requestId = requestId;
    this.serviceType = serviceType;
    this.status = status;
    this.amount = amount;
    this.serviceDateTime = serviceDateTime;
    this.serviceSummary = serviceSummary;
    this.serviceDetails = serviceDetails;
    this.refundInitiatedAmount = refundInitiatedAmount;
    this.refundNote = refundNote;
  }

  public String getRequestId() {
    return requestId;
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

  public String getServiceDateTime() {
    return serviceDateTime;
  }

  public String getServiceSummary() {
    return serviceSummary;
  }

  public String getServiceDetails() {
    return serviceDetails;
  }

  public int getRefundInitiatedAmount() {
    return refundInitiatedAmount;
  }

  public String getRefundNote() {
    return refundNote;
  }
}
