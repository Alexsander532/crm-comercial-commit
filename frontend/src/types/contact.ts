export interface Contact {
  id: string;
  leadId: string;
  name: string;
  role: string | null;
  phone: string | null;
  email: string | null;
  whatsapp: string | null;
  isMain: boolean;
  notes: string | null;
  createdAt: string;
}

export interface ContactRequest {
  name: string;
  role?: string;
  phone?: string;
  email?: string;
  whatsapp?: string;
  notes?: string;
}
