import { Component } from '@angular/core';
import { NgIf } from '@angular/common';
import { RouterLink } from '@angular/router';
import { RegistrationStateService } from '../../../core/services/registration-state.service';
import { CustomerRegistrationResult } from '../../../core/models/customer.model';

@Component({
  selector: 'app-registration-success',
  standalone: true,
  imports: [NgIf, RouterLink],
  templateUrl: './registration-success.component.html'
})
export class RegistrationSuccessComponent {
  result: CustomerRegistrationResult | null;

  constructor(private registrationState: RegistrationStateService) {
    this.result = this.registrationState.consume();
  }
}
