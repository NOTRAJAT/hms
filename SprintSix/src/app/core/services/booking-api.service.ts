import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { AppConfigService } from './app-config.service';
import { CustomerService } from './customer.service';
import {
  BookingResponse,
  CancellationPreviewResponse,
  InvoiceResponse,
  ModifyBookingConfirmPayload,
  ModifyBookingPayload,
  ModifyBookingPreviewResponse,
  PaymentPayload
} from '../models/booking.model';

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
export class BookingApiService {
  private readonly baseUrl: string;

  constructor(
    private http: HttpClient,
    private customerService: CustomerService,
    config: AppConfigService
  ) {
    this.baseUrl = `${config.apiBaseUrl.replace(/\/$/, '')}/bookings`;
  }

  pay(payload: PaymentPayload): Observable<BookingResponse> {
    return this.withCsrf((headerName, token) =>
      this.http.post<BookingResponse>(`${this.baseUrl}/pay`, payload, {
        headers: { [headerName]: token }
      })
    );
  }

  list(userId: string): Observable<BookingResponse[]> {
    return this.http.get<BookingResponse[]>(this.baseUrl);
  }

  cancel(bookingId: string, userId: string): Observable<BookingResponse> {
    const encodedBookingId = encodeURIComponent(bookingId);
    return this.withCsrf((headerName, token) =>
      this.http.patch<BookingResponse>(`${this.baseUrl}/${encodedBookingId}/cancel`, {}, {
        headers: { [headerName]: token }
      })
    );
  }

  cancellationPreview(bookingId: string, userId: string): Observable<CancellationPreviewResponse> {
    const encodedBookingId = encodeURIComponent(bookingId);
    return this.http.get<CancellationPreviewResponse>(`${this.baseUrl}/${encodedBookingId}/cancel-preview`);
  }

  invoice(bookingId: string, userId: string): Observable<InvoiceResponse> {
    const encodedBookingId = encodeURIComponent(bookingId);
    return this.http.get<InvoiceResponse>(`${this.baseUrl}/${encodedBookingId}/invoice`);
  }

  previewModification(bookingId: string, payload: ModifyBookingPayload): Observable<ModifyBookingPreviewResponse> {
    return this.withCsrf((headerName, token) =>
      this.http.post<ModifyBookingPreviewResponse>(
        `${this.baseUrl}/${encodeURIComponent(bookingId)}/modify/preview`,
        payload,
        { headers: { [headerName]: token } }
      )
    );
  }

  confirmModification(bookingId: string, payload: ModifyBookingConfirmPayload): Observable<BookingResponse> {
    return this.withCsrf((headerName, token) =>
      this.http.post<BookingResponse>(
        `${this.baseUrl}/${encodeURIComponent(bookingId)}/modify/confirm`,
        payload,
        { headers: { [headerName]: token } }
      )
    );
  }

  private withCsrf<T>(next: (headerName: string, token: string) => Observable<T>): Observable<T> {
    return this.customerService.csrf().pipe(
      switchMap((csrf) => {
        const payload = csrf as CsrfResponse | null;
        const token = String(payload?.token ?? '').trim() || readCookie('XSRF-TOKEN');
        const headerName = String(payload?.headerName ?? 'X-XSRF-TOKEN').trim();
        return next(headerName || 'X-XSRF-TOKEN', token);
      })
    );
  }
}
