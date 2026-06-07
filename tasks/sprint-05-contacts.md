# Sprint 5 — Contatos

**Duração estimada**: 0.5 dia
**Depende de**: S3 (Leads CRUD)
**Bloqueia**: S7 (Timeline)
**Status**: ✅ Concluído (PR #6)

---

## 🎯 Objetivo

Múltiplos contatos por lead, com marcação de principal.

---

## Tasks

### T29-T32 — Backend (entity + repository + service + controller) ✅

**O que foi feito:**
- `Contact.java` — JPA entity vinculada a Lead (ManyToOne), com name, role, phone, email, whatsapp, isMain
- `ContactRepository` — findByLeadIdOrderByCreatedAtAsc, findByLeadIdAndIsMainTrue, countByLeadId
- `ContactRequest` DTO com name (NotBlank), role, phone, email, whatsapp, notes (todos opcionais exceto name)
- `ContactResponse` DTO com todos campos
- `ContactService`:
  - `create(leadId, request)` — primeiro contato vira automaticamente main
  - `delete(leadId, contactId)` — valida pertinência ao lead
  - `setAsMain(leadId, contactId)` — desmarca todos, marca selecionado
- `ContactController`:
  - `GET /leads/{id}/contacts` — listar
  - `POST /leads/{id}/contacts` — criar
  - `DELETE /leads/{id}/contacts/{cid}` — remover
  - `PATCH /leads/{id}/contacts/{cid}/main` — definir como principal

### T33 — Frontend Contatos ✅
- Seção de contatos integrada ao `LeadDetailPage.tsx`
- Formulário inline para adicionar contato (nome obrigatório, cargo/telefone/email/whatsapp opcionais)
- Lista de contatos com badge "Principal"
- Botões para definir como principal e remover, com confirmação

### Builds
- Backend: 52/52 testes
- Frontend: npm run build ✅

### PR
**PR #6:** https://github.com/Alexsander532/crm-comercial-commit/pull/6

---

**Próximo sprint:** [Sprint 06 — Tarefas](sprint-06-tasks.md)
