import { Routes } from '@angular/router';
import { AppShellComponent } from './shared/layout/app-shell.component';
import { HomeComponent } from './features/customer/registration/home.component';
import { CustomerRegistrationComponent } from './features/customer/registration/customer-registration.component';
import { RegistrationSuccessComponent } from './features/customer/registration/registration-success.component';
import { LoginComponent } from './features/customer/auth/login.component';
import { ChangePasswordComponent } from './features/customer/auth/change-password.component';
import { PasswordResetComponent } from './features/customer/auth/password-reset.component';
import { CustomerHomeComponent } from './features/customer/portal/customer-home.component';
import { SearchAvailabilityComponent } from './features/customer/portal/search-availability.component';
import { MyBookingsComponent } from './features/customer/portal/my-bookings.component';
import { ContactUsComponent } from './features/customer/portal/contact-us.component';
import { ComplaintsComponent } from './features/customer/portal/complaints.component';
import { ProfileComponent } from './features/customer/portal/profile.component';
import { BookingConfirmationComponent } from './features/customer/portal/booking-confirmation.component';
import { PaymentComponent } from './features/customer/portal/payment.component';
import { CabServiceComponent } from './features/customer/portal/cab-service.component';
import { SalonServiceComponent } from './features/customer/portal/salon-service.component';
import { DiningServiceComponent } from './features/customer/portal/dining-service.component';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';
import { adminOrStaffGuard } from './core/guards/admin-or-staff.guard';
import { AdminHomeComponent } from './features/admin/admin-home.component';
import { AdminRoomsComponent } from './features/admin/admin-rooms.component';
import { AdminBookingsComponent } from './features/admin/admin-bookings.component';
import { AdminUsersComponent } from './features/admin/admin-users.component';
import { AdminReportsComponent } from './features/admin/admin-reports.component';
import { AdminBillsComponent } from './features/admin/admin-bills.component';
import { AdminComplaintsComponent } from './features/admin/admin-complaints.component';
import { AdminLoginComponent } from './features/admin/admin-login.component';
import { AdminServicesComponent } from './features/admin/admin-services.component';

export const routes: Routes = [
  {
    path: '',
    component: AppShellComponent,
    children: [
      { path: '', component: HomeComponent },
      { path: 'register', component: CustomerRegistrationComponent },
      { path: 'register/success', component: RegistrationSuccessComponent },
      { path: 'login', component: LoginComponent },
      { path: 'change-password', component: ChangePasswordComponent, canActivate: [authGuard] },
      { path: 'password-reset', component: PasswordResetComponent },
      { path: 'dashboard', component: CustomerHomeComponent, canActivate: [authGuard] },
      { path: 'search', component: SearchAvailabilityComponent, canActivate: [authGuard] },
      { path: 'bookings', component: MyBookingsComponent, canActivate: [authGuard] },
      { path: 'contact', component: ContactUsComponent, canActivate: [authGuard] },
      { path: 'complaints', component: ComplaintsComponent, canActivate: [authGuard] },
      { path: 'complaints/view', component: ComplaintsComponent, canActivate: [authGuard] },
      { path: 'services/cab', component: CabServiceComponent, canActivate: [authGuard] },
      { path: 'services/salon', component: SalonServiceComponent, canActivate: [authGuard] },
      { path: 'services/dining', component: DiningServiceComponent, canActivate: [authGuard] },
      { path: 'profile', component: ProfileComponent, canActivate: [authGuard] },
      {
        path: 'booking',
        children: [
          { path: 'confirm', component: BookingConfirmationComponent, canActivate: [authGuard] }
        ]
      },
      { path: 'payment', component: PaymentComponent, canActivate: [authGuard] },
      { path: 'admin', component: AdminLoginComponent },
      { path: 'admin/home', component: AdminHomeComponent, canActivate: [authGuard, adminGuard] },
      { path: 'admin/rooms', component: AdminRoomsComponent, canActivate: [authGuard, adminGuard] },
      { path: 'admin/bookings', component: AdminBookingsComponent, canActivate: [authGuard, adminGuard] },
      { path: 'admin/bills', component: AdminBillsComponent, canActivate: [authGuard, adminGuard] },
      { path: 'admin/complaints', component: AdminComplaintsComponent, canActivate: [authGuard, adminOrStaffGuard] },
      { path: 'admin/users', component: AdminUsersComponent, canActivate: [authGuard, adminGuard] },
      { path: 'admin/reports', component: AdminReportsComponent, canActivate: [authGuard, adminGuard] },
      { path: 'admin/services', component: AdminServicesComponent, canActivate: [authGuard, adminGuard] }
    ]
  }
];
