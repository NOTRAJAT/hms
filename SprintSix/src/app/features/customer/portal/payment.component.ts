import { Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators, ValidatorFn } from '@angular/forms';
import { NgIf } from '@angular/common';
import { AuthSessionService } from '../../../core/services/auth-session.service';
import { BookingApiService } from '../../../core/services/booking-api.service';
import { ModifyBookingConfirmPayload } from '../../../core/models/booking.model';

interface PaymentContext {
  mode: 'booking' | 'modify';
  bookingId?: string;
  roomId: string;
  roomType: string;
  price: number;
  occAdults: number;
  occChildren: number;
  checkIn: string;
  checkOut: string;
  adults: number;
  children: number;
  specialRequests: string;
  additionalAmount: number;
  paymentMethod: string;
}

const isValidCardNumber = (value: string): boolean => {
  if (!/^\d{16}$/.test(value)) {
    return false;
  }
  let sum = 0;
  let doubleIt = false;
  for (let i = value.length - 1; i >= 0; i -= 1) {
    let digit = Number(value[i]);
    if (doubleIt) {
      digit *= 2;
      if (digit > 9) {
        digit -= 9;
      }
    }
    sum += digit;
    doubleIt = !doubleIt;
  }
  return sum % 10 === 0;
};

const isExpired = (value: string): boolean => {
  if (!/^(0[1-9]|1[0-2])\/\d{2}$/.test(value)) {
    return false;
  }
  const [month, year] = value.split('/');
  const expYear = 2000 + Number(year);
  const expMonth = Number(month);
  const now = new Date();
  const currentYear = now.getFullYear();
  const currentMonth = now.getMonth() + 1;
  if (expYear < currentYear) {
    return true;
  }
  if (expYear === currentYear && expMonth <= currentMonth) {
    return true;
  }
  return false;
};

const luhnValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const value = String(control.value ?? '');
  if (!value) {
    return null;
  }
  return isValidCardNumber(value) ? null : { cardNumberInvalid: true };
};

const expiryValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const value = String(control.value ?? '');
  if (!value) {
    return null;
  }
  const match = /^(0[1-9]|1[0-2])\/\d{2}$/.test(value);
  if (!match) {
    return { expiryInvalid: true };
  }
  return isExpired(value) ? { expiryPast: true } : null;
};

@Component({
  selector: 'app-payment',
  standalone: true,
  imports: [ReactiveFormsModule, NgIf],
  templateUrl: './payment.component.html'
})
export class PaymentComponent {
  context: PaymentContext | null = null;
  paymentMethod = 'Card';
  message = '';
  messageType: 'error' | 'warning' | '' = '';

  form = this.fb.group({
    cardholderName: [''],
    cardNumber: [''],
    expiry: [''],
    cvv: [''],
    billingAddress: ['']
  });

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private fb: FormBuilder,
    private bookingApi: BookingApiService,
    public session: AuthSessionService
  ) {
    this.form = this.fb.group({
      cardholderName: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50), Validators.pattern(/^[A-Za-z ]+$/)]],
      cardNumber: ['', [Validators.required, Validators.pattern(/^\d{16}$/), luhnValidator]],
      expiry: ['', [Validators.required, expiryValidator]],
      cvv: ['', [Validators.required, Validators.pattern(/^\d{3,4}$/)]],
      billingAddress: ['', [Validators.minLength(5)]]
    });

    this.route.queryParamMap.subscribe((params) => {
      const timeout = params.get('timeout');
      if (timeout === '1') {
        this.messageType = 'warning';
        this.message = 'Your session has expired. Please re-enter your payment details.';
      }
      this.context = {
        mode: params.get('mode') === 'modify' ? 'modify' : 'booking',
        bookingId: params.get('bookingId') ?? '',
        roomId: params.get('roomId') ?? '',
        roomType: params.get('roomType') ?? '',
        price: Number(params.get('price') ?? 0),
        occAdults: Number(params.get('occAdults') ?? 0),
        occChildren: Number(params.get('occChildren') ?? 0),
        checkIn: params.get('checkIn') ?? '',
        checkOut: params.get('checkOut') ?? '',
        adults: Number(params.get('adults') ?? 1),
        children: Number(params.get('children') ?? 0),
        specialRequests: params.get('specialRequests') ?? '',
        additionalAmount: Number(params.get('additionalAmount') ?? 0),
        paymentMethod: params.get('paymentMethod') ?? 'credit'
      };
      this.paymentMethod = params.get('paymentMethod') ?? 'Card';
    });
  }

  pay(): void {
    if (this.form.invalid || !this.context) {
      this.form.markAllAsTouched();
      return;
    }

    const paymentMethodValid = this.validateCvvByCardType();
    if (!paymentMethodValid) {
      this.messageType = 'error';
      this.message = 'Invalid CVV. Please check again.';
      return;
    }

    if (!this.session.value) {
      this.messageType = 'warning';
      this.message = 'Your session has expired. Please re-enter your payment details.';
      return;
    }

    if (this.context.mode === 'modify') {
      if (!this.context.bookingId) {
        this.messageType = 'error';
        this.message = 'Modification failed due to payment error. Please try again.';
        return;
      }
      const payload: ModifyBookingConfirmPayload = {
        userId: this.session.value.userId,
        checkInDate: this.context.checkIn,
        checkOutDate: this.context.checkOut,
        adults: this.context.adults,
        children: this.context.children,
        roomType: this.context.roomType,
        paymentMethod: this.context.paymentMethod,
        paymentCompleted: true
      };
      this.bookingApi.confirmModification(this.context.bookingId, payload).subscribe({
        next: () => {
          this.router.navigate(['/bookings'], {
            queryParams: { modified: '1' }
          });
        },
        error: (error) => {
          const message = error?.error?.error;
          this.messageType = 'error';
          this.message = message || 'Modification failed due to payment error. Please try again.';
        }
      });
      return;
    }

    this.bookingApi.pay({
      userId: this.session.value.userId,
      customerName: this.session.value.name,
      customerEmail: this.session.value.email,
      customerMobile: this.session.value.mobile,
      roomId: this.context.roomId,
      roomType: this.context.roomType,
      checkInDate: this.context.checkIn,
      checkOutDate: this.context.checkOut,
      adults: this.context.adults,
      children: this.context.children,
      paymentMethod: this.paymentMethod,
      specialRequests: this.context.specialRequests,
      cardholderName: String(this.form.value.cardholderName ?? ''),
      cardNumber: String(this.form.value.cardNumber ?? ''),
      expiryDate: String(this.form.value.expiry ?? ''),
      cvv: String(this.form.value.cvv ?? ''),
      billingAddress: String(this.form.value.billingAddress ?? '')
    }).subscribe({
      next: (booking) => {
        this.router.navigate(['/booking/confirm'], {
          queryParams: {
            roomId: booking.roomId,
            roomType: booking.roomType,
            price: booking.price,
            occAdults: booking.occupancyAdults,
            occChildren: booking.occupancyChildren,
            checkIn: booking.checkInDate,
            checkOut: booking.checkOutDate,
            adults: booking.adults,
            children: booking.children,
            bookingId: booking.bookingId,
            transactionId: booking.transactionId,
            invoiceId: booking.invoiceId,
            paymentStatus: 'success',
            paymentMethod: booking.paymentMethod
          }
        });
      },
      error: (error) => {
        const message = error?.error?.error;
        this.messageType = 'error';
        this.message = message || 'Transaction failed. Please check your details and try again.';
      }
    });
  }

  retry(): void {
    this.message = '';
    this.messageType = '';
  }

  formatExpiry(): void {
    const control = this.form.get('expiry');
    if (!control) {
      return;
    }
    const raw = String(control.value ?? '').replace(/\D/g, '').slice(0, 4);
    const formatted = raw.length >= 3 ? `${raw.slice(0, 2)}/${raw.slice(2)}` : raw;
    if (formatted !== control.value) {
      control.setValue(formatted, { emitEvent: false });
    }
  }

  private validateCvvByCardType(): boolean {
    const number = String(this.form.value.cardNumber ?? '');
    const cvv = String(this.form.value.cvv ?? '');
    const isAmex = /^3[47]\d{13}$/.test(number);
    if (isAmex) {
      return /^\d{4}$/.test(cvv);
    }
    return /^\d{3}$/.test(cvv);
  }

  get isModificationPayment(): boolean {
    return this.context?.mode === 'modify';
  }

}
