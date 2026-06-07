# Plano Mestre — CRM Comercial

**Projeto**: CRM Interno Comercial da Commit  
**Data**: 2026-06-07 (atualizado após grilling session)  
**Versão**: 2.0

---

## Visão Geral

Este documento é o **plano mestre** que conecta todos os planos individuais de cada feature. Cada feature tem sua própria pasta em `.specify/features/XX-nome/` com:
- `plan.md` — Plano detalhado com tasks TDD
- `spec.md` — Especificação (quando necessário)
- `notes.md` — Notas de implementação (preenchido durante o desenvolvimento)

## Arquitetura de Dependências

```
                    ┌─────────────────┐
                    │   01-setup      │
                    │   (fundacao)    │
                    └────────┬────────┘
                             │
              ┌──────────────┴──────────────┐
              │                             │
    ┌─────────▼─────────┐         ┌─────────▼─────────┐
    │   02-auth         │         │   03-leads-crud   │
    │   (JWT + roles)   │         │   (entidades)     │
    └─────────┬─────────┘         └─────────┬─────────┘
              │                             │
              │         ┌───────────────────┴───────────────┐
              │         │                                   │
    ┌─────────▼─────────▼─────────┐         ┌───────────────▼───────────┐
    │   04-pipeline-kanban        │         │   05-contatos             │
    │   (drag & drop)             │         │   (multiplos)             │
    └─────────┬───────────────────┘         └───────────┬───────────────┘
              │                                         │
              └───────────────┬─────────────────────────┘
                              │
                    ┌─────────▼─────────┐
                    │   06-tarefas      │
                    │   (accountability)│
                    └─────────┬─────────┘
                              │
                    ┌─────────▼─────────┐
                    │   07-timeline     │
                    │   (eventos)       │
                    └─────────┬─────────┘
                              │
                    ┌─────────▼─────────┐
                    │   08-dashboard    │
                    │   (metricas)      │
                    └─────────┬─────────┘
                              │
                    ┌─────────▼─────────┐
                    │   09-polish       │
                    │   (finalizacao)   │
                    └───────────────────┘
```

## Dependências entre Features

| Feature | Depende de | Pode rodar em paralelo com |
|---------|-----------|---------------------------|
| 01-setup | — | — |
| 02-auth | 01-setup | — |
| 03-leads-crud | 01-setup | 02-auth (depois precisa integrar) |
| 04-pipeline-kanban | 03-leads-crud | 02-auth |
| 05-contatos | 03-leads-crud | 02-auth, 04-pipeline |
| 06-tarefas | 03-leads-crud, 02-auth | 04-pipeline, 05-contatos |
| 07-timeline | 03-leads-crud, 02-auth, 04-pipeline, 05-contatos, 06-tarefas | — |
| 08-dashboard | 03-leads-crud, 06-tarefas, 07-timeline | — |
| 09-polish | TODAS | — |

## Fluxo de Worktrees

Cada feature é desenvolvida em uma **worktree isolada**:

```bash
# Main (produção)
main
├── 01-setup → merge → main
├── 02-auth → merge → main
├── 03-leads-crud → merge → main
├── ...
```

## Regras de Permissão

| Ação | DIRETOR | GER_AQUIS | GER_PROSP | AQUISICAO | PROSPECCAO |
|------|---------|-----------|-----------|-----------|------------|
| Criar lead | ✅ | ✅ | ❌ | ✅ | ❌ |
| Mover no pipeline | ✅ | ❌ | ✅ | ❌ | ✅ (próprio) |
| Editar lead | ✅ (qualquer) | ✅ (time) | ✅ (time) | ✅ (próprio) | ✅ (atribuído) |
| Arquivar lead | ✅ | ❌ | ✅ (time) | ❌ | ✅ (atribuído) |
| Atribuir lead | ✅ | ❌ | ✅ | ❌ | ❌ |
| Reativar lead | ✅ | ❌ | ✅ | ❌ | ✅ (atribuído) |
| Criar tarefa | ✅ (qualquer) | ✅ (time aquis.) | ✅ (time prosp.) | ❌ | ❌ |
| Completar tarefa | — | — | — | — | ✅ (atribuída) |
| Cancelar tarefa | ✅ | ✅ (time) | ✅ (time) | ❌ | ❌ |
| Registrar interação | ✅ (qualquer) | ✅ (time) | ✅ (time) | ✅ (próprio lead) | ✅ (atribuído) |
| Gerenciar contatos | ✅ (qualquer) | ✅ (time) | ✅ (time) | ✅ (próprio lead) | ✅ (atribuído) |

## Regras de Visibilidade

| Papel | Leads visíveis | Tarefas visíveis |
|-------|---------------|-----------------|
| DIRETOR | Todos | Todas |
| GERENTE_AQUISICAO | Criados pelo time de aquisição (qualquer estágio) | Tarefas do time |
| GERENTE_PROSPECCAO | Atribuídos ao time de prospecção (qualquer estágio) | Tarefas do time |
| AQUISICAO | Que criou (qualquer estágio) | Atribuídas a si |
| PROSPECCAO | Atribuídos a si | Atribuídas a si |

## Fluxo de Atribuição

1. AQUISICAO cria lead → `assigned_to = null`, status = NOVO
2. Lead sem atribuição visível apenas para GERENTES e DIRETOR
3. GERENTE_PROSPECCAO atribui manualmente a PROSPECCAO do time
4. PROSPECCAO só vê leads atribuídos a si

## Transições do Pipeline

```
Avanço:     NOVO → CONTATO → NEGOCIACAO → {GANHO, PERDIDO}
Retrocesso: NEGOCIACAO → CONTATO → NOVO
Arquivo:     Qualquer (exceto GANHO/PERDIDO) → ARQUIVADO
Reativação:  ARQUIVADO → NOVO (sempre NOVO, nunca ao estágio anterior)
Bloqueado:   Pular estágio (NOVO ↛ NEGOCIACAO), GANHO/PERDIDO ↛ qualquer
```

## Dashboard por Papel

| Papel | Cards |
|-------|-------|
| DIRETOR | Total leads, leads por status, conversão geral, tarefas atrasadas por time, leads frios, atividade |
| GERENTE_AQUISICAO | Leads criados pelo time, avanço no pipeline, produtividade |
| GERENTE_PROSPECCAO | Leads do time, conversão, tarefas atrasadas, leads frios |
| AQUISICAO + PROSPECCAO | Dashboard simplificado compartilhado: meus leads, minhas tarefas |

## Regras do Kanban

- Cards ordenados por **última atividade** (timeline mais recente primeiro)
- Sem drag-to-reorder no MVP
- Busca textual apenas em campos do lead (company_name, site, instagram, notes)
- Busca em contatos fica para versão futura

## Regras de Desativação

Ao desativar um usuário (`is_active = false`):
- `assigned_to` e `created_by` permanecem intactos (auditoria)
- Gerente encontra leads órfãos via filtro "usuários inativos": `WHERE assigned_to IN (SELECT id FROM users WHERE is_active = false)`
- Reatribuição manual pelo gerente

## Hierarquia

```
DIRETOR (manager_id = null)
├── GERENTE_AQUISICAO (manager_id = DIRETOR)
│   └── AQUISICAO (manager_id = GERENTE_AQUISICAO)
└── GERENTE_PROSPECCAO (manager_id = DIRETOR)
    └── PROSPECCAO (manager_id = GERENTE_PROSPECCAO)
```

`manager_id` sempre aponta para o superior imediato. Usado em queries para determinar escopo de visibilidade.

## Convenções

### Commits
- Formato: `tipo(scope): descricao`
- Tipos: `feat`, `fix`, `test`, `docs`, `refactor`, `chore`
- Exemplo: `feat(auth): add JWT token generation`

### Branches
- Nome: `feature/XX-nome`
- Exemplo: `feature/01-setup-projeto`

### PRs
- Todo PR deve ter:
  1. Descrição do que foi feito
  2. Screenshots (se frontend)
  3. Resultado dos testes
  4. Checklist de verificação

---

## Índice de Planos

| # | Feature | Arquivo | Status |
|---|---------|---------|--------|
| 01 | Setup do Projeto | [01-setup-projeto/plan.md](01-setup-projeto/plan.md) | 🔴 Não iniciado |
| 02 | Autenticação e Autorização | [02-auth/plan.md](02-auth/plan.md) | 🔴 Não iniciado |
| 03 | CRUD de Leads | [03-leads-crud/plan.md](03-leads-crud/plan.md) | 🔴 Não iniciado |
| 04 | Pipeline Kanban | [04-pipeline-kanban/plan.md](04-pipeline-kanban/plan.md) | 🔴 Não iniciado |
| 05 | Contatos | [05-contatos/plan.md](05-contatos/plan.md) | 🔴 Não iniciado |
| 06 | Tarefas | [06-tarefas/plan.md](06-tarefas/plan.md) | 🔴 Não iniciado |
| 07 | Timeline | [07-timeline/plan.md](07-timeline/plan.md) | 🔴 Não iniciado |
| 08 | Dashboard | [08-dashboard/plan.md](08-dashboard/plan.md) | 🔴 Não iniciado |
| 09 | Polish | [09-polish/plan.md](09-polish/plan.md) | 🔴 Não iniciado |

## Decisões Arquiteturais Globais

1. **Backend MVC**: Controller → Service → Repository
2. **DTO Pattern**: Nunca expor entidade JPA diretamente
3. **Response Wrapper**: `{ data, message, timestamp }` em TODOS endpoints
4. **Timeline Events**: Toda ação de estado gera evento (imutável). Interações (ligação, email, etc.) são tipos de TimelineEvent, não tabela separada
5. **Hierarquia**: `manager_id` sempre aponta para o superior imediato (incluindo GERENTE → DIRETOR)
6. **Tests**: TDD obrigatório — teste falha → implementa → teste passa
7. **Segments**: Enum fixo (TECNOLOGIA, FINANCAS, SAUDE, EDUCACAO, VAREJO, OUTRO), sem CRUD no MVP
8. **Search**: Apenas campos do lead (ILIKE), sem JOIN em contatos
9. **Kanban order**: Por última atividade, sem drag-to-reorder
10. **Reativação**: ARQUIVADO sempre volta a NOVO
11. **Permissões**: Quem vê pode editar (leads, contatos). Cancelamento de tarefa: só criador/gerente/DIRETOR

## Tecnologias

| Camada | Tech | Versão |
|--------|------|--------|
| Backend | Spring Boot | 3.2.x |
| Linguagem | Java | 17 |
| Banco | PostgreSQL | 15 |
| Migrations | Flyway | latest |
| Auth | Spring Security + JWT | latest |
| Frontend | React + TypeScript | 18 |
| Build FE | Vite | latest |
| Estilo | Tailwind + shadcn/ui | latest |
| Testes BE | JUnit 5 + Mockito + Testcontainers | latest |
| Testes FE | Vitest + Testing Library + MSW | latest |
| Docker | Docker + Docker Compose | latest |

## Banco de Dados — Schema Completo

```sql
-- ============================================================
-- V1__create_users.sql
-- ============================================================
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(150) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL CHECK (role IN (
        'DIRETOR', 'GERENTE_AQUISICAO', 'GERENTE_PROSPECCAO',
        'AQUISICAO', 'PROSPECCAO'
    )),
    manager_id UUID REFERENCES users(id),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_manager ON users(manager_id);
CREATE INDEX idx_users_active ON users(is_active);

-- ============================================================
-- V2__create_leads.sql
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

-- ============================================================
-- V3__create_contacts.sql
-- ============================================================
CREATE TABLE contacts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lead_id UUID NOT NULL REFERENCES leads(id) ON DELETE CASCADE,
    name VARCHAR(150) NOT NULL,
    role VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(255),
    whatsapp VARCHAR(20),
    is_main BOOLEAN DEFAULT FALSE,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_contacts_lead ON contacts(lead_id);

-- ============================================================
-- V4__create_tasks.sql
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

-- ============================================================
-- V5__create_timeline_events.sql
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

-- ============================================================
-- V6__seed_data.sql
-- ============================================================
INSERT INTO users (id, name, email, password_hash, role, manager_id, is_active)
VALUES
    ('550e8400-e29b-41d4-a716-446655440000', 'Diretor', 'diretor@commit.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqBtF6zOqTdpA6wYJp7zXxL1O0k2a', 'DIRETOR', NULL, TRUE),
    ('550e8400-e29b-41d4-a716-446655440001', 'Gerente Aquisição', 'gerente.aquisicao@commit.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqBtF6zOqTdpA6wYJp7zXxL1O0k2a', 'GERENTE_AQUISICAO', '550e8400-e29b-41d4-a716-446655440000', TRUE),
    ('550e8400-e29b-41d4-a716-446655440002', 'Gerente Prospecção', 'gerente.prospeccao@commit.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqBtF6zOqTdpA6wYJp7zXxL1O0k2a', 'GERENTE_PROSPECCAO', '550e8400-e29b-41d4-a716-446655440000', TRUE);
```

---

## API — Endpoints Completos

### Auth
```
POST   /api/auth/login              → Login (JWT)
POST   /api/auth/register           → Criar usuário (DIRETOR)
POST   /api/auth/refresh            → Refresh token
GET    /api/auth/me                 → Dados do usuário logado
PUT    /api/auth/password            → Alterar senha
```

### Users
```
GET    /api/users                   → Listar (DIRETOR)
GET    /api/users/{id}              → Detalhe
GET    /api/users/hierarchy         → Árvore (DIRETOR)
GET    /api/users/team              → Meu time (GERENTE)
PUT    /api/users/{id}              → Editar
DELETE /api/users/{id}              → Desativar (soft delete)
```

### Leads
```
GET    /api/leads                   → Listar (paginado, filtros)
POST   /api/leads                   → Criar (AQUISICAO)
GET    /api/leads/{id}              → Detalhe
PUT    /api/leads/{id}              → Editar
DELETE /api/leads/{id}              → Arquivar
PATCH  /api/leads/{id}/status        → Mover pipeline
PATCH  /api/leads/{id}/assign         → Atribuir
GET    /api/leads/kanban             → Pipeline agrupado
GET    /api/leads/search?q=          → Busca textual
```

### Contacts
```
GET    /api/leads/{id}/contacts       → Listar
POST   /api/leads/{id}/contacts      → Adicionar
PUT    /api/leads/{id}/contacts/{cid} → Editar
DELETE /api/leads/{id}/contacts/{cid} → Remover
PATCH  /api/leads/{id}/contacts/{cid}/main → Definir principal
```

### Interactions
```
POST   /api/leads/{id}/timeline      → Registrar interação (tipo INTERACTION no TimelineEvent)
```
> Interações são registradas como TimelineEvent do tipo INTERACTION. Não existe endpoint separado.

### Tasks
```
GET    /api/leads/{id}/tasks          → Tarefas do lead
POST   /api/leads/{id}/tasks          → Criar (gerente)
PUT    /api/leads/{id}/tasks/{tid}    → Editar
PATCH  /api/leads/{id}/tasks/{tid}/complete → Concluir
PATCH  /api/leads/{id}/tasks/{tid}/reopen  → Reabrir
DELETE /api/leads/{id}/tasks/{tid}    → Remover
GET    /api/tasks/my                 → Minhas tarefas
GET    /api/tasks/team               → Tarefas do time
```

### Timeline
```
GET    /api/leads/{id}/timeline       → Feed cronológico
```

### Dashboard
```
GET    /api/dashboard                 → Resumo (por role)
GET    /api/dashboard/team            → Overview do time
```

### Segments
```
GET    /api/segments                  → Listar (enum fixo, sem CRUD)
```
> Segmentos são um enum fixo (TECNOLOGIA, FINANCAS, SAUDE, EDUCACAO, VAREJO, OUTRO). Sem CRUD no MVP.

---

## Frontend — Rotas

```
/login                              → LoginPage
/dashboard                          → DashboardPage
/leads                              → LeadsPage (kanban)
/leads/new                          → LeadCreatePage
/leads/:id                          → LeadDetailPage
/leads/:id/edit                     → LeadEditPage
/tasks                              → MyTasksPage
/users                              → UsersPage
/users/hierarchy                    → HierarchyPage
```

## Estrutura de Pastas do Projeto

```
CRM_interno_comercial/
├── backend/
│   ├── src/main/java/com/commit/crm/
│   │   ├── config/security/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── model/
│   │   ├── dto/
│   │   │   ├── request/
│   │   │   └── response/
│   │   ├── exception/
│   │   └── mapper/
│   ├── src/main/resources/
│   │   └── db/migration/
│   └── src/test/java/com/commit/crm/
│       ├── controller/
│       ├── service/
│       └── repository/
│
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   │   ├── ui/
│   │   │   ├── layout/
│   │   │   ├── lead/
│   │   │   ├── contact/
│   │   │   ├── task/
│   │   │   ├── timeline/
│   │   │   └── dashboard/
│   │   ├── pages/
│   │   ├── services/
│   │   ├── hooks/
│   │   ├── context/
│   │   ├── types/
│   │   ├── utils/
│   │   ├── App.tsx
│   │   └── main.tsx
│   ├── tests/
│   └── public/
│
├── docker-compose.yml
├── .env.example
├── .gitignore
└── docs/
    └── plans/
```

---

## Próximos Passos

1. Ir para [01-setup-projeto/plan.md](01-setup-projeto/plan.md)
2. Criar worktree: `git worktree add .worktrees/feature/01-setup-projeto -b feature/01-setup-projeto`
3. Executar tasks de setup
4. PR → merge → próxima feature

---

**Registro de Alterações**:
- v1.0 (2026-06-06) — Plano mestre inicial com 9 features, schema completo, API, rotas
- v2.0 (2026-06-07) — Refinamento após grilling session: remoção da tabela interactions, address opcional, regras de permissão/visibilidade, hierarquia com manager_id apontando para DIRETOR, enum fixo de segmentos, transições do pipeline, dashboard simplificado para AQUISICAO+PROSPECCAO, fluxo de atribuição, regras de desativação
