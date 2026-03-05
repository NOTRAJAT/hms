import { Component } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { NgFor, NgIf } from '@angular/common';
import { Router } from '@angular/router';
import { AuthSessionService } from '../../../core/services/auth-session.service';
import { CustomerService } from '../../../core/services/customer.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [ReactiveFormsModule, NgIf, NgFor],
  templateUrl: './profile.component.html'
})
export class ProfileComponent {
  readonly countryCodes = ['+91'];
  isEditing = false;
  isSaving = false;
  successMessage = '';
  errorMessage = '';
  serverErrors: Record<string, string> = {};

  private readonly fullNamePattern = /^[A-Za-z ]{2,50}$/;
  private readonly countryCodePattern = /^\+91$/;
  private readonly mobileNumberPattern = /^[789]\d{9}$/;

  form = this.fb.group({
    name: ['', [Validators.required, Validators.pattern(this.fullNamePattern)]],
    email: ['', [Validators.required, Validators.email]],
    countryCode: ['+91', [Validators.required, Validators.pattern(this.countryCodePattern)]],
    mobileNumber: ['', [Validators.required, Validators.pattern(this.mobileNumberPattern)]],
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
      countryCode: '+91',
      mobileNumber: this.extractMobileNumber(data.mobile),
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
    const countryCode = String(this.form.value.countryCode ?? '').trim();
    const mobileNumber = String(this.form.value.mobileNumber ?? '').trim();
    this.customerService.updateProfile(this.session.value.userId, {
      name: String(this.form.value.name ?? '').trim(),
      email: String(this.form.value.email ?? '').trim(),
      mobile: `${countryCode}${mobileNumber}`,
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
    const current = String(this.form.get('mobileNumber')?.value ?? '');
    const cleaned = current.replace(/\D/g, '').slice(0, 10);
    if (cleaned !== current) {
      this.form.get('mobileNumber')?.setValue(cleaned, { emitEvent: false });
    }
  }

  private extractMobileNumber(value: string | null | undefined): string {
    const raw = String(value ?? '').trim();
    if (raw.startsWith('+91') && raw.length >= 13) {
      return raw.slice(3, 13);
    }
    const digits = raw.replace(/\D/g, '');
    return digits.length <= 10 ? digits : digits.slice(-10);
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
