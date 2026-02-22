import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CustomerRegistrationPayload, CustomerRegistrationResult } from '../models/customer.model';
import { LoginPayload, LoginResult } from '../models/auth.model';
import { AppConfigService } from './app-config.service';

export interface CustomerProfileUpdate {
  name: string;
  email: string;
  mobile: string;
  address: string;
}

export interface ChangePasswordPayload {
  currentPassword: string;
  newPassword: string;
}

@Injectable({ providedIn: 'root' })
export class CustomerService {
  private readonly baseUrl: string;

  constructor(
    private http: HttpClient,
    config: AppConfigService
  ) {
    this.baseUrl = `${config.apiBaseUrl.replace(/\/$/, '')}/customers`;
  }

  register(payload: CustomerRegistrationPayload): Observable<CustomerRegistrationResult> {
    return this.http.post<CustomerRegistrationResult>(`${this.baseUrl}/register`, payload);
  }

  login(payload: LoginPayload): Observable<LoginResult> {
    return this.http.post<LoginResult>(`${this.baseUrl}/login`, payload);
  }

  logout(): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/logout`, {});
  }

  me(): Observable<LoginResult> {
    return this.http.get<LoginResult>(`${this.baseUrl}/me`);
  }

  csrf(): Observable<unknown> {
    const authBase = this.baseUrl.replace(/\/customers$/, '/auth');
    return this.http.get(`${authBase}/csrf`);
  }

  updateProfile(userId: string, payload: CustomerProfileUpdate): Observable<LoginResult> {
    return this.http.put<LoginResult>(`${this.baseUrl}/${userId}`, payload);
  }

  changePassword(payload: ChangePasswordPayload): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/change-password`, payload);
  }
}
