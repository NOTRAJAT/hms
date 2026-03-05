import { Component } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { NgFor, NgIf } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { CustomerService } from '../../../core/services/customer.service';
import { RegistrationStateService } from '../../../core/services/registration-state.service';
import {
  confirmPasswordValidator,
  mobileValidator,
  nameValidator,
  passwordStrengthValidator,
  usernameValidator
} from '../../../core/validators/registration.validators';

@Component({
  selector: 'app-customer-registration',
  standalone: true,
  imports: [ReactiveFormsModule, NgIf, NgFor, RouterLink],
  templateUrl: './customer-registration.component.html'
})
export class CustomerRegistrationComponent {
  readonly countryCodes = ['+91'];
  isSubmitting = false;
  serverErrors: Record<string, string> = {};

  form = this.fb.group({
    name: ['', [Validators.required, nameValidator]],
    email: ['', [Validators.required, Validators.email]],
    countryCode: ['+91', [Validators.required]],
    mobileNumber: ['', [Validators.required, mobileValidator]],
    address: ['', [Validators.required, Validators.minLength(10)]],
    username: ['', [Validators.required, usernameValidator]],
    password: ['', [Validators.required, passwordStrengthValidator]],
    confirmPassword: ['', [Validators.required, confirmPasswordValidator('password')]]
  });

  constructor(
    private fb: FormBuilder,
    private customerService: CustomerService,
    private registrationState: RegistrationStateService,
    private router: Router
  ) {
    this.form.valueChanges.subscribe(() => {
      this.serverErrors = {};
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    const payload = {
      name: String(this.form.value.name ?? '').trim(),
      email: String(this.form.value.email ?? '').trim(),
      countryCode: String(this.form.value.countryCode ?? '').trim(),
      mobileNumber: String(this.form.value.mobileNumber ?? '').trim(),
      address: String(this.form.value.address ?? '').trim(),
      username: String(this.form.value.username ?? '').trim(),
      password: String(this.form.value.password ?? '')
    };

    this.customerService.register(payload).subscribe({
      next: (result) => {
        this.registrationState.set(result);
        this.form.reset();
        this.isSubmitting = false;
        this.router.navigateByUrl('/register/success');
      },
      error: (error) => {
        this.isSubmitting = false;
        const apiError = error?.error;
        if (apiError?.field && apiError?.error) {
          this.serverErrors[apiError.field] = apiError.error;
        }
      }
    });
  }

  reset(): void {
    this.form.reset({
      countryCode: '+91'
    });
    this.serverErrors = {};
  }

  enforceNumericMobile(value: string | null | undefined): void {
    const raw = String(value ?? '');
    const digitsOnly = raw.replace(/\D/g, '').slice(0, 10);
    if (digitsOnly !== raw) {
      this.form.get('mobileNumber')?.setValue(digitsOnly, { emitEvent: false });
    }
  }
}
