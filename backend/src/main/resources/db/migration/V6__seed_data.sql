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
        '$2a$10$3ami2.k/35etGTCDudQwx.sWLUXYl1r0b221HmrIBG1LOlU52NJAe',
        'DIRETOR',
        NULL,
        TRUE
    ),
    -- Gerente de Aquisição (manager_id = Diretor)
    (
        '550e8400-e29b-41d4-a716-446655440001',
        'Gerente de Aquisição',
        'gerente.aquisicao@commit.com',
        '$2a$10$3ami2.k/35etGTCDudQwx.sWLUXYl1r0b221HmrIBG1LOlU52NJAe',
        'GERENTE_AQUISICAO',
        '550e8400-e29b-41d4-a716-446655440000',
        TRUE
    ),
    -- Gerente de Prospecção (manager_id = Diretor)
    (
        '550e8400-e29b-41d4-a716-446655440002',
        'Gerente de Prospecção',
        'gerente.prospeccao@commit.com',
        '$2a$10$3ami2.k/35etGTCDudQwx.sWLUXYl1r0b221HmrIBG1LOlU52NJAe',
        'GERENTE_PROSPECCAO',
        '550e8400-e29b-41d4-a716-446655440000',
        TRUE
    );
