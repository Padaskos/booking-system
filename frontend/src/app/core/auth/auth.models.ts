export interface LoginRequest {
  email: string;
  password: string;
}

export interface UserDetails {
  id: string;
  name: string;
  email: string;
  role: 'USER' | 'ADMIN';
}
