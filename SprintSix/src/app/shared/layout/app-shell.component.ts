import { Component } from '@angular/core';
import { NgIf } from '@angular/common';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthSessionService } from '../../core/services/auth-session.service';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, NgIf],
  templateUrl: './app-shell.component.html'
})
export class AppShellComponent {
  constructor(public session: AuthSessionService) {}

  get isAdmin(): boolean {
    return this.session.value?.role === 'ADMIN';
  }

  get isStaff(): boolean {
    return this.session.value?.role === 'STAFF';
  }
}
