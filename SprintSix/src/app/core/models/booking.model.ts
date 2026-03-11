export interface RoomSearchResult {
  roomId: string;
  roomType: 'Standard' | 'Deluxe' | 'Suite' | string;
  price: number;
  occupancyAdults: number;
  occupancyChildren: number;
  amenities: string[];
  roomSizeSqFt: number;
  imageUrl: string;
  available: boolean;
}

export interface BookingResponse {
  bookingId: string;
  invoiceId: string;
  transactionId: string;
  roomId: string;
  roomType: string;
  occupancyAdults: number;
  occupancyChildren: number;
  price: number;
  checkInDate: string;
  checkOutDate: string;
  nights: number;
  adults: number;
  children: number;
  basePrice: number;
  gstAmount: number;
  serviceChargeAmount: number;
  additionalServiceAmount: number;
  additionalServiceCount: number;
  grandTotalAmount: number;
  totalAmount: number;
  paymentMethod: string;
  status: 'Confirmed' | 'Cancelled' | string;
  createdAt: string;
  cancelledAt?: string | null;
  cancellationRefundAmount?: number | null;
  cancellationNote?: string | null;
}

export interface PaymentPayload {
  userId: string;
  customerName: string;
  customerEmail: string;
  customerMobile: string;
  roomId: string;
  roomType: string;
  checkInDate: string;
  checkOutDate: string;
  adults: number;
  children: number;
  paymentMethod: string;
  specialRequests?: string;
  cardholderName: string;
  cardNumber: string;
  expiryDate: string;
  cvv: string;
  otp: string;
  billingAddress?: string;
}

export interface InvoiceResponse {
  invoiceId: string;
  bookingId: string;
  transactionId: string;
  customerName: string;
  customerEmail: string;
  customerMobile: string;
  roomType: string;
  occupancyAdults: number;
  occupancyChildren: number;
  pricePerNight: number;
  checkInDate: string;
  checkOutDate: string;
  nights: number;
  adults: number;
  children: number;
  basePrice: number;
  gstAmount: number;
  serviceChargeAmount: number;
  additionalServiceAmount: number;
  additionalServiceCount: number;
  serviceRefundInitiatedAmount: number;
  netPayableAmount: number;
  serviceChargeDetails: InvoiceServiceChargeDetail[];
  grandTotalAmount: number;
  totalAmount: number;
  paymentMethod: string;
  invoiceDateTime: string;
  hotelName: string;
  hotelAddress: string;
  hotelEmail: string;
  hotelSupportNumber: string;
}

export interface InvoiceServiceChargeDetail {
  requestId: string;
  serviceType: string;
  status: string;
  amount: number;
  serviceDateTime: string;
  serviceSummary: string;
  serviceDetails: string;
  refundInitiatedAmount: number;
  refundNote: string;
}

export interface ModifyBookingPayload {
  userId: string;
  checkInDate: string;
  checkOutDate: string;
  adults: number;
  children: number;
  roomType: string;
  paymentMethod?: string;
}

export interface ModifyBookingPreviewResponse {
  bookingId: string;
  roomId: string;
  roomType: string;
  occupancyAdults: number;
  occupancyChildren: number;
  pricePerNight: number;
  oldTotalAmount: number;
  newTotalAmount: number;
  additionalAmount: number;
  refundAmount: number;
  paymentRequired: boolean;
  message: string;
}

export interface ModifyBookingConfirmPayload extends ModifyBookingPayload {
  paymentCompleted: boolean;
}

export interface CancellationPreviewResponse {
  cancellable: boolean;
  refundAmount: number;
  message: string;
}
