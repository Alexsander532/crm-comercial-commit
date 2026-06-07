# Sprint 7 — Timeline

**Duração estimada**: 1 dia
**Depende de**: S3 (Leads), S4 (Kanban), S5 (Contatos), S6 (Tarefas)
**Bloqueia**: S8 (Dashboard precisa de dados da timeline)

---

## 🎯 Objetivo

Timeline de eventos imutável para cada lead. Todo estado gera um evento: criação, edição, mudança de status, interação, tarefa, contato.

---

## 📊 Dependências

```
T41 (TimelineEvent entity) ──→ T42 (TimelineEventRepository) ──→ T43 (TimelineService)
                                                                    │
                                              ┌─────────────────────┤
                                              │                     │
                                    T44 (Integrar nos services)   T45 (TimelineController)
                                              │                     │
                                       (modifica arquivos      ┌───┴───┐
                                        já existentes)     T46 (Testar) T47 (Frontend)
```

### Pode rodar em paralelo:
- **T46 + T47** (após T45)

### Atenção:
- **T44 toca em arquivos que já existem** (LeadService, PipelineService, TaskService, ContactService)
- Importante: fazer merge cuidadoso se houve mudanças paralelas

---

## Tasks

### T41 — TimelineEvent entity + TimelineEventType enum

| Campo | Valor |
|-------|-------|
| **ID** | T41 |
| **Tempo est.** | 15 min |
| **Depende de** | S3, S4, S5, S6 completos |
| **Arquivos** | `model/TimelineEvent.java`, `model/TimelineEventType.java` |
| **Teste** | `mvn compile` |

TimelineEvent: id (UUID), lead (ManyToOne), user (ManyToOne), type (enum), metadata (JSONB), createdAt.
TimelineEventType: CREATED, STATUS_CHANGED, FIELD_UPDATED, INTERACTION, NOTE_ADDED, TASK_CREATED, TASK_COMPLETED, ASSIGNED, CONTACT_ADDED, CONTACT_UPDATED.

Commit: `feat(timeline): add TimelineEvent entity and type enum`

---

### T42 — TimelineEventRepository

| Campo | Valor |
|-------|-------|
| **ID** | T42 |
| **Tempo est.** | 10 min |
| **Depende de** | T41 |
| **Arquivos** | `repository/TimelineEventRepository.java` |
| **Teste** | `mvn compile` |

Queries: findByLeadIdOrderByCreatedAtDesc, findByLeadIdOrderByCreatedAtDescPagable.

Commit: `feat(timeline): add TimelineEventRepository`

---

### T43 — TimelineService

| Campo | Valor |
|-------|-------|
| **ID** | T43 |
| **Tempo est.** | 45 min |
| **Depende de** | T42 |
| **Arquivos** | `service/TimelineService.java` |
| **Teste** | `mvn test` |

Métodos de registro:
- recordCreated(lead, user)
- recordStatusChanged(lead, user, from, to)
- recordFieldUpdated(lead, user, field, old, new)
- recordInteraction(lead, user, type, description)
- recordTaskCreated(lead, user, task)
- recordTaskCompleted(lead, user, task, status, daysOverdue)
- recordAssigned(lead, user, fromId, toId)
- recordContactAdded(lead, user, contact)
- recordContactUpdated(lead, user, contact, field)

Método de consulta: getTimeline(leadId).

Commit: `feat(timeline): add TimelineService with event recording methods`

---

### T44 — Integrar Timeline nos services existentes

| Campo | Valor |
|-------|-------|
| **ID** | T44 |
| **Tempo est.** | 45 min |
| **Depende de** | T43 |
| **Arquivos** | Modificar: `LeadService.java`, `PipelineService.java`, `TaskService.java`, `ContactService.java` |
| **Teste** | `mvn test` ⚠️ Pode quebrar testes existentes |

Injetar TimelineService em cada service e chamar os métodos de registro nos momentos certos:
- LeadService.create → recordCreated
- LeadService.update → recordFieldUpdated
- PipelineService.moveStatus → recordStatusChanged
- TaskService.create → recordTaskCreated
- TaskService.complete → recordTaskCompleted
- ContactService.create → recordContactAdded

⚠️ **Atenção:** Esta task modifica arquivos que já existem. Fazer commits pequenos e testar após cada modificação.

Commit: `feat(timeline): integrate TimelineService into all domain services`

---

### T45 — TimelineController

| Campo | Valor |
|-------|-------|
| **ID** | T45 |
| **Tempo est.** | 15 min |
| **Depende de** | T43 |
| **Arquivos** | `controller/TimelineController.java` |
| **Teste** | curl GET timeline |

Endpoint:
- GET /api/leads/{leadId}/timeline → lista eventos (paginado, mais recentes primeiro)

Commit: `feat(timeline): add TimelineController`

---

### T46 — Testar timeline

| Campo | Valor |
|-------|-------|
| **ID** | T46 |
| **Tempo est.** | 15 min |
| **Depende de** | T44, T45 |
| **Teste** | curl verificando eventos |

```bash
# Criar lead → evento CREATED
# Mudar status → evento STATUS_CHANGED
# Adicionar contato → evento CONTACT_ADDED
# Criar tarefa → evento TASK_CREATED

curl http://localhost:8080/api/leads/$ID/timeline \
  -H "Authorization: Bearer $TOKEN"
# Esperado: lista de eventos em ordem cronológica
```

---

### T47 — Frontend Timeline

| Campo | Valor |
|-------|-------|
| **ID** | T47 |
| **Tempo est.** | 45 min |
| **Depende de** | T45 |
| **Arquivos** | `components/Timeline.tsx`, `components/TimelineEvent.tsx`, `services/timelineService.ts` |
| **Teste** | Navegador: timeline mostra eventos |

Feed cronológico no detail do lead mostrando: tipo de evento, quem fez, quando, metadados.
Ícones por tipo: 🆕 CREATED, 🔄 STATUS_CHANGED, ✏️ FIELD_UPDATED, 📞 INTERACTION, 📝 NOTE_ADDED, ✅ TASK_CREATED, 🏁 TASK_COMPLETED, 👤 ASSIGNED, 👥 CONTACT_ADDED.

Commit: `feat(timeline): add frontend timeline component`

---

## ✅ Checklist

- [ ] Criar lead gera evento CREATED
- [ ] Mudar status gera evento STATUS_CHANGED
- [ ] Criar tarefa gera evento TASK_CREATED
- [ ] Adicionar contato gera evento CONTACT_ADDED
- [ ] GET /leads/{id}/timeline retorna eventos ordenados
- [ ] Frontend: feed visual com ícones por tipo

**Próximo sprint:** [Sprint 08 — Dashboard](sprint-08-dashboard.md)