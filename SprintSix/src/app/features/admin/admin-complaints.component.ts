import { Component, OnInit } from '@angular/core';
import { DatePipe, NgClass, NgFor, NgIf } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminComplaintItem, AdminComplaintUpdatePayload } from '../../core/models/admin.model';
import { AdminService } from '../../core/services/admin.service';
import { AuthSessionService } from '../../core/services/auth-session.service';

@Component({
  selector: 'app-admin-complaints',
  standalone: true,
  imports: [FormsModule, NgFor, NgIf, NgClass, DatePipe],
  templateUrl: './admin-complaints.component.html'
})
export class AdminComplaintsComponent implements OnInit {
  complaints: AdminComplaintItem[] = [];
  loading = false;
  detailLoading = false;
  updating = false;
  errorMessage = '';
  successMessage = '';
  assignmentNotice = '';

  q = '';
  category = '';
  priority = '';
  status = '';
  assignedTo = '';
  fromDate = '';
  toDate = '';
  sortBy = 'submissionDate';
  sortDir: 'asc' | 'desc' = 'desc';
  page = 0;
  readonly size = 10;
  totalItems = 0;
  totalPages = 0;

  selected: AdminComplaintItem | null = null;
  updateForm = {
    assignedStaffMember: '',
    assignedDepartment: '',
    status: '',
    supportResponse: '',
    resolutionNotes: '',
    actionDetails: ''
  };
  staffDigits = '';
  staffSuggestions: Array<{ userId: string; name: string; department: string }> = [];
  staffLookupLoading = false;
  private staffLookupTimer: ReturnType<typeof setTimeout> | null = null;

  readonly categories = ['Room Issue', 'Service Issue', 'Billing Issue', 'Other'];
  readonly priorities = ['High', 'Medium', 'Low'];
  readonly statuses = ['Open', 'In Progress', 'Escalated', 'Resolved', 'Closed'];

  constructor(
    private adminService: AdminService,
    private session: AuthSessionService
  ) {}

  ngOnInit(): void {
    this.load(true);
  }

  load(resetPage = false): void {
    if (resetPage) {
      this.page = 0;
    }
    if (this.invalidDateRange) {
      return;
    }
    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';
    this.assignmentNotice = '';
    this.adminService.complaints({
      q: this.q.trim() || undefined,
      category: this.category || undefined,
      priority: this.priority || undefined,
      status: this.status || undefined,
      assignedTo: this.assignedTo.trim() || undefined,
      fromDate: this.fromDate || undefined,
      toDate: this.toDate || undefined,
      sortBy: this.sortBy,
      sortDir: this.sortDir,
      page: this.page,
      size: this.size
    }).subscribe({
      next: (response) => {
        this.complaints = response.items;
        this.totalItems = response.totalItems;
        this.totalPages = response.totalPages;
        if (this.isStaff) {
          const newAssignments = this.complaints.filter((item) => item.newAssignment).length;
          if (newAssignments > 0) {
            this.assignmentNotice = `You have ${newAssignments} newly assigned complaint${newAssignments > 1 ? 's' : ''}.`;
          }
        }
        this.loading = false;
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage = error?.error?.error ?? 'Unable to fetch complaints.';
      }
    });
  }

  onFromDateChange(value: string): void {
    this.fromDate = value;
    if (!this.toDate || this.toDate < value) {
      this.toDate = value;
    }
  }

  openComplaint(complaintId: string): void {
    this.detailLoading = true;
    this.errorMessage = '';
    this.successMessage = '';
    this.adminService.complaintDetail(complaintId).subscribe({
      next: (detail) => {
        this.detailLoading = false;
        this.selected = detail;
        this.prefillUpdateForm(detail);
      },
      error: (error) => {
        this.detailLoading = false;
        this.errorMessage = error?.error?.error ?? 'Unable to fetch complaint detail.';
      }
    });
  }

  closeDetail(): void {
    this.selected = null;
  }

  saveUpdate(): void {
    if (!this.selected || this.updating) {
      return;
    }
    const payload = this.buildPayload();
    if (!payload) {
      this.errorMessage = 'No changes to update.';
      return;
    }
    this.updating = true;
    this.errorMessage = '';
    this.successMessage = '';
    this.adminService.updateComplaint(this.selected.complaintId, payload).subscribe({
      next: (updated) => {
        this.updating = false;
        this.selected = updated;
        this.prefillUpdateForm(updated);
        this.successMessage = `Complaint ${updated.complaintId} updated successfully.`;
        this.load();
      },
      error: (error) => {
        this.updating = false;
        this.errorMessage = error?.error?.error ?? 'Unable to update complaint.';
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
    if (this.page === 0) {
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

  get invalidDateRange(): boolean {
    return !!this.fromDate && !!this.toDate && this.fromDate > this.toDate;
  }

  get isAdmin(): boolean {
    return this.session.value?.role === 'ADMIN';
  }

  get isStaff(): boolean {
    return this.session.value?.role === 'STAFF';
  }

  get displayName(): string {
    return this.session.value?.name || this.session.value?.userId || 'Staff';
  }

  private prefillUpdateForm(item: AdminComplaintItem): void {
    const assigned = item.assignedStaffMember || '';
    this.staffDigits = assigned.toUpperCase().startsWith('STA-') ? assigned.slice(4) : '';
    this.staffSuggestions = [];
    this.updateForm = {
      assignedStaffMember: assigned,
      assignedDepartment: item.assignedDepartment || '',
      status: item.currentStatus || '',
      supportResponse: item.supportResponse || '',
      resolutionNotes: item.resolutionNotes || '',
      actionDetails: ''
    };
  }

  onStaffDigitsChange(raw: string): void {
    const digits = String(raw ?? '').replace(/\D/g, '').slice(0, 6);
    this.staffDigits = digits;
    this.updateForm.assignedStaffMember = digits ? `STA-${digits}` : '';

    if (this.staffLookupTimer) {
      clearTimeout(this.staffLookupTimer);
      this.staffLookupTimer = null;
    }
    if (!digits) {
      this.staffSuggestions = [];
      this.staffLookupLoading = false;
      return;
    }

    this.staffLookupLoading = true;
    const prefix = `STA-${digits}`;
    this.staffLookupTimer = setTimeout(() => {
      this.adminService.users({
        q: prefix,
        role: 'STAFF',
        status: 'ACTIVE',
        sortBy: 'userId',
        sortDir: 'asc',
        page: 0,
        size: 8
      }).subscribe({
        next: (response) => {
          this.staffSuggestions = (response.items || [])
            .filter((item) => item.userId.toUpperCase().startsWith(prefix.toUpperCase()))
            .map((item) => ({
              userId: item.userId,
              name: item.name,
              department: item.department || ''
            }));
          this.staffLookupLoading = false;
        },
        error: () => {
          this.staffSuggestions = [];
          this.staffLookupLoading = false;
        }
      });
    }, 180);
  }

  selectStaffSuggestion(suggestion: { userId: string; name: string; department: string }): void {
    this.updateForm.assignedStaffMember = suggestion.userId;
    this.staffDigits = suggestion.userId.replace(/^STA-/, '');
    this.updateForm.assignedDepartment = suggestion.department;
    this.staffSuggestions = [];
  }

  private buildPayload(): AdminComplaintUpdatePayload | null {
    if (!this.selected) {
      return null;
    }
    const payload: AdminComplaintUpdatePayload = {};
    const staff = this.updateForm.assignedStaffMember.trim();
    const department = this.updateForm.assignedDepartment.trim();
    const status = this.updateForm.status.trim();
    const supportResponse = this.updateForm.supportResponse.trim();
    const resolutionNotes = this.updateForm.resolutionNotes.trim();
    const actionDetails = this.updateForm.actionDetails.trim();

    if (this.isAdmin && staff !== (this.selected.assignedStaffMember || '')) {
      payload.assignedStaffMember = staff;
    }
    if (this.isAdmin && department !== (this.selected.assignedDepartment || '')) {
      payload.assignedDepartment = department;
    }
    if (status && status !== this.selected.currentStatus) {
      payload.status = status;
    }
    if (supportResponse !== (this.selected.supportResponse || '')) {
      payload.supportResponse = supportResponse;
    }
    if (this.isAdmin && resolutionNotes !== (this.selected.resolutionNotes || '')) {
      payload.resolutionNotes = resolutionNotes;
    }
    if (actionDetails) {
      payload.actionDetails = actionDetails;
    }

    return Object.keys(payload).length ? payload : null;
  }
}
