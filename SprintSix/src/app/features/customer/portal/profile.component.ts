import { Component } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { NgIf } from '@angular/common';
import { Router } from '@angular/router';
import { AuthSessionService } from '../../../core/services/auth-session.service';
import { CustomerService } from '../../../core/services/customer.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [ReactiveFormsModule, NgIf],
  templateUrl: './profile.component.html'
})
export class ProfileComponent {
  isEditing = false;
  isSaving = false;
  successMessage = '';
  errorMessage = '';
  serverErrors: Record<string, string> = {};

  private readonly fullNamePattern = /^[A-Za-z ]{2,50}$/;
  private readonly phonePattern = /^\+[0-9]{1,3}[0-9]{10}$/;

  form = this.fb.group({
    name: ['', [Validators.required, Validators.pattern(this.fullNamePattern)]],
    email: ['', [Validators.required, Validators.email]],
    mobile: ['', [Validators.required, Validators.pattern(this.phonePattern)]],
    address: ['', [Validators.maxLength(100)]]
  });

  constructor(
    public session: AuthSessionService,
    private customerService: CustomerService,
    private router: Router,
    private fb: FormBuilder
  ) {
    this.patchForm();
    this.form.valueChanges.subscribe(() => {
      this.successMessage = '';
      this.errorMessage = '';
      this.serverErrors = {};
    });
  }

  patchForm(): void {
    const data = this.session.value;
    if (!data) {
      return;
    }
    this.form.patchValue({
      name: data.name,
      email: data.email,
      mobile: data.mobile,
      address: data.address
    });
  }

  edit(): void {
    this.successMessage = '';
    this.errorMessage = '';
    this.serverErrors = {};
    this.isEditing = true;
  }

  cancel(): void {
    this.isEditing = false;
    this.successMessage = '';
    this.errorMessage = '';
    this.serverErrors = {};
    this.patchForm();
  }

  save(): void {
    if (this.form.invalid || !this.session.value) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSaving = true;
    this.customerService.updateProfile(this.session.value.userId, {
      name: String(this.form.value.name ?? '').trim(),
      email: String(this.form.value.email ?? '').trim(),
      mobile: String(this.form.value.mobile ?? '').trim(),
      address: String(this.form.value.address ?? '').trim()
    }).subscribe({
      next: (result) => {
        this.session.set({
          userId: result.userId,
          name: result.name,
          email: result.email,
          mobile: result.mobile,
          address: result.address,
          role: result.role,
          passwordChangeRequired: result.passwordChangeRequired
        });
        this.isSaving = false;
        this.isEditing = false;
        this.successMessage = 'Your profile has been updated successfully.';
        this.errorMessage = '';
        this.serverErrors = {};
      },
      error: (error) => {
        this.isSaving = false;
        this.successMessage = '';
        const apiError = error?.error;
        if (apiError?.field && apiError?.error) {
          this.serverErrors[apiError.field] = apiError.error;
          this.errorMessage = '';
          return;
        }
        this.errorMessage = apiError?.error ?? 'Unable to update profile right now.';
      }
    });
  }

  mobileInput(): void {
    const current = String(this.form.get('mobile')?.value ?? '');
    const cleaned = current.replace(/[^0-9+]/g, '');
    if (cleaned !== current) {
      this.form.get('mobile')?.setValue(cleaned, { emitEvent: false });
    }
  }

  logout(): void {
    this.customerService.logout().subscribe({
      next: () => {
        this.session.clear();
        this.router.navigateByUrl('/login');
      },
      error: () => {
        this.session.clear();
        this.router.navigateByUrl('/login');
      }
    });
  }
}
