import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { AppConfigService } from './app-config.service';
import { CustomerService } from './customer.service';
import {
  CabServiceCreatePayload,
  DiningServiceCreatePayload,
  SalonServiceCreatePayload,
  ServiceCatalogResponse,
  ServiceTransaction
} from '../models/service.model';

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
export class ServiceRequestApiService {
  private readonly baseUrl: string;

  constructor(
    private http: HttpClient,
    private customerService: CustomerService,
    config: AppConfigService
  ) {
    this.baseUrl = `${config.apiBaseUrl.replace(/\/$/, '')}/services`;
  }

  catalog(): Observable<ServiceCatalogResponse> {
    return this.http.get<ServiceCatalogResponse>(`${this.baseUrl}/catalog`);
  }

  createCab(payload: CabServiceCreatePayload): Observable<ServiceTransaction> {
    return this.withCsrf((headerName, token) =>
      this.http.post<ServiceTransaction>(`${this.baseUrl}/cab`, payload, {
        headers: { [headerName]: token }
      })
    );
  }

  createSalon(payload: SalonServiceCreatePayload): Observable<ServiceTransaction> {
    return this.withCsrf((headerName, token) =>
      this.http.post<ServiceTransaction>(`${this.baseUrl}/salon`, payload, {
        headers: { [headerName]: token }
      })
    );
  }

  createDining(payload: DiningServiceCreatePayload): Observable<ServiceTransaction> {
    return this.withCsrf((headerName, token) =>
      this.http.post<ServiceTransaction>(`${this.baseUrl}/dining`, payload, {
        headers: { [headerName]: token }
      })
    );
  }

  my(bookingId?: string): Observable<ServiceTransaction[]> {
    const params: Record<string, string> = {};
    if (bookingId && bookingId.trim()) {
      params['bookingId'] = bookingId.trim();
    }
    return this.http.get<ServiceTransaction[]>(`${this.baseUrl}/my`, { params });
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
