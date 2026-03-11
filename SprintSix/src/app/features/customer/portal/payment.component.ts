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
  stage: 'card' | 'otp' = 'card';
  isMockLoading = false;
  isSubmitting = false;
  otpPopupOpen = false;

  form = this.fb.group({
    cardholderName: [''],
    cardNumber: [''],
    expiry: [''],
    cvv: [''],
    otp: [''],
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
      otp: ['', [Validators.required, Validators.pattern(/^\d{6}$/)]],
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
        paymentMethod: 'Card'
      };
      this.paymentMethod = 'Card';
      this.stage = 'card';
      this.isMockLoading = false;
      this.isSubmitting = false;
      this.otpPopupOpen = false;
      this.form.get('otp')?.setValue('');
    });
  }

  proceedToOtp(): void {
    if (!this.context || this.isMockLoading || this.isSubmitting || this.stage === 'otp') {
      return;
    }

    this.markCardFieldsTouched();
    if (!this.areCardDetailsValid()) {
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

    this.message = '';
    this.messageType = '';
    this.isMockLoading = true;
    setTimeout(() => {
      this.isMockLoading = false;
      this.stage = 'otp';
      this.otpPopupOpen = true;
      this.form.get('otp')?.setValue('');
      this.form.get('otp')?.markAsUntouched();
    }, 1200);
  }

  pay(): void {
    if (this.stage !== 'otp') {
      this.proceedToOtp();
      return;
    }
    if (!this.context || this.isMockLoading || this.isSubmitting) {
      return;
    }

    const otpControl = this.form.get('otp');
    otpControl?.markAsTouched();
    if (!otpControl || otpControl.invalid) {
      return;
    }

    this.isMockLoading = true;
    this.isSubmitting = true;
    const otpValue = String(this.form.value.otp ?? '');
    setTimeout(() => {
      const otpCheck = this.checkOtpSimulation(otpValue);
      if (otpCheck !== 'ok') {
        this.isMockLoading = false;
        this.isSubmitting = false;
        if (otpCheck === 'expired') {
          this.messageType = 'warning';
          this.message = 'Your OTP session has expired. Redirecting to home...';
        } else {
          this.messageType = 'error';
          this.message = 'Transaction failed. Invalid OTP. Redirecting to home...';
        }
        this.redirectToHomeAfterDelay();
        return;
      }
      this.submitPayment(otpValue);
    }, 1800);
  }

  private submitPayment(otpValue: string): void {
    if (!this.context || !this.session.value) {
      this.isMockLoading = false;
      this.isSubmitting = false;
      return;
    }

    if (this.context.mode === 'modify') {
      if (!this.context.bookingId) {
        this.messageType = 'error';
        this.message = 'Modification failed due to payment error. Please try again.';
        this.isMockLoading = false;
        this.isSubmitting = false;
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
          this.isMockLoading = false;
          this.isSubmitting = false;
          this.router.navigate(['/bookings'], {
            queryParams: { modified: '1' }
          });
        },
        error: (error) => {
          this.isMockLoading = false;
          this.isSubmitting = false;
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
      otp: otpValue,
      billingAddress: String(this.form.value.billingAddress ?? '')
    }).subscribe({
      next: (booking) => {
        this.isMockLoading = false;
        this.isSubmitting = false;
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
        this.isMockLoading = false;
        this.isSubmitting = false;
        const message = error?.error?.error;
        this.messageType = 'error';
        this.message = message || 'Transaction failed. Please check your details and try again.';
        if (typeof message === 'string' && message.toLowerCase().includes('otp')) {
          this.message = `${message} Redirecting to home...`;
          this.redirectToHomeAfterDelay();
        }
      }
    });
  }

  retry(): void {
    this.message = '';
    this.messageType = '';
  }

  backToCard(): void {
    if (this.isMockLoading || this.isSubmitting) {
      return;
    }
    this.stage = 'card';
    this.otpPopupOpen = false;
    this.form.get('otp')?.setValue('');
    this.retry();
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

  formatOtp(): void {
    const control = this.form.get('otp');
    if (!control) {
      return;
    }
    const onlyDigits = String(control.value ?? '').replace(/\D/g, '').slice(0, 6);
    if (onlyDigits !== control.value) {
      control.setValue(onlyDigits, { emitEvent: false });
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

  private markCardFieldsTouched(): void {
    this.form.get('cardholderName')?.markAsTouched();
    this.form.get('cardNumber')?.markAsTouched();
    this.form.get('expiry')?.markAsTouched();
    this.form.get('cvv')?.markAsTouched();
    this.form.get('billingAddress')?.markAsTouched();
  }

  private areCardDetailsValid(): boolean {
    return !!this.form.get('cardholderName')?.valid
      && !!this.form.get('cardNumber')?.valid
      && !!this.form.get('expiry')?.valid
      && !!this.form.get('cvv')?.valid
      && !!this.form.get('billingAddress')?.valid;
  }

  private checkOtpSimulation(otp: string): 'ok' | 'invalid' | 'expired' {
    if (otp === '123456') {
      return 'ok';
    }
    if (otp === '000000') {
      return 'expired';
    }
    return 'invalid';
  }

  private redirectToHomeAfterDelay(): void {
    setTimeout(() => {
      this.otpPopupOpen = false;
      this.stage = 'card';
      this.router.navigateByUrl('/dashboard');
    }, 2000);
  }

  get isModificationPayment(): boolean {
    return this.context?.mode === 'modify';
  }

}
