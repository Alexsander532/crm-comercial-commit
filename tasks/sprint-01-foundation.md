# Sprint 1 — Setup do Projeto

**Duração estimada**: 1 dia
**Depende de**: Nada (primeiro sprint)
**Bloqueia**: S2, S3, S4, S5, S6, S7, S8, S9

---

## 🎯 Objetivo

Ter um projeto Spring Boot + React + Docker + Banco rodando localmente.

---

## 📊 Dependências entre Tasks

```
Stream A (Backend)     Stream B (Frontend)     Stream C (Infra)
─────────────────       ─────────────────       ─────────────────
T1 (pom.xml) ──┐       T4 (React) ───┐       T5 (Docker) ──┐
                │                       │                       │
T2 (App.java) ─┤       T4b (Tailwind)┤       T5b (compose) ┤
                │                       │                       │
T3 (app.yml) ──┘       T4c (main.tsx)┘       T5c (pg ready)┘
       │                       │                       │
       └───────────────────────┼───────────────────────┘
                               │
                        T6 (Migrations) ← depende de A + C
                               │
                        T7 (Build test) ← depende de tudo
```

### Pode rodar em paralelo:
- **T1 + T4** — Backend e Frontend setup são independentes
- **T2 + T4b + T5** — App.java, Tailwind e Docker são independentes
- **T3 + T4c + T5b** — app.yml, main.tsx e compose são independentes

### Sequencial (depende da anterior):
- **T6** depende de T5 (Docker sobe) 
- **T7** depende de T6 (migrations aplicadas)

---

## Tasks

### T1 — Criar pom.xml ✅

| Campo | Valor |
|-------|-------|
| **ID** | T1 |
| **Stream** | A (Backend) |
| **Tempo est.** | 10 min |
| **Depende de** | — |
| **Paralelo com** | T4 |
| **Arquivo** | `backend/pom.xml` |
| **Teste** | `mvn clean compile` ✅ |
| **Status** | ✅ Concluído |

**Commit:** `feat(setup): add Spring Boot pom.xml with all dependencies`

---

### T2 — Criar Application.java ✅

| Campo | Valor |
|-------|-------|
| **ID** | T2 |
| **Stream** | A (Backend) |
| **Tempo est.** | 5 min |
| **Depende de** | T1 |
| **Paralelo com** | T4b, T5 |
| **Arquivo** | `backend/src/main/java/com/commit/crm/Application.java` |
| **Teste** | `mvn clean compile` ✅ |
| **Status** | ✅ Concluído |

**Commit:** `feat(setup): add Spring Boot pom.xml with all dependencies`

---

### T3 — Criar application.yml ✅

| Campo | Valor |
|-------|-------|
| **ID** | T3 |
| **Stream** | A (Backend) |
| **Tempo est.** | 10 min |
| **Depende de** | T1 |
| **Paralelo com** | T4c, T5b |
| **Arquivo** | `backend/src/main/resources/application.yml`, `application-test.yml` |
| **Teste** | `mvn clean compile` ✅ |
| **Status** | ✅ Concluído |

**Commit:** `feat(setup): add application.yml with datasource, jpa, flyway, jwt`

---

### T4 — Criar projeto React ✅

| Campo | Valor |
|-------|-------|
| **ID** | T4 |
| **Stream** | B (Frontend) |
| **Tempo est.** | 15 min |
| **Depende de** | — |
| **Paralelo com** | T1, T2, T3 |
| **Arquivo** | `frontend/` (package.json, vite.config.ts, tsconfig, tailwind, etc.) |
| **Teste** | `npm run build` ✅ |
| **Status** | ✅ Concluído |

**O que foi criado:**
- `frontend/package.json` — React 18, TypeScript 5.4, Vite 5, Tailwind 3.4, Axios, react-router-dom 6, shadcn/ui deps
- `frontend/vite.config.ts` — proxy /api → localhost:8080
- `frontend/tsconfig.json` + `tsconfig.node.json`
- `frontend/tailwind.config.js` — custom primary colors
- `frontend/postcss.config.js`
- `frontend/index.html` — lang pt-BR
- `frontend/src/main.tsx` — entry point
- `frontend/src/App.tsx` — BrowserRouter + placeholder dashboard
- `frontend/src/index.css` — Tailwind directives + design tokens
- `frontend/src/vite-env.d.ts`

**Commit:** `feat(setup): add React project with TypeScript, Vite, Tailwind`

---

### T5 — Criar Docker Compose ✅

| Campo | Valor |
|-------|-------|
| **ID** | T5 |
| **Stream** | C (Infra) |
| **Tempo est.** | 15 min |
| **Depende de** | T1 |
| **Paralelo com** | T2, T3, T4 |
| **Arquivos** | `docker-compose.yml`, `.env.example`, `backend/Dockerfile`, `frontend/Dockerfile` |
| **Teste** | `docker compose up -d postgres` ✅ |
| **Status** | ✅ Concluído |

**O que foi criado:**
- `docker-compose.yml` — 2 serviços: backend (8080), frontend (3000)
- PostgreSQL removido do Docker — usa **Supabase** como banco
- `.env` com credentials do Supabase (gitignored)
- `.env.example` com placeholders
- `backend/Dockerfile` — multi-stage: Maven 3.9 (build) → JRE 17 (run)
- `frontend/Dockerfile` — multi-stage: Node 20 (build) → Nginx 1.25 (serve)
- Nginx config: SPA fallback, proxy /api → backend:8080

**Commit:** `feat(setup): add Docker Compose with PostgreSQL, backend, frontend`

---

### T6 — Criar Flyway Migrations ✅

| Campo | Valor |
|-------|-------|
| **ID** | T6 |
| **Stream** | A (Backend) |
| **Tempo est.** | 30 min |
| **Depende de** | T3, T5 |
| **Paralelo com** | Nada |
| **Arquivo** | `backend/src/main/resources/db/migration/V1__create_users.sql` a `V6__seed_data.sql` |
| **Teste** | `mvn flyway:migrate` ✅ |
| **Status** | ✅ Concluído — 6 migrations no Supabase PostgreSQL 17.6 |

**O que foi criado:**
- 6 migrations Flyway no Supabase PostgreSQL 17.6
- Schema completo: users, leads, contacts, tasks, timeline_events
- Sem tabela `interactions` — interações são TimelineEvent tipo INTERACTION
- Seed: Diretor + Gerente de Aquisição + Gerente de Prospecção
- `manager_id` dos gerentes aponta para o Diretor

**Verificado:** `mvn flyway:migrate` — 6 migrations em 7.4s ✅

**Commit:** `feat(setup): add Flyway migrations for Supabase schema`

---

### T7 — Verificação final ✅

**Commit:**
```bash
git add backend/src/main/resources/db/migration/
git commit -m "feat(setup): add Flyway migrations with complete schema + seed data"
```

---

### T7 — Testar build completo

| Campo | Valor |
|-------|-------|
| **ID** | T7 |
| **Stream** | Integração |
| **Tempo est.** | 15 min |
| **Depende de** | T6 (tudo anterior) |
| **Paralelo com** | Nada |
| **Arquivo** | Nenhum (validação) |
| **Teste** | Verificar tudo funciona junto |
| **Esperado** | Backend compila, Frontend compila, Docker sobe |

**O que fazer:**
1. Backend: `cd backend && mvn clean compile` → BUILD SUCCESS
2. Frontend: `cd frontend && npm run build` → Build completo
3. Docker: `docker-compose up -d postgres` → PostgreSQL sobe
4. Flyway: `mvn flyway:migrate` → Migrations aplicadas
5. Backend: `mvn spring-boot:run` → API sobe na porta 8080
6. Testar: `curl http://localhost:8080/api/health` → `{"status":"UP",...}` (endpoint será criado na feature 09)

**Commit:**
```bash
git add -A
git commit -m "chore(setup): verify full build works"
```

---

## ✅ Checklist de Finalização

Depois de completar todas as tasks:

- [x] Backend compila sem erros
- [x] Frontend compila sem erros
- [x] Conexão com Supabase PostgreSQL configurada via .env
- [x] Flyway migra as 6 tabelas no Supabase
- [x] Diretor + 2 gerentes iniciais estão no banco Supabase

### 🎉 **Sprint 1 Concluído!**

**Quando tudo estiver ✅, criar PR para `main`:**

```bash
git checkout main
git merge feature/01-setup-projeto
git push origin main
```

---

**Próximo sprint:** [Sprint 02 — Auth](sprint-02-auth.md)