export interface AdminDashboardSummary {
  dailyBookings: number;
  weeklyBookings: number;
  monthlyBookings: number;
  availableRooms: number;
}

export interface AdminRoomItem {
  roomCode: string;
  roomType: string;
  bedType: string;
  pricePerNight: number;
  occupancyAdults: number;
  occupancyChildren: number;
  maxOccupancy: number;
  amenitiesCsv: string;
  availabilityStatus: 'AVAILABLE' | 'NOT_AVAILABLE';
  roomStatus: 'AVAILABLE' | 'OCCUPIED' | 'UNDER_MAINTENANCE' | 'DEPRECATED';
  description: string;
  active: boolean;
}

export interface AdminRoomPageResponse {
  items: AdminRoomItem[];
  page: number;
  size: number;
  totalItems: number;
  totalPages: number;
}

export interface AdminRoomQuery {
  q?: string;
  roomType?: string;
  priceMin?: number;
  priceMax?: number;
  availability?: 'AVAILABLE' | 'NOT_AVAILABLE' | '';
  amenity?: string;
  maxOccupancy?: number;
  date?: string;
  sortBy?: string;
  sortDir?: 'asc' | 'desc';
  page?: number;
  size?: number;
}

export interface AdminRoomUpdatePayload {
  bedType: string;
  pricePerNight: number;
  roomStatus: 'AVAILABLE' | 'OCCUPIED' | 'UNDER_MAINTENANCE' | 'DEPRECATED';
  amenitiesCsv: string;
  occupancyAdults: number;
  occupancyChildren: number;
}

export interface AdminRoomCreatePayload {
  roomType: string;
  bedType: string;
  pricePerNight: number;
  amenitiesCsv: string;
  availability: 'Available' | 'Not Available';
  occupancyAdults: number;
  occupancyChildren: number;
  description: string;
}

export interface AdminBookingItem {
  bookingId: string;
  customerName: string;
  customerEmail: string;
  customerMobile: string;
  customerUserId: string;
  roomCode: string;
  roomType: string;
  checkInDate: string;
  checkOutDate: string;
  adults: number;
  children: number;
  status: string;
  totalAmount: number;
  paymentMethod: string;
  specialRequests: string;
  createdAt: string;
}

export interface AdminBookingPageResponse {
  items: AdminBookingItem[];
  page: number;
  size: number;
  totalItems: number;
  totalPages: number;
}

export interface AdminBookingQuery {
  q?: string;
  roomCode?: string;
  status?: string;
  roomType?: string;
  bookingDate?: string;
  fromDate?: string;
  toDate?: string;
  sortBy?: string;
  sortDir?: 'asc' | 'desc';
  page?: number;
  size?: number;
}

export interface AdminBookingCreatePayload {
  customerUserId?: string;
  customerName: string;
  customerEmail: string;
  customerMobile: string;
  checkInDate: string;
  checkOutDate: string;
  adults: number;
  children: number;
  roomType: string;
  roomCode?: string;
  paymentMethod: string;
  depositAmount?: number;
  specialRequests?: string;
}

export interface AdminBookingUpdatePayload {
  checkInDate: string;
  checkOutDate: string;
  adults: number;
  children: number;
  roomCode: string;
  specialRequests?: string;
}

export interface AdminUserItem {
  userId: string;
  name: string;
  username: string;
  email: string;
  mobile: string;
  locked: boolean;
  role: 'ADMIN' | 'CUSTOMER' | 'STAFF';
  status: 'ACTIVE' | 'INACTIVE';
  department: string;
}

export interface AdminUserPageResponse {
  items: AdminUserItem[];
  page: number;
  size: number;
  totalItems: number;
  totalPages: number;
}

export interface AdminUserQuery {
  q?: string;
  role?: string;
  status?: string;
  sortBy?: string;
  sortDir?: 'asc' | 'desc';
  page?: number;
  size?: number;
}

export interface AdminUserCreatePayload {
  username: string;
  role: 'CUSTOMER' | 'STAFF';
  email: string;
  mobile: string;
  name?: string;
  department?: string;
}

export interface AdminUserUpdatePayload {
  role: 'ADMIN' | 'CUSTOMER' | 'STAFF';
  email: string;
  mobile: string;
  status: 'ACTIVE' | 'INACTIVE';
  department?: string;
}

export interface AdminUserCreateResult {
  user: AdminUserItem;
  temporaryPassword: string;
  message: string;
}

export interface AdminPasswordResetResult {
  userId: string;
  username: string;
  temporaryPassword: string;
  message: string;
}

export interface AdminBillItem {
  billId: string;
  bookingId: string;
  customerUserId: string;
  customerName: string;
  issueDate: string;
  roomCharges: number;
  serviceCharges: number;
  additionalFees: number;
  taxes: number;
  discounts: number;
  totalAmount: number;
  paymentStatus: 'PAID' | 'PENDING';
  editable: boolean;
  serviceItems: AdminBillServiceItem[];
}

export interface AdminBillPageResponse {
  items: AdminBillItem[];
  page: number;
  size: number;
  totalItems: number;
  totalPages: number;
}

export interface AdminBillSummaryResponse {
  totalRevenue: number;
  invoiceRevenue: number;
  manualBillRevenue: number;
  billRoomRevenue: number;
  roomRevenue: number;
  serviceRevenue: number;
  otherRevenue: number;
  taxRevenue: number;
  discountTotal: number;
  billCount: number;
}

export interface AdminComplaintActionItem {
  actionAt: string;
  actionType: string;
  actorUserId: string;
  fromStatus: string;
  toStatus: string;
  assignedStaffMember: string;
  assignedDepartment: string;
  actionDetails: string;
}

export interface AdminComplaintItem {
  complaintId: string;
  customerUserId: string;
  customerName: string;
  bookingId: string;
  submissionDate: string;
  category: string;
  title: string;
  description: string;
  contactPreference: string;
  priorityLevel: 'High' | 'Medium' | 'Low' | string;
  currentStatus: 'Open' | 'In Progress' | 'Escalated' | 'Resolved' | 'Closed';
  newAssignment: boolean;
  assignedStaffMember: string;
  assignedDepartment: string;
  expectedResolutionDate: string;
  supportResponse: string;
  resolutionNotes: string;
  actions: AdminComplaintActionItem[];
}

export interface AdminComplaintPageResponse {
  items: AdminComplaintItem[];
  page: number;
  size: number;
  totalItems: number;
  totalPages: number;
}

export interface AdminComplaintQuery {
  q?: string;
  category?: string;
  priority?: string;
  status?: string;
  assignedTo?: string;
  fromDate?: string;
  toDate?: string;
  sortBy?: string;
  sortDir?: 'asc' | 'desc';
  page?: number;
  size?: number;
}

export interface AdminComplaintUpdatePayload {
  assignedStaffMember?: string;
  assignedDepartment?: string;
  status?: string;
  supportResponse?: string;
  resolutionNotes?: string;
  actionDetails?: string;
}

export interface AdminBillQuery {
  q?: string;
  paymentStatus?: 'PAID' | 'PENDING' | '';
  fromDate?: string;
  toDate?: string;
  sortBy?: string;
  sortDir?: 'asc' | 'desc';
  page?: number;
  size?: number;
}

export interface AdminBillServiceItem {
  serviceDateTime: string;
  serviceType: string;
  description: string;
  quantity: number;
  unitPrice: number;
  taxPercent: number;
  discountPercent: number;
}

export interface AdminBillCreatePayload {
  customerUserId: string;
  roomCharges: number;
  additionalFees: number;
  taxes: number;
  discounts: number;
  totalAmountDue: number;
  serviceItems: AdminBillServiceItem[];
}

export interface AdminBillUpdatePayload {
  roomCharges: number;
  additionalFees: number;
  taxes: number;
  discounts: number;
  totalAmountDue: number;
  serviceItems: AdminBillServiceItem[];
}

export interface AdminRoomOccupancyPoint {
  date: string;
  occupied: boolean;
}

export interface AdminRoomOccupancyResponse {
  roomCode: string;
  points: AdminRoomOccupancyPoint[];
}

export interface AdminRoomOccupancyGridRow {
  roomCode: string;
  occupied: boolean[];
}

export interface AdminRoomOccupancyGridResponse {
  roomType: string;
  page: number;
  pageSize: number;
  totalRooms: number;
  totalPages: number;
  dates: string[];
  rows: AdminRoomOccupancyGridRow[];
}

export interface AdminServiceItem {
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

export interface AdminServicePageResponse {
  items: AdminServiceItem[];
  page: number;
  size: number;
  totalItems: number;
  totalPages: number;
}

export interface AdminServiceQuery {
  q?: string;
  serviceType?: string;
  status?: string;
  bookingId?: string;
  customer?: string;
  page?: number;
  size?: number;
}
