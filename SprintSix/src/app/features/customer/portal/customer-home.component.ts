import { Component, OnInit } from '@angular/core';
import { DatePipe, NgClass, NgFor, NgIf } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthSessionService } from '../../../core/services/auth-session.service';
import { BookingApiService } from '../../../core/services/booking-api.service';
import { BookingResponse } from '../../../core/models/booking.model';

@Component({
  selector: 'app-customer-home',
  standalone: true,
  imports: [RouterLink, NgIf, NgFor, NgClass, DatePipe],
  templateUrl: './customer-home.component.html'
})
export class CustomerHomeComponent implements OnInit {
  bookings: BookingResponse[] = [];
  isLoadingBookings = false;
  bookingError = '';

  constructor(
    public session: AuthSessionService,
    private bookingApi: BookingApiService
  ) {}

  ngOnInit(): void {
    this.loadBookings();
  }

  get hasBookings(): boolean {
    return this.bookings.length > 0;
  }

  get bookingSnapshot(): BookingResponse[] {
    return this.bookings.slice(0, 3);
  }

  get upcomingCount(): number {
    const today = this.toDateOnly(new Date());
    return this.bookings.filter((booking) => {
      if (String(booking.status).toLowerCase() === 'cancelled') {
        return false;
      }
      const checkOut = this.toDateOnly(this.parseDate(booking.checkOutDate));
      return checkOut >= today;
    }).length;
  }

  get pastCount(): number {
    const today = this.toDateOnly(new Date());
    return this.bookings.filter((booking) => {
      const checkOut = this.toDateOnly(this.parseDate(booking.checkOutDate));
      return checkOut < today;
    }).length;
  }

  get cancelledCount(): number {
    return this.bookings.filter((booking) => String(booking.status).toLowerCase() === 'cancelled').length;
  }

  private loadBookings(): void {
    const userId = this.session.value?.userId;
    if (!userId) {
      return;
    }
    this.isLoadingBookings = true;
    this.bookingError = '';
    this.bookingApi.list(userId).subscribe({
      next: (items) => {
        this.bookings = Array.isArray(items) ? items : [];
        this.isLoadingBookings = false;
      },
      error: () => {
        this.bookings = [];
        this.bookingError = 'Unable to load your booking snapshot right now.';
        this.isLoadingBookings = false;
      }
    });
  }

  private parseDate(value: string): Date {
    const raw = String(value ?? '').slice(0, 10);
    return new Date(`${raw}T00:00:00`);
  }

  private toDateOnly(date: Date): number {
    const next = new Date(date);
    next.setHours(0, 0, 0, 0);
    return next.getTime();
  }
}
