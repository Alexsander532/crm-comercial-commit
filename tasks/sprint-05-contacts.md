# Sprint 5 — Contatos

**Duração estimada**: 0.5 dia
**Depende de**: S3 (Leads CRUD)
**Bloqueia**: S7 (Timeline precisa de eventos de contato)
**Pode rodar em paralelo com**: S4 (Kanban) — são independentes!

---

## 🎯 Objetivo

Múltiplos contatos por lead, com marcação de principal, tipos de contato (email, telefone, etc).

---

## 📊 Dependências

```
T29 (Contact entity) ──→ T30 (ContactRepository) ──┬── T31 (ContactService)
                                                      │
                       T29 ──→ ContactDTOs ──────────┘
                                                      │
                                               T32 (ContactController)
                                                      │
                                               T33 (Frontend Contatos)
```

### Pode rodar em paralelo com:
- **Sprint 4 (Kanban)** — Contatos e Pipeline são independentes

---

## Tasks

### T29 — Contact entity + ContactType

| Campo | Valor |
|-------|-------|
| **ID** | T29 |
| **Tempo est.** | 10 min |
| **Depende de** | S3 (Lead entity existe) |
| **Arquivos** | `model/Contact.java`, `model/ContactType.java` |
| **Teste** | `mvn compile` |

Contact: id, name, email, phone, role, type (enum), isMain, lead (ManyToOne), createdAt, updatedAt.
ContactType: EMAIL, TELEFONE, LINKEDIN, WHATSAPP, OUTRO.

Commit: `feat(contacts): add Contact entity and ContactType enum`

---

### T30 — ContactRepository

| Campo | Valor |
|-------|-------|
| **ID** | T30 |
| **Tempo est.** | 10 min |
| **Depende de** | T29 |
| **Arquivos** | `repository/ContactRepository.java` |
| **Teste** | `mvn compile` |

Queries: findByLeadId, findMainContactByLeadId, existsByEmailAndLeadId.

Commit: `feat(contacts): add ContactRepository`

---

### T31 — ContactService

| Campo | Valor |
|-------|-------|
| **ID** | T31 |
| **Tempo est.** | 30 min |
| **Depende de** | T30 |
| **Arquivos** | `service/ContactService.java` |
| **Teste** | `mvn test` |

Métodos: create (verifica main), update, delete, setAsMain (desmarca o anterior), getContactsByLead, getMainContact.
Regra: só pode haver 1 contato principal por lead.

Commit: `feat(contacts): add ContactService with set-as-main logic`

---

### T32 — ContactController

| Campo | Valor |
|-------|-------|
| **ID** | T32 |
| **Tempo est.** | 20 min |
| **Depende de** | T31 |
| **Arquivos** | `controller/ContactController.java`, `dto/request/ContactRequest.java`, `dto/response/ContactResponse.java` |
| **Teste** | curl nos endpoints |

Endpoints:
- POST /api/leads/{leadId}/contacts → criar contato
- GET /api/leads/{leadId}/contacts → listar contatos
- GET /api/leads/{leadId}/contacts/{id} → detalhar
- PUT /api/leads/{leadId}/contacts/{id} → editar
- PATCH /api/leads/{leadId}/contacts/{id}/main → definir como principal
- DELETE /api/leads/{leadId}/contacts/{id} → remover

Commit: `feat(contacts): add ContactController with CRUD and set-main`

---

### T33 — Frontend Contatos

| Campo | Valor |
|-------|-------|
| **ID** | T33 |
| **Tempo est.** | 45 min |
| **Depende de** | T32 |
| **Arquivos** | `components/ContactList.tsx`, `components/ContactForm.tsx`, `services/contactService.ts`, `types/contact.ts` |
| **Teste** | Navegador: CRUD de contatos |

Lista de contatos no detail do lead, form para adicionar, badge "Principal", botão para definir como principal.

Commit: `feat(contacts): add frontend contact list and form in lead detail`

---

## 🔁 Execução paralela com Kanban (S4)

```
Agente A: S4 (Kanban completo)
Agente B: S5 (Contatos) ← PODE RODAR EM PARALELO!
```

---

## ✅ Checklist

- [ ] Criar contato em um lead funciona
- [ ] Listar contatos de um lead
- [ ] Definir contato como principal (desmarca o anterior)
- [ ] Deletar contato
- [ ] Frontend: lista + form + badge principal

**Próximo sprint:** [Sprint 06 — Tarefas](sprint-06-tasks.md)