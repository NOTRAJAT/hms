import { Inject, Injectable, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { BehaviorSubject, Observable } from 'rxjs';

export interface BookingRecord {
  bookingId: string;
  invoiceId: string;
  transactionId: string;
  roomId: string;
  roomType: string;
  occupancyAdults: number;
  occupancyChildren: number;
  price: number;
  checkIn: string;
  checkOut: string;
  nights: number;
  adults: number;
  children: number;
  totalAmount: number;
  paymentMethod: string;
  status: 'Confirmed' | 'Cancelled';
  createdAt: string;
}

const STORAGE_KEY = 'hms_bookings';

@Injectable({ providedIn: 'root' })
export class BookingService {
  private readonly isBrowser: boolean;
  private readonly subject: BehaviorSubject<BookingRecord[]>;
  bookings$!: Observable<BookingRecord[]>;

  constructor(@Inject(PLATFORM_ID) platformId: object) {
    this.isBrowser = isPlatformBrowser(platformId);
    this.subject = new BehaviorSubject<BookingRecord[]>(this.load());
    this.bookings$ = this.subject.asObservable();
  }

  list(): BookingRecord[] {
    return this.subject.value;
  }

  add(record: BookingRecord): { ok: boolean; reason?: string } {
    const current = this.subject.value;
    const duplicate = current.some((item) =>
      item.roomId === record.roomId &&
      item.checkIn === record.checkIn &&
      item.checkOut === record.checkOut &&
      item.status === 'Confirmed'
    );
    if (duplicate) {
      return { ok: false, reason: 'Duplicate booking for the same room and dates.' };
    }
    const next: BookingRecord[] = [record, ...current];
    this.subject.next(next);
    this.save(next);
    return { ok: true };
  }

  cancel(bookingId: string): void {
    const next: BookingRecord[] = this.subject.value.map((item) =>
      item.bookingId === bookingId ? { ...item, status: 'Cancelled' as const } : item
    );
    this.subject.next(next);
    this.save(next);
  }

  private load(): BookingRecord[] {
    if (!this.isBrowser) {
      return [];
    }
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) {
      return [];
    }
    try {
      return JSON.parse(raw) as BookingRecord[];
    } catch {
      return [];
    }
  }

  private save(records: BookingRecord[]): void {
    if (!this.isBrowser) {
      return;
    }
    localStorage.setItem(STORAGE_KEY, JSON.stringify(records));
  }
}
