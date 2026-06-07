-- ============================================================
-- V2__create_leads.sql
-- Criação da tabela de leads (company_name + segment obrigatórios,
-- address opcional, enriched_data JSONB reservado para pós-MVP)
-- ============================================================

CREATE TABLE leads (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_name VARCHAR(200) NOT NULL,
    site VARCHAR(500),
    instagram VARCHAR(200),
    whatsapp VARCHAR(20),
    address TEXT,
    segment VARCHAR(100) NOT NULL CHECK (segment IN (
        'TECNOLOGIA', 'FINANCAS', 'SAUDE', 'EDUCACAO', 'VAREJO', 'OUTRO'
    )),
    notes TEXT,
    enriched_data JSONB,
    status VARCHAR(20) NOT NULL DEFAULT 'NOVO' CHECK (status IN (
        'NOVO', 'CONTATO', 'NEGOCIACAO', 'GANHO', 'PERDIDO', 'ARQUIVADO'
    )),
    created_by UUID NOT NULL REFERENCES users(id),
    assigned_to UUID REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_leads_status ON leads(status);
CREATE INDEX idx_leads_assigned ON leads(assigned_to);
CREATE INDEX idx_leads_created_by ON leads(created_by);
CREATE INDEX idx_leads_segment ON leads(segment);
