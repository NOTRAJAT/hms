package com.hms.api.dto;

import java.util.List;

public class InvoiceResponse {
  private String invoiceId;
  private String bookingId;
  private String transactionId;
  private String customerName;
  private String customerEmail;
  private String customerMobile;
  private String roomType;
  private int occupancyAdults;
  private int occupancyChildren;
  private int pricePerNight;
  private String checkInDate;
  private String checkOutDate;
  private int nights;
  private int adults;
  private int children;
  private int basePrice;
  private int gstAmount;
  private int serviceChargeAmount;
  private int additionalServiceAmount;
  private int additionalServiceCount;
  private int serviceRefundInitiatedAmount;
  private int netPayableAmount;
  private List<InvoiceServiceChargeDetail> serviceChargeDetails;
  private int grandTotalAmount;
  private int totalAmount;
  private String paymentMethod;
  private String invoiceDateTime;
  private String hotelName;
  private String hotelAddress;
  private String hotelEmail;
  private String hotelSupportNumber;

  public InvoiceResponse(
      String invoiceId,
      String bookingId,
      String transactionId,
      String customerName,
      String customerEmail,
      String customerMobile,
      String roomType,
      int occupancyAdults,
      int occupancyChildren,
      int pricePerNight,
      String checkInDate,
      String checkOutDate,
      int nights,
      int adults,
      int children,
      int basePrice,
      int gstAmount,
      int serviceChargeAmount,
      int additionalServiceAmount,
      int additionalServiceCount,
      int serviceRefundInitiatedAmount,
      int netPayableAmount,
      List<InvoiceServiceChargeDetail> serviceChargeDetails,
      int grandTotalAmount,
      int totalAmount,
      String paymentMethod,
      String invoiceDateTime,
      String hotelName,
      String hotelAddress,
      String hotelEmail,
      String hotelSupportNumber
  ) {
    this.invoiceId = invoiceId;
    this.bookingId = bookingId;
    this.transactionId = transactionId;
    this.customerName = customerName;
    this.customerEmail = customerEmail;
    this.customerMobile = customerMobile;
    this.roomType = roomType;
    this.occupancyAdults = occupancyAdults;
    this.occupancyChildren = occupancyChildren;
    this.pricePerNight = pricePerNight;
    this.checkInDate = checkInDate;
    this.checkOutDate = checkOutDate;
    this.nights = nights;
    this.adults = adults;
    this.children = children;
    this.basePrice = basePrice;
    this.gstAmount = gstAmount;
    this.serviceChargeAmount = serviceChargeAmount;
    this.additionalServiceAmount = additionalServiceAmount;
    this.additionalServiceCount = additionalServiceCount;
    this.serviceRefundInitiatedAmount = serviceRefundInitiatedAmount;
    this.netPayableAmount = netPayableAmount;
    this.serviceChargeDetails = serviceChargeDetails;
    this.grandTotalAmount = grandTotalAmount;
    this.totalAmount = totalAmount;
    this.paymentMethod = paymentMethod;
    this.invoiceDateTime = invoiceDateTime;
    this.hotelName = hotelName;
    this.hotelAddress = hotelAddress;
    this.hotelEmail = hotelEmail;
    this.hotelSupportNumber = hotelSupportNumber;
  }

  public String getInvoiceId() {
    return invoiceId;
  }

  public String getBookingId() {
    return bookingId;
  }

  public String getTransactionId() {
    return transactionId;
  }

  public String getCustomerName() {
    return customerName;
  }

  public String getCustomerEmail() {
    return customerEmail;
  }

  public String getCustomerMobile() {
    return customerMobile;
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

  public String getCheckInDate() {
    return checkInDate;
  }

  public String getCheckOutDate() {
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

  public int getAdditionalServiceAmount() {
    return additionalServiceAmount;
  }

  public int getAdditionalServiceCount() {
    return additionalServiceCount;
  }

  public int getServiceRefundInitiatedAmount() {
    return serviceRefundInitiatedAmount;
  }

  public int getNetPayableAmount() {
    return netPayableAmount;
  }

  public List<InvoiceServiceChargeDetail> getServiceChargeDetails() {
    return serviceChargeDetails;
  }

  public int getGrandTotalAmount() {
    return grandTotalAmount;
  }

  public int getTotalAmount() {
    return totalAmount;
  }

  public String getPaymentMethod() {
    return paymentMethod;
  }

  public String getInvoiceDateTime() {
    return invoiceDateTime;
  }

  public String getHotelName() {
    return hotelName;
  }

  public String getHotelAddress() {
    return hotelAddress;
  }

  public String getHotelEmail() {
    return hotelEmail;
  }

  public String getHotelSupportNumber() {
    return hotelSupportNumber;
  }
}
