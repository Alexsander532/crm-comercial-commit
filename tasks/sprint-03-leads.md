# Sprint 3 — Leads CRUD

**Duração estimada**: 1 dia
**Depende de**: S1 (Setup), S2 (Auth)
**Bloqueia**: S4, S5, S6, S7, S8
**Status**: ✅ Concluído (PR #4)

---

## 🎯 Objetivo

CRUD completo de leads com filtros, busca textual, atribuição por role e frontend.

---

## 📊 Dependências entre Tasks

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

---

## Tasks

### T17 — Lead entity + enums ✅

| Campo | Valor |
|-------|-------|
| **ID** | T17 |
| **Tempo real** | 15 min |
| **Depende de** | S2 |
| **Arquivos** | `model/Lead.java`, `model/LeadStatus.java`, `model/LeadSegment.java`, `model/LeadTest.java` |
| **Testes** | 4/4 ✅ |

**O que foi feito:**
- `Lead.java` — JPA entity mapeando tabela `leads`:
  - Campos: companyName (NOT NULL), site, instagram, whatsapp, address (opcional), segment, notes, enrichedData (JSONB), status
  - `@ManyToOne` para createdBy (User) e assignedTo (User)
  - Status default: `NOVO` via `@Builder.Default`
  - `enrichedData` com `@JdbcTypeCode(SqlTypes.JSON)` para JSONB
- `LeadStatus.java` — enum: NOVO, CONTATO, NEGOCIACAO, GANHO, PERDIDO, ARQUIVADO
- `LeadSegment.java` — enum: TECNOLOGIA, FINANCAS, SAUDE, EDUCACAO, VAREJO, OUTRO

**Edge cases:**
- assignedTo pode ser null (lead não atribuído)
- Status default NOVO mesmo sem setter explícito
- enrichedData é JSONB (reservado para pós-MVP)

**Testes:** 4 — criação, status default, todos segments, todos statuses

**Commit:** `feat(leads): add Lead entity with enums`

---

### T18 — LeadRepository ✅

| Campo | Valor |
|-------|-------|
| **ID** | T18 |
| **Tempo real** | 10 min |
| **Depende de** | T17 |
| **Arquivos** | `repository/LeadRepository.java`, `repository/LeadRepositoryTest.java` |
| **Testes** | 3/3 ✅ |

**O que foi feito:**
- `findByStatus(LeadStatus)` — busca por status
- `findBySegment(String)` — busca por segmento
- `findByAssignedToId(UUID)` — leads atribuídos a um usuário
- `countByStatus(LeadStatus)` — contagem por status (dashboard)
- `search(String, Pageable)` — ILIKE em companyName, site, instagram, notes
- `findByCreatedById(UUID, Pageable)` — leads de um criador
- `findByAssignedToId(UUID, Pageable)` — leads atribuídos (paginado)

**Testes:** Mockito — findByStatus, countByStatus, search

**Commit:** `feat(leads): add LeadRepository with search and filters`

---

### T19 — Lead DTOs ✅

| Campo | Valor |
|-------|-------|
| **ID** | T19 |
| **Tempo real** | 10 min |
| **Depende de** | T17 |
| **Arquivos** | `dto/request/LeadRequest.java`, `dto/response/LeadResponse.java`, `dto/response/LeadListResponse.java`, `dto/LeadDtoTest.java` |
| **Testes** | 4/4 ✅ |

**O que foi feito:**
- `LeadRequest` — record com `@NotBlank companyName`, `@NotNull segment`, demais opcionais
- `LeadResponse` — record com todos os dados + createdByName e assignedToName (resolvidos)
- `LeadListResponse` — paginação: content, page, size, totalElements, totalPages

**Edge cases:**
- address é opcional (validation só exige companyName + segment)
- createdByName resolvido do relacionamento (não expõe o User inteiro)
- assignedToName null se lead não atribuído

**Testes:** 4 — validation, valid request, build response, list response

**Commit:** `feat(leads): add Lead DTOs with validation`

---

### T20 — LeadService ✅

| Campo | Valor |
|-------|-------|
| **ID** | T20 |
| **Tempo real** | 30 min |
| **Depende de** | T18, T19 |
| **Arquivos** | `service/LeadService.java`, `service/LeadServiceTest.java` |
| **Testes** | 7/7 ✅ |

**O que foi feito:**

#### Permissões implementadas:

| Operação | DIRETOR | GER_AQUIS | GER_PROSP | AQUISICAO | PROSPECCAO |
|----------|---------|-----------|-----------|-----------|------------|
| Criar | ✅ | ✅ | ❌ | ✅ | ❌ |
| Editar | ✅ | ✅ | ✅ | ✅ (próprio) | ❌ |
| Arquivar | ✅ | ❌ | ✅ | ❌ | ✅ (atribuído) |
| Atribuir | ✅ | ❌ | ✅ | ❌ | ❌ |
| Visualizar | ✅ | ✅ | ✅ | ✅ (criou) | ✅ (atribuído) |

#### Métodos:
- `create(LeadRequest, User)` — valida permissão, salva, retorna response
- `update(UUID, LeadRequest, User)` — valida permissão, atualiza campos
- `archive(UUID, User)` — soft delete (status = ARQUIVADO)
- `assignTo(UUID leadId, UUID userId, User)` — atribui lead
- `findById(UUID, User)` — busca + valida visibilidade
- `findAll(Pageable, User)` — lista com filtro por role
- `search(String, Pageable, User)` — busca textual

**Role-based findAll:**
- DIRETOR/GERENTES → findAll (todos)
- AQUISICAO → findByCreatedById
- PROSPECCAO → findByAssignedToId

**Testes:** 7 — criar, prospeccao bloqueado, update, archive, assign, role filter, search

**Commit:** `feat(leads): add LeadService with CRUD, role-based visibility, and search`

---

### T21 — LeadController ✅

| Campo | Valor |
|-------|-------|
| **ID** | T21 |
| **Tempo real** | 15 min |
| **Depende de** | T20 |
| **Arquivos** | `controller/LeadController.java` |
| **Teste** | Testado via curl ✅ |

**Endpoints REST:**

| Método | Path | Descrição |
|--------|------|-----------|
| POST | `/api/leads` | Criar lead |
| GET | `/api/leads` | Listar (paginado) |
| GET | `/api/leads/{id}` | Detalhar |
| PUT | `/api/leads/{id}` | Editar |
| DELETE | `/api/leads/{id}` | Arquivar (soft delete) |
| PUT | `/api/leads/{id}/assign?userId=X` | Atribuir |
| GET | `/api/leads/search?q=` | Buscar textual |

**Todos os endpoints retornam `ApiResponse<T>` padronizado.**

**Commit:** `feat(leads): add LeadController with CRUD endpoints`

---

### T22 — Testar CRUD com curl ✅

| Campo | Valor |
|-------|-------|
| **ID** | T22 |
| **Tempo real** | 15 min |
| **Depende de** | T21 |
| **Teste** | Todos endpoints validados ✅ |

**Cenários testados:**

```bash
# 1. Login como diretor
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"diretor@commit.com","password":"admin123"}' | jq -r '.data.token')

# 2. Criar lead
curl -X POST http://localhost:8080/api/leads \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"companyName":"Empresa Exemplo","segment":"TECNOLOGIA"}'
# RESULTADO: 201 Created, id retornado

# 3. Listar leads
curl http://localhost:8080/api/leads -H "Authorization: Bearer $TOKEN"
# RESULTADO: 200, totalElements: 1

# 4. Detalhar lead
curl http://localhost:8080/api/leads/$ID -H "Authorization: Bearer $TOKEN"
# RESULTADO: 200, "Empresa Exemplo - NOVO"

# 5. Editar lead
curl -X PUT http://localhost:8080/api/leads/$ID \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"companyName":"Empresa Editada","segment":"FINANCAS"}'
# RESULTADO: 200, "Empresa Editada - FINANCAS"

# 6. Buscar
curl "http://localhost:8080/api/leads/search?q=Editada" -H "Authorization: Bearer $TOKEN"
# RESULTADO: 200, 1 resultado

# 7. Arquivar
curl -X DELETE http://localhost:8080/api/leads/$ID -H "Authorization: Bearer $TOKEN"
# RESULTADO: 200, "Lead arquivado"
```

**⚠️ Nota:** O teste foi feito após build do JAR com `mvn clean package -DskipTests` e startup da aplicação com variáveis de ambiente do Supabase.

**Commit:** `test(leads): verify CRUD endpoints with curl`

---

### T23 — Frontend Leads ✅

| Campo | Valor |
|-------|-------|
| **ID** | T23 |
| **Tempo real** | 45 min |
| **Depende de** | T21 |
| **Arquivos** | `pages/LeadsPage.tsx`, `pages/LeadDetailPage.tsx`, `pages/LeadFormPage.tsx`, `services/leadService.ts`, `types/lead.ts`, `App.tsx` |
| **Teste** | `npm run build` ✅ |

**O que foi feito:**

- **LeadsPage** — tabela responsiva com:
  - Busca textual (filtra via API)
  - Badges de status coloridos (azul NOVO, amarelo CONTATO, roxo NEGOCIACAO, verde GANHO, vermelho PERDIDO, cinza ARQUIVADO)
  - Links para detalhe/edição
  - Botão de arquivar com confirmação
  - Loading state, empty state

- **LeadDetailPage** — visualização completa:
  - Todos os campos do lead
  - Grid 2 colunas responsivo
  - Observações como bloco separado
  - Botão de editar

- **LeadFormPage** — formulário criar/editar:
  - Campos: companyName (obrigatório), segment (select, obrigatório), address, site, instagram, whatsapp, notes
  - Submit loading state
  - Tratamento de erro do backend
  - Redireciona para lista após sucesso

- **leadService.ts** — chamadas à API:
  - createLead, listLeads, getLead, updateLead, archiveLead, searchLeads
  - Tipagem completa

- **App.tsx** — 4 novas rotas protegidas:
  - `/leads` → tabela
  - `/leads/new` → criar
  - `/leads/:id` → detalhe
  - `/leads/:id/edit` → editar

**Edge cases:**
- Loading state enquanto carrega lista/detalhe
- Empty state quando não há leads
- Error state quando API falha
- Confirmação antes de arquivar

**Build:** `npm run build` → tsc + vite (2.34s) ✅

**Commit:** `feat(leads): add frontend leads page with table, detail, and form`

---

## ✅ Checklist de Finalização

- [x] POST /api/leads → 201 (criar)
- [x] GET /api/leads → 200 (listar com paginação)
- [x] GET /api/leads/{id} → 200 (detalhar)
- [x] PUT /api/leads/{id} → 200 (editar)
- [x] DELETE /api/leads/{id} → 200 (arquivar)
- [x] GET /api/leads/search?q= → 200 (buscar)
- [x] Frontend: tabela + busca + formulário + detalhe
- [x] Role-based visibility: AQUISICAO vê próprios, PROSPECCAO vê atribuídos

### Builds verificados

| Comando | Resultado |
|---------|-----------|
| `mvn clean test` | ✅ **38/38 testes** |
| `npm run build` | ✅ **tsc + vite (2.34s)** |
| `curl POST /api/leads` | ✅ **201** |
| `curl GET /api/leads` | ✅ **200** |
| `curl GET /api/leads/{id}` | ✅ **200** |
| `curl PUT /api/leads/{id}` | ✅ **200** |
| `curl DELETE /api/leads/{id}` | ✅ **200** |
| `curl GET /api/leads/search` | ✅ **200** |

### PR

**PR #4:** https://github.com/Alexsander532/crm-comercial-commit/pull/4

---

**Próximo sprint:** [Sprint 04 — Kanban](sprint-04-kanban.md)
