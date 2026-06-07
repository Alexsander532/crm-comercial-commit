# Sprint 8 — Dashboard

**Duração estimada**: 0.5 dia
**Depende de**: S2 (Auth), S3 (Leads), S6 (Tarefas), S7 (Timeline)
**Status**: ✅ Concluído (PR #9)

---

## 🎯 Objetivo

Dashboard com métricas por papel: DIRETOR vê tudo, AQUISICAO vê próprios leads, PROSPECCAO vê atribuídos.

---

## Tasks

### T48-T50 — Backend ✅

**O que foi feito:**
- `DashboardResponse` DTO com:
  - `Summary` — totalLeads, leadsThisMonth, tasksPending, tasksOverdue
  - `pipelineChart` — Map<String, Long> com contagem por status
  - `recentTasks` — lista de TaskSummary (title, status, leadName, daysOverdue)
- `DashboardService.getDashboard(User)`:
  - Carrega leads conforme a role do usuário (DIRETOR=all, AQUISICAO=createdBy, PROSPECCAO=assignedTo)
  - Calcula leads dos últimos 30 dias
  - Agrupa por status para o pipeline chart
  - Busca tarefas pendentes e atrasadas do usuário
  - Top 5 tarefas pendentes
- `DashboardController` — `GET /api/dashboard`

### T51 — Testes ✅
- Testado com curl: 200 OK com métricas
- **⚠️ Bug encontrado e corrigido:** TaskRepository.countByAssignedToIdAndStatus usava `String` mas o campo Task.status é `TaskStatus` — corrigido

### T52 — Frontend Dashboard ✅
- `DashboardPage.tsx` substitui o placeholder antigo
- 4 cards de métricas (total leads, leads 30 dias, tarefas pendentes, tarefas atrasadas)
- Pipeline chart (lista de status com contagem)
- Tarefas recentes
- Design responsivo com grid

### Builds
- Backend: 56/56 testes
- Frontend: npm run build ✅

### PR
**PR #9:** https://github.com/Alexsander532/crm-comercial-commit/pull/9

---

**Próximo sprint:** [Sprint 09 — Polish](sprint-09-polish.md)
