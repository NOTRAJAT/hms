import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AppConfigService } from './app-config.service';
import { RoomSearchResult } from '../models/booking.model';

export interface RoomSearchParams {
  checkInDate: string;
  checkOutDate: string;
  adults: number;
  children: number;
  roomType: string;
}

@Injectable({ providedIn: 'root' })
export class RoomService {
  private readonly baseUrl: string;

  constructor(
    private http: HttpClient,
    config: AppConfigService
  ) {
    this.baseUrl = `${config.apiBaseUrl.replace(/\/$/, '')}/rooms`;
  }

  search(params: RoomSearchParams): Observable<RoomSearchResult[]> {
    const query = new HttpParams()
      .set('checkInDate', params.checkInDate)
      .set('checkOutDate', params.checkOutDate)
      .set('adults', String(params.adults))
      .set('children', String(params.children))
      .set('roomType', params.roomType);

    return this.http.get<RoomSearchResult[]>(`${this.baseUrl}/search`, { params: query });
  }

  roomTypes(): Observable<string[]> {
    return this.http.get<string[]>(`${this.baseUrl}/types`);
  }
}
