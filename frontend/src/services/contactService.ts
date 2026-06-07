import api from './api';
import type { Contact, ContactRequest } from '../types/contact';

export async function listContacts(leadId: string): Promise<Contact[]> {
  const response = await api.get<{ data: Contact[] }>(`/leads/${leadId}/contacts`);
  return response.data.data;
}

export async function createContact(leadId: string, data: ContactRequest): Promise<Contact> {
  const response = await api.post<{ data: Contact }>(`/leads/${leadId}/contacts`, data);
  return response.data.data;
}

export async function deleteContact(leadId: string, contactId: string): Promise<void> {
  await api.delete(`/leads/${leadId}/contacts/${contactId}`);
}

export async function setMainContact(leadId: string, contactId: string): Promise<Contact> {
  const response = await api.patch<{ data: Contact }>(`/leads/${leadId}/contacts/${contactId}/main`);
  return response.data.data;
}
