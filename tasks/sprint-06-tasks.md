# Sprint 6 — Tarefas

**Duração estimada**: 1 dia
**Depende de**: S2 (Auth), S3 (Leads CRUD)
**Bloqueia**: S7 (Timeline)
**Status**: ✅ Concluído (PR #7)

---

## 🎯 Objetivo

Tarefas com accountability (status calculado: NO_PRAZO, COM_ATRASO, VENCIDA).

---

## Tasks

### T34-T38 — Backend (entity + repository + service + controller) ✅

**O que foi feito:**
- `Task.java` — entity com title, description, priority, status, dueDate, completedAt
  - `getCompletionStatus()` — calculado em runtime: NO_PRAZO / COM_ATRASO / VENCIDA / SEM_PRAZO
  - `getDaysOverdue()` — dias de atraso
- `TaskPriority` enum — BAIXA, MEDIA, ALTA
- `TaskStatus` enum — PENDENTE, CONCLUIDA, CANCELADA
- `TaskRepository` — findByLeadId, findByAssignedToId, countByAssignedToIdAndStatus, countOverdueByUserId
- `TaskRequest` DTO com title (NotBlank), priority, assignedToId, dueDate
- `TaskService`:
  - `create(leadId, request, creator)` — só GERENTES e DIRETOR criam (AQUISICAO/PROSPECCAO → 403)
  - `complete(taskId, user)` — só o assigned user pode completar
  - `cancel(taskId, user)` — só criador ou DIRETOR pode cancelar
- `TaskController`:
  - `POST /leads/{id}/tasks` — criar
  - `GET /leads/{id}/tasks` — listar do lead
  - `GET /tasks/my` — minhas tarefas
  - `PATCH /tasks/{id}/complete` — concluir
  - `PATCH /tasks/{id}/cancel` — cancelar

### T39 — Testes ✅
- Testado via unit tests (4 testes de TaskService)
- Testes de permissão (funcionário não cria, only assigned completes)

### T40 — Frontend Tarefas ✅
- `TasksPage.tsx` — lista de tarefas com:
  - Prioridade colorida (ALTA=vermelho, MEDIA=amarelo, BAIXA=verde)
  - Status de completion colorido
  - Botão "Concluir" para tarefas pendentes
  - Empty state
- Rota `/tasks` protegida

### Builds
- Backend: 56/56 testes
- Frontend: npm run build ✅
- **⚠️ Bug pós-merge:** `countByAssignedToIdAndStatus` usava `String` mas o banco esperava `TaskStatus` — corrigido com hotfix no DashboardService

### PR
**PR #7:** https://github.com/Alexsander532/crm-comercial-commit/pull/7

---

**Próximo sprint:** [Sprint 07 — Timeline](sprint-07-timeline.md)
