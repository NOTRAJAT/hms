import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

const NAME_PATTERN = /^[A-Za-z ]+$/;
const USERNAME_PATTERN = /^\S+$/;
const MOBILE_PATTERN = /^\d{8,10}$/;

export const nameValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const value = String(control.value ?? '').trim();
  if (!value) {
    return null;
  }
  if (value.length < 3 || !NAME_PATTERN.test(value)) {
    return { nameInvalid: true };
  }
  return null;
};

export const usernameValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const value = String(control.value ?? '').trim();
  if (!value) {
    return null;
  }
  if (value.length < 5 || !USERNAME_PATTERN.test(value)) {
    return { usernameInvalid: true };
  }
  return null;
};

export const mobileValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const value = String(control.value ?? '').trim();
  if (!value) {
    return null;
  }
  if (!MOBILE_PATTERN.test(value)) {
    return { mobileInvalid: true };
  }
  return null;
};

export const passwordStrengthValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const value = String(control.value ?? '');
  if (!value) {
    return null;
  }
  const hasUpper = /[A-Z]/.test(value);
  const hasLower = /[a-z]/.test(value);
  const hasDigit = /\d/.test(value);
  const hasSpecial = /[^A-Za-z0-9]/.test(value);
  const meetsLength = value.length >= 8;
  return hasUpper && hasLower && hasDigit && hasSpecial && meetsLength
    ? null
    : { passwordWeak: true };
};

export const confirmPasswordValidator = (passwordControlName: string): ValidatorFn => {
  return (control: AbstractControl): ValidationErrors | null => {
    const parent = control.parent;
    if (!parent) {
      return null;
    }
    const passwordValue = String(parent.get(passwordControlName)?.value ?? '');
    const confirmValue = String(control.value ?? '');
    if (!confirmValue) {
      return null;
    }
    return passwordValue === confirmValue ? null : { passwordMismatch: true };
  };
};
