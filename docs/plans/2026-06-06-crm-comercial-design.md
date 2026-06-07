# CRM Interno — Equipe Comercial Commit

**Data:** 2026-06-06  
**Stack:** Spring Boot 3 + React + TypeScript + PostgreSQL  
**Referência:** Ploomes (adaptado para modelo simplificado)

---

## 1. Visão Geral

Sistema CRM interno para gestão do fluxo comercial da Commit, dividido em duas frentes:

- **🔍 Aquisição:** Pesquisa e cadastra leads com dados da empresa (site, instagram, whatsapp, endereço, segmento)
- **📞 Prospecção:** Acompanha leads no pipeline kanban, registra interações, fecha negócios

### Objetivos futuros
- Automação com IA para preenchimento de dados (campo `enriched_data` JSONB já preparado)
- Integração com Google Maps API e análise de conteúdo via IA

---

## 2. Arquitetura

### Backend — Spring Boot 3

```
spring-crm/
└── src/main/java/com/commit/crm/
    ├── config/
    │   ├── security/
    │   │   ├── SecurityConfig.java
    │   │   ├── JwtAuthenticationFilter.java
    │   │   ├── JwtTokenProvider.java
    │   │   └── CustomUserDetailsService.java
    │   └── web/
    │       └── WebConfig.java
    ├── controller/
    │   ├── AuthController.java
    │   ├── LeadController.java
    │   ├── ContactController.java
    │   ├── InteractionController.java
    │   ├── TaskController.java
    │   ├── TimelineController.java
    │   ├── UserController.java
    │   ├── PipelineController.java
    │   ├── SegmentController.java
    │   └── DashboardController.java
    ├── service/
    │   ├── LeadService.java
    │   ├── ContactService.java
    │   ├── InteractionService.java
    │   ├── TaskService.java
    │   ├── TimelineService.java
    │   ├── PipelineService.java
    │   ├── AuthService.java
    │   ├── UserService.java
    │   ├── DashboardService.java
    │   └── SegmentService.java
    ├── repository/
    ├── model/
    ├── dto/
    │   ├── request/
    │   └── response/
    ├── exception/
    │   ├── GlobalExceptionHandler.java
    │   ├── ResourceNotFoundException.java
    │   ├── InvalidTransitionException.java
    │   ├── DuplicateResourceException.java
    │   └── UnauthorizedException.java
    └── mapper/
```

### Frontend — React + TypeScript + Vite

```
crm-web/
├── src/
│   ├── components/
│   │   ├── ui/                    → Button, Input, Modal, Badge, Card
│   │   ├── layout/                → Sidebar, Header, PageContainer
│   │   ├── lead/                  → LeadCard, LeadForm, KanbanColumn, KanbanBoard
│   │   ├── contact/               → ContactList, ContactForm
│   │   ├── task/                  → TaskList, TaskCard, TaskForm
│   │   ├── timeline/              → TimelineFeed, TimelineEvent
│   │   └── dashboard/             → MetricsCard, TeamOverview
│   ├── pages/                     → Login, Dashboard, Leads, LeadDetail, Users, Tasks
│   ├── services/                  → api.ts, authService, leadService, etc
│   ├── hooks/                     → useAuth, useLeads, useTasks, etc
│   ├── context/                   → AuthContext
│   ├── types/                     → Interfaces TypeScript
│   └── utils/                     → Constants, formatters
```

---

## 3. Estrutura Organizacional

### Perfis de usuário

```
👤 DIRETOR
├── 👤 GERENTE_AQUISICAO
│   ├── 👤 AQUISICAO
│   └── 👤 AQUISICAO
└── 👤 GERENTE_PROSPECCAO
    ├── 👤 PROSPECCAO
    └── 👤 PROSPECCAO
```

### Hierarquia no banco

```sql
users (
  id              UUID PRIMARY KEY,
  name            VARCHAR(150) NOT NULL,
  email           VARCHAR(255) UNIQUE NOT NULL,
  password_hash   VARCHAR(255) NOT NULL,
  role            ENUM('DIRETOR','GERENTE_AQUISICAO','GERENTE_PROSPECCAO',
                       'AQUISICAO','PROSPECCAO') NOT NULL,
  manager_id      UUID REFERENCES users(id),
  is_active       BOOLEAN DEFAULT TRUE,
  created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
)
```

### Matriz de permissões

| Ação | DIRETOR | GERENTE AQUIS. | GERENTE PROSPEC. | AQUISIÇÃO | PROSPECÇÃO |
|------|---------|----------------|------------------|-----------|------------|
| Criar lead | ✅ | ✅ | ❌ | ✅ | ❌ |
| Editar lead | ✅ | ✅* | ❌ | ✅* | ❌ |
| Ver todos leads | ✅ | ✅** | ✅** | ❌ | ❌ |
| Ver leads do time | ✅ | ✅ | ✅ | ❌ | ❌ |
| Ver meus leads | ✅ | ✅ | ✅ | ✅ | ✅ |
| Mover kanban | ✅ | ✅ | ✅ | ❌ | ✅ |
| Interagir | ✅ | ✅ | ✅ | ❌ | ✅ |
| Atribuir lead (qualquer) | ✅ | ❌ | ❌ | ❌ | ❌ |
| Atribuir lead (próprio time) | ✅ | ✅ | ✅ | ❌ | ❌ |
| Criar tarefa (qualquer) | ✅ | ❌ | ❌ | ❌ | ❌ |
| Criar tarefa (próprio time) | ✅ | ✅ | ✅ | ❌ | ❌ |
| Concluir tarefa própria | ✅ | ✅ | ✅ | ✅ | ✅ |
| Gerenciar usuários | ✅ | ❌ | ❌ | ❌ | ❌ |

\* Só leads do time  
\** Leads do time que gerencia

---

## 4. Modelo de Dados

### leads

| Campo | Tipo | Descrição |
|-------|------|-----------|
| id | UUID (PK) | |
| company_name | VARCHAR(200) | Obrigatório |
| site | VARCHAR(500) | Opcional |
| instagram | VARCHAR(200) | Opcional |
| whatsapp | VARCHAR(20) | Opcional |
| address | TEXT | Obrigatório |
| segment | VARCHAR(100) | Obrigatório |
| notes | TEXT | Opcional |
| enriched_data | JSONB | Preparado para IA futura |
| status | ENUM('NOVO','CONTATO','NEGOCIACAO','GANHO','PERDIDO','ARQUIVADO') | Pipeline |
| created_by | UUID (FK → users) | |
| assigned_to | UUID (FK → users) | Responsável pela prospecção |
| created_at | TIMESTAMP | |
| updated_at | TIMESTAMP | |

### contacts

| Campo | Tipo | Descrição |
|-------|------|-----------|
| id | UUID (PK) | |
| lead_id | UUID (FK → leads) | |
| name | VARCHAR(150) | Obrigatório |
| role | VARCHAR(100) | Cargo (ex: Diretor Financeiro) |
| phone | VARCHAR(20) | |
| email | VARCHAR(255) | |
| whatsapp | VARCHAR(20) | |
| is_main | BOOLEAN | Contato principal |
| notes | TEXT | |
| created_at | TIMESTAMP | |
| updated_at | TIMESTAMP | |

### interactions

| Campo | Tipo | Descrição |
|-------|------|-----------|
| id | UUID (PK) | |
| lead_id | UUID (FK → leads) | |
| user_id | UUID (FK → users) | |
| type | ENUM('LIGACAO','EMAIL','REUNIAO','OBSERVACAO','PROPOSTA') | |
| description | TEXT | |
| proposal_url | VARCHAR(500) | Só para PROPOSTA |
| created_at | TIMESTAMP | |

### tasks

| Campo | Tipo | Descrição |
|-------|------|-----------|
| id | UUID (PK) | |
| lead_id | UUID (FK → leads) | |
| created_by | UUID (FK → users) | |
| assigned_to | UUID (FK → users) | |
| title | VARCHAR(255) | |
| description | TEXT | |
| priority | ENUM('BAIXA','MEDIA','ALTA') | |
| status | ENUM('PENDENTE','CONCLUIDA','CANCELADA') | |
| due_date | TIMESTAMP | Prazo |
| completed_at | TIMESTAMP | Quando concluiu |
| created_at | TIMESTAMP | |
| updated_at | TIMESTAMP | |

### timeline_events

| Campo | Tipo | Descrição |
|-------|------|-----------|
| id | UUID (PK) | |
| lead_id | UUID (FK → leads) | |
| user_id | UUID (FK → users) | |
| type | ENUM('CREATED','STATUS_CHANGED','FIELD_UPDATED','INTERACTION','NOTE_ADDED','TASK_CREATED','TASK_COMPLETED','ASSIGNED','CONTACT_ADDED','CONTACT_UPDATED') | |
| metadata | JSONB | Dados específicos do evento |
| created_at | TIMESTAMP | |

### Transições de status (Pipeline)

```
NOVO ──▶ CONTATO ──▶ NEGOCIACAO ──▶ GANHO
  │         │            │              │
  └──▶ ARQUIVADO ◀─── ARQUIVADO ◀───── ARQUIVADO
                        │
                   PERDIDO ──▶ ARQUIVADO
                   
ARQUIVADO ──▶ NOVO (reativar)
```

---

## 5. API REST

### Padrão de respostas

```json
// Sucesso individual
{ "data": { ... }, "message": "...", "timestamp": "..." }

// Lista paginada
{ "data": [...], "page": 0, "size": 20, "totalElements": 150, "totalPages": 8 }

// Erro
{ "status": 400, "error": "Bad Request", "message": "...", "path": "/api/...", "timestamp": "..." }
```

### Endpoints

#### Autenticação
```
POST   /api/auth/login                → Login (JWT)
POST   /api/auth/register             → Admin cria usuário
POST   /api/auth/refresh              → Refresh token
GET    /api/auth/me                   → Dados do usuário logado
PUT    /api/auth/password             → Alterar senha
```

#### Usuários (DIRETOR)
```
GET    /api/users                     → Listar (paginado, filtros)
GET    /api/users/{id}                → Detalhe
PUT    /api/users/{id}                → Editar
DELETE /api/users/{id}                → Desativar
GET    /api/users/hierarchy           → Árvore hierárquica
GET    /api/users/team                → Time do gerente
```

#### Leads
```
GET    /api/leads                     → Listar (paginado, filtros, busca)
POST   /api/leads                     → Criar (AQUISICAO, DIRETOR, GERENTE_AQUISICAO)
GET    /api/leads/{id}                → Detalhe (com contatos, última interação)
PUT    /api/leads/{id}                → Editar
DELETE /api/leads/{id}                → Arquivar (soft delete)
GET    /api/leads/search?q=           → Busca textual full
GET    /api/leads/kanban              → Pipeline agrupado por status
```

#### Pipeline
```
PATCH  /api/leads/{id}/status         → Mover no kanban
PATCH  /api/leads/{id}/assign         → Atribuir responsável
```

#### Contatos
```
GET    /api/leads/{leadId}/contacts               → Listar
POST   /api/leads/{leadId}/contacts               → Adicionar
PUT    /api/leads/{leadId}/contacts/{id}          → Editar
DELETE /api/leads/{leadId}/contacts/{id}          → Remover
PATCH  /api/leads/{leadId}/contacts/{id}/main     → Definir como principal
```

#### Interações
```
GET    /api/leads/{leadId}/interactions           → Histórico
POST   /api/leads/{leadId}/interactions           → Registrar
PUT    /api/leads/{leadId}/interactions/{id}      → Editar (só admin)
```

#### Tarefas
```
GET    /api/leads/{leadId}/tasks                  → Tarefas do lead
POST   /api/leads/{leadId}/tasks                  → Criar (gerente/diretor)
PUT    /api/leads/{leadId}/tasks/{id}             → Editar
PATCH  /api/leads/{leadId}/tasks/{id}/complete    → Concluir
PATCH  /api/leads/{leadId}/tasks/{id}/reopen      → Reabrir
DELETE /api/leads/{leadId}/tasks/{id}             → Remover
GET    /api/tasks/my                              → Minhas tarefas (todos leads)
GET    /api/tasks/team                            → Tarefas do time (gerente)
GET    /api/users/{userId}/tasks                  → Tarefas de um usuário (gerente)
```

#### Timeline
```
GET    /api/leads/{leadId}/timeline               → Feed completo do lead
```

#### Dashboard
```
GET    /api/dashboard                             → Resumo (personalizado por role)
GET    /api/dashboard/team                        → Overview do time (gerente/diretor)
```

#### Segmentos
```
GET    /api/segments                              → Lista segmentos existentes
POST   /api/segments                              → Adicionar (admin)
```

---

## 6. Controle de Tarefas & Accountability

### Ciclo de vida

```
CRIADA (gerente/diretor atribui)
  │
  ├── ✅ CONCLUÍDA NO PRAZO  (completed_at <= due_date)
  │
  ├── ⚠️ CONCLUÍDA COM ATRASO (completed_at > due_date)
  │     └── daysOverdue calculado automaticamente
  │
  └── ❌ VENCIDA (due_date passou, status = PENDENTE)
```

### Cálculo automático de `completionStatus` (backend)

| Situação | Status calculado |
|----------|-----------------|
| `completed_at <= due_date` | `NO_PRAZO` |
| `completed_at > due_date` | `COM_ATRASO` |
| Pendente + `due_date < now()` | `VENCIDA` |
| Pendente + sem due_date ou `due_date >= now()` | `EM_DIA` |

### Timeline registra tudo

Toda conclusão de task gera evento na timeline do lead com:
- Título da tarefa
- Data de conclusão vs prazo
- Status (no prazo / com atraso)
- Dias de atraso

---

## 7. Tratamento de Erros

`GlobalExceptionHandler.java` captura:

| Exceção | Status | Caso |
|---------|--------|------|
| `MethodArgumentNotValidException` | 400 | Validação de campos |
| `BadCredentialsException` | 401 | Login inválido |
| `AccessDeniedException` | 403 | Role não permitida |
| `ResourceNotFoundException` | 404 | Lead/user não encontrado |
| `DuplicateResourceException` | 409 | Email duplicado |
| `InvalidTransitionException` | 422 | Transição de status inválida |
| `GenericException` | 500 | Erro inesperado |

---

## 8. Rotas do Frontend

```
/login                          → LoginPage
/dashboard                      → DashboardPage
/leads                          → KanbanPage
/leads?status=NOVO              → Kanban filtrado
/leads?assignedTo=me            → Kanban só meus leads
/leads/new                      → LeadCreatePage
/leads/:id                      → LeadDetailPage (dados + timeline + contatos + tarefas)
/leads/:id/edit                 → LeadEditPage
/tasks                          → MyTasksPage
/users                          → UsersPage (admin)
/users/hierarchy                → HierarchyPage (admin)
```

---

## 9. Testes

### Spring Boot
- **Unitários:** Service + Mockito (regras de negócio, transições, permissões)
- **Integração:** @SpringBootTest + Testcontainers (PostgreSQL real)
- **Segurança:** Testar que cada endpoint respeita as roles

### React
- **Vitest** + **Testing Library** para componentes
- **MSW** para mockar API

---

## 10. Preparação para IA Futura

- Campo `enriched_data` (JSONB) na tabela `leads` para dados enriquecidos por IA
- Separação clara entre dados manuais (campos fixos) e dados automáticos (JSONB)
- Sem impacto no MVP — o campo simplesmente fica null até a IA ser integrada
