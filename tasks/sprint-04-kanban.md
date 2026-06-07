# Sprint 4 — Pipeline Kanban

**Duração estimada**: 1 dia
**Depende de**: S2 (Auth), S3 (Leads CRUD)
**Bloqueia**: S7 (Timeline)
**Status**: ✅ Concluído (PR #5)

---

## 🎯 Objetivo

Pipeline com validação de transições, endpoints REST e frontend Kanban com 6 colunas.

---

## Tasks

### T24 — PipelineService + T25 + T26 ✅

**O que foi feito:**
- `LeadStatus.java` refatorado com `canTransitionTo()` e `isTerminal()` — regras de transição embutidas no enum
- `PipelineService` — `moveStatus(leadId, newStatus, currentUser)` com validação de permissão:
  - DIRETOR/GERENTE_PROSPECCAO/PROSPECCAO (atribuído) podem mover
  - AQUISICAO/GERENTE_AQUISICAO não movem (403)
- `PipelineService.getKanbanView()` retorna `List<LeadResponse>` filtrado por role
- `StatusUpdateRequest` DTO com validação
- `PipelineController` com endpoints:
  - `PATCH /leads/{id}/status` — mover status
  - `PATCH /leads/{id}/assign` — atribuir lead
  - `GET /leads/kanban` — leads agrupados por status

**Transições validadas:**
| De → Para | Válido? |
|-----------|---------|
| NOVO → CONTATO | ✅ |
| NOVO → ARQUIVADO | ✅ |
| CONTATO → NEGOCIACAO | ✅ |
| CONTATO → NOVO (back) | ✅ |
| NEGOCIACAO → GANHO | ✅ |
| NEGOCIACAO → PERDIDO | ✅ |
| NEGOCIACAO → CONTATO (back) | ✅ |
| ARQUIVADO → NOVO | ✅ |
| NOVO → GANHO (skip) | ❌ |
| GANHO → qualquer | ❌ (terminal) |

### T27 — Testar transições ✅
- Testado com curl: NOVO→CONTATO (200), CONTATO→GANHO (erro 400 esperado)
- LazyInitializationException corrigida (PipelineService agora retorna DTOs, não entities)
- Kanban endpoint validado com leads agrupados por status

### T28 — Frontend Kanban ✅
- `KanbanPage.tsx` — 6 colunas visuais (NOVO..ARQUIVADO)
- Cards com companyName, segment, assignedToName
- Contagem de leads por coluna
- Loading state
- Rota `/kanban` protegida

### Builds
- Backend: 52/52 testes
- Frontend: npm run build ✅

### PR
**PR #5:** https://github.com/Alexsander532/crm-comercial-commit/pull/5

---

**Próximo sprint:** [Sprint 05 — Contatos](sprint-05-contacts.md)
