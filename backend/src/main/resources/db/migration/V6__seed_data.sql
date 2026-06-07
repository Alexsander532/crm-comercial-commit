-- ============================================================
-- V6__seed_data.sql
-- Dados iniciais: diretor + 2 gerentes
-- Senha: admin123 (BCrypt hash gerado)
-- ============================================================

INSERT INTO users (id, name, email, password_hash, role, manager_id, is_active)
VALUES
    -- Diretor (manager_id = null)
    (
        '550e8400-e29b-41d4-a716-446655440000',
        'Diretor',
        'diretor@commit.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqBtF6zOqTdpA6wYJp7zXxL1O0k2a',
        'DIRETOR',
        NULL,
        TRUE
    ),
    -- Gerente de Aquisição (manager_id = Diretor)
    (
        '550e8400-e29b-41d4-a716-446655440001',
        'Gerente de Aquisição',
        'gerente.aquisicao@commit.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqBtF6zOqTdpA6wYJp7zXxL1O0k2a',
        'GERENTE_AQUISICAO',
        '550e8400-e29b-41d4-a716-446655440000',
        TRUE
    ),
    -- Gerente de Prospecção (manager_id = Diretor)
    (
        '550e8400-e29b-41d4-a716-446655440002',
        'Gerente de Prospecção',
        'gerente.prospeccao@commit.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqBtF6zOqTdpA6wYJp7zXxL1O0k2a',
        'GERENTE_PROSPECCAO',
        '550e8400-e29b-41d4-a716-446655440000',
        TRUE
    );
