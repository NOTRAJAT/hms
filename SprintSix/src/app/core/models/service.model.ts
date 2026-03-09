export interface CabDestinationOption {
  destination: string;
  fare: number;
}

export interface SalonPackageOption {
  packageCode: string;
  packageName: string;
  price: number;
  durationMinutes: number;
}

export interface DiningMenuOption {
  itemCode: string;
  itemName: string;
  price: number;
}

export interface ServiceCatalogResponse {
  cabDestinations: CabDestinationOption[];
  salonPackages: SalonPackageOption[];
  diningMenu: DiningMenuOption[];
}

export interface ServiceTransaction {
  requestId: string;
  bookingId: string;
  customerUserId: string;
  customerName: string;
  serviceType: 'Cab' | 'Salon' | 'Dining' | string;
  status: 'Requested' | 'Confirmed' | 'Completed' | 'Cancelled' | string;
  amount: number;
  paymentStatus: 'PAID' | 'PENDING' | string;
  paymentMethod: string;
  transactionId: string;
  serviceDateTime: string;
  serviceSummary: string;
  serviceDetails: string;
  createdAt: string;
  updatedAt: string;
}

export interface ServicePaymentDetails {
  paymentMethod: string;
  cardholderName: string;
  cardNumber: string;
  expiryDate: string;
  cvv: string;
  otp: string;
  billingAddress?: string;
}

export interface CabServiceCreatePayload extends ServicePaymentDetails {
  bookingId: string;
  destination: string;
  pickupDateTime: string;
}

export interface SalonServiceCreatePayload extends ServicePaymentDetails {
  bookingId: string;
  packageCode: string;
  slotDateTime: string;
}

export interface DiningServiceItemPayload {
  itemCode: string;
  quantity: number;
}

export interface DiningServiceCreatePayload extends ServicePaymentDetails {
  bookingId: string;
  deliveryDateTime: string;
  items: DiningServiceItemPayload[];
  specialInstructions?: string;
}
