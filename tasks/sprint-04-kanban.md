# Sprint 4 — Pipeline Kanban

**Duração estimada**: 1 dia
**Depende de**: S2 (Auth), S3 (Leads CRUD)
**Bloqueia**: S7 (Timeline precisa de status changes)

---

## 🎯 Objetivo

Kanban com colunas de status, drag & drop, validação de transições (não pular etapas), e atribuição.

---

## 📊 Dependências

```
T24 (PipelineService) ──→ T25 (StatusUpdateRequest DTO) ──→ T26 (PipelineController)
                                                              │
                                                        ┌─────┴──────┐
                                                   T27 (Testar)  T28 (Frontend Kanban)
```

### Pode rodar em paralelo:
- **T27 + T28** (após T26)

---

## Tasks

### T24 — PipelineService (validação de transições)

| Campo | Valor |
|-------|-------|
| **ID** | T24 |
| **Tempo est.** | 30 min |
| **Depende de** | S3 completo |
| **Arquivos** | `service/PipelineService.java` |
| **Teste** | `mvn test` |

Regras de transição:
- NOVO → CONTATO ✅
- CONTATO → NEGOCIACAO ✅
- NEGOCIACAO → GANHO ✅
- NEGOCIACAO → PERDIDO ✅
- Qualquer status → ARQUIVADO ✅
- Pular etapas para frente: ❌ (exceto ARQUIVADO)
- Voltar etapas: ✅ (NEGOCIACAO → CONTATO)
- GANHO/PERDIDO → não pode mover

Métodos: moveStatus(leadId, newStatus, userId), validateTransition(current, new).

Commit: `feat(kanban): add PipelineService with transition validation`

---

### T25 — DTOs de Pipeline

| Campo | Valor |
|-------|-------|
| **ID** | T25 |
| **Tempo est.** | 10 min |
| **Depende de** | T24 |
| **Arquivos** | `dto/request/StatusUpdateRequest.java`, `dto/request/AssignRequest.java`, `dto/response/PipelineResponse.java` |
| **Teste** | `mvn compile` |

StatusUpdateRequest: newStatus (NotBlank), reason (opcional para PERDIDO/ARQUIVADO).
AssignRequest: userId (NotBlank).
PipelineResponse: id, companyName, status, assignedTo, order, updatedAt.

Commit: `feat(kanban): add pipeline DTOs`

---

### T26 — PipelineController

| Campo | Valor |
|-------|-------|
| **ID** | T26 |
| **Tempo est.** | 20 min |
| **Depende de** | T25 |
| **Arquivos** | `controller/PipelineController.java` |
| **Teste** | curl PATCH |

Endpoints:
- PATCH /api/leads/{id}/status → mover no pipeline
- PATCH /api/leads/{id}/assign → atribuir lead
- GET /api/pipeline → visão kanban (leads agrupados por status)

Commit: `feat(kanban): add PipelineController with move and assign endpoints`

---

### T27 — Testar transições com curl

| Campo | Valor |
|-------|-------|
| **ID** | T27 |
| **Tempo est.** | 15 min |
| **Depende de** | T26 |
| **Teste** | curl testando todas as transições |

```bash
# NOVO → CONTATO (válido)
curl -X PATCH http://localhost:8080/api/leads/$ID/status \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"newStatus": "CONTATO"}' # 200 OK

# NOVO → NEGOCIACAO (inválido - pula etapa)
curl -X PATCH http://localhost:8080/api/leads/$ID/status \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"newStatus": "NEGOCIACAO"}' # 400 Bad Request

# Qualquer → ARQUIVADO (sempre válido)
curl -X PATCH http://localhost:8080/api/leads/$ID/status \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"newStatus": "ARQUIVADO"}' # 200 OK
```

---

### T28 — Frontend Kanban (drag & drop)

| Campo | Valor |
|-------|-------|
| **ID** | T28 |
| **Tempo est.** | 90 min |
| **Depende de** | T26 |
| **Arquivos** | `pages/KanbanPage.tsx`, `components/KanbanColumn.tsx`, `components/KanbanCard.tsx`, `services/pipelineService.ts` |
| **Teste** | Navegador: drag & drop funciona |

6 colunas: NOVO, CONTATO, NEGOTIACAO, GANHO, PERDIDO, ARQUIVADO.
Cards com: companyName, assignedTo, indicators.
Drag & drop com transição PATCH.
Validação frontend: não permitir saltos.

Commit: `feat(kanban): add frontend kanban with drag & drop and transition validation`

---

## ✅ Checklist

- [ ] Mover lead de NOVO → CONTATO funciona
- [ ] Pular etapa (NOVO → NEGOTIACAO) retorna erro
- [ ] Arquivar lead (qualquer → ARQUIVADO) funciona
- [ ] Atribuir lead funciona
- [ ] GET /api/pipeline retorna leads agrupados por status
- [ ] Frontend: drag & drop com validação visual

**Próximo sprint:** [Sprint 05 — Contatos](sprint-05-contacts.md)