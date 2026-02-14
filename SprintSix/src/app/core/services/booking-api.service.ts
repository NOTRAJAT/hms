import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AppConfigService } from './app-config.service';
import {
  BookingResponse,
  CancellationPreviewResponse,
  InvoiceResponse,
  ModifyBookingConfirmPayload,
  ModifyBookingPayload,
  ModifyBookingPreviewResponse,
  PaymentPayload
} from '../models/booking.model';

@Injectable({ providedIn: 'root' })
export class BookingApiService {
  private readonly baseUrl: string;

  constructor(
    private http: HttpClient,
    config: AppConfigService
  ) {
    this.baseUrl = `${config.apiBaseUrl.replace(/\/$/, '')}/bookings`;
  }

  pay(payload: PaymentPayload): Observable<BookingResponse> {
    return this.http.post<BookingResponse>(`${this.baseUrl}/pay`, payload);
  }

  list(userId: string): Observable<BookingResponse[]> {
    return this.http.get<BookingResponse[]>(`${this.baseUrl}?userId=${encodeURIComponent(userId)}`);
  }

  cancel(bookingId: string, userId: string): Observable<BookingResponse> {
    const encodedBookingId = encodeURIComponent(bookingId);
    const encodedUserId = encodeURIComponent(userId);
    return this.http.patch<BookingResponse>(`${this.baseUrl}/${encodedBookingId}/cancel?userId=${encodedUserId}`, {});
  }

  cancellationPreview(bookingId: string, userId: string): Observable<CancellationPreviewResponse> {
    const encodedBookingId = encodeURIComponent(bookingId);
    const encodedUserId = encodeURIComponent(userId);
    return this.http.get<CancellationPreviewResponse>(`${this.baseUrl}/${encodedBookingId}/cancel-preview?userId=${encodedUserId}`);
  }

  invoice(bookingId: string, userId: string): Observable<InvoiceResponse> {
    const encodedBookingId = encodeURIComponent(bookingId);
    const encodedUserId = encodeURIComponent(userId);
    return this.http.get<InvoiceResponse>(`${this.baseUrl}/${encodedBookingId}/invoice?userId=${encodedUserId}`);
  }

  previewModification(bookingId: string, payload: ModifyBookingPayload): Observable<ModifyBookingPreviewResponse> {
    return this.http.post<ModifyBookingPreviewResponse>(
      `${this.baseUrl}/${encodeURIComponent(bookingId)}/modify/preview`,
      payload
    );
  }

  confirmModification(bookingId: string, payload: ModifyBookingConfirmPayload): Observable<BookingResponse> {
    return this.http.post<BookingResponse>(
      `${this.baseUrl}/${encodeURIComponent(bookingId)}/modify/confirm`,
      payload
    );
  }
}
