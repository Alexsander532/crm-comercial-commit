# Sprint 3 — Leads CRUD

**Duração estimada**: 1 dia
**Depende de**: S1 (Setup), S2 (Auth)
**Bloqueia**: S4, S5, S6, S7, S8

---

## 🎯 Objetivo

CRUD completo de leads com filtros, busca e atribuição por role.

---

## 📊 Dependências

```
T17 (Lead entity/enums) ──┬── T18 (LeadRepository)
                          ├── T19 (Lead DTOs)
                          │
T18 + T19 ────────────────┴── T20 (LeadService)
                                      │
                               T21 (LeadController)
                                      │
                               ┌──────┴──────┐
                          T22 (Testar)   T23 (Frontend Leads)
```

### Pode rodar em paralelo:
- **T18 + T19** — Repository e DTOs são independentes

### Sequencial:
- **T20** depende de T18 e T19
- **T21** depende de T20
- **T22 e T23** dependem de T21 (podem rodar em paralelo entre si)

---

## Tasks

### T17 — Lead entity + LeadStatus + LeadSegment enums

| Campo | Valor |
|-------|-------|
| **ID** | T17 |
| **Tempo est.** | 15 min |
| **Depende de** | S2 completo |
| **Arquivos** | `model/Lead.java`, `model/LeadStatus.java`, `model/LeadSegment.java`, `model/InteractionType.java` |
| **Teste** | `mvn compile` |

Lead: id (UUID), companyName, address, segment (enum), status (enum), enrichedData (JSONB), assignedTo (User), contacts, interactions, tasks, timelineEvents, createdAt, updatedAt.
LeadStatus: NOVO, CONTATO, NEGOCIACAO, GANHO, PERDIDO, ARQUIVADO.
LeadSegment: TECNOLOGIA, FINANCAS, SAUDE, EDUCACAO, VAREJO, OUTRO.
InteractionType: EMAIL, TELEFONE, WHATSAPP, REUNIAO, OUTRO.

Commit: `feat(leads): add Lead entity and enums`

---

### T18 — LeadRepository

| Campo | Valor |
|-------|-------|
| **ID** | T18 |
| **Tempo est.** | 15 min |
| **Depende de** | T17 |
| **Arquivos** | `repository/LeadRepository.java` |
| **Teste** | `mvn compile` |

Queries: findByStatus, findBySegment, findByAssignedToId, countByStatus, search by companyName containing (IgnoreCase), findByAssignedToIdAndStatus.

Commit: `feat(leads): add LeadRepository with custom queries`

---

### T19 — Lead DTOs

| Campo | Valor |
|-------|-------|
| **ID** | T19 |
| **Tempo est.** | 15 min |
| **Depende de** | T17 |
| **Arquivos** | `dto/request/LeadRequest.java`, `dto/response/LeadResponse.java`, `dto/response/LeadListResponse.java` |
| **Teste** | `mvn compile` |

LeadRequest: companyName (NotBlank), address, segment, assignedToId.
LeadResponse: id, companyName, address, segment, status, assignedTo (nome), createdAt, updatedAt.
LeadListResponse: content (List), page, size, totalElements, totalPages.

Commit: `feat(leads): add Lead DTOs`

---

### T20 — LeadService

| Campo | Valor |
|-------|-------|
| **ID** | T20 |
| **Tempo est.** | 45 min |
| **Depende de** | T18, T19 |
| **Arquivos** | `service/LeadService.java` |
| **Teste** | `mvn test` |

Métodos: create, update, delete (soft — status ARQUIVADO), findById, findAll (paginado com filtros), search, assignTo, getLeadsByStatus, getLeadsByUser.
Regras: AQUISICAO/GERENTE_AQUISICAO criam, PROSPECCAO só visualiza, DIRETOR vê tudo.

Commit: `feat(leads): add LeadService with CRUD, filters, and role-based visibility`

---

### T21 — LeadController

| Campo | Valor |
|-------|-------|
| **ID** | T21 |
| **Tempo est.** | 30 min |
| **Depende de** | T20 |
| **Arquivos** | `controller/LeadController.java` |
| **Teste** | curl nos endpoints |

Endpoints:
- POST /api/leads → criar (AQUISICAO+, GERENTE_AQUISICAO+, DIRETOR)
- GET /api/leads → listar (todos autenticados, filtrado por role)
- GET /api/leads/{id} → detalhar
- PUT /api/leads/{id} → editar (owner ou DIRETOR)
- DELETE /api/leads/{id} → arquivar (soft delete)
- PUT /api/leads/{id}/assign → atribuir (GERENTE+, DIRETOR)

Commit: `feat(leads): add LeadController with role-based endpoints`

---

### T22 — Testar CRUD com curl

| Campo | Valor |
|-------|-------|
| **ID** | T22 |
| **Tempo est.** | 15 min |
| **Depende de** | T21 |
| **Teste** | curl em todos os endpoints |

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"diretor@commit.com","password":"admin123"}' | jq -r '.data.token')

# Criar lead
curl -X POST http://localhost:8080/api/leads \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"companyName":"Empresa Test","segment":"TECNOLOGIA"}'

# Listar leads
curl http://localhost:8080/api/leads \
  -H "Authorization: Bearer $TOKEN"

# Buscar por nome
curl "http://localhost:8080/api/leads?search=Empresa" \
  -H "Authorization: Bearer $TOKEN"
```

---

### T23 — Frontend Leads (tabela + form)

| Campo | Valor |
|-------|-------|
| **ID** | T23 |
| **Tempo est.** | 60 min |
| **Depende de** | T21 |
| **Arquivos** | `pages/LeadsPage.tsx`, `pages/LeadDetailPage.tsx`, `components/LeadTable.tsx`, `components/LeadForm.tsx`, `services/leadService.ts`, `types/lead.ts` |
| **Teste** | Navegador: CRUD funciona |

Criar:
- LeadsPage: tabela com busca e filtros
- LeadDetailPage: detalhes do lead + editar
- LeadForm: criar/editar lead
- leadService.ts: chamadas à API
- types/lead.ts: tipos TypeScript

Commit: `feat(leads): add frontend leads page with table, detail, and form`

---

## 🔁 Execução paralela por agentes

```
Agente A: T17 → T18 → T20 → T21 → T22
Agente B: T17 → T19 ───────────────→ T23 (após T21)
```

**Ou sequencial se 1 agente só:** T17 → T18 → T19 → T20 → T21 → T22 → T23

---

## ✅ Checklist

- [ ] Criar lead funciona (POST /api/leads)
- [ ] Listar leads com filtros (GET /api/leads?status=NOVO&search=Empresa)
- [ ] Editar lead (PUT /api/leads/{id})
- [ ] Arquivar lead (DELETE → status ARQUIVADO)
- [ ] Atribuir lead (PUT /api/leads/{id}/assign)
- [ ] Frontend: tabela + busca + form

**Próximo sprint:** [Sprint 04 — Kanban](sprint-04-kanban.md)