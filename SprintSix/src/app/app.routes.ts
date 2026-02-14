import { Routes } from '@angular/router';
import { AppShellComponent } from './shared/layout/app-shell.component';
import { HomeComponent } from './features/customer/registration/home.component';
import { CustomerRegistrationComponent } from './features/customer/registration/customer-registration.component';
import { RegistrationSuccessComponent } from './features/customer/registration/registration-success.component';
import { LoginComponent } from './features/customer/auth/login.component';
import { PasswordResetComponent } from './features/customer/auth/password-reset.component';
import { CustomerHomeComponent } from './features/customer/portal/customer-home.component';
import { SearchAvailabilityComponent } from './features/customer/portal/search-availability.component';
import { MyBookingsComponent } from './features/customer/portal/my-bookings.component';
import { ContactUsComponent } from './features/customer/portal/contact-us.component';
import { ComplaintsComponent } from './features/customer/portal/complaints.component';
import { ProfileComponent } from './features/customer/portal/profile.component';
import { BookingConfirmationComponent } from './features/customer/portal/booking-confirmation.component';
import { PaymentComponent } from './features/customer/portal/payment.component';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    component: AppShellComponent,
    children: [
      { path: '', component: HomeComponent },
      { path: 'register', component: CustomerRegistrationComponent },
      { path: 'register/success', component: RegistrationSuccessComponent },
      { path: 'login', component: LoginComponent },
      { path: 'password-reset', component: PasswordResetComponent },
      { path: 'dashboard', component: CustomerHomeComponent, canActivate: [authGuard] },
      { path: 'search', component: SearchAvailabilityComponent, canActivate: [authGuard] },
      { path: 'bookings', component: MyBookingsComponent, canActivate: [authGuard] },
      { path: 'contact', component: ContactUsComponent, canActivate: [authGuard] },
      { path: 'complaints', component: ComplaintsComponent, canActivate: [authGuard] },
      { path: 'complaints/view', component: ComplaintsComponent, canActivate: [authGuard] },
      { path: 'profile', component: ProfileComponent, canActivate: [authGuard] },
      {
        path: 'booking',
        children: [
          { path: 'confirm', component: BookingConfirmationComponent, canActivate: [authGuard] }
        ]
      },
      { path: 'payment', component: PaymentComponent, canActivate: [authGuard] }
    ]
  }
];
