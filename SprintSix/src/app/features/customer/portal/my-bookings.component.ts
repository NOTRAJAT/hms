import { Component, OnInit } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  BookingResponse,
  ModifyBookingPayload,
  ModifyBookingPreviewResponse
} from '../../../core/models/booking.model';
import { InvoiceService } from '../../../core/services/invoice.service';
import { AuthSessionService } from '../../../core/services/auth-session.service';
import { BookingApiService } from '../../../core/services/booking-api.service';
import { RoomService } from '../../../core/services/room.service';
import { ServiceRequestApiService } from '../../../core/services/service-request-api.service';
import { ServiceTransaction } from '../../../core/models/service.model';

@Component({
  selector: 'app-my-bookings',
  standalone: true,
  imports: [NgFor, NgIf, ReactiveFormsModule],
  templateUrl: './my-bookings.component.html'
})
export class MyBookingsComponent implements OnInit {
  bookings: BookingResponse[] = [];
  errorMessage = '';
  selectedBookingId = '';
  successMessage = '';
  cancelDialog: { bookingId: string; message: string; refundAmount: number } | null = null;
  cancelSubmitting = false;

  modifyingBooking: BookingResponse | null = null;
  modificationPreview: ModifyBookingPreviewResponse | null = null;
  modificationError = '';
  availabilityMessage = '';
  serviceTransactionsByBooking: Record<string, ServiceTransaction[]> = {};

  roomTypes: string[] = ['Standard', 'Deluxe', 'Suite', 'Supreme'];

  private readonly hotelName = 'Renaissance Stay';
  private readonly hotelLocation = '12 Garden Lane, City Center';

  modifyForm = this.fb.group({
    checkInDate: ['', [Validators.required]],
    checkOutDate: ['', [Validators.required]],
    adults: [1, [Validators.required, Validators.min(1), Validators.max(10)]],
    children: [0, [Validators.required, Validators.min(0), Validators.max(5)]],
    roomType: ['', [Validators.required]]
  });

  constructor(
    private bookingApi: BookingApiService,
    private invoiceService: InvoiceService,
    public session: AuthSessionService,
    private fb: FormBuilder,
    private roomService: RoomService,
    private serviceApi: ServiceRequestApiService,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.route.queryParamMap.subscribe((params) => {
      if (params.get('modified') === '1') {
        this.successMessage = 'Your booking has been successfully modified.';
      }
    });

    this.modifyForm.get('checkInDate')?.valueChanges.subscribe((value) => {
      const nextCheckIn = this.toDate(String(value ?? ''));
      const currentCheckOut = this.toDate(String(this.modifyForm.get('checkOutDate')?.value ?? ''));
      if (nextCheckIn && currentCheckOut && currentCheckOut <= nextCheckIn) {
        this.modifyForm.get('checkOutDate')?.setValue('', { emitEvent: false });
      }
    });

    this.modifyForm.valueChanges.subscribe(() => {
      this.modificationPreview = null;
      this.modificationError = '';
      this.checkRealtimeAvailability();
    });

    this.loadBookings();
  }

  ngOnInit(): void {
    this.roomService.roomTypes().subscribe({
      next: (types) => {
        const normalized = (types || [])
          .map((type) => String(type ?? '').trim())
          .filter((type) => !!type);
        if (normalized.length > 0) {
          this.roomTypes = Array.from(new Set([...this.roomTypes, ...normalized]));
        }
      },
      error: () => {
        // Keep fallback types if API fails.
      }
    });
  }

  cancel(bookingId: string): void {
    const userId = this.session.value?.userId;
    if (!userId) {
      this.errorMessage = 'Unable to cancel booking right now.';
      return;
    }

    this.bookingApi.cancellationPreview(bookingId, userId).subscribe({
      next: (preview) => {
        this.cancelDialog = {
          bookingId,
          message: preview.message,
          refundAmount: preview.refundAmount ?? 0
        };
      },
      error: (error) => {
        this.errorMessage = error?.error?.error || 'Unable to cancel booking right now.';
      }
    });
  }

  confirmCancel(): void {
    if (!this.cancelDialog || this.cancelSubmitting) {
      return;
    }
    const userId = this.session.value?.userId;
    if (!userId) {
      this.errorMessage = 'Unable to cancel booking right now.';
      this.closeCancelDialog();
      return;
    }

    this.cancelSubmitting = true;
    this.bookingApi.cancel(this.cancelDialog.bookingId, userId).subscribe({
      next: (updated) => {
        this.successMessage = updated.cancellationNote || 'Your booking has been canceled.';
        this.cancelSubmitting = false;
        this.closeCancelDialog();
        this.loadBookings();
      },
      error: (error) => {
        this.errorMessage = error?.error?.error || 'Unable to cancel booking right now.';
        this.cancelSubmitting = false;
      }
    });
  }

  closeCancelDialog(): void {
    this.cancelDialog = null;
    this.cancelSubmitting = false;
  }

  openModify(booking: BookingResponse): void {
    this.successMessage = '';
    this.modificationError = '';
    this.modificationPreview = null;
    this.modifyingBooking = booking;
    this.modifyForm.patchValue({
      checkInDate: booking.checkInDate,
      checkOutDate: booking.checkOutDate,
      adults: booking.adults,
      children: booking.children,
      roomType: booking.roomType
    }, { emitEvent: false });
    if (booking.roomType && !this.roomTypes.includes(booking.roomType)) {
      this.roomTypes = [...this.roomTypes, booking.roomType];
    }
    this.checkRealtimeAvailability();
  }

  closeModify(): void {
    this.modifyingBooking = null;
    this.modificationPreview = null;
    this.modificationError = '';
    this.availabilityMessage = '';
  }

  previewModification(): void {
    if (!this.modifyingBooking || this.modifyForm.invalid) {
      this.modifyForm.markAllAsTouched();
      return;
    }
    const payload = this.buildModifyPayload();
    if (!payload) {
      return;
    }

    this.bookingApi.previewModification(this.modifyingBooking.bookingId, payload).subscribe({
      next: (preview) => {
        this.modificationPreview = preview;
        this.modificationError = '';
      },
      error: (error) => {
        this.modificationPreview = null;
        this.modificationError = error?.error?.error || 'Unable to preview booking changes.';
      }
    });
  }

  confirmModification(): void {
    if (!this.modifyingBooking || !this.modificationPreview) {
      return;
    }
    const payload = this.buildModifyPayload();
    if (!payload) {
      return;
    }

    if (this.modificationPreview.additionalAmount > 0) {
      this.router.navigate(['/payment'], {
        queryParams: {
          mode: 'modify',
          bookingId: this.modifyingBooking.bookingId,
          roomId: this.modificationPreview.roomId,
          roomType: payload.roomType,
          price: this.modificationPreview.pricePerNight,
          occAdults: this.modificationPreview.occupancyAdults,
          occChildren: this.modificationPreview.occupancyChildren,
          checkIn: payload.checkInDate,
          checkOut: payload.checkOutDate,
          adults: payload.adults,
          children: payload.children,
          additionalAmount: this.modificationPreview.additionalAmount,
          paymentMethod: 'Card'
        }
      });
      return;
    }

    this.bookingApi.confirmModification(this.modifyingBooking.bookingId, {
      ...payload,
      paymentCompleted: true
    }).subscribe({
      next: () => {
        const refundMessage = this.modificationPreview && this.modificationPreview.refundAmount > 0
          ? ' Your updated booking costs less. Refund (if applicable) will be processed as per the cancellation policy.'
          : '';
        this.successMessage = `Your booking has been successfully modified.${refundMessage}`;
        this.closeModify();
        this.loadBookings();
      },
      error: (error) => {
        this.modificationError = error?.error?.error || 'Modification failed due to payment error. Please try again.';
      }
    });
  }

  modificationRefundPercent(preview: ModifyBookingPreviewResponse | null): number {
    if (!preview || preview.refundAmount <= 0 || preview.oldTotalAmount <= 0) {
      return 0;
    }
    return Math.round((preview.refundAmount / preview.oldTotalAmount) * 100);
  }

  viewDetails(bookingId: string): void {
    this.selectedBookingId = this.selectedBookingId === bookingId ? '' : bookingId;
  }

  downloadInvoice(booking: BookingResponse): void {
    const userId = this.session.value?.userId;
    if (!this.canDownloadInvoice(booking)) {
      if (this.paymentStatus(booking) !== 'Paid') {
        this.errorMessage = 'Invoice will be available after full payment.';
      }
      return;
    }
    if (!userId) {
      this.errorMessage = 'Unable to generate invoice. Please try again later.';
      return;
    }
    this.bookingApi.invoice(booking.bookingId, userId).subscribe({
      next: (invoice) => {
        this.invoiceService.downloadInvoice(invoice);
        this.successMessage = 'Your invoice has been successfully downloaded.';
      },
      error: () => {
        this.errorMessage = 'Unable to generate invoice. Please try again later.';
      }
    });
  }

  get upcomingBookings(): BookingResponse[] {
    const today = this.startOfDay(new Date());
    return this.bookings.filter((booking) => {
      const checkOut = this.toDate(booking.checkOutDate);
      return !!checkOut && checkOut >= today;
    });
  }

  get pastBookings(): BookingResponse[] {
    const today = this.startOfDay(new Date());
    return this.bookings.filter((booking) => {
      const checkOut = this.toDate(booking.checkOutDate);
      return !!checkOut && checkOut < today;
    });
  }

  formattedDate(value: string | null | undefined): string {
    const date = this.toDate(value ?? '');
    if (!date) {
      return value ?? '';
    }
    const dd = String(date.getDate()).padStart(2, '0');
    const mm = String(date.getMonth() + 1).padStart(2, '0');
    const yyyy = date.getFullYear();
    return `${dd}-${mm}-${yyyy}`;
  }

  displayStatus(booking: BookingResponse): string {
    if (booking.status === 'Cancelled') {
      return 'Canceled';
    }
    const today = this.startOfDay(new Date());
    const checkIn = this.toDate(booking.checkInDate);
    const checkOut = this.toDate(booking.checkOutDate);
    if (!checkIn || !checkOut) {
      return booking.status;
    }
    if (today < checkIn) {
      return 'Confirmed';
    }
    if (today >= checkIn && today < checkOut) {
      return 'Checked-in';
    }
    return 'Checked-out';
  }

  paymentStatus(booking: BookingResponse): 'Paid' | 'Pending' {
    return booking.status === 'Pending' ? 'Pending' : 'Paid';
  }

  roomCategory(booking: BookingResponse): string {
    const normalized = (booking.roomType || '').toLowerCase();
    if (normalized === 'suite') {
      return 'Luxury';
    }
    if (normalized === 'deluxe') {
      return 'Premium';
    }
    return 'Standard';
  }

  canDownloadInvoice(booking: BookingResponse): boolean {
    return booking.status === 'Confirmed' && this.paymentStatus(booking) === 'Paid';
  }

  canModifyBooking(booking: BookingResponse): boolean {
    if (booking.status !== 'Confirmed') {
      return false;
    }
    const checkIn = this.toDate(booking.checkInDate);
    if (!checkIn) {
      return false;
    }
    const cutoff = new Date(checkIn);
    cutoff.setHours(cutoff.getHours() - 24);
    return new Date() < cutoff;
  }

  canCancelBooking(booking: BookingResponse): boolean {
    if (booking.status !== 'Confirmed') {
      return false;
    }
    const checkIn = this.toDate(booking.checkInDate);
    if (!checkIn) {
      return false;
    }
    return this.startOfDay(new Date()) < checkIn;
  }

  canceledOnLabel(booking: BookingResponse): string {
    if (booking.status !== 'Cancelled' || !booking.cancelledAt) {
      return '';
    }
    return `This booking was canceled on ${this.formattedDate(booking.cancelledAt)}.`;
  }

  isSelected(bookingId: string): boolean {
    return this.selectedBookingId === bookingId;
  }

  private loadBookings(): void {
    const userId = this.session.value?.userId;
    if (!userId) {
      this.bookings = [];
      this.selectedBookingId = '';
      return;
    }
    this.bookingApi.list(userId).subscribe({
      next: (items) => {
        this.bookings = items;
        this.selectedBookingId = '';
        this.errorMessage = '';
        this.loadServiceTransactions();
      },
      error: () => {
        this.bookings = [];
        this.selectedBookingId = '';
        this.errorMessage = 'Unable to load bookings right now.';
        this.serviceTransactionsByBooking = {};
      }
    });
  }

  private loadServiceTransactions(): void {
    this.serviceApi.my().subscribe({
      next: (items) => {
        const grouped: Record<string, ServiceTransaction[]> = {};
        (items || []).forEach((item) => {
          const key = String(item.bookingId ?? '');
          if (!key) {
            return;
          }
          if (!grouped[key]) {
            grouped[key] = [];
          }
          grouped[key].push(item);
        });
        Object.values(grouped).forEach((list) => {
          list.sort((a, b) => String(b.createdAt).localeCompare(String(a.createdAt)));
        });
        this.serviceTransactionsByBooking = grouped;
      },
      error: () => {
        this.serviceTransactionsByBooking = {};
      }
    });
  }

  serviceTransactions(bookingId: string): ServiceTransaction[] {
    return this.serviceTransactionsByBooking[bookingId] ?? [];
  }

  private buildModifyPayload(): ModifyBookingPayload | null {
    const userId = this.session.value?.userId;
    if (!userId) {
      this.modificationError = 'Your session has expired. Please login again.';
      return null;
    }
    return {
      userId,
      checkInDate: String(this.modifyForm.value.checkInDate ?? ''),
      checkOutDate: String(this.modifyForm.value.checkOutDate ?? ''),
      adults: Number(this.modifyForm.value.adults ?? 1),
      children: Number(this.modifyForm.value.children ?? 0),
      roomType: String(this.modifyForm.value.roomType ?? ''),
      paymentMethod: 'Card'
    };
  }

  private checkRealtimeAvailability(): void {
    if (!this.modifyingBooking) {
      this.availabilityMessage = '';
      return;
    }
    const checkInDate = String(this.modifyForm.value.checkInDate ?? '');
    const checkOutDate = String(this.modifyForm.value.checkOutDate ?? '');
    const adults = Number(this.modifyForm.value.adults ?? 1);
    const children = Number(this.modifyForm.value.children ?? 0);
    const roomType = String(this.modifyForm.value.roomType ?? '');

    if (!checkInDate || !checkOutDate || !roomType || adults < 1 || children < 0) {
      this.availabilityMessage = '';
      return;
    }

    this.roomService.search({ checkInDate, checkOutDate, adults, children, roomType }).subscribe({
      next: (rooms) => {
        const available = rooms.some((room) => room.available);
        this.availabilityMessage = available
          ? 'Real-time availability: room is available for selected criteria.'
          : 'The selected room type is fully booked for these dates. Please choose another option.';
      },
      error: () => {
        this.availabilityMessage = '';
      }
    });
  }

  private toDate(value: string): Date | null {
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
      return null;
    }
    return this.startOfDay(date);
  }

  private startOfDay(date: Date): Date {
    const next = new Date(date);
    next.setHours(0, 0, 0, 0);
    return next;
  }

  get hotelDisplayName(): string {
    return this.hotelName;
  }

  get hotelDisplayLocation(): string {
    return this.hotelLocation;
  }

  get modifyMinCheckInDate(): string {
    const today = this.startOfDay(new Date());
    if (!this.modifyingBooking) {
      return this.toInputDate(today);
    }
    const bookingCheckIn = this.toDate(this.modifyingBooking.checkInDate);
    if (!bookingCheckIn) {
      return this.toInputDate(today);
    }
    const min = bookingCheckIn > today ? bookingCheckIn : today;
    return this.toInputDate(min);
  }

  get modifyMinCheckOutDate(): string {
    const checkIn = this.toDate(String(this.modifyForm.value.checkInDate ?? ''));
    const base = checkIn ?? this.toDate(this.modifyMinCheckInDate) ?? this.startOfDay(new Date());
    const next = new Date(base);
    next.setDate(next.getDate() + 1);
    return this.toInputDate(next);
  }

  private toInputDate(value: Date): string {
    const yyyy = value.getFullYear();
    const mm = String(value.getMonth() + 1).padStart(2, '0');
    const dd = String(value.getDate()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd}`;
  }
}
