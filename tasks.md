# Tasks Board — CRM Comercial

> **Como usar:** Olhe a seção `HOJE` toda manhã. Faça a task. Marque como `✅ Feito`. Passe para a próxima.

---

## 📋 Board de Tasks

### 🟢 Backlog (a fazer)

| ID | Feature | Task | Descrição | Tempo Est. | Depende de |
|----|---------|------|-----------|------------|------------|
| T1 | F01 | Criar pom.xml | Spring Boot com todas dependências | 10 min | — |
| T2 | F01 | Criar Application.java | Classe principal | 5 min | T1 |
| T3 | F01 | Criar application.yml | Configuração de banco + JWT | 10 min | T1 |
| T4 | F01 | Criar projeto React | package.json + Vite + Tailwind | 15 min | — |
| T5 | F01 | Criar docker-compose.yml | PostgreSQL + backend + frontend | 15 min | T1, T4 |
| T6 | F01 | Criar migrations Flyway | V1 a V7 (schema completo) | 30 min | T5 |
| T7 | F01 | Testar build | `mvn compile` + `npm run build` + `docker up` | 15 min | T1-T6 |
| T8 | F02 | User entity + Role | User.java + UserRole enum | 15 min | T7 |
| T9 | F02 | UserRepository | JPA repository com findByEmail | 10 min | T8 |
| T10 | F02 | DTOs de Auth | LoginRequest, RegisterRequest, LoginResponse | 20 min | T8 |
| T11 | F02 | JWT Provider | Gerar/validar token + filter | 30 min | T9 |
| T12 | F02 | Security Config | Spring Security + CORS + roles | 30 min | T11 |
| T13 | F02 | AuthService | Login + register com BCrypt | 30 min | T10, T12 |
| T14 | F02 | AuthController | Endpoints POST /login, /register | 20 min | T13 |
| T15 | F02 | Testar login | curl no /api/auth/login | 10 min | T14 |
| T16 | F02 | Frontend Login | LoginPage.tsx + AuthContext | 45 min | T14 |
| T17 | F03 | Lead entity + Status | Lead.java + LeadStatus enum | 15 min | T7 |
| T18 | F03 | LeadRepository | JPA + queries customizadas | 20 min | T17 |
| T19 | F03 | Lead DTOs | LeadRequest + LeadResponse | 15 min | T17 |
| T20 | F03 | LeadService | CRUD + filtros + busca | 45 min | T18, T19 |
| T21 | F03 | LeadController | REST endpoints CRUD | 30 min | T20 |
| T22 | F03 | Testar CRUD | curl no POST, GET, PUT, DELETE | 15 min | T21 |
| T23 | F03 | Frontend Leads | Tabela de leads + form | 60 min | T21 |
| T24 | F04 | PipelineService | Validar transições de status | 30 min | T22 |
| T25 | F04 | PipelineController | PATCH /status + /assign | 20 min | T24 |
| T26 | F04 | Testar transições | curl validando movimentos | 15 min | T25 |
| T27 | F04 | Frontend Kanban | Drag & drop com 6 colunas | 90 min | T25 |
| T28 | F05 | Contact entity + repo | Contact.java + ContactRepository | 15 min | T22 |
| T29 | F05 | ContactService | CRUD + definir principal | 30 min | T28 |
| T30 | F05 | ContactController | REST endpoints | 20 min | T29 |
| T31 | F05 | Frontend Contatos | Lista + form no lead | 45 min | T30 |
| T32 | F06 | Task entity + enums | Task.java + Priority + Status | 15 min | T22 |
| T33 | F06 | TaskRepository | Queries por usuário e prazo | 15 min | T32 |
| T34 | F06 | TaskService | CRUD + concluir + accountability | 45 min | T33 |
| T35 | F06 | TaskController | REST endpoints | 30 min | T34 |
| T36 | F06 | Testar accountability | curl validando status calculado | 15 min | T35 |
| T37 | F06 | Frontend Tarefas | Lista + form + marcar concluído | 60 min | T35 |
| T38 | F07 | TimelineEvent entity | TimelineEvent + TimelineEventType | 15 min | T22 |
| T39 | F07 | TimelineService | Métodos de registro por evento | 45 min | T38 |
| T40 | F07 | Integrar timeline | Hook em Lead, Task, Contact, Pipeline | 45 min | T39 |
| T41 | F07 | TimelineController | GET /leads/{id}/timeline | 15 min | T40 |
| T42 | F07 | Frontend Timeline | Feed cronológico no lead | 45 min | T41 |
| T43 | F08 | Dashboard DTO | DashboardResponse com todas métricas | 20 min | T42 |
| T44 | F08 | DashboardService | Queries por role (DIRETOR/GERENTE/FUNC) | 45 min | T43 |
| T45 | F08 | DashboardController | GET /dashboard | 20 min | T44 |
| T46 | F08 | Frontend Dashboard | Cards com métricas | 60 min | T45 |
| T47 | F09 | Testes de integração | AuthControllerTest com Testcontainers | 45 min | T46 |
| T48 | F09 | Testes de segurança | RBAC tests (401/403) | 45 min | T47 |
| T49 | F09 | Health check | GET /api/health | 10 min | T48 |
| T50 | F09 | API Docs | README com endpoints e exemplos | 30 min | T49 |

---

## 🎯 HOJE — O que fazer agora

> Atualize esta seção toda manhã. Copie a próxima task do Backlog.

**Data:** 2026-06-06
**Task atual:** T1
**Feature:** F01 — Setup do Projeto

### Task T1: Criar pom.xml do Spring Boot

**Arquivo:** `backend/pom.xml`
**Tempo estimado:** 10 minutos
**Depois desta:** T2 (Application.java)

**O que fazer:**
1. Criar pasta `backend/`
2. Criar `pom.xml` com Spring Boot 3.2.5, Web, Data JPA, Security, PostgreSQL, Flyway, JWT, Lombok, Testcontainers
3. Verificar: `mvn clean compile` deve passar

**Como testar:**
```bash
cd backend
mvn clean compile
# Esperado: BUILD SUCCESS
```

**Quando terminar:**
- Marque T1 como `✅ Feito` abaixo
- Passe para T2

---

## ✅ Feito (concluído)

| ID | Task | Feature | Data de conclusão |
|----|------|---------|-------------------|
| | | | |

---

## 🔄 Em andamento

| ID | Task | Feature | Iniciado em | Progresso |
|----|------|---------|-------------|-----------|
| T1 | Criar pom.xml | F01 | 2026-06-06 | 0% |

---

## 🚧 Bloqueado

| ID | Task | Feature | Bloqueado por | Desde |
|----|------|---------|---------------|-------|
| | | | | |

---

## 📊 Estatísticas

| Métrica | Valor |
|---------|-------|
| Total de tasks | 50 |
| Concluídas | 0 |
| Em andamento | 1 |
| Bloqueadas | 0 |
| A fazer | 49 |
| Features concluídas | 0/9 |
| Tempo estimado total | ~25 horas |

---

## 🗓️ Calendário estimado

| Dia | Features | Tasks | Objetivo |
|-----|----------|-------|----------|
| Dia 1 | F01 | T1-T7 | Projeto compila, banco sobe |
| Dia 2 | F02 | T8-T16 | Login funciona, frontend loga |
| Dia 3 | F03 | T17-T23 | Criar/editar/buscar leads |
| Dia 4 | F04 | T24-T27 | Mover leads no kanban |
| Dia 5 | F05 | T28-T31 | Adicionar contatos |
| Dia 6 | F06 | T32-T37 | Tarefas com accountability |
| Dia 7 | F07 | T38-T42 | Timeline registrando tudo |
| Dia 8 | F08 | T43-T46 | Dashboard com métricas |
| Dia 9 | F09 | T47-T50 | Testes, docs, health check |

---

## 📝 Como atualizar este arquivo

**Quando terminar uma task:**
1. Mova a linha da seção `Em andamento` → `Feito`
2. Adicione a data de conclusão
3. Atualize a estatística
4. Escreva a próxima task na seção `HOJE`

**Quando iniciar uma task:**
1. Mova a linha do `Backlog` → `Em andamento`
2. Coloque a data de início
3. Atualize a seção `HOJE` com a descrição

**Quando uma feature acabar:**
1. Marque a feature como concluída no calendário
2. Atualize a estatística de features

---

## 🔗 Referências rápidas

- Plano mestre: [`.specify/features/_master-plan.md`](.specify/features/_master-plan.md)
- Spec: [`.specify/features/mvp-crm-comercial.md`](.specify/features/mvp-crm-comercial.md)
- F01 Setup: [`.specify/features/01-setup-projeto/plan.md`](.specify/features/01-setup-projeto/plan.md)
- F02 Auth: [`.specify/features/02-auth/plan.md`](.specify/features/02-auth/plan.md)
- F03 Leads: [`.specify/features/03-leads-crud/plan.md`](.specify/features/03-leads-crud/plan.md)
- F04 Kanban: [`.specify/features/04-pipeline-kanban/plan.md`](.specify/features/04-pipeline-kanban/plan.md)
- F05 Contatos: [`.specify/features/05-contatos/plan.md`](.specify/features/05-contatos/plan.md)
- F06 Tarefas: [`.specify/features/06-tarefas/plan.md`](.specify/features/06-tarefas/plan.md)
- F07 Timeline: [`.specify/features/07-timeline/plan.md`](.specify/features/07-timeline/plan.md)
- F08 Dashboard: [`.specify/features/08-dashboard/plan.md`](.specify/features/08-dashboard/plan.md)
- F09 Polish: [`.specify/features/09-polish/plan.md`](.specify/features/09-polish/plan.md)
