import { Component, OnInit } from '@angular/core';
import { DatePipe, NgClass, NgFor, NgIf } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminServiceItem } from '../../core/models/admin.model';
import { AdminService } from '../../core/services/admin.service';

@Component({
  selector: 'app-admin-services',
  standalone: true,
  imports: [NgIf, NgFor, NgClass, DatePipe, FormsModule],
  templateUrl: './admin-services.component.html'
})
export class AdminServicesComponent implements OnInit {
  items: AdminServiceItem[] = [];
  loading = false;
  errorMessage = '';
  successMessage = '';

  q = '';
  serviceType = '';
  status = '';
  bookingId = '';
  customer = '';

  page = 0;
  readonly size = 10;
  totalItems = 0;
  totalPages = 0;

  updatingRequestId = '';

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
    this.adminService.services({
      q: this.q.trim() || undefined,
      serviceType: this.serviceType || undefined,
      status: this.status || undefined,
      bookingId: this.bookingId.trim() || undefined,
      customer: this.customer.trim() || undefined,
      page: this.page,
      size: this.size
    }).subscribe({
      next: (response) => {
        this.items = response.items;
        this.totalItems = response.totalItems;
        this.totalPages = response.totalPages;
        this.loading = false;
      },
      error: (error) => {
        this.errorMessage = error?.error?.error ?? 'Unable to fetch service requests.';
        this.loading = false;
      }
    });
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

  canMoveTo(item: AdminServiceItem, target: 'Confirmed' | 'Completed' | 'Cancelled'): boolean {
    if (item.status === target) {
      return false;
    }
    if (item.status === 'Requested') {
      return target === 'Confirmed' || target === 'Cancelled';
    }
    if (item.status === 'Confirmed') {
      return target === 'Completed' || target === 'Cancelled';
    }
    return false;
  }

  updateStatus(item: AdminServiceItem, target: 'Confirmed' | 'Completed' | 'Cancelled'): void {
    if (!this.canMoveTo(item, target) || this.updatingRequestId) {
      return;
    }
    this.updatingRequestId = item.requestId;
    this.successMessage = '';
    this.errorMessage = '';
    this.adminService.updateServiceStatus(item.requestId, target).subscribe({
      next: () => {
        this.successMessage = target === 'Cancelled'
          ? `Request ${item.requestId} moved to Cancelled. Refund amount INR ${item.amount} initiated to bank and will be processed in 2 business days.`
          : `Request ${item.requestId} moved to ${target}.`;
        this.updatingRequestId = '';
        this.load();
      },
      error: (error) => {
        this.errorMessage = error?.error?.error ?? 'Unable to update request status.';
        this.updatingRequestId = '';
      }
    });
  }

  statusClass(status: string): string {
    const normalized = String(status).toLowerCase();
    if (normalized === 'completed') {
      return 'bg-emerald-100 text-emerald-800 border-emerald-200';
    }
    if (normalized === 'confirmed') {
      return 'bg-sky-100 text-sky-800 border-sky-200';
    }
    if (normalized === 'cancelled') {
      return 'bg-rose-100 text-rose-800 border-rose-200';
    }
    return 'bg-amber-100 text-amber-800 border-amber-200';
  }
}
