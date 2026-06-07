# Sprint 8 — Dashboard

**Duração estimada**: 0.5 dia
**Depende de**: S2, S3, S4, S5, S6, S7 (todos anteriores)
**Bloqueia**: S9

---

## 🎯 Objetivo

Dashboard com métricas por papel: DIRETOR vê tudo, GERENTE vê seu time, FUNCIONÁRIO vê só o seu.

---

## 📊 Dependências

```
T48 (DashboardResponse DTO) ──→ T49 (DashboardService) ──→ T50 (DashboardController)
                                                                  │
                                                           ┌──────┴──────┐
                                                      T51 (Testar)  T52 (Frontend Dashboard)
```

---

## Tasks

### T48 — DashboardResponse DTO

| Campo | Valor |
|-------|-------|
| **ID** | T48 |
| **Tempo est.** | 20 min |
| **Depende de** | S7 completo |
| **Arquivos** | `dto/response/DashboardResponse.java` com inner classes: Summary, RecentLead, RecentActivity, TopSegment, UserActivity, OverdueTask, StaleLead |
| **Teste** | `mvn compile` |

Commit: `feat(dashboard): add DashboardResponse DTO`

---

### T49 — DashboardService (métricas por role)

| Campo | Valor |
|-------|-------|
| **ID** | T49 |
| **Tempo est.** | 45 min |
| **Depende de** | T48 |
| **Arquivos** | `service/DashboardService.java` |
| **Teste** | `mvn test` |

Três builders por role:
- **DIRETOR:** total de leads, leads por status, taxa de conversão, tarefas atrasadas por time, leads frios
- **GERENTE:** leads do time, tarefas atrasadas do time, conversão do time
- **FUNCIONÁRIO:** meus leads, minhas tarefas pendentes, minhas atrasadas

Commit: `feat(dashboard): add DashboardService with metrics per role`

---

### T50 — DashboardController

| Campo | Valor |
|-------|-------|
| **ID** | T50 |
| **Tempo est.** | 20 min |
| **Depende de** | T49 |
| **Arquivos** | `controller/DashboardController.java` |
| **Teste** | curl GET /api/dashboard |

Endpoints:
- GET /api/dashboard → dashboard personalizado por role
- GET /api/dashboard/stale-leads?days=30 → leads sem interação há X dias (DIRETOR, GERENTE)

Commit: `feat(dashboard): add DashboardController`

---

### T51 — Testar dashboard

| Campo | Valor |
|-------|-------|
| **ID** | T51 |
| **Tempo est.** | 15 min |
| **Depende de** | T50 |
| **Teste** | curl com diferentes roles |

```bash
# Login como DIRETOR → dashboard com métricas globais
# Login como GERENTE → dashboard com métricas do time
# Login como FUNCIONÁRIO → dashboard pessoal
```

---

### T52 — Frontend Dashboard

| Campo | Valor |
|-------|-------|
| **ID** | T52 |
| **Tempo est.** | 60 min |
| **Depende de** | T50 |
| **Arquivos** | `pages/DashboardPage.tsx`, `components/DashboardCards.tsx`, `components/PipelineChart.tsx`, `components/StaleLeadsList.tsx`, `services/dashboardService.ts` |
| **Teste** | Navegador: dashboard mostra métricas |

Cards com: total de leads, taxa de conversão, tarefas atrasadas, leads sem interação.
Pipeline chart: barras por status.
Lista de leads frios (sem interação há 30+ dias).
Tudo personalizado por role.

Commit: `feat(dashboard): add frontend dashboard with role-based metrics`

---

## ✅ Checklist

- [ ] Dashboard do diretor mostra métricas globais
- [ ] Dashboard do gerente mostra métricas do time
- [ ] Dashboard do funcionário mostra métricas pessoais
- [ ] Leads frios (sem interação há 30+ dias) aparecem
- [ ] Frontend: cards + chart + lista

**Próximo sprint:** [Sprint 09 — Polish](sprint-09-polish.md)