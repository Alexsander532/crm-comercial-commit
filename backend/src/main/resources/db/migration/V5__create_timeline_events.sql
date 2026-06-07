-- ============================================================
-- V5__create_timeline_events.sql
-- Criação da tabela de timeline (inclui interações como tipo
-- INTERACTION — não existe tabela separada de interações)
-- ============================================================

CREATE TABLE timeline_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lead_id UUID NOT NULL REFERENCES leads(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id),
    type VARCHAR(20) NOT NULL CHECK (type IN (
        'CREATED', 'STATUS_CHANGED', 'FIELD_UPDATED',
        'INTERACTION', 'NOTE_ADDED', 'TASK_CREATED',
        'TASK_COMPLETED', 'ASSIGNED', 'CONTACT_ADDED',
        'CONTACT_UPDATED'
    )),
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_timeline_lead ON timeline_events(lead_id);
CREATE INDEX idx_timeline_created ON timeline_events(created_at);
