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

  updateProfile(userId: string, payload: CustomerProfileUpdate): Observable<LoginResult> {
    return this.http.put<LoginResult>(`${this.baseUrl}/${userId}`, payload);
  }
}
