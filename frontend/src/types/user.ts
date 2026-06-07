export enum UserRole {
  DIRETOR = 'DIRETOR',
  GERENTE_AQUISICAO = 'GERENTE_AQUISICAO',
  GERENTE_PROSPECCAO = 'GERENTE_PROSPECCAO',
  AQUISICAO = 'AQUISICAO',
  PROSPECCAO = 'PROSPECCAO',
}

export interface User {
  id: string;
  name: string;
  email: string;
  role: UserRole;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
  role: UserRole;
}

export interface LoginResponse {
  token: string;
  expiresIn: number;
  user: User;
}

export interface ApiResponse<T> {
  data: T | null;
  message: string;
  timestamp: string;
}
