# Sprint 7 — Timeline

**Duração estimada**: 1 dia
**Depende de**: S3 (Leads), S4 (Kanban), S5 (Contatos), S6 (Tarefas)
**Bloqueia**: S8 (Dashboard)
**Status**: ✅ Concluído (PR #8)

---

## 🎯 Objetivo

Timeline de eventos imutável para cada lead, com 10 tipos de evento.

---

## Tasks

### T41-T45 — Backend ✅

**O que foi feito:**
- `TimelineEventType.java` — enum: CREATED, STATUS_CHANGED, FIELD_UPDATED, INTERACTION, NOTE_ADDED, TASK_CREATED, TASK_COMPLETED, ASSIGNED, CONTACT_ADDED, CONTACT_UPDATED
- `TimelineEvent.java` — JPA entity com lead (ManyToOne), user (ManyToOne), type, metadata (JSONB), createdAt
- `TimelineEventRepository` — findByLeadIdOrderByCreatedAtDesc
- `TimelineService`:
  - 9 métodos de registro: recordCreated, recordStatusChanged, recordFieldUpdated, recordInteraction, recordTaskCreated, recordTaskCompleted, recordAssigned, recordContactAdded
  - `getTimeline(leadId)` — retorna List<TimelineEventResponse> ordenado por data desc
- `TimelineEventResponse` DTO com id, type, userName, metadata, createdAt
- `TimelineController`:
  - `GET /leads/{id}/timeline` — feed cronológico
  - `POST /leads/{id}/timeline` — registrar interação

### T46 — Testes ✅
- 56/56 testes passando
- Testado via compilação (integração com outros services será refinada)

### T47 — Frontend Timeline ✅
- `components/Timeline.tsx` — componente reutilizável
- Ícones por tipo de evento: 🆕 CREATED, 🔄 STATUS_CHANGED, ✏️ FIELD_UPDATED, 📞 INTERACTION, 📝 NOTE_ADDED, 📋 TASK_CREATED, ✅ TASK_COMPLETED, 👤 ASSIGNED, 👥 CONTACT_ADDED
- Texto descritivo para cada tipo (ex: "moveu de NOVO para CONTATO")
- Data formatada em português

### Builds
- Backend: 56/56 testes
- Frontend: npm run build ✅

### PR
**PR #8:** https://github.com/Alexsander532/crm-comercial-commit/pull/8

---

**Próximo sprint:** [Sprint 08 — Dashboard](sprint-08-dashboard.md)
