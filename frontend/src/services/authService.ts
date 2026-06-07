import api from './api';
import type { LoginRequest, LoginResponse, User } from '../types/user';

export async function login(data: LoginRequest): Promise<LoginResponse> {
  const response = await api.post<{ data: LoginResponse }>('/auth/login', data);
  return response.data.data;
}

export async function getMe(): Promise<User> {
  const response = await api.get<{ data: User }>('/auth/me');
  return response.data.data;
}

export function isAuthenticated(): boolean {
  return !!localStorage.getItem('token');
}

export function getStoredUser(): User | null {
  const stored = localStorage.getItem('user');
  return stored ? JSON.parse(stored) : null;
}
