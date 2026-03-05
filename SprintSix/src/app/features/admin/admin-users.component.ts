import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgFor, NgIf } from '@angular/common';
import {
  AdminUserCreatePayload,
  AdminUserItem,
  AdminUserUpdatePayload
} from '../../core/models/admin.model';
import { AdminService } from '../../core/services/admin.service';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [FormsModule, NgFor, NgIf],
  templateUrl: './admin-users.component.html'
})
export class AdminUsersComponent implements OnInit {
  readonly countryCodeOptions = ['+91'];
  createCountryCode = '+91';
  editCountryCode = '+91';
  createMobileNumber = '';
  editMobileNumber = '';

  users: AdminUserItem[] = [];
  loading = false;
  errorMessage = '';
  successMessage = '';

  q = '';
  role = '';
  status = '';
  sortBy = 'username';
  sortDir: 'asc' | 'desc' = 'asc';
  page = 0;
  readonly size = 10;
  totalItems = 0;
  totalPages = 0;

  createMode = false;
  createSubmitted = false;
  creating = false;
  createdTempPassword = '';
  createTouched = {
    username: false,
    email: false,
    mobile: false
  };
  createForm: AdminUserCreatePayload = {
    username: '',
    role: 'CUSTOMER',
    email: '',
    mobile: '',
    name: '',
    department: ''
  };

  selected: AdminUserItem | null = null;
  updating = false;
  editSubmitted = false;
  editForm: AdminUserUpdatePayload = {
    role: 'CUSTOMER',
    email: '',
    mobile: '',
    status: 'ACTIVE',
    department: ''
  };

  pendingStatusUser: AdminUserItem | null = null;
  pendingStatus: 'ACTIVE' | 'INACTIVE' = 'INACTIVE';
  changingStatus = false;

  resetTarget: AdminUserItem | null = null;
  resettingPassword = false;
  resetTempPassword = '';

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
    this.adminService.users({
      q: this.q.trim() || undefined,
      role: this.role || undefined,
      status: this.status || undefined,
      sortBy: this.sortBy,
      sortDir: this.sortDir,
      page: this.page,
      size: this.size
    }).subscribe({
      next: (response) => {
        this.users = response.items;
        this.totalItems = response.totalItems;
        this.totalPages = response.totalPages;
        this.loading = false;
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage = error?.error?.error ?? 'Unable to fetch users.';
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

  openCreate(): void {
    this.createMode = true;
    this.createSubmitted = false;
    this.createdTempPassword = '';
    this.createTouched = {
      username: false,
      email: false,
      mobile: false
    };
    this.createForm = {
      username: '',
      role: 'CUSTOMER',
      email: '',
      mobile: '',
      name: '',
      department: ''
    };
    this.createCountryCode = '+91';
    this.createMobileNumber = '';
  }

  closeCreate(): void {
    this.createMode = false;
  }

  submitCreate(): void {
    this.createSubmitted = true;
    if (!this.isCreateValid || this.creating) {
      return;
    }
    this.creating = true;
    const payload: AdminUserCreatePayload = {
      ...this.createForm,
      mobile: this.composeMobile(this.createCountryCode, this.createMobileNumber)
    };
    this.adminService.createUser(payload).subscribe({
      next: (result) => {
        this.creating = false;
        this.createdTempPassword = result.temporaryPassword;
        this.successMessage = result.message;
        this.load(true);
      },
      error: (error) => {
        this.creating = false;
        this.errorMessage = error?.error?.error ?? 'Unable to create user.';
      }
    });
  }

  startEdit(user: AdminUserItem): void {
    this.selected = user;
    this.editSubmitted = false;
    this.editCountryCode = '+91';
    this.editMobileNumber = this.extractMobileNumber(user.mobile);
    this.editForm = {
      role: user.role || 'CUSTOMER',
      email: user.email,
      mobile: this.composeMobile(this.editCountryCode, this.editMobileNumber),
      status: user.status,
      department: user.department || ''
    };
  }

  closeEdit(): void {
    this.selected = null;
  }

  submitEdit(): void {
    if (!this.selected) {
      return;
    }
    this.editSubmitted = true;
    if (!this.isEditValid || this.updating) {
      return;
    }
    this.updating = true;
    const payload: AdminUserUpdatePayload = {
      ...this.editForm,
      mobile: this.composeMobile(this.editCountryCode, this.editMobileNumber)
    };
    this.adminService.updateUser(this.selected.userId, payload).subscribe({
      next: () => {
        this.updating = false;
        this.successMessage = 'User account updated successfully.';
        this.selected = null;
        this.load();
      },
      error: (error) => {
        this.updating = false;
        this.errorMessage = error?.error?.error ?? 'Unable to update user.';
      }
    });
  }

  promptStatusChange(user: AdminUserItem, status: 'ACTIVE' | 'INACTIVE'): void {
    this.pendingStatusUser = user;
    this.pendingStatus = status;
  }

  closeStatusPrompt(): void {
    this.pendingStatusUser = null;
  }

  confirmStatusChange(): void {
    if (!this.pendingStatusUser || this.changingStatus) {
      return;
    }
    this.changingStatus = true;
    this.adminService.updateUserStatus(this.pendingStatusUser.userId, this.pendingStatus).subscribe({
      next: () => {
        this.changingStatus = false;
        this.successMessage = this.pendingStatus === 'INACTIVE'
          ? 'User deactivated successfully.'
          : 'User reactivated successfully.';
        this.pendingStatusUser = null;
        this.load();
      },
      error: (error) => {
        this.changingStatus = false;
        this.errorMessage = error?.error?.error ?? 'Unable to change account status.';
      }
    });
  }

  promptResetPassword(user: AdminUserItem): void {
    this.resetTarget = user;
    this.resetTempPassword = '';
  }

  closeResetPrompt(): void {
    this.resetTarget = null;
  }

  confirmResetPassword(): void {
    if (!this.resetTarget || this.resettingPassword) {
      return;
    }
    this.resettingPassword = true;
    this.adminService.resetUserPassword(this.resetTarget.userId).subscribe({
      next: (result) => {
        this.resettingPassword = false;
        this.resetTempPassword = result.temporaryPassword;
        this.successMessage = result.message;
      },
      error: (error) => {
        this.resettingPassword = false;
        this.errorMessage = error?.error?.error ?? 'Unable to reset password.';
      }
    });
  }

  get usernameInvalid(): boolean {
    return !/^[a-zA-Z0-9._-]{5,30}$/.test(this.createForm.username);
  }

  get emailInvalid(): boolean {
    return !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.createForm.email);
  }

  get mobileInvalid(): boolean {
    return this.createCountryCode !== '+91' || !/^[789]\d{9}$/.test(this.createMobileNumber);
  }

  get editEmailInvalid(): boolean {
    return !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.editForm.email);
  }

  get editMobileInvalid(): boolean {
    return this.editCountryCode !== '+91' || !/^[789]\d{9}$/.test(this.editMobileNumber);
  }

  get isCreateValid(): boolean {
    return !this.usernameInvalid
      && !this.emailInvalid
      && !this.mobileInvalid
      && !!this.createForm.role
      && (!this.requiresDepartment(this.createForm.role) || !!String(this.createForm.department ?? '').trim());
  }

  touchCreateField(field: keyof typeof this.createTouched): void {
    this.createTouched[field] = true;
  }

  get isEditValid(): boolean {
    return !this.editEmailInvalid
      && !this.editMobileInvalid
      && !!this.editForm.role
      && !!this.editForm.status
      && (!this.requiresDepartment(this.editForm.role) || !!String(this.editForm.department ?? '').trim());
  }

  requiresDepartment(role: string): boolean {
    return role === 'STAFF';
  }

  sanitizeMobile(field: 'create' | 'edit'): void {
    if (field === 'create') {
      this.createMobileNumber = this.createMobileNumber.replace(/\D/g, '').slice(0, 10);
      this.createForm.mobile = this.composeMobile(this.createCountryCode, this.createMobileNumber);
    } else {
      this.editMobileNumber = this.editMobileNumber.replace(/\D/g, '').slice(0, 10);
      this.editForm.mobile = this.composeMobile(this.editCountryCode, this.editMobileNumber);
    }
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

  private composeMobile(countryCode: string, mobileNumber: string): string {
    return `${countryCode}${mobileNumber.trim()}`;
  }

  private extractMobileNumber(mobile: string): string {
    const value = String(mobile ?? '').trim();
    if (value.startsWith('+91') && value.length >= 13) {
      return value.slice(3, 13);
    }
    const digits = value.replace(/\D/g, '');
    return digits.length <= 10 ? digits : digits.slice(-10);
  }
}
