# Implementation Plan: MVP CRM Comercial

**Branch**: `feature/mvp-crm` | **Date**: 2026-06-06 | **Spec**: [mvp-crm-comercial.md](mvp-crm-comercial.md)

**Input**: Feature specification from `/speckit.specify` (User Stories 1-7)

---

## Summary

Implementar um CRM interno web (Spring Boot + React) para a equipe comercial da
Commit. O sistema gerencia leads (aquisição → prospecção), pipeline kanban,
contatos, tarefas com accountability, timeline de eventos e hierarquia de
usuários (Diretor → Gerente → Funcionário).

---

## Technical Context

| Aspecto | Decisão |
|---------|---------|
| **Language** | Java 17 + TypeScript |
| **Backend** | Spring Boot 3.2.x (Web, Data JPA, Security) |
| **Frontend** | React 18 + Vite + Tailwind CSS + shadcn/ui |
| **Database** | PostgreSQL 15 (via Docker local) |
| **Migrations** | Flyway |
| **Auth** | Spring Security + JWT (stateless) |
| **API Style** | REST JSON com response wrapper padronizado |
| **Testing BE** | JUnit 5 + Mockito + Testcontainers |
| **Testing FE** | Vitest + React Testing Library + MSW |
| **Deploy** | Docker + Coolify (futuro) |
| **Git** | Conventional Commits + feature branches + worktrees |

---

## Constitution Check

| Princípio | Status | Verificação |
|-----------|--------|-------------|
| I. Stack fixa | ✅ | Spring Boot 3 + React + PostgreSQL |
| II. API-First | ✅ | Todos endpoints documentados com response wrapper |
| III. Hierarquia | ✅ | RBAC em Service layer + JWT |
| IV. Accountability | ✅ | Timeline events + Task completion tracking |
| V. Test-First | ✅ | TDD: teste antes de implementação |
| VI. Ploomes UX | ✅ | Kanban + timeline + cards com indicadores |
| VII. YAGNI | ✅ | MVP focado, IA deixada pra depois |

---

## Project Structure

```
CRM_interno_comercial/
├── backend/                          # Spring Boot
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/commit/crm/
│   │   │   │   ├── config/
│   │   │   │   │   └── security/
│   │   │   │   │       ├── SecurityConfig.java
│   │   │   │   │       ├── JwtAuthenticationFilter.java
│   │   │   │   │       ├── JwtTokenProvider.java
│   │   │   │   │       └── CustomUserDetailsService.java
│   │   │   │   ├── controller/
│   │   │   │   │   ├── AuthController.java
│   │   │   │   │   ├── UserController.java
│   │   │   │   │   ├── LeadController.java
│   │   │   │   │   ├── ContactController.java
│   │   │   │   │   ├── InteractionController.java
│   │   │   │   │   ├── TaskController.java
│   │   │   │   │   ├── TimelineController.java
│   │   │   │   │   ├── PipelineController.java
│   │   │   │   │   ├── SegmentController.java
│   │   │   │   │   └── DashboardController.java
│   │   │   │   ├── service/
│   │   │   │   │   ├── AuthService.java
│   │   │   │   │   ├── UserService.java
│   │   │   │   │   ├── LeadService.java
│   │   │   │   │   ├── ContactService.java
│   │   │   │   │   ├── InteractionService.java
│   │   │   │   │   ├── TaskService.java
│   │   │   │   │   ├── TimelineService.java
│   │   │   │   │   ├── PipelineService.java
│   │   │   │   │   └── DashboardService.java
│   │   │   │   ├── repository/
│   │   │   │   │   ├── UserRepository.java
│   │   │   │   │   ├── LeadRepository.java
│   │   │   │   │   ├── ContactRepository.java
│   │   │   │   │   ├── InteractionRepository.java
│   │   │   │   │   ├── TaskRepository.java
│   │   │   │   │   ├── TimelineEventRepository.java
│   │   │   │   │   └── SegmentRepository.java
│   │   │   │   ├── model/
│   │   │   │   │   ├── User.java
│   │   │   │   │   ├── UserRole.java
│   │   │   │   │   ├── Lead.java
│   │   │   │   │   ├── LeadStatus.java
│   │   │   │   │   ├── Contact.java
│   │   │   │   │   ├── Interaction.java
│   │   │   │   │   ├── InteractionType.java
│   │   │   │   │   ├── Task.java
│   │   │   │   │   ├── TaskPriority.java
│   │   │   │   │   ├── TaskStatus.java
│   │   │   │   │   ├── TimelineEvent.java
│   │   │   │   │   ├── TimelineEventType.java
│   │   │   │   │   └── Segment.java
│   │   │   │   ├── dto/
│   │   │   │   │   ├── request/
│   │   │   │   │   │   ├── LoginRequest.java
│   │   │   │   │   │   ├── RegisterRequest.java
│   │   │   │   │   │   ├── LeadRequest.java
│   │   │   │   │   │   ├── ContactRequest.java
│   │   │   │   │   │   ├── InteractionRequest.java
│   │   │   │   │   │   ├── TaskRequest.java
│   │   │   │   │   │   └── StatusUpdateRequest.java
│   │   │   │   │   └── response/
│   │   │   │   │       ├── ApiResponse.java
│   │   │   │   │       ├── LoginResponse.java
│   │   │   │   │       ├── LeadResponse.java
│   │   │   │   │       ├── LeadKanbanResponse.java
│   │   │   │   │       ├── DashboardResponse.java
│   │   │   │   │       └── TaskResponse.java
│   │   │   │   ├── exception/
│   │   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   │   ├── InvalidTransitionException.java
│   │   │   │   │   ├── DuplicateResourceException.java
│   │   │   │   │   └── UnauthorizedException.java
│   │   │   │   └── mapper/
│   │   │   │       └── LeadMapper.java
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       └── db/migration/
│   │   │           ├── V1__create_users.sql
│   │   │           ├── V2__create_leads.sql
│   │   │           ├── V3__create_contacts.sql
│   │   │           ├── V4__create_interactions.sql
│   │   │           ├── V5__create_tasks.sql
│   │   │           ├── V6__create_timeline_events.sql
│   │   │           └── V7__seed_data.sql
│   │   └── test/java/com/commit/crm/
│   │       ├── controller/
│   │       │   ├── AuthControllerTest.java
│   │       │   ├── LeadControllerTest.java
│   │       │   └── TaskControllerTest.java
│   │       ├── service/
│   │       │   ├── LeadServiceTest.java
│   │       │   ├── TaskServiceTest.java
│   │       │   └── PipelineServiceTest.java
│   │       └── repository/
│   │           └── LeadRepositoryTest.java
│   └── pom.xml
│
├── frontend/                         # React
│   ├── src/
│   │   ├── components/
│   │   │   ├── ui/
│   │   │   │   ├── Button.tsx
│   │   │   │   ├── Input.tsx
│   │   │   │   ├── Modal.tsx
│   │   │   │   ├── Badge.tsx
│   │   │   │   └── Card.tsx
│   │   │   ├── layout/
│   │   │   │   ├── Sidebar.tsx
│   │   │   │   ├── Header.tsx
│   │   │   │   └── PageContainer.tsx
│   │   │   ├── lead/
│   │   │   │   ├── LeadCard.tsx
│   │   │   │   ├── LeadForm.tsx
│   │   │   │   ├── KanbanColumn.tsx
│   │   │   │   └── KanbanBoard.tsx
│   │   │   ├── contact/
│   │   │   │   ├── ContactList.tsx
│   │   │   │   └── ContactForm.tsx
│   │   │   ├── task/
│   │   │   │   ├── TaskList.tsx
│   │   │   │   ├── TaskCard.tsx
│   │   │   │   └── TaskForm.tsx
│   │   │   ├── timeline/
│   │   │   │   ├── TimelineFeed.tsx
│   │   │   │   └── TimelineEvent.tsx
│   │   │   └── dashboard/
│   │   │       ├── MetricsCard.tsx
│   │   │       └── TeamOverview.tsx
│   │   ├── pages/
│   │   │   ├── LoginPage.tsx
│   │   │   ├── DashboardPage.tsx
│   │   │   ├── LeadsPage.tsx
│   │   │   ├── LeadDetailPage.tsx
│   │   │   ├── LeadCreatePage.tsx
│   │   │   ├── LeadEditPage.tsx
│   │   │   ├── MyTasksPage.tsx
│   │   │   ├── UsersPage.tsx
│   │   │   └── HierarchyPage.tsx
│   │   ├── services/
│   │   │   ├── api.ts
│   │   │   ├── authService.ts
│   │   │   ├── leadService.ts
│   │   │   ├── contactService.ts
│   │   │   ├── taskService.ts
│   │   │   └── dashboardService.ts
│   │   ├── hooks/
│   │   │   ├── useAuth.ts
│   │   │   ├── useLeads.ts
│   │   │   ├── useTasks.ts
│   │   │   └── useKanban.ts
│   │   ├── context/
│   │   │   └── AuthContext.tsx
│   │   ├── types/
│   │   │   ├── lead.ts
│   │   │   ├── user.ts
│   │   │   ├── task.ts
│   │   │   └── api.ts
│   │   ├── utils/
│   │   │   ├── constants.ts
│   │   │   └── formatters.ts
│   │   ├── App.tsx
│   │   └── main.tsx
│   ├── tests/
│   │   ├── components/
│   │   │   └── KanbanBoard.test.tsx
│   │   └── services/
│   │       └── api.test.ts
│   ├── public/
│   ├── index.html
│   ├── package.json
│   ├── tailwind.config.js
│   ├── vite.config.ts
│   └── tsconfig.json
│
├── docker-compose.yml                # PostgreSQL + app
├── .env.example
├── .gitignore
└── docs/
    └── plans/
        └── 2026-06-06-crm-comercial-design.md
```

---

## Milestones

| Milestone | Entrega | Features |
|-----------|---------|----------|
| **M1** | Setup + Auth | Projeto Spring Boot, React, Docker, JWT, roles |
| **M2** | Leads CRUD | Cadastro, edição, listagem, busca, filtros |
| **M3** | Pipeline Kanban | 6 colunas, drag & drop, transições validadas |
| **M4** | Contatos | Múltiplos contatos, principal, CRUD |
| **M5** | Tarefas | Criação, conclusão, accountability, status calculado |
| **M6** | Timeline | Eventos automáticos, feed cronológico |
| **M7** | Dashboard | Métricas por role, leads frios, tarefas atrasadas |
| **M8** | Polish | Testes, documentação, ajustes finais |

---

## Critical Decisions

### 1. JWT Stateless (sem refresh token)
Token expira em 24h. No MVP não teremos refresh token rotativo.
**Trade-off:** Simplicidade vs. segurança. Aceitável para MVP interno.

### 2. `assigned_to` permanece ao desativar
Não limpamos automaticamente. Gerente reatribui manualmente.
**Trade-off:** Histórico auditável vs. automação. Escolha do usuário.

### 3. TimelineEvent é imutável
Nunca editamos nem deletamos. Apenas inserimos.
**Trade-off:** Banco cresce sem limites. Solução futura: arquivar eventos > 1 ano.

### 4. completionStatus é calculado em runtime
Não é persistido. Calculado a partir de `completed_at`, `due_date` e `now()`.
**Trade-off:** Performance vs. consistência. Query pode ser otimizada com index.

---

## API Endpoints Map

```
POST   /api/auth/login              → Login
POST   /api/auth/register           → Admin cria usuário
POST   /api/auth/refresh            → Refresh token
GET    /api/auth/me                 → Dados do usuário
PUT    /api/auth/password            → Alterar senha

GET    /api/users                    → Listar (DIRETOR)
GET    /api/users/{id}               → Detalhe
GET    /api/users/hierarchy          → Árvore (DIRETOR)
GET    /api/users/team               → Meu time (GERENTE)
PUT    /api/users/{id}               → Editar
DELETE /api/users/{id}               → Desativar

GET    /api/leads                    → Listar (paginado, filtros)
POST   /api/leads                    → Criar (AQUISICAO)
GET    /api/leads/{id}               → Detalhe
PUT    /api/leads/{id}               → Editar
DELETE /api/leads/{id}               → Arquivar
PATCH  /api/leads/{id}/status        → Mover pipeline
PATCH  /api/leads/{id}/assign         → Atribuir
GET    /api/leads/kanban             → Pipeline agrupado
GET    /api/leads/search?q=           → Busca textual

GET    /api/leads/{id}/contacts       → Listar contatos
POST   /api/leads/{id}/contacts      → Adicionar
PUT    /api/leads/{id}/contacts/{cid} → Editar
DELETE /api/leads/{id}/contacts/{cid} → Remover
PATCH  /api/leads/{id}/contacts/{cid}/main → Definir principal

GET    /api/leads/{id}/interactions   → Histórico
POST   /api/leads/{id}/interactions   → Registrar

GET    /api/leads/{id}/tasks         → Tarefas do lead
POST   /api/leads/{id}/tasks         → Criar (gerente)
PUT    /api/leads/{id}/tasks/{tid}    → Editar
PATCH  /api/leads/{id}/tasks/{tid}/complete → Concluir
PATCH  /api/leads/{id}/tasks/{tid}/reopen  → Reabrir
DELETE /api/leads/{id}/tasks/{tid}    → Remover
GET    /api/tasks/my                 → Minhas tarefas
GET    /api/tasks/team               → Tarefas do time

GET    /api/leads/{id}/timeline       → Feed cronológico

GET    /api/dashboard                → Resumo (por role)
GET    /api/dashboard/team            → Overview do time

GET    /api/segments                  → Listar segmentos
POST   /api/segments                  → Adicionar (DIRETOR)
```

---

## Database Schema

```sql
-- V1__create_users.sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(150) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL CHECK (role IN ('DIRETOR', 'GERENTE_AQUISICAO', 'GERENTE_PROSPECCAO', 'AQUISICAO', 'PROSPECCAO')),
    manager_id UUID REFERENCES users(id),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_manager ON users(manager_id);
CREATE INDEX idx_users_active ON users(is_active);

-- V2__create_leads.sql
CREATE TABLE leads (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_name VARCHAR(200) NOT NULL,
    site VARCHAR(500),
    instagram VARCHAR(200),
    whatsapp VARCHAR(20),
    address TEXT NOT NULL,
    segment VARCHAR(100) NOT NULL,
    notes TEXT,
    enriched_data JSONB,
    status VARCHAR(20) NOT NULL DEFAULT 'NOVO' CHECK (status IN ('NOVO', 'CONTATO', 'NEGOCIACAO', 'GANHO', 'PERDIDO', 'ARQUIVADO')),
    created_by UUID NOT NULL REFERENCES users(id),
    assigned_to UUID REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_leads_status ON leads(status);
CREATE INDEX idx_leads_assigned ON leads(assigned_to);
CREATE INDEX idx_leads_created_by ON leads(created_by);
CREATE INDEX idx_leads_segment ON leads(segment);
CREATE INDEX idx_leads_search ON leads USING gin(to_tsvector('portuguese', company_name || ' ' || COALESCE(notes, '')));

-- V3__create_contacts.sql
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

-- V4__create_interactions.sql
CREATE TABLE interactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lead_id UUID NOT NULL REFERENCES leads(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id),
    type VARCHAR(20) NOT NULL CHECK (type IN ('LIGACAO', 'EMAIL', 'REUNIAO', 'OBSERVACAO', 'PROPOSTA')),
    description TEXT NOT NULL,
    proposal_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_interactions_lead ON interactions(lead_id);
CREATE INDEX idx_interactions_user ON interactions(user_id);

-- V5__create_tasks.sql
CREATE TABLE tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lead_id UUID NOT NULL REFERENCES leads(id) ON DELETE CASCADE,
    created_by UUID NOT NULL REFERENCES users(id),
    assigned_to UUID REFERENCES users(id),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    priority VARCHAR(10) DEFAULT 'MEDIA' CHECK (priority IN ('BAIXA', 'MEDIA', 'ALTA')),
    status VARCHAR(15) DEFAULT 'PENDENTE' CHECK (status IN ('PENDENTE', 'CONCLUIDA', 'CANCELADA')),
    due_date TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_tasks_assigned ON tasks(assigned_to);
CREATE INDEX idx_tasks_lead ON tasks(lead_id);
CREATE INDEX idx_tasks_status ON tasks(status);

-- V6__create_timeline_events.sql
CREATE TABLE timeline_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lead_id UUID NOT NULL REFERENCES leads(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id),
    type VARCHAR(20) NOT NULL CHECK (type IN ('CREATED', 'STATUS_CHANGED', 'FIELD_UPDATED', 'INTERACTION', 'NOTE_ADDED', 'TASK_CREATED', 'TASK_COMPLETED', 'ASSIGNED', 'CONTACT_ADDED', 'CONTACT_UPDATED')),
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_timeline_lead ON timeline_events(lead_id);
CREATE INDEX idx_timeline_created ON timeline_events(created_at);

-- V7__seed_data.sql
-- Seed diretor inicial
INSERT INTO users (name, email, password_hash, role, is_active)
VALUES ('Admin', 'admin@commit.com', '$2a$10$...', 'DIRETOR', TRUE);
```

---

## Frontend Routes

```typescript
// React Router v6
<Routes>
  <Route path="/login" element={<LoginPage />} />
  <Route path="/" element={<ProtectedLayout />}>
    <Route path="dashboard" element={<DashboardPage />} />
    <Route path="leads" element={<LeadsPage />} />
    <Route path="leads/new" element={<LeadCreatePage />} />
    <Route path="leads/:id" element={<LeadDetailPage />} />
    <Route path="leads/:id/edit" element={<LeadEditPage />} />
    <Route path="tasks" element={<MyTasksPage />} />
    <Route path="users" element={<UsersPage />} />
    <Route path="users/hierarchy" element={<HierarchyPage />} />
  </Route>
</Routes>
```

---

## Testing Strategy

### Backend
- **Unit (Service):** Mockito, cobertura de regras de negócio
- **Integration (Controller):** @SpringBootTest, Testcontainers (PostgreSQL)
- **Security:** Testar 401/403 para cada role em cada endpoint
- **Repository:** @DataJpaTest para queries customizadas

### Frontend
- **Component:** Vitest + Testing Library (KanbanBoard, LeadForm)
- **API:** MSW para mockar endpoints
- **Hooks:** Testar useAuth, useLeads

---

## Docker Compose (desenvolvimento)

```yaml
version: '3.8'
services:
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: crm_comercial
      POSTGRES_USER: crm
      POSTGRES_PASSWORD: crm_password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  backend:
    build: ./backend
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/crm_comercial
      SPRING_DATASOURCE_USERNAME: crm
      SPRING_DATASOURCE_PASSWORD: crm_password
    depends_on:
      - postgres

  frontend:
    build: ./frontend
    ports:
      - "3000:3000"
    depends_on:
      - backend

volumes:
  postgres_data:
```

---

## Next Steps

1. **Worktree:** `feature/setup-projeto` — Spring Boot init, React init, Docker
2. **Worktree:** `feature/auth` — JWT, roles, SecurityConfig
3. **Worktree:** `feature/crud-leads` — Entidades, repository, service, controller
4. **Worktree:** `feature/kanban` — Pipeline, transições, drag & drop
5. **Worktree:** `feature/contacts` — CRUD contatos
6. **Worktree:** `feature/tasks` — Tasks, accountability
7. **Worktree:** `feature/timeline` — Timeline events
8. **Worktree:** `feature/dashboard` — Métricas
