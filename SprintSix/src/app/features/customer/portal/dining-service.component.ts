import { Component, OnInit } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { BookingApiService } from '../../../core/services/booking-api.service';
import { AuthSessionService } from '../../../core/services/auth-session.service';
import { ServiceRequestApiService } from '../../../core/services/service-request-api.service';
import { BookingResponse } from '../../../core/models/booking.model';
import { DiningMenuOption, ServiceTransaction } from '../../../core/models/service.model';
import { checkOtpSimulation, expiryValidator, isCvvValid, luhnValidator } from '../../../core/utils/payment-validators';

@Component({
  selector: 'app-dining-service',
  standalone: true,
  imports: [NgIf, NgFor, ReactiveFormsModule],
  templateUrl: './dining-service.component.html'
})
export class DiningServiceComponent implements OnInit {
  bookings: BookingResponse[] = [];
  menu: DiningMenuOption[] = [];
  quantities: Record<string, number> = {};
  loading = true;
  submitting = false;
  isMockLoading = false;
  stage: 'card' | 'otp' = 'card';
  message = '';
  messageType: 'error' | 'warning' | 'success' | '' = '';
  latestRequest: ServiceTransaction | null = null;

  form = this.fb.group({
    bookingId: ['', [Validators.required]],
    deliveryDateTime: ['', [Validators.required]],
    specialInstructions: ['', [Validators.maxLength(300)]],
    paymentMethod: ['Card', [Validators.required]],
    cardholderName: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50), Validators.pattern(/^[A-Za-z ]+$/)]],
    cardNumber: ['', [Validators.required, Validators.pattern(/^\d{16}$/), luhnValidator]],
    expiryDate: ['', [Validators.required, expiryValidator]],
    cvv: ['', [Validators.required, Validators.pattern(/^\d{3,4}$/)]],
    otp: ['', [Validators.required, Validators.pattern(/^\d{6}$/)]],
    billingAddress: ['', [Validators.minLength(5)]]
  });

  constructor(
    private fb: FormBuilder,
    private bookingApi: BookingApiService,
    private serviceApi: ServiceRequestApiService,
    private session: AuthSessionService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadData();
  }

  get eligibleBookings(): BookingResponse[] {
    const today = this.toDateOnly(new Date());
    return this.bookings.filter((booking) => {
      if (booking.status !== 'Confirmed') {
        return false;
      }
      const checkOut = this.toDateOnly(new Date(`${booking.checkOutDate}T00:00:00`));
      return checkOut >= today;
    });
  }

  get selectedBooking(): BookingResponse | null {
    const bookingId = String(this.form.value.bookingId ?? '').trim();
    if (!bookingId) {
      return null;
    }
    return this.eligibleBookings.find((booking) => booking.bookingId === bookingId) ?? null;
  }

  get minDateTime(): string {
    const now = new Date();
    now.setSeconds(0, 0);
    return this.toDateTimeLocal(now);
  }

  get maxDateTime(): string {
    const booking = this.selectedBooking;
    if (!booking) {
      return '';
    }
    const checkOut = new Date(`${booking.checkOutDate}T00:00:00`);
    checkOut.setMinutes(checkOut.getMinutes() - 1);
    return this.toDateTimeLocal(checkOut);
  }

  get hasSelectedItems(): boolean {
    return this.selectedItems.length > 0;
  }

  get selectedItems(): Array<{ itemCode: string; itemName: string; quantity: number; unitPrice: number; total: number }> {
    return this.menu
      .map((item) => {
        const quantity = this.quantities[item.itemCode] ?? 0;
        return {
          itemCode: item.itemCode,
          itemName: item.itemName,
          quantity,
          unitPrice: item.price,
          total: item.price * quantity
        };
      })
      .filter((item) => item.quantity > 0);
  }

  get orderTotal(): number {
    return this.selectedItems.reduce((sum, item) => sum + item.total, 0);
  }

  onBookingChange(): void {
    this.latestRequest = null;
    const booking = this.selectedBooking;
    if (!booking) {
      return;
    }
    const currentDelivery = String(this.form.value.deliveryDateTime ?? '');
    if (currentDelivery) {
      return;
    }
    const checkIn = new Date(`${booking.checkInDate}T20:00:00`);
    const now = new Date();
    const suggested = checkIn > now ? checkIn : now;
    suggested.setSeconds(0, 0);
    this.form.patchValue({ deliveryDateTime: this.toDateTimeLocal(suggested) });
  }

  adjustQuantity(itemCode: string, delta: number): void {
    const current = this.quantities[itemCode] ?? 0;
    const next = Math.max(0, Math.min(20, current + delta));
    this.quantities[itemCode] = next;
    this.latestRequest = null;
  }

  proceedToOtp(): void {
    if (this.stage === 'otp' || this.isMockLoading || this.submitting) {
      return;
    }
    this.markCardFieldsTouched();
    if (!this.isCardStageValid()) {
      return;
    }
    if (!this.hasSelectedItems) {
      this.messageType = 'error';
      this.message = 'Select at least one dining item before proceeding.';
      return;
    }
    if (!this.validateCvvByCardType()) {
      this.messageType = 'error';
      this.message = 'Invalid CVV. Please check again.';
      return;
    }
    this.message = '';
    this.messageType = '';
    this.isMockLoading = true;
    setTimeout(() => {
      this.isMockLoading = false;
      this.stage = 'otp';
      this.form.patchValue({ otp: '123456' });
      this.form.get('otp')?.markAsUntouched();
    }, 1200);
  }

  backToCard(): void {
    if (this.isMockLoading || this.submitting) {
      return;
    }
    this.stage = 'card';
    this.form.patchValue({ otp: '' });
    this.message = '';
    this.messageType = '';
  }

  submit(): void {
    if (this.stage !== 'otp') {
      this.proceedToOtp();
      return;
    }
    if (this.submitting || this.isMockLoading) {
      return;
    }

    this.form.get('otp')?.markAsTouched();
    if (this.form.get('otp')?.invalid) {
      return;
    }

    const otp = String(this.form.value.otp ?? '');
    this.submitting = true;
    this.isMockLoading = true;
    setTimeout(() => {
      const otpCheck = checkOtpSimulation(otp);
      if (otpCheck !== 'ok') {
        this.submitting = false;
        this.isMockLoading = false;
        this.messageType = otpCheck === 'expired' ? 'warning' : 'error';
        this.message = otpCheck === 'expired'
          ? 'Your OTP session has expired. Redirecting to dashboard...'
          : 'Transaction failed. Invalid OTP. Redirecting to dashboard...';
        this.redirectHomeAfterDelay();
        return;
      }
      this.createRequest();
    }, 1800);
  }

  formatExpiry(): void {
    const control = this.form.get('expiryDate');
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

  setDemoOtp(value: string): void {
    if (this.submitting || this.isMockLoading) {
      return;
    }
    this.form.patchValue({ otp: value });
  }

  private loadData(): void {
    this.loading = true;
    const userId = this.session.value?.userId;
    if (!userId) {
      this.loading = false;
      this.messageType = 'warning';
      this.message = 'Please login again to continue.';
      return;
    }

    this.bookingApi.list(userId).subscribe({
      next: (bookings) => {
        this.bookings = bookings ?? [];
        this.serviceApi.catalog().subscribe({
          next: (catalog) => {
            this.menu = catalog.diningMenu ?? [];
            this.menu.forEach((item) => {
              this.quantities[item.itemCode] = 0;
            });
            this.loading = false;
          },
          error: () => {
            this.loading = false;
            this.messageType = 'error';
            this.message = 'Unable to load service catalog right now.';
          }
        });
      },
      error: () => {
        this.loading = false;
        this.messageType = 'error';
        this.message = 'Unable to load booking eligibility right now.';
      }
    });
  }

  private createRequest(): void {
    const booking = this.selectedBooking;
    if (!booking) {
      this.submitting = false;
      this.isMockLoading = false;
      this.messageType = 'error';
      this.message = 'Please choose a valid booking to continue.';
      return;
    }

    this.serviceApi.createDining({
      bookingId: booking.bookingId,
      deliveryDateTime: String(this.form.value.deliveryDateTime ?? ''),
      items: this.selectedItems.map((item) => ({ itemCode: item.itemCode, quantity: item.quantity })),
      specialInstructions: String(this.form.value.specialInstructions ?? ''),
      paymentMethod: String(this.form.value.paymentMethod ?? 'Card'),
      cardholderName: String(this.form.value.cardholderName ?? ''),
      cardNumber: String(this.form.value.cardNumber ?? ''),
      expiryDate: String(this.form.value.expiryDate ?? ''),
      cvv: String(this.form.value.cvv ?? ''),
      otp: String(this.form.value.otp ?? ''),
      billingAddress: String(this.form.value.billingAddress ?? '')
    }).subscribe({
      next: (result) => {
        this.latestRequest = result;
        this.messageType = 'success';
        this.message = `Dining order confirmed. Request ID: ${result.requestId}`;
        this.stage = 'card';
        this.submitting = false;
        this.isMockLoading = false;
        this.form.patchValue({ otp: '' });
      },
      error: (error) => {
        this.submitting = false;
        this.isMockLoading = false;
        this.messageType = 'error';
        this.message = error?.error?.error || 'Unable to submit dining request right now.';
      }
    });
  }

  private isCardStageValid(): boolean {
    return !!this.form.get('bookingId')?.valid
      && !!this.form.get('deliveryDateTime')?.valid
      && !!this.form.get('specialInstructions')?.valid
      && !!this.form.get('paymentMethod')?.valid
      && !!this.form.get('cardholderName')?.valid
      && !!this.form.get('cardNumber')?.valid
      && !!this.form.get('expiryDate')?.valid
      && !!this.form.get('cvv')?.valid
      && !!this.form.get('billingAddress')?.valid;
  }

  private markCardFieldsTouched(): void {
    ['bookingId', 'deliveryDateTime', 'specialInstructions', 'paymentMethod', 'cardholderName', 'cardNumber', 'expiryDate', 'cvv', 'billingAddress']
      .forEach((field) => this.form.get(field)?.markAsTouched());
  }

  private validateCvvByCardType(): boolean {
    const number = String(this.form.value.cardNumber ?? '');
    const cvv = String(this.form.value.cvv ?? '');
    return isCvvValid(number, cvv);
  }

  private redirectHomeAfterDelay(): void {
    setTimeout(() => {
      this.stage = 'card';
      this.router.navigateByUrl('/dashboard');
    }, 2000);
  }

  private toDateOnly(value: Date): number {
    const next = new Date(value);
    next.setHours(0, 0, 0, 0);
    return next.getTime();
  }

  private toDateTimeLocal(value: Date): string {
    const year = value.getFullYear();
    const month = String(value.getMonth() + 1).padStart(2, '0');
    const day = String(value.getDate()).padStart(2, '0');
    const hours = String(value.getHours()).padStart(2, '0');
    const minutes = String(value.getMinutes()).padStart(2, '0');
    return `${year}-${month}-${day}T${hours}:${minutes}`;
  }
}
