import { Component } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { NgIf } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { CustomerService } from '../../../core/services/customer.service';
import { AuthSessionService } from '../../../core/services/auth-session.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, NgIf, RouterLink],
  templateUrl: './login.component.html'
})
export class LoginComponent {
  isSubmitting = false;
  errorMessage = '';

  form = this.fb.group({
    username: ['', [Validators.required]],
    password: ['', [Validators.required]]
  });

  constructor(
    private fb: FormBuilder,
    private customerService: CustomerService,
    private session: AuthSessionService,
    private router: Router
  ) {}

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = '';

    const payload = {
      username: String(this.form.value.username ?? '').trim(),
      password: String(this.form.value.password ?? '')
    };

    this.customerService.login(payload).subscribe({
      next: (result) => {
        this.isSubmitting = false;
        this.form.reset();
        this.session.set({
          userId: result.userId,
          name: result.name,
          email: result.email,
          mobile: result.mobile,
          address: result.address
        });
        this.router.navigateByUrl('/dashboard');
      },
      error: (error) => {
        this.isSubmitting = false;
        const apiError = error?.error;
        if (apiError?.error) {
          this.errorMessage = apiError.error;
        } else {
          this.errorMessage = 'Invalid username or password.';
        }
      }
    });
  }
}
