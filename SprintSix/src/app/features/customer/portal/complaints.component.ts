import { Component } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { DatePipe, NgClass, NgFor, NgIf } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ComplaintService } from '../../../core/services/complaint.service';
import { AuthSessionService } from '../../../core/services/auth-session.service';
import { ComplaintRecord } from '../../../core/models/complaint.model';
import { BookingApiService } from '../../../core/services/booking-api.service';

@Component({
  selector: 'app-complaints',
  standalone: true,
  imports: [ReactiveFormsModule, NgFor, NgIf, NgClass, DatePipe],
  templateUrl: './complaints.component.html'
})
export class ComplaintsComponent {
  complaints: ComplaintRecord[] = [];
  bookingIds: string[] = [];
  isSubmitting = false;
  errorMessage = '';
  successMessage = '';
  editingComplaintId = '';
  activeTab: 'register' | 'view' = 'register';
  selectedComplaint: ComplaintRecord | null = null;

  readonly categories = ['Room Issue', 'Service Issue', 'Billing Issue', 'Other'];
  readonly contactPreferences = ['Call', 'Email'];

  form = this.fb.group({
    category: ['', [Validators.required]],
    bookingId: ['', [Validators.required]],
    title: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(100)]],
    description: ['', [Validators.required, Validators.minLength(20), Validators.maxLength(500)]],
    contactPreference: ['', [Validators.required]]
  });

  constructor(
    private fb: FormBuilder,
    private complaintService: ComplaintService,
    private bookingApi: BookingApiService,
    public session: AuthSessionService,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.activeTab = this.route.snapshot.routeConfig?.path === 'complaints/view' ? 'view' : 'register';
    this.loadBookings();
    this.loadComplaints();
  }

  switchTab(tab: 'register' | 'view'): void {
    this.activeTab = tab;
    this.selectedComplaint = null;
    this.errorMessage = '';
    if (tab === 'view') {
      this.router.navigate(['/complaints/view']);
    } else {
      this.router.navigate(['/complaints']);
    }
  }

  loadBookings(): void {
    const userId = this.session.value?.userId;
    if (!userId) {
      return;
    }
    this.bookingApi.list(userId).subscribe({
      next: (items) => {
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        this.bookingIds = items
          .filter((item) => this.isComplaintEligible(item.checkInDate, today))
          .map((item) => item.bookingId);
      },
      error: () => {
        this.bookingIds = [];
      }
    });
  }

  loadComplaints(): void {
    const userId = this.session.value?.userId;
    if (!userId) {
      return;
    }
    this.complaintService.list(userId).subscribe({
      next: (items) => {
        this.complaints = items;
      },
      error: () => {
        this.errorMessage = 'Unable to fetch complaint status. Please try again later.';
      }
    });
  }

  openComplaint(complaintId: string): void {
    const userId = this.session.value?.userId;
    if (!userId) {
      return;
    }
    this.errorMessage = '';
    this.complaintService.detail(complaintId, userId).subscribe({
      next: (detail) => {
        this.selectedComplaint = detail;
      },
      error: (error) => {
        this.selectedComplaint = null;
        this.errorMessage = error?.error?.error || 'Unable to fetch complaint status. Please try again later.';
      }
    });
  }

  confirmResolution(complaintId: string): void {
    const userId = this.session.value?.userId;
    if (!userId) {
      return;
    }
    this.complaintService.confirmResolution(complaintId, userId).subscribe({
      next: () => {
        this.successMessage = 'Complaint marked as closed.';
        this.loadComplaints();
        this.openComplaint(complaintId);
      },
      error: (error) => {
        this.errorMessage = error?.error?.error || 'Unable to fetch complaint status. Please try again later.';
      }
    });
  }

  reopenComplaint(complaintId: string): void {
    const userId = this.session.value?.userId;
    if (!userId) {
      return;
    }
    this.complaintService.reopen(complaintId, userId).subscribe({
      next: () => {
        this.successMessage = 'Complaint reopened successfully.';
        this.loadComplaints();
        this.openComplaint(complaintId);
      },
      error: (error) => {
        this.errorMessage = error?.error?.error || 'Unable to fetch complaint status. Please try again later.';
      }
    });
  }

  resetForm(): void {
    this.form.reset();
    this.editingComplaintId = '';
    this.errorMessage = '';
  }

  editComplaint(complaint: ComplaintRecord): void {
    if (!complaint.editable || complaint.status !== 'Open') {
      return;
    }
    this.activeTab = 'register';
    this.editingComplaintId = complaint.id;
    this.form.patchValue({
      category: complaint.category,
      bookingId: complaint.bookingId,
      title: complaint.title,
      description: complaint.description,
      contactPreference: complaint.contactPreference
    });
    this.errorMessage = '';
    this.successMessage = '';
  }

  submit(): void {
    this.errorMessage = '';
    this.successMessage = '';

    if (this.form.invalid || !this.session.value?.userId) {
      this.form.markAllAsTouched();
      if (!this.form.value.category || !this.form.value.bookingId || !this.form.value.title || !this.form.value.description || !this.form.value.contactPreference) {
        this.errorMessage = 'Please fill in all required fields.';
      } else {
        this.errorMessage = 'Please provide more details to help us resolve your issue.';
      }
      return;
    }

    const title = String(this.form.value.title ?? '').trim();
    const description = String(this.form.value.description ?? '').trim();
    if (title.length < 10 || description.length < 20) {
      this.errorMessage = 'Please provide more details to help us resolve your issue.';
      return;
    }

    this.isSubmitting = true;
    const payload = {
      userId: this.session.value.userId,
      category: String(this.form.value.category ?? '').trim(),
      bookingId: String(this.form.value.bookingId ?? '').trim(),
      title,
      description,
      contactPreference: String(this.form.value.contactPreference ?? '').trim()
    };

    const done = () => {
      this.isSubmitting = false;
      this.resetForm();
      this.loadComplaints();
    };

    if (this.editingComplaintId) {
      this.complaintService.update(this.editingComplaintId, payload).subscribe({
        next: (updated) => {
          this.successMessage = `Complaint ${updated.id} has been updated successfully.`;
          done();
        },
        error: (error) => {
          this.isSubmitting = false;
          this.errorMessage = error?.error?.error || 'Unable to update complaint right now.';
        }
      });
      return;
    }

    this.complaintService.create(payload).subscribe({
      next: (created) => {
        this.successMessage = `Your complaint has been successfully submitted. Complaint ID: ${created.id}. Our support team will get back to you soon. ${created.acknowledgementMessage}`;
        done();
        this.switchTab('view');
      },
      error: (error) => {
        this.isSubmitting = false;
        this.errorMessage = error?.error?.error || 'Unable to submit complaint right now.';
      }
    });
  }

  private isComplaintEligible(checkInDate: string, today: Date): boolean {
    const normalized = String(checkInDate ?? '').slice(0, 10);
    if (!normalized) {
      return false;
    }
    const checkIn = new Date(`${normalized}T00:00:00`);
    if (Number.isNaN(checkIn.getTime())) {
      return false;
    }
    return checkIn.getTime() <= today.getTime();
  }
}
