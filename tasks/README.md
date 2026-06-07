# 🎯 Tasks — Visão Geral

> **Como usar:** Leia este arquivo para entender o plano. Depois abra o sprint da vez para detalhes.

---

## 📅 Sprints

| Sprint | Feature | Arquivo | Duração Est. | Status |
|--------|---------|---------|-------------|--------|
| S1 | Setup do Projeto | [sprint-01-foundation.md](sprint-01-foundation.md) | 1 dia | ✅ Concluído |
| S2 | Auth + Login | [sprint-02-auth.md](sprint-02-auth.md) | 1 dia | ✅ Concluído (PR #3) |
| S3 | Leads CRUD | [sprint-03-leads.md](sprint-03-leads.md) | 1 dia | ✅ Concluído (PR #4) |
| S4 | Pipeline Kanban | [sprint-04-kanban.md](sprint-04-kanban.md) | 1 dia | 🔴 Não iniciado |
| S5 | Contatos | [sprint-05-contacts.md](sprint-05-contacts.md) | 0.5 dia | 🔴 Não iniciado |
| S6 | Tarefas | [sprint-06-tasks.md](sprint-06-tasks.md) | 1 dia | 🔴 Não iniciado |
| S7 | Timeline | [sprint-07-timeline.md](sprint-07-timeline.md) | 1 dia | 🔴 Não iniciado |
| S8 | Dashboard | [sprint-08-dashboard.md](sprint-08-dashboard.md) | 0.5 dia | 🔴 Não iniciado |
| S9 | Polish | [sprint-09-polish.md](sprint-09-polish.md) | 1 dia | 🔴 Não iniciado |

---

## 🔗 Dependências entre Sprints

```
S1 (Setup)
├── S2 (Auth) ← precisa de S1
│   └── S3 (Leads) ← precisa de S1 + S2
│       ├── S4 (Kanban) ← precisa de S2 + S3
│       │                  ┌──────────────────┐
│       └── S5 (Contatos) ← precisa de S3 ────┤ PODE RODAR EM PARALELO!
│                                          └───┘
│       └── S6 (Tarefas) ← precisa de S2 + S3
│           └── S7 (Timeline) ← precisa de S3, S4, S5, S6
│               └── S8 (Dashboard) ← precisa de tudo
│                   └── S9 (Polish) ← precisa de tudo
```

---

## ⚡ Otimização com Agentes Paralelos

### Sprint 1 — Máximo paralelismo (3 agentes)

```
Agente A: T1→T2→T3 (Backend setup)
Agente B: T4 (Frontend setup)           ← PARALELO
Agente C: T5 (Docker)                    ← PARALELO com A após T1
Depois: T6→T7 (Migrations + Build test)  ← Sequencial
```

### Sprints 2-3 — Sequencial (1 agente cada)

```
S2: T8→T9→T10→T11→T12→T13→T14→T15→T16
S3: T17→T18→T19→T20→T21→T22→T23
```

### Sprints 4-5 — PARALELO! (2 agentes)

```
Agente A: S4 (Kanban completo) → T24→T25→T26→T27→T28
Agente B: S5 (Contatos completo) → T29→T30→T31→T32→T33
```

### Sprints 6-9 — Sequencial (1 agente cada)

```
S6: T34→T35→T36→T37→T38→T39→T40
S7: T41→T42→T43→T44→T45→T46→T47
S8: T48→T49→T50→T51→T52
S9: T53→T54→T55→T56→T57
```

---

## 📊 Resumo por Sprint

| Sprint | Tasks | Tempo Est. | Paralelizável? |
|--------|-------|------------|----------------|
| S1 | T1-T7 (7 tasks) | ~2h | Sim (3 agentes) |
| S2 | T8-T16 (9 tasks) | ~3h | Parcial (T9||T11, T15||T16) |
| S3 | T17-T23 (7 tasks) | ~3h | Parcial (T18||T19) |
| S4 | T24-T28 (5 tasks) | ~3h | Parcial (T27||T28) |
| S5 | T29-T33 (5 tasks) | ~2h | Pouco (T30||DTOs) |
| S6 | T34-T40 (7 tasks) | ~3h | Parcial (T35||T36) |
| S7 | T41-T47 (7 tasks) | ~3h | Parcial (T46||T47) |
| S8 | T48-T52 (5 tasks) | ~2h | Parcial (T51||T52) |
| S9 | T53-T57 (5 tasks) | ~3h | Sim (T53||T54) |
| **Total** | **57 tasks** | **~24h** | |

---

## 🗓️ Calendário Estimado (1 pessoa, sequencial)

| Dia | Sprint | Objetivo do dia |
|-----|--------|-----------------|
| Dia 1 | S1 | Projeto compila, banco sobe, migrations OK |
| Dia 2 | S2 | Login funciona, frontend loga |
| Dia 3 | S3 | CRUD de leads funciona |
| Dia 4 | S4+S5 | Kanban + Contatos (paralelo) |
| Dia 5 | S6 | Tarefas com accountability |
| Dia 6 | S7 | Timeline registrando tudo |
| Dia 7 | S8 | Dashboard com métricas |
| Dia 8 | S9 | Testes, docs, health check |

---

## 📋 Todas as Tasks (checklist)

### S1 — Foundation
- [ ] T1 — Criar pom.xml
- [ ] T2 — Criar Application.java
- [ ] T3 — Criar application.yml
- [ ] T4 — Criar projeto React
- [ ] T5 — Criar Docker Compose
- [ ] T6 — Criar Flyway Migrations
- [ ] T7 — Testar build completo

### S2 — Auth
- [ ] T8 — User entity + UserRole
- [ ] T9 — UserRepository
- [ ] T10 — Auth DTOs
- [ ] T11 — JWT (Token Provider + Filter + UserDetailsService)
- [ ] T12 — Security Config
- [ ] T13 — AuthService
- [ ] T14 — AuthController
- [ ] T15 — Testar login com curl
- [ ] T16 — Frontend Login Page

### S3 — Leads CRUD
- [ ] T17 — Lead entity + enums
- [ ] T18 — LeadRepository
- [ ] T19 — Lead DTOs
- [ ] T20 — LeadService
- [ ] T21 — LeadController
- [ ] T22 — Testar CRUD com curl
- [ ] T23 — Frontend Leads (tabela + form)

### S4 — Kanban
- [ ] T24 — PipelineService
- [ ] T25 — Pipeline DTOs
- [ ] T26 — PipelineController
- [ ] T27 — Testar transições
- [ ] T28 — Frontend Kanban (drag & drop)

### S5 — Contatos
- [ ] T29 — Contact entity + ContactType
- [ ] T30 — ContactRepository
- [ ] T31 — ContactService
- [ ] T32 — ContactController
- [ ] T33 — Frontend Contatos

### S6 — Tarefas
- [ ] T34 — Task entity + enums
- [ ] T35 — TaskRepository
- [ ] T36 — Task DTOs
- [ ] T37 — TaskService (accountability)
- [ ] T38 — TaskController
- [ ] T39 — Testar tarefas
- [ ] T40 — Frontend Tarefas

### S7 — Timeline
- [ ] T41 — TimelineEvent entity + TimelineEventType
- [ ] T42 — TimelineEventRepository
- [ ] T43 — TimelineService
- [ ] T44 — Integrar Timeline nos services
- [ ] T45 — TimelineController
- [ ] T46 — Testar timeline
- [ ] T47 — Frontend Timeline

### S8 — Dashboard
- [ ] T48 — DashboardResponse DTO
- [ ] T49 — DashboardService (métricas por role)
- [ ] T50 — DashboardController
- [ ] T51 — Testar dashboard
- [ ] T52 — Frontend Dashboard

### S9 — Polish
- [ ] T53 — Testes de integração
- [ ] T54 — Testes de segurança (RBAC)
- [ ] T55 — Documentação da API
- [ ] T56 — Health check + CORS final
- [ ] T57 — Verificação final

---

## 🔗 Referências rápidas

- **Spec:** `.specify/features/mvp-crm-comercial.md`
- **Plano mestre:** `.specify/features/_master-plan.md`
- **Planos por feature:** `.specify/features/0{N}-{name}/plan.md`
- **Este board:** `tasks/README.md`
- **Sprints detalhados:** `tasks/sprint-{NN}-*.md`

---

## 📝 Como usar no dia a dia

### Manhã (início do dia):
1. Abra `tasks/README.md` → veja qual sprint está
2. Abra `tasks/sprint-{NN}-*.md` → veja qual task fazer
3. Marque a task como "em andamento" no checklist acima

### Desenvolvimento:
4. Siga os passos da task (arquivos, código, commit)
5. Teste conforme indicado (mvn compile, curl, navegador)

### Fim do dia:
6. Marque tasks concluídas no checklist
7. Commit tudo
8. Se o sprint acabou, faça PR e merge para main
9. Atualize o status no README acima

### Com agentes de IA:
10. Para paralelizar, use o diagrama de dependências no sprint
11. Dispatch agentes para tasks independentes (marcadas como "paralelo com")
12. Agente = 1 task por vez, com contexto do sprint inteiro