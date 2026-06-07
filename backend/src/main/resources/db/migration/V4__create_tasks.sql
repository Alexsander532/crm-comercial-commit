-- ============================================================
-- V4__create_tasks.sql
-- Criação da tabela de tarefas com accountability
-- ============================================================

CREATE TABLE tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lead_id UUID NOT NULL REFERENCES leads(id) ON DELETE CASCADE,
    created_by UUID NOT NULL REFERENCES users(id),
    assigned_to UUID REFERENCES users(id),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    priority VARCHAR(10) DEFAULT 'MEDIA' CHECK (priority IN (
        'BAIXA', 'MEDIA', 'ALTA'
    )),
    status VARCHAR(15) DEFAULT 'PENDENTE' CHECK (status IN (
        'PENDENTE', 'CONCLUIDA', 'CANCELADA'
    )),
    due_date TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_tasks_assigned ON tasks(assigned_to);
CREATE INDEX idx_tasks_lead ON tasks(lead_id);
CREATE INDEX idx_tasks_status ON tasks(status);
