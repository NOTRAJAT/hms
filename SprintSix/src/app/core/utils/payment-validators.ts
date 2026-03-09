import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export const isValidCardNumber = (value: string): boolean => {
  if (!/^\d{16}$/.test(value)) {
    return false;
  }
  let sum = 0;
  let doubleIt = false;
  for (let i = value.length - 1; i >= 0; i -= 1) {
    let digit = Number(value[i]);
    if (doubleIt) {
      digit *= 2;
      if (digit > 9) {
        digit -= 9;
      }
    }
    sum += digit;
    doubleIt = !doubleIt;
  }
  return sum % 10 === 0;
};

export const isExpired = (value: string): boolean => {
  if (!/^(0[1-9]|1[0-2])\/\d{2}$/.test(value)) {
    return false;
  }
  const [month, year] = value.split('/');
  const expYear = 2000 + Number(year);
  const expMonth = Number(month);
  const now = new Date();
  const currentYear = now.getFullYear();
  const currentMonth = now.getMonth() + 1;
  if (expYear < currentYear) {
    return true;
  }
  if (expYear === currentYear && expMonth <= currentMonth) {
    return true;
  }
  return false;
};

export const luhnValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const value = String(control.value ?? '');
  if (!value) {
    return null;
  }
  return isValidCardNumber(value) ? null : { cardNumberInvalid: true };
};

export const expiryValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const value = String(control.value ?? '');
  if (!value) {
    return null;
  }
  const match = /^(0[1-9]|1[0-2])\/\d{2}$/.test(value);
  if (!match) {
    return { expiryInvalid: true };
  }
  return isExpired(value) ? { expiryPast: true } : null;
};

export const isCvvValid = (cardNumber: string, cvv: string): boolean => {
  const isAmex = /^3[47]\d{13}$/.test(cardNumber);
  if (isAmex) {
    return /^\d{4}$/.test(cvv);
  }
  return /^\d{3}$/.test(cvv);
};

export const checkOtpSimulation = (otp: string): 'ok' | 'invalid' | 'expired' => {
  if (otp === '123456') {
    return 'ok';
  }
  if (otp === '000000') {
    return 'expired';
  }
  return 'invalid';
};
