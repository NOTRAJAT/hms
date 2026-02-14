export interface CustomerRegistrationPayload {
  name: string;
  email: string;
  countryCode: string;
  mobileNumber: string;
  address: string;
  username: string;
  password: string;
}

export interface CustomerRegistrationResult {
  userId: string;
  name: string;
  email: string;
}
