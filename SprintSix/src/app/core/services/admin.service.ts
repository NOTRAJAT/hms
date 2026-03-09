import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import {
  AdminBookingItem,
  AdminBookingPageResponse,
  AdminBookingQuery,
  AdminBookingCreatePayload,
  AdminBookingUpdatePayload,
  AdminDashboardSummary,
  AdminRoomPageResponse,
  AdminRoomCreatePayload,
  AdminRoomQuery,
  AdminRoomUpdatePayload,
  AdminRoomOccupancyGridResponse,
  AdminRoomOccupancyResponse,
  AdminRoomItem,
  AdminUserItem,
  AdminUserPageResponse,
  AdminUserQuery,
  AdminUserCreatePayload,
  AdminUserUpdatePayload,
  AdminUserCreateResult,
  AdminPasswordResetResult,
  AdminBillQuery,
  AdminBillPageResponse,
  AdminBillSummaryResponse,
  AdminBillItem,
  AdminBillCreatePayload,
  AdminBillUpdatePayload,
  AdminComplaintItem,
  AdminComplaintPageResponse,
  AdminComplaintQuery,
  AdminComplaintUpdatePayload,
  AdminServiceItem,
  AdminServicePageResponse,
  AdminServiceQuery
} from '../models/admin.model';
import { AppConfigService } from './app-config.service';
import { CustomerService } from './customer.service';

interface CsrfResponse {
  token?: string;
  headerName?: string;
}

function readCookie(name: string): string {
  if (typeof document === 'undefined') {
    return '';
  }
  const cookie = document.cookie
    .split('; ')
    .find((row) => row.startsWith(`${name}=`));
  return cookie ? decodeURIComponent(cookie.split('=')[1]) : '';
}

@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly baseUrl: string;

  constructor(
    private http: HttpClient,
    private customerService: CustomerService,
    config: AppConfigService
  ) {
    this.baseUrl = `${config.apiBaseUrl.replace(/\/$/, '')}/admin`;
  }

  dashboardSummary(): Observable<AdminDashboardSummary> {
    return this.http.get<AdminDashboardSummary>(`${this.baseUrl}/dashboard-summary`);
  }

  rooms(query: AdminRoomQuery = {}): Observable<AdminRoomPageResponse> {
    const params: Record<string, string> = {};
    Object.entries(query).forEach(([key, value]) => {
      if (value !== undefined && value !== null && String(value).trim() !== '') {
        params[key] = String(value);
      }
    });
    return this.http.get<AdminRoomPageResponse>(`${this.baseUrl}/rooms`, { params });
  }

  updateRoom(roomCode: string, payload: AdminRoomUpdatePayload): Observable<AdminRoomItem> {
    return this.withCsrf((headerName, token) =>
      this.http.put<AdminRoomItem>(`${this.baseUrl}/rooms/${encodeURIComponent(roomCode)}`, payload, {
        headers: { [headerName]: token }
      })
    );
  }

  addRoom(payload: AdminRoomCreatePayload): Observable<AdminRoomItem> {
    return this.withCsrf((headerName, token) =>
      this.http.post<AdminRoomItem>(`${this.baseUrl}/rooms`, payload, {
        headers: { [headerName]: token }
      })
    );
  }

  bulkUploadRooms(file: File): Observable<{ importedCount: number; message: string }> {
    const form = new FormData();
    form.append('file', file);
    return this.withCsrf((headerName, token) =>
      this.http.post<{ importedCount: number; message: string }>(`${this.baseUrl}/rooms/bulk`, form, {
        headers: { [headerName]: token }
      })
    );
  }

  downloadRoomTemplate(): Observable<HttpResponse<Blob>> {
    return this.http.get(`${this.baseUrl}/rooms/template`, {
      responseType: 'blob',
      observe: 'response'
    });
  }

  roomOccupancy(roomCode: string): Observable<AdminRoomOccupancyResponse> {
    return this.http.get<AdminRoomOccupancyResponse>(`${this.baseUrl}/rooms/occupancy`, {
      params: { roomCode }
    });
  }

  bookings(query: AdminBookingQuery = {}): Observable<AdminBookingPageResponse> {
    const params: Record<string, string> = {};
    Object.entries(query).forEach(([key, value]) => {
      if (value !== undefined && value !== null && String(value).trim() !== '') {
        params[key] = String(value);
      }
    });
    return this.http.get<AdminBookingPageResponse>(`${this.baseUrl}/bookings`, { params });
  }

  createBooking(payload: AdminBookingCreatePayload): Observable<AdminBookingItem> {
    return this.withCsrf((headerName, token) =>
      this.http.post<AdminBookingItem>(`${this.baseUrl}/bookings`, payload, {
        headers: { [headerName]: token }
      })
    );
  }

  updateBooking(bookingId: string, payload: AdminBookingUpdatePayload): Observable<AdminBookingItem> {
    return this.withCsrf((headerName, token) =>
      this.http.put<AdminBookingItem>(`${this.baseUrl}/bookings/${encodeURIComponent(bookingId)}`, payload, {
        headers: { [headerName]: token }
      })
    );
  }

  cancelBooking(bookingId: string): Observable<AdminBookingItem> {
    return this.withCsrf((headerName, token) =>
      this.http.post<AdminBookingItem>(`${this.baseUrl}/bookings/${encodeURIComponent(bookingId)}/cancel`, {}, {
        headers: { [headerName]: token }
      })
    );
  }

  bills(query: AdminBillQuery = {}): Observable<AdminBillPageResponse> {
    const params: Record<string, string> = {};
    Object.entries(query).forEach(([key, value]) => {
      if (value !== undefined && value !== null && String(value).trim() !== '') {
        params[key] = String(value);
      }
    });
    return this.http.get<AdminBillPageResponse>(`${this.baseUrl}/bills`, { params });
  }

  billSummary(query: AdminBillQuery = {}): Observable<AdminBillSummaryResponse> {
    const params: Record<string, string> = {};
    Object.entries(query).forEach(([key, value]) => {
      if (value !== undefined && value !== null && String(value).trim() !== '' && key !== 'page' && key !== 'size' && key !== 'sortBy' && key !== 'sortDir') {
        params[key] = String(value);
      }
    });
    return this.http.get<AdminBillSummaryResponse>(`${this.baseUrl}/bills/summary`, { params });
  }

  downloadBillsCsv(query: AdminBillQuery = {}): Observable<HttpResponse<Blob>> {
    const params: Record<string, string> = {};
    Object.entries(query).forEach(([key, value]) => {
      if (value !== undefined && value !== null && String(value).trim() !== '' && key !== 'page' && key !== 'size') {
        params[key] = String(value);
      }
    });
    return this.http.get(`${this.baseUrl}/bills/export`, {
      params,
      responseType: 'blob',
      observe: 'response'
    });
  }

  createBill(payload: AdminBillCreatePayload): Observable<AdminBillItem> {
    return this.withCsrf((headerName, token) =>
      this.http.post<AdminBillItem>(`${this.baseUrl}/bills`, payload, {
        headers: { [headerName]: token }
      })
    );
  }

  updateBill(billId: string, payload: AdminBillUpdatePayload): Observable<AdminBillItem> {
    return this.withCsrf((headerName, token) =>
      this.http.put<AdminBillItem>(`${this.baseUrl}/bills/${encodeURIComponent(billId)}`, payload, {
        headers: { [headerName]: token }
      })
    );
  }

  markBillPaid(billId: string): Observable<AdminBillItem> {
    return this.withCsrf((headerName, token) =>
      this.http.post<AdminBillItem>(`${this.baseUrl}/bills/${encodeURIComponent(billId)}/mark-paid`, {}, {
        headers: { [headerName]: token }
      })
    );
  }

  complaints(query: AdminComplaintQuery = {}): Observable<AdminComplaintPageResponse> {
    const params: Record<string, string> = {};
    Object.entries(query).forEach(([key, value]) => {
      if (value !== undefined && value !== null && String(value).trim() !== '') {
        params[key] = String(value);
      }
    });
    return this.http.get<AdminComplaintPageResponse>(`${this.baseUrl}/complaints`, { params });
  }

  complaintDetail(complaintId: string): Observable<AdminComplaintItem> {
    return this.http.get<AdminComplaintItem>(`${this.baseUrl}/complaints/${encodeURIComponent(complaintId)}`);
  }

  updateComplaint(complaintId: string, payload: AdminComplaintUpdatePayload): Observable<AdminComplaintItem> {
    return this.withCsrf((headerName, token) =>
      this.http.patch<AdminComplaintItem>(`${this.baseUrl}/complaints/${encodeURIComponent(complaintId)}`, payload, {
        headers: { [headerName]: token }
      })
    );
  }

  users(query: AdminUserQuery = {}): Observable<AdminUserPageResponse> {
    const params: Record<string, string> = {};
    Object.entries(query).forEach(([key, value]) => {
      if (value !== undefined && value !== null && String(value).trim() !== '') {
        params[key] = String(value);
      }
    });
    return this.http.get<AdminUserPageResponse>(`${this.baseUrl}/users`, { params });
  }

  createUser(payload: AdminUserCreatePayload): Observable<AdminUserCreateResult> {
    return this.withCsrf((headerName, token) =>
      this.http.post<AdminUserCreateResult>(`${this.baseUrl}/users`, payload, {
        headers: { [headerName]: token }
      })
    );
  }

  updateUser(userId: string, payload: AdminUserUpdatePayload): Observable<AdminUserItem> {
    return this.withCsrf((headerName, token) =>
      this.http.put<AdminUserItem>(`${this.baseUrl}/users/${encodeURIComponent(userId)}`, payload, {
        headers: { [headerName]: token }
      })
    );
  }

  updateUserStatus(userId: string, status: 'ACTIVE' | 'INACTIVE'): Observable<AdminUserItem> {
    return this.withCsrf((headerName, token) =>
      this.http.post<AdminUserItem>(`${this.baseUrl}/users/${encodeURIComponent(userId)}/status`, null, {
        headers: { [headerName]: token },
        params: { status }
      })
    );
  }

  resetUserPassword(userId: string): Observable<AdminPasswordResetResult> {
    return this.withCsrf((headerName, token) =>
      this.http.post<AdminPasswordResetResult>(`${this.baseUrl}/users/${encodeURIComponent(userId)}/reset-password`, null, {
        headers: { [headerName]: token }
      })
    );
  }

  roomOccupancyGrid(roomType: string, page = 0, pageSize = 5): Observable<AdminRoomOccupancyGridResponse> {
    return this.http.get<AdminRoomOccupancyGridResponse>(`${this.baseUrl}/rooms/occupancy-grid`, {
      params: {
        roomType,
        page,
        pageSize
      }
    });
  }

  services(query: AdminServiceQuery = {}): Observable<AdminServicePageResponse> {
    const params: Record<string, string> = {};
    Object.entries(query).forEach(([key, value]) => {
      if (value !== undefined && value !== null && String(value).trim() !== '') {
        params[key] = String(value);
      }
    });
    return this.http.get<AdminServicePageResponse>(`${this.baseUrl}/services`, { params });
  }

  updateServiceStatus(
    requestId: string,
    status: 'Requested' | 'Confirmed' | 'Completed' | 'Cancelled'
  ): Observable<AdminServiceItem> {
    return this.withCsrf((headerName, token) =>
      this.http.patch<AdminServiceItem>(`${this.baseUrl}/services/${encodeURIComponent(requestId)}/status`, { status }, {
        headers: { [headerName]: token }
      })
    );
  }

  private withCsrf<T>(next: (headerName: string, token: string) => Observable<T>): Observable<T> {
    return this.customerService.csrf().pipe(
      switchMap((csrf) => {
        const payload = csrf as CsrfResponse | null;
        const token = String(payload?.token ?? '').trim() || readCookie('XSRF-TOKEN');
        const headerName = String(payload?.headerName ?? 'X-XSRF-TOKEN').trim() || 'X-XSRF-TOKEN';
        return next(headerName, token);
      })
    );
  }
}
