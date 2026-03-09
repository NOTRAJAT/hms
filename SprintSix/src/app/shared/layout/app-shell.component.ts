import { Component, HostListener } from '@angular/core';
import { NgIf, NgTemplateOutlet } from '@angular/common';
import { NavigationEnd, Router } from '@angular/router';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthSessionService } from '../../core/services/auth-session.service';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, NgIf, NgTemplateOutlet],
  templateUrl: './app-shell.component.html'
})
export class AppShellComponent {
  mobileMenuOpen = false;
  customerMoreMenuOpen = false;
  adminMoreMenuOpen = false;
  readonly currentYear = new Date().getFullYear();

  constructor(
    public session: AuthSessionService,
    private router: Router
  ) {
    this.router.events.subscribe((event) => {
      if (event instanceof NavigationEnd) {
        this.closeMobileMenu();
        this.closeCustomerMoreMenu();
        this.closeAdminMoreMenu();
      }
    });
  }

  get isAdmin(): boolean {
    return this.session.value?.role === 'ADMIN';
  }

  get isStaff(): boolean {
    return this.session.value?.role === 'STAFF';
  }

  get roleLabel(): string {
    if (this.isAdmin) {
      return 'Admin Portal';
    }
    if (this.isStaff) {
      return 'Staff Console';
    }
    if (this.session.value) {
      return 'Guest Experience';
    }
    return 'Public Access';
  }

  get desktopNavClass(): string {
    if (this.isAdmin) {
      return 'app-nav-desktop app-nav-desktop--compact';
    }
    return 'app-nav-desktop app-nav-desktop--regular';
  }

  openMobileMenu(): void {
    this.mobileMenuOpen = true;
  }

  closeMobileMenu(): void {
    this.mobileMenuOpen = false;
  }

  toggleMobileMenu(): void {
    this.mobileMenuOpen = !this.mobileMenuOpen;
  }

  toggleCustomerMoreMenu(): void {
    this.customerMoreMenuOpen = !this.customerMoreMenuOpen;
    if (this.customerMoreMenuOpen) {
      this.adminMoreMenuOpen = false;
    }
  }

  closeCustomerMoreMenu(): void {
    this.customerMoreMenuOpen = false;
  }

  toggleAdminMoreMenu(): void {
    this.adminMoreMenuOpen = !this.adminMoreMenuOpen;
    if (this.adminMoreMenuOpen) {
      this.customerMoreMenuOpen = false;
    }
  }

  closeAdminMoreMenu(): void {
    this.adminMoreMenuOpen = false;
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    this.closeMobileMenu();
    this.closeCustomerMoreMenu();
    this.closeAdminMoreMenu();
  }
}
