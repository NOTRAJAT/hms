import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ComplaintPayload, ComplaintRecord } from '../models/complaint.model';
import { AppConfigService } from './app-config.service';

@Injectable({ providedIn: 'root' })
export class ComplaintService {
  private readonly baseUrl: string;

  constructor(
    private http: HttpClient,
    config: AppConfigService
  ) {
    this.baseUrl = `${config.apiBaseUrl.replace(/\/$/, '')}/complaints`;
  }

  list(userId: string): Observable<ComplaintRecord[]> {
    return this.http.get<ComplaintRecord[]>(`${this.baseUrl}?userId=${encodeURIComponent(userId)}`);
  }

  detail(complaintId: string, userId: string): Observable<ComplaintRecord> {
    return this.http.get<ComplaintRecord>(`${this.baseUrl}/${encodeURIComponent(complaintId)}?userId=${encodeURIComponent(userId)}`);
  }

  create(payload: ComplaintPayload): Observable<ComplaintRecord> {
    return this.http.post<ComplaintRecord>(this.baseUrl, payload);
  }

  update(complaintId: string, payload: ComplaintPayload): Observable<ComplaintRecord> {
    return this.http.put<ComplaintRecord>(`${this.baseUrl}/${encodeURIComponent(complaintId)}`, payload);
  }

  confirmResolution(complaintId: string, userId: string): Observable<ComplaintRecord> {
    return this.http.patch<ComplaintRecord>(
      `${this.baseUrl}/${encodeURIComponent(complaintId)}/confirm?userId=${encodeURIComponent(userId)}`,
      {}
    );
  }

  reopen(complaintId: string, userId: string): Observable<ComplaintRecord> {
    return this.http.patch<ComplaintRecord>(
      `${this.baseUrl}/${encodeURIComponent(complaintId)}/reopen?userId=${encodeURIComponent(userId)}`,
      {}
    );
  }
}
