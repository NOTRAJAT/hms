import { Component, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { NgIf } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { CustomerService } from '../../core/services/customer.service';
import { AuthSessionService } from '../../core/services/auth-session.service';

@Component({
  selector: 'app-admin-login',
  standalone: true,
  imports: [ReactiveFormsModule, NgIf, RouterLink],
  templateUrl: './admin-login.component.html'
})
export class AdminLoginComponent implements OnInit {
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

  ngOnInit(): void {
    this.isSubmitting = true;
    this.session.invalidateServerSession().subscribe({
      next: () => {
        this.isSubmitting = false;
      }
    });
  }

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
        if (result.role !== 'ADMIN' && result.role !== 'STAFF') {
          this.errorMessage = 'Admin/Staff access only. Please use customer login.';
          return;
        }
        this.form.reset();
        this.session.set({
          userId: result.userId,
          name: result.name,
          email: result.email,
          mobile: result.mobile,
          address: result.address,
          role: result.role,
          passwordChangeRequired: result.passwordChangeRequired
        });
        if (result.passwordChangeRequired) {
          this.router.navigateByUrl('/change-password');
        } else {
          this.router.navigateByUrl(result.role === 'ADMIN' ? '/admin/home' : '/admin/complaints');
        }
      },
      error: (error) => {
        this.isSubmitting = false;
        const apiError = error?.error;
        this.errorMessage = apiError?.error ?? 'Invalid admin/staff username or password.';
      }
    });
  }
}
