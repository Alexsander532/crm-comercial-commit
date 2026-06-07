import api from './api';
import type { Lead, LeadRequest, LeadListResponse } from '../types/lead';

export async function createLead(data: LeadRequest): Promise<Lead> {
  const response = await api.post<{ data: Lead }>('/leads', data);
  return response.data.data;
}

export async function listLeads(page = 0, size = 10): Promise<LeadListResponse> {
  const response = await api.get<{ data: LeadListResponse }>('/leads', {
    params: { page, size },
  });
  return response.data.data;
}

export async function getLead(id: string): Promise<Lead> {
  const response = await api.get<{ data: Lead }>(`/leads/${id}`);
  return response.data.data;
}

export async function updateLead(id: string, data: LeadRequest): Promise<Lead> {
  const response = await api.put<{ data: Lead }>(`/leads/${id}`, data);
  return response.data.data;
}

export async function archiveLead(id: string): Promise<void> {
  await api.delete(`/leads/${id}`);
}

export async function searchLeads(q: string): Promise<LeadListResponse> {
  const response = await api.get<{ data: LeadListResponse }>('/leads/search', {
    params: { q },
  });
  return response.data.data;
}
