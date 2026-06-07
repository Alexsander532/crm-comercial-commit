# Sprint 6 — Tarefas

**Duração estimada**: 1 dia
**Depende de**: S2 (Auth), S3 (Leads CRUD)
**Bloqueia**: S7 (Timeline precisa de eventos de tarefas)

---

## 🎯 Objetivo

Tarefas atribuídas a leads e usuários, com prazos, prioridade e accountability (status calculado: NO_PRAZO, COM_ATRASO, VENCIDA).

---

## 📊 Dependências

```
T34 (Task entity/enums) ──┬── T35 (TaskRepository) ──┬── T37 (TaskService)
                           │                           │
                           └── T36 (Task DTOs) ────────┘
                                                              │
                                                       T38 (TaskController)
                                                              │
                                                       ┌──────┴──────┐
                                                  T39 (Testar)  T40 (Frontend)
```

### Pode rodar em paralelo:
- **T35 + T36** (após T34)

---

## Tasks

### T34 — Task entity + enums

| Campo | Valor |
|-------|-------|
| **ID** | T34 |
| **Tempo est.** | 15 min |
| **Depende de** | S3 (Lead existe) |
| **Arquivos** | `model/Task.java`, `model/TaskPriority.java`, `model/TaskStatus.java` |
| **Teste** | `mvn compile` |

Task: id, title, description, priority (enum), status (enum), dueDate, completedAt, assignedTo (User), lead (ManyToOne), createdBy (User), createdAt, updatedAt.
TaskPriority: ALTA, MEDIA, BAIXA.
TaskStatus: PENDENTE, EM_ANDAMENTO, CONCLUIDA, CANCELADA.

Commit: `feat(tasks): add Task entity and enums`

---

### T35 — TaskRepository

| Campo | Valor |
|-------|-------|
| **ID** | T35 |
| **Tempo est.** | 15 min |
| **Depende de** | T34 |
| **Arquivos** | `repository/TaskRepository.java` |
| **Teste** | `mvn compile` |

Queries: findByAssignedToId, findByLeadId, countPendingByUserId, countOverdueByUserId, findByDueDateBeforeAndStatusNot.

Commit: `feat(tasks): add TaskRepository with overdue queries`

---

### T36 — Task DTOs

| Campo | Valor |
|-------|-------|
| **ID** | T36 |
| **Tempo est.** | 15 min |
| **Depende de** | T34 |
| **Arquivos** | `dto/request/TaskRequest.java`, `dto/request/TaskCompleteRequest.java`, `dto/response/TaskResponse.java` |
| **Teste** | `mvn compile` |

TaskRequest: title, description, priority, dueDate, assignedToId, leadId.
TaskCompleteRequest: completionNotes (opcional).
TaskResponse: id, title, priority, status, dueDate, completionStatus (calculado), assignedTo, leadName, createdAt.

Commit: `feat(tasks): add Task DTOs with completionStatus`

---

### T37 — TaskService (accountability)

| Campo | Valor |
|-------|-------|
| **ID** | T37 |
| **Tempo est.** | 45 min |
| **Depende de** | T35, T36 |
| **Arquivos** | `service/TaskService.java` |
| **Teste** | `mvn test` |

Métodos: create, update, complete, cancel, findByLead, findByUser, getOverdue.
Regras de accountability (calculado em runtime):
- PENDENTE + prazo futuro → NO_PRAZO
- PENDENTE/EM_ANDAMENTO + prazo passado → COM_ATRASO
- VENCIDA se > 3 dias atrasada e sem interação
Apenas GERENTE+ e DIRETOR criam tarefas. Funcionário apenas visualiza e completa.

Commit: `feat(tasks): add TaskService with accountability logic`

---

### T38 — TaskController

| Campo | Valor |
|-------|-------|
| **ID** | T38 |
| **Tempo est.** | 30 min |
| **Depende de** | T37 |
| **Arquivos** | `controller/TaskController.java` |
| **Teste** | curl nos endpoints |

Endpoints:
- POST /api/leads/{leadId}/tasks → criar (GERENTE+, DIRETOR)
- GET /api/leads/{leadId}/tasks → listar tarefas do lead
- GET /api/tasks/my → minhas tarefas
- GET /api/tasks/overdue → tarefas atrasadas (GERENTE+, DIRETOR)
- PUT /api/tasks/{id} → editar (owner ou criador)
- PATCH /api/tasks/{id}/complete → completar (assignedTo)
- PATCH /api/tasks/{id}/cancel → cancelar (criador ou DIRETOR)

Commit: `feat(tasks): add TaskController with role-based endpoints`

---

### T39 — Testar tarefas com curl

| Campo | Valor |
|-------|-------|
| **ID** | T39 |
| **Tempo est.** | 15 min |
| **Depende de** | T38 |
| **Teste** | curl testando CRUD + accountability |

```bash
# Criar tarefa
curl -X POST http://localhost:8080/api/leads/$ID/tasks \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"title":"Ligar para cliente","priority":"ALTA","dueDate":"2026-06-10","assignedToId":"$USER_ID"}'

# Completar tarefa
curl -X PATCH http://localhost:8080/api/tasks/$TASK_ID/complete \
  -H "Authorization: Bearer $TOKEN"

# Ver minhas tarefas
curl http://localhost:8080/api/tasks/my \
  -H "Authorization: Bearer $TOKEN"
```

---

### T40 — Frontend Tarefas

| Campo | Valor |
|-------|-------|
| **ID** | T40 |
| **Tempo est.** | 60 min |
| **Depende de** | T38 |
| **Arquivos** | `pages/TasksPage.tsx`, `components/TaskList.tsx`, `components/TaskForm.tsx`, `components/TaskCard.tsx`, `services/taskService.ts`, `types/task.ts` |
| **Teste** | Navegador: tarefas funcionam |

TasksPage: minhas tarefas + indicadores de prazo (verde/amarelo/vermelho).
TaskList no detail do lead: tarefas do lead.
TaskForm: criar tarefa com data, prioridade, atribuição.
Indicadores: 🟢 NO_PRAZO, 🟡 COM_ATRASO, 🔴 VENCIDA.

Commit: `feat(tasks): add frontend tasks with accountability indicators`

---

## ✅ Checklist

- [ ] Criar tarefa funciona (GERENTE+ cria)
- [ ] Completar tarefa funciona (funcionário completa)
- [ ] Status calculado: NO_PRAZO, COM_ATRASO, VENCIDA
- [ ] Tarefas atrasadas aparecem no endpoint
- [ ] Frontend: indicadores visuais de prazo

**Próximo sprint:** [Sprint 07 — Timeline](sprint-07-timeline.md)