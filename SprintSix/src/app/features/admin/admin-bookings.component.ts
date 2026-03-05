import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DatePipe, NgFor, NgIf } from '@angular/common';
import {
  AdminBookingCreatePayload,
  AdminBookingItem,
  AdminBookingUpdatePayload
} from '../../core/models/admin.model';
import { AdminService } from '../../core/services/admin.service';

@Component({
  selector: 'app-admin-bookings',
  standalone: true,
  imports: [FormsModule, NgFor, NgIf, DatePipe],
  templateUrl: './admin-bookings.component.html'
})
export class AdminBookingsComponent implements OnInit {
  readonly countryCodeOptions = ['+91'];
  createMobileCountryCode = '+91';
  createMobileNumber = '';

  bookings: AdminBookingItem[] = [];
  loading = false;
  errorMessage = '';
  successMessage = '';

  q = '';
  roomCode = '';
  status = '';
  roomType = '';
  bookingDate = '';
  fromDate = '';
  toDate = '';
  sortBy = 'createdAt';
  sortDir: 'asc' | 'desc' = 'desc';
  page = 0;
  readonly size = 10;
  totalItems = 0;
  totalPages = 0;

  createMode = false;
  submittingCreate = false;
  createSubmitted = false;
  createTouched = {
    customerName: false,
    customerEmail: false,
    customerMobile: false,
    checkInDate: false,
    checkOutDate: false,
    adults: false,
    children: false
  };
  createForm: AdminBookingCreatePayload = {
    customerName: '',
    customerEmail: '',
    customerMobile: '',
    checkInDate: '',
    checkOutDate: '',
    adults: 1,
    children: 0,
    roomType: 'Standard',
    roomCode: '',
    paymentMethod: 'Credit Card',
    depositAmount: 0,
    specialRequests: ''
  };

  selected: AdminBookingItem | null = null;
  updating = false;
  editSubmitted = false;
  editSuccessMessage = '';
  editErrorMessage = '';
  editForm: AdminBookingUpdatePayload = {
    checkInDate: '',
    checkOutDate: '',
    adults: 1,
    children: 0,
    roomCode: '',
    specialRequests: ''
  };
  cancelTarget: AdminBookingItem | null = null;
  cancelling = false;

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.load(true);
  }

  load(resetPage = false): void {
    if (resetPage) {
      this.page = 0;
    }
    this.loading = true;
    this.errorMessage = '';
    this.adminService.bookings({
      q: this.q.trim() || undefined,
      roomCode: this.roomCode.trim() || undefined,
      status: this.status || undefined,
      roomType: this.roomType || undefined,
      bookingDate: this.bookingDate || undefined,
      fromDate: this.fromDate || undefined,
      toDate: this.toDate || undefined,
      sortBy: this.sortBy,
      sortDir: this.sortDir,
      page: this.page,
      size: this.size
    }).subscribe({
      next: (response) => {
        this.bookings = response.items;
        this.totalItems = response.totalItems;
        this.totalPages = response.totalPages;
        this.loading = false;
      },
      error: (error) => {
        this.errorMessage = error?.error?.error ?? 'Unable to fetch reservations.';
        this.loading = false;
      }
    });
  }

  toggleSort(column: string): void {
    if (this.sortBy === column) {
      this.sortDir = this.sortDir === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortBy = column;
      this.sortDir = 'asc';
    }
    this.load();
  }

  prevPage(): void {
    if (this.page <= 0) {
      return;
    }
    this.page -= 1;
    this.load();
  }

  nextPage(): void {
    if (this.page + 1 >= this.totalPages) {
      return;
    }
    this.page += 1;
    this.load();
  }

  openCreate(): void {
    this.createMode = true;
    this.createSubmitted = false;
    this.createTouched = {
      customerName: false,
      customerEmail: false,
      customerMobile: false,
      checkInDate: false,
      checkOutDate: false,
      adults: false,
      children: false
    };
    this.createForm = {
      customerName: '',
      customerEmail: '',
      customerMobile: '',
      checkInDate: '',
      checkOutDate: '',
      adults: 1,
      children: 0,
      roomType: 'Standard',
      roomCode: '',
      paymentMethod: 'Credit Card',
      depositAmount: 0,
      specialRequests: ''
    };
    this.createMobileCountryCode = '+91';
    this.createMobileNumber = '';
  }

  closeCreate(): void {
    this.createMode = false;
  }

  submitCreate(): void {
    this.createSubmitted = true;
    if (!this.isCreateFormValid || this.submittingCreate) {
      return;
    }
    this.submittingCreate = true;
    const payload: AdminBookingCreatePayload = {
      ...this.createForm,
      customerMobile: this.composeMobile(this.createMobileCountryCode, this.createMobileNumber)
    };
    this.adminService.createBooking(payload).subscribe({
      next: (booking) => {
        this.successMessage = `You have successfully reserved the room. Reservation ID: ${booking.bookingId}`;
        this.submittingCreate = false;
        this.createMode = false;
        this.load(true);
      },
      error: (error) => {
        this.errorMessage = error?.error?.error ?? 'Unable to create reservation.';
        this.submittingCreate = false;
      }
    });
  }

  startEdit(booking: AdminBookingItem): void {
    this.selected = booking;
    this.editSubmitted = false;
    this.editSuccessMessage = '';
    this.editErrorMessage = '';
    this.editForm = {
      checkInDate: booking.checkInDate,
      checkOutDate: booking.checkOutDate,
      adults: booking.adults,
      children: booking.children,
      roomCode: booking.roomCode,
      specialRequests: booking.specialRequests || ''
    };
  }

  closeEdit(): void {
    this.selected = null;
    this.editSuccessMessage = '';
    this.editErrorMessage = '';
  }

  submitEdit(): void {
    if (!this.selected) {
      return;
    }
    this.editSubmitted = true;
    this.editSuccessMessage = '';
    this.editErrorMessage = '';
    if (!this.isEditFormValid || this.updating) {
      return;
    }
    this.updating = true;
    this.adminService.updateBooking(this.selected.bookingId, this.editForm).subscribe({
      next: (updated) => {
        this.successMessage = `Reservation ${updated.bookingId} updated successfully.`;
        this.errorMessage = '';
        this.editSuccessMessage = `Reservation ${updated.bookingId} updated successfully.`;
        this.editErrorMessage = '';
        this.selected = updated;
        this.editForm = {
          checkInDate: updated.checkInDate,
          checkOutDate: updated.checkOutDate,
          adults: updated.adults,
          children: updated.children,
          roomCode: updated.roomCode,
          specialRequests: updated.specialRequests || ''
        };
        this.updating = false;
        this.load();
      },
      error: (error) => {
        const message = error?.error?.error ?? 'Unable to update reservation.';
        this.errorMessage = message;
        this.editErrorMessage = message;
        this.editSuccessMessage = '';
        this.updating = false;
      }
    });
  }

  openCancel(booking: AdminBookingItem): void {
    this.cancelTarget = booking;
  }

  closeCancel(): void {
    this.cancelTarget = null;
  }

  confirmCancel(): void {
    if (!this.cancelTarget || this.cancelling) {
      return;
    }
    this.cancelling = true;
    this.adminService.cancelBooking(this.cancelTarget.bookingId).subscribe({
      next: () => {
        this.successMessage = `Reservation ${this.cancelTarget?.bookingId} has been cancelled.`;
        this.cancelling = false;
        this.cancelTarget = null;
        this.load();
      },
      error: (error) => {
        this.errorMessage = error?.error?.error ?? 'Unable to cancel reservation.';
        this.cancelling = false;
      }
    });
  }

  canEdit(booking: AdminBookingItem): boolean {
    return booking.status === 'Confirmed';
  }

  canCancel(booking: AdminBookingItem): boolean {
    return booking.status === 'Confirmed';
  }

  blockNegativeInput(event: KeyboardEvent): void {
    if (event.key === '-' || event.key === '+' || event.key.toLowerCase() === 'e') {
      event.preventDefault();
    }
  }

  sanitizeCreateInt(field: 'adults' | 'children' | 'depositAmount'): void {
    const value = Number(this.createForm[field] ?? 0);
    if (!Number.isFinite(value)) {
      return;
    }
    this.createForm[field] = Math.trunc(value) as never;
  }

  sanitizeCreateMobile(): void {
    this.createMobileNumber = this.createMobileNumber.replace(/\D/g, '').slice(0, 10);
    this.createForm.customerMobile = this.composeMobile(this.createMobileCountryCode, this.createMobileNumber);
  }

  get searchTextInvalid(): boolean {
    return !/^[a-zA-Z0-9\s-]*$/.test(this.q);
  }

  get roomCodeInvalid(): boolean {
    return !/^[a-zA-Z0-9-]*$/.test(this.roomCode);
  }

  blockPhoneInput(event: KeyboardEvent): void {
    const allowedControlKeys = ['Backspace', 'Delete', 'Tab', 'ArrowLeft', 'ArrowRight', 'Home', 'End'];
    if (allowedControlKeys.includes(event.key)) {
      return;
    }
    if (event.key >= '0' && event.key <= '9') {
      return;
    }
    event.preventDefault();
  }

  touchCreateField(field: keyof typeof this.createTouched): void {
    this.createTouched[field] = true;
  }

  sanitizeEditInt(field: 'adults' | 'children'): void {
    const value = Number(this.editForm[field] ?? 0);
    if (!Number.isFinite(value)) {
      return;
    }
    this.editForm[field] = Math.trunc(value) as never;
  }

  get today(): string {
    return new Date().toISOString().slice(0, 10);
  }

  get createDateInvalid(): boolean {
    if (!this.createForm.checkInDate || !this.createForm.checkOutDate) {
      return true;
    }
    return this.createForm.checkInDate < this.today || this.createForm.checkOutDate <= this.createForm.checkInDate;
  }

  get createAdultsInvalid(): boolean {
    return this.createForm.adults < 1 || this.createForm.adults > 10;
  }

  get createChildrenInvalid(): boolean {
    return this.createForm.children < 0 || this.createForm.children > 5;
  }

  get createGuestTotalInvalid(): boolean {
    return (this.createForm.adults + this.createForm.children) > 10;
  }

  get createEmailInvalid(): boolean {
    return !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.createForm.customerEmail);
  }

  get createMobileInvalid(): boolean {
    return this.createMobileCountryCode !== '+91' || !/^[789]\d{9}$/.test(this.createMobileNumber);
  }

  get isCreateFormValid(): boolean {
    return this.createForm.customerName.trim().length >= 2
      && !this.createEmailInvalid
      && !this.createMobileInvalid
      && !this.createDateInvalid
      && !this.createAdultsInvalid
      && !this.createChildrenInvalid
      && !this.createGuestTotalInvalid
      && !!this.createForm.roomType
      && !!this.createForm.paymentMethod;
  }

  get editDateInvalid(): boolean {
    if (!this.editForm.checkInDate || !this.editForm.checkOutDate) {
      return true;
    }
    return this.editForm.checkInDate < this.today || this.editForm.checkOutDate <= this.editForm.checkInDate;
  }

  get editAdultsInvalid(): boolean {
    return this.editForm.adults < 1 || this.editForm.adults > 10;
  }

  get editChildrenInvalid(): boolean {
    return this.editForm.children < 0 || this.editForm.children > 5;
  }

  get editGuestTotalInvalid(): boolean {
    return (this.editForm.adults + this.editForm.children) > 10;
  }

  get isEditFormValid(): boolean {
    return !this.editDateInvalid
      && !this.editAdultsInvalid
      && !this.editChildrenInvalid
      && !this.editGuestTotalInvalid
      && this.editForm.roomCode.trim().length > 0;
  }

  private composeMobile(countryCode: string, mobileNumber: string): string {
    return `${countryCode}${mobileNumber.trim()}`;
  }
}
