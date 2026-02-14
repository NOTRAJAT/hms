package com.hms.api.dto;

public class ModifyBookingPreviewResponse {
  private String bookingId;
  private String roomId;
  private String roomType;
  private int occupancyAdults;
  private int occupancyChildren;
  private int pricePerNight;
  private int oldTotalAmount;
  private int newTotalAmount;
  private int additionalAmount;
  private int refundAmount;
  private boolean paymentRequired;
  private String message;

  public ModifyBookingPreviewResponse(
      String bookingId,
      String roomId,
      String roomType,
      int occupancyAdults,
      int occupancyChildren,
      int pricePerNight,
      int oldTotalAmount,
      int newTotalAmount,
      int additionalAmount,
      int refundAmount,
      boolean paymentRequired,
      String message
  ) {
    this.bookingId = bookingId;
    this.roomId = roomId;
    this.roomType = roomType;
    this.occupancyAdults = occupancyAdults;
    this.occupancyChildren = occupancyChildren;
    this.pricePerNight = pricePerNight;
    this.oldTotalAmount = oldTotalAmount;
    this.newTotalAmount = newTotalAmount;
    this.additionalAmount = additionalAmount;
    this.refundAmount = refundAmount;
    this.paymentRequired = paymentRequired;
    this.message = message;
  }

  public String getBookingId() {
    return bookingId;
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

  public int getPricePerNight() {
    return pricePerNight;
  }

  public int getOldTotalAmount() {
    return oldTotalAmount;
  }

  public int getNewTotalAmount() {
    return newTotalAmount;
  }

  public int getAdditionalAmount() {
    return additionalAmount;
  }

  public int getRefundAmount() {
    return refundAmount;
  }

  public boolean isPaymentRequired() {
    return paymentRequired;
  }

  public String getMessage() {
    return message;
  }
}
