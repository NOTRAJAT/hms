import { Injectable } from '@angular/core';
import { CustomerRegistrationResult } from '../models/customer.model';

@Injectable({ providedIn: 'root' })
export class RegistrationStateService {
  private lastRegistration: CustomerRegistrationResult | null = null;

  set(result: CustomerRegistrationResult): void {
    this.lastRegistration = result;
  }

  consume(): CustomerRegistrationResult | null {
    const result = this.lastRegistration;
    this.lastRegistration = null;
    return result;
  }
}
