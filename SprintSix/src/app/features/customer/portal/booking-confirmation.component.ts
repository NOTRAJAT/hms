import { Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { NgIf } from '@angular/common';
import { AuthSessionService } from '../../../core/services/auth-session.service';
import { InvoiceService } from '../../../core/services/invoice.service';
import { BookingApiService } from '../../../core/services/booking-api.service';

interface BookingRoomInfo {
  roomId: string;
  roomType: string;
  price: number;
  occAdults: number;
  occChildren: number;
  checkIn: string;
  checkOut: string;
  adults: number;
  children: number;
}

@Component({
  selector: 'app-booking-confirmation',
  standalone: true,
  imports: [ReactiveFormsModule, NgIf],
  templateUrl: './booking-confirmation.component.html'
})
export class BookingConfirmationComponent {
  readonly taxRate = 0.12;
  room: BookingRoomInfo | null = null;
  paymentMessage = '';
  paymentStatus: 'success' | 'failure' | 'timeout' | '' = '';
  paymentMethod = 'Card';
  bookingId = '';
  transactionId = '';
  invoiceId = '';

  form = this.fb.group({
    specialRequests: [''],
    paymentMethod: ['Card', [Validators.required]]
  });

  constructor(
    public route: ActivatedRoute,
    private router: Router,
    private fb: FormBuilder,
    public session: AuthSessionService,
    private invoiceService: InvoiceService,
    private bookingApi: BookingApiService
  ) {
    this.route.queryParamMap.subscribe((params) => {
      this.room = {
        roomId: params.get('roomId') ?? '',
        roomType: params.get('roomType') ?? '',
        price: Number(params.get('price') ?? 0),
        occAdults: Number(params.get('occAdults') ?? 0),
        occChildren: Number(params.get('occChildren') ?? 0),
        checkIn: params.get('checkIn') ?? '',
        checkOut: params.get('checkOut') ?? '',
        adults: Number(params.get('adults') ?? 1),
        children: Number(params.get('children') ?? 0)
      };
      this.paymentMethod = this.normalizePaymentMethod(params.get('paymentMethod'));
      this.bookingId = params.get('bookingId') ?? '';
      this.transactionId = params.get('transactionId') ?? '';
      this.invoiceId = params.get('invoiceId') ?? '';
      this.form.patchValue({ paymentMethod: 'Card' }, { emitEvent: false });

      const status = params.get('paymentStatus');
      if (status === 'success') {
        this.paymentStatus = 'success';
        this.paymentMessage = 'Payment successful! Your booking is confirmed.';
      } else if (status === 'failure') {
        this.paymentStatus = 'failure';
        this.paymentMessage = 'Transaction failed. Please check your details and try again.';
      } else if (status === 'timeout') {
        this.paymentStatus = 'timeout';
        this.paymentMessage = 'Your session has expired. Please re-enter your payment details.';
        this.router.navigate(['/payment'], {
          queryParams: {
            roomId: this.room.roomId,
            roomType: this.room.roomType,
            price: this.room.price,
            occAdults: this.room.occAdults,
            occChildren: this.room.occChildren,
            checkIn: this.room.checkIn,
            checkOut: this.room.checkOut,
            adults: this.room.adults,
            children: this.room.children,
            timeout: 1
          }
        });
      } else {
        this.paymentStatus = '';
        this.paymentMessage = '';
      }
    });
  }

  get nights(): number {
    if (!this.room?.checkIn || !this.room?.checkOut) {
      return 0;
    }
    const start = new Date(this.room.checkIn);
    const end = new Date(this.room.checkOut);
    const diff = end.getTime() - start.getTime();
    return diff > 0 ? Math.ceil(diff / (1000 * 60 * 60 * 24)) : 0;
  }

  get subtotal(): number {
    if (!this.room) {
      return 0;
    }
    return this.room.price * this.nights;
  }

  get tax(): number {
    return Math.round(this.subtotal * this.taxRate);
  }

  get total(): number {
    return this.subtotal + this.tax;
  }

  get occupancyInvalid(): boolean {
    if (!this.room) {
      return false;
    }
    return this.room.adults > this.room.occAdults || this.room.children > this.room.occChildren;
  }

  proceed(): void {
    if (this.hasSuccessfulPayment) {
      this.router.navigateByUrl('/bookings');
      return;
    }
    if (this.form.invalid || this.occupancyInvalid) {
      this.form.markAllAsTouched();
      return;
    }
    if (!this.room) {
      return;
    }
    this.router.navigate(['/payment'], {
      queryParams: {
        roomId: this.room.roomId,
        roomType: this.room.roomType,
        price: this.room.price,
        occAdults: this.room.occAdults,
        occChildren: this.room.occChildren,
        checkIn: this.room.checkIn,
        checkOut: this.room.checkOut,
        adults: this.room.adults,
        children: this.room.children,
        paymentMethod: 'Card',
        specialRequests: String(this.form.value.specialRequests ?? '')
      }
    });
  }

  goToMyBookings(): void {
    this.router.navigateByUrl('/bookings');
  }

  get hasSuccessfulPayment(): boolean {
    return this.paymentStatus === 'success' && !!this.bookingId;
  }

  private normalizePaymentMethod(raw: string | null): string {
    return raw && raw.trim().toLowerCase() === 'card' ? 'Card' : 'Card';
  }

  downloadInvoice(): void {
    const userId = this.session.value?.userId;
    if (!this.bookingId || !userId) {
      this.paymentStatus = 'failure';
      this.paymentMessage = 'Invoice generation failed. Please try again later.';
      return;
    }
    this.bookingApi.invoice(this.bookingId, userId).subscribe({
      next: (invoice) => {
        this.invoiceService.downloadInvoice(invoice);
        this.paymentMessage = 'Your invoice has been successfully downloaded.';
        this.paymentStatus = 'success';
      },
      error: () => {
        this.paymentStatus = 'failure';
        this.paymentMessage = 'Invoice generation failed. Please try again later.';
      }
    });
  }
}
