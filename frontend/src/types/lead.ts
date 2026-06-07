export enum LeadStatus {
  NOVO = 'NOVO',
  CONTATO = 'CONTATO',
  NEGOCIACAO = 'NEGOCIACAO',
  GANHO = 'GANHO',
  PERDIDO = 'PERDIDO',
  ARQUIVADO = 'ARQUIVADO',
}

export enum LeadSegment {
  TECNOLOGIA = 'TECNOLOGIA',
  FINANCAS = 'FINANCAS',
  SAUDE = 'SAUDE',
  EDUCACAO = 'EDUCACAO',
  VAREJO = 'VAREJO',
  OUTRO = 'OUTRO',
}

export interface Lead {
  id: string;
  companyName: string;
  site: string | null;
  instagram: string | null;
  whatsapp: string | null;
  address: string | null;
  segment: string;
  notes: string | null;
  status: string;
  createdByName: string;
  assignedToName: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface LeadRequest {
  companyName: string;
  site?: string;
  instagram?: string;
  whatsapp?: string;
  address?: string;
  segment: string;
  notes?: string;
}

export interface LeadListResponse {
  content: Lead[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
