package com.hms.api.dto;

public class CancellationPreviewResponse {
  private final boolean cancellable;
  private final int refundAmount;
  private final String message;

  public CancellationPreviewResponse(boolean cancellable, int refundAmount, String message) {
    this.cancellable = cancellable;
    this.refundAmount = refundAmount;
    this.message = message;
  }

  public boolean isCancellable() {
    return cancellable;
  }

  public int getRefundAmount() {
    return refundAmount;
  }

  public String getMessage() {
    return message;
  }
}
