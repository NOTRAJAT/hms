import { Component } from '@angular/core';
import { NgIf } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthSessionService } from '../../../core/services/auth-session.service';

@Component({
  selector: 'app-customer-home',
  standalone: true,
  imports: [RouterLink, NgIf],
  templateUrl: './customer-home.component.html'
})
export class CustomerHomeComponent {
  constructor(public session: AuthSessionService) {}
}
