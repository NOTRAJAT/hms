export interface LoginPayload {
  username: string;
  password: string;
}

export interface LoginResult {
  userId: string;
  name: string;
  email: string;
  mobile: string;
  address: string;
}
