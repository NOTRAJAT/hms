import { Component } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { NgIf } from '@angular/common';
import { Router } from '@angular/router';
import { CustomerService } from '../../../core/services/customer.service';
import { AuthSessionService } from '../../../core/services/auth-session.service';

@Component({
  selector: 'app-change-password',
  standalone: true,
  imports: [ReactiveFormsModule, NgIf],
  templateUrl: './change-password.component.html'
})
export class ChangePasswordComponent {
  isSubmitting = false;
  errorMessage = '';
  successMessage = '';

  form = this.fb.group({
    currentPassword: ['', [Validators.required]],
    newPassword: ['', [Validators.required, Validators.minLength(8)]],
    confirmPassword: ['', [Validators.required]]
  });

  constructor(
    private fb: FormBuilder,
    private customerService: CustomerService,
    private session: AuthSessionService,
    private router: Router
  ) {}

  submit(): void {
    if (this.form.invalid || this.passwordMismatch || this.newPasswordWeak) {
      this.form.markAllAsTouched();
      return;
    }
    this.isSubmitting = true;
    this.errorMessage = '';
    this.customerService.changePassword({
      currentPassword: String(this.form.value.currentPassword ?? ''),
      newPassword: String(this.form.value.newPassword ?? '')
    }).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.successMessage = 'Password updated successfully.';
        const current = this.session.value;
        if (current) {
          this.session.set({ ...current, passwordChangeRequired: false });
          if (current.role === 'ADMIN') {
            this.router.navigateByUrl('/admin/home');
          } else if (current.role === 'STAFF') {
            this.router.navigateByUrl('/admin/complaints');
          } else {
            this.router.navigateByUrl('/dashboard');
          }
        } else {
          this.router.navigateByUrl('/login');
        }
      },
      error: (error) => {
        this.isSubmitting = false;
        this.errorMessage = error?.error?.error ?? 'Unable to change password.';
      }
    });
  }

  get passwordMismatch(): boolean {
    return String(this.form.value.newPassword ?? '') !== String(this.form.value.confirmPassword ?? '');
  }

  get newPasswordWeak(): boolean {
    const value = String(this.form.value.newPassword ?? '');
    if (value.length < 8) {
      return true;
    }
    const hasUpper = /[A-Z]/.test(value);
    const hasLower = /[a-z]/.test(value);
    const hasDigit = /[0-9]/.test(value);
    const hasSpecial = /[^A-Za-z0-9]/.test(value);
    return !(hasUpper && hasLower && hasDigit && hasSpecial);
  }
}
