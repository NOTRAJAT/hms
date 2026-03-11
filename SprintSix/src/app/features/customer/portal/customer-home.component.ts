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
  bookingCarouselIndex = 0;
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

  get currentBookings(): BookingResponse[] {
    const today = this.toDateOnly(new Date());
    const confirmed = this.bookings
      .filter((booking) => String(booking.status ?? '').trim().toLowerCase() === 'confirmed')
      .filter((booking) => this.toDateOnly(this.parseDate(booking.checkOutDate)) >= today)
      .sort((a, b) => this.toDateOnly(this.parseDate(a.checkInDate)) - this.toDateOnly(this.parseDate(b.checkInDate)));

    const seen = new Set<string>();
    return confirmed.filter((booking) => {
      const key = String(booking.bookingId ?? '').trim();
      if (!key || seen.has(key)) {
        return false;
      }
      seen.add(key);
      return true;
    });
  }

  get hasCurrentBookings(): boolean {
    return this.currentBookings.length > 0;
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
        this.resetBookingCarousel();
        this.isLoadingBookings = false;
      },
      error: () => {
        this.bookings = [];
        this.resetBookingCarousel();
        this.bookingError = 'Unable to load your booking snapshot right now.';
        this.isLoadingBookings = false;
      }
    });
  }

  prevBookingSlide(): void {
    const total = this.currentBookings.length;
    if (total <= 1) {
      return;
    }
    this.bookingCarouselIndex = (this.bookingCarouselIndex - 1 + total) % total;
  }

  nextBookingSlide(): void {
    const total = this.currentBookings.length;
    if (total <= 1) {
      return;
    }
    this.bookingCarouselIndex = (this.bookingCarouselIndex + 1) % total;
  }

  goToBookingSlide(index: number): void {
    const total = this.currentBookings.length;
    if (index < 0 || index >= total) {
      return;
    }
    this.bookingCarouselIndex = index;
  }

  private resetBookingCarousel(): void {
    if (this.bookingCarouselIndex >= this.currentBookings.length) {
      this.bookingCarouselIndex = 0;
    }
  }

  roomImage(roomType: string): string {
    const normalized = String(roomType ?? '').trim().toLowerCase();
    if (normalized === 'deluxe') {
      return '/assets/Deluxe.png';
    }
    if (normalized === 'suite') {
      return '/assets/Suite.png';
    }
    if (normalized === 'supreme') {
      return '/assets/Supreme.png';
    }
    return '/assets/Standard.png';
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
