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

### T1 — Criar pom.xml

| Campo | Valor |
|-------|-------|
| **ID** | T1 |
| **Stream** | A (Backend) |
| **Tempo est.** | 10 min |
| **Depende de** | — |
| **Paralelo com** | T4 |
| **Arquivo** | `backend/pom.xml` |
| **Teste** | `cd backend && mvn clean compile` |
| **Esperado** | BUILD SUCCESS |

**O que fazer:**
1. Criar pasta `backend/`
2. Criar `pom.xml` com:
   - Spring Boot 3.2.5 parent
   - spring-boot-starter-web
   - spring-boot-starter-data-jpa
   - spring-boot-starter-security
   - spring-boot-starter-validation
   - postgresql (runtime)
   - flyway-core
   - jjwt (api, impl, jackson)
   - lombok
   - spring-boot-starter-test
   - spring-security-test
   - testcontainers (junit-jupiter, postgresql)
3. Verificar: `mvn clean compile`

**Commit:**
```bash
git add backend/pom.xml
git commit -m "feat(setup): add Spring Boot pom.xml with all dependencies"
```

---

### T2 — Criar Application.java

| Campo | Valor |
|-------|-------|
| **ID** | T2 |
| **Stream** | A (Backend) |
| **Tempo est.** | 5 min |
| **Depende de** | T1 |
| **Paralelo com** | T4b, T5 |
| **Arquivo** | `backend/src/main/java/com/commit/crm/Application.java` |
| **Teste** | `mvn clean compile` |
| **Esperado** | BUILD SUCCESS |

**O que fazer:**
1. Criar estrutura de pastas: `src/main/java/com/commit/crm/`
2. Criar `Application.java` com `@SpringBootApplication`
3. Verificar: `mvn clean compile`

**Commit:**
```bash
git add backend/src/
git commit -m "feat(setup): add Spring Boot Application class"
```

---

### T3 — Criar application.yml

| Campo | Valor |
|-------|-------|
| **ID** | T3 |
| **Stream** | A (Backend) |
| **Tempo est.** | 10 min |
| **Depende de** | T1 |
| **Paralelo com** | T4c, T5b |
| **Arquivo** | `backend/src/main/resources/application.yml` |
| **Teste** | `mvn clean compile` |
| **Esperado** | BUILD SUCCESS |

**O que fazer:**
1. Criar `application.yml` com:
   - datasource url/username/password
   - jpa hibernate ddl-auto: validate
   - flyway enabled
   - server port: 8080
   - jwt secret + expiration
2. Criar `application-test.yml` para testes (testcontainers)
3. Verificar: `mvn clean compile`

**Commit:**
```bash
git add backend/src/main/resources/
git commit -m "feat(setup): add application.yml with datasource, jpa, flyway, jwt"
```

---

### T4 — Criar projeto React

| Campo | Valor |
|-------|-------|
| **ID** | T4 |
| **Stream** | B (Frontend) |
| **Tempo est.** | 15 min |
| **Depende de** | — |
| **Paralelo com** | T1, T2, T3 |
| **Arquivo** | `frontend/package.json` |
| **Teste** | `cd frontend && npm install && npm run build` |
| **Esperado** | Build completo sem erros |

**O que fazer:**
1. Criar pasta `frontend/`
2. Criar `package.json` com React 18, TypeScript, Vite, Tailwind, Axios, react-router-dom
3. Criar `vite.config.ts` com proxy para backend
4. Criar `tsconfig.json`
5. Criar `tailwind.config.js`
6. Criar `index.html`
7. Criar `src/main.tsx`
8. Criar `src/App.tsx`
9. Criar `src/index.css` (tailwind directives)
10. Instalar dependências: `npm install`
11. Verificar: `npm run build`

**Commit:**
```bash
git add frontend/
git commit -m "feat(setup): add React project with TypeScript, Tailwind, Vite"
```

---

### T5 — Criar Docker Compose

| Campo | Valor |
|-------|-------|
| **ID** | T5 |
| **Stream** | C (Infra) |
| **Tempo est.** | 15 min |
| **Depende de** | T1 (precisa saber as portas do backend) |
| **Paralelo com** | T2, T3, T4 |
| **Arquivo** | `docker-compose.yml`, `.env.example`, `backend/Dockerfile`, `frontend/Dockerfile` |
| **Teste** | `docker-compose up -d postgres` |
| **Esperado** | PostgreSQL sobe sem erros |

**O que fazer:**
1. Criar `docker-compose.yml` com:
   - postgres (porta 5432, healthcheck)
   - backend (porta 8080, depende de postgres)
   - frontend (porta 3000, depende de backend)
2. Criar `.env.example`
3. Criar `backend/Dockerfile` (multi-stage: build + run)
4. Criar `frontend/Dockerfile` (multi-stage: build + nginx)
5. Verificar: `docker-compose up -d postgres`
6. Verificar: `docker exec -it crm-postgres psql -U crm -d crm_comercial -c '\dt'`

**Commit:**
```bash
git add docker-compose.yml .env.example backend/Dockerfile frontend/Dockerfile
git commit -m "feat(setup): add Docker Compose with PostgreSQL, backend, frontend"
```

---

### T6 — Criar Flyway Migrations

| Campo | Valor |
|-------|-------|
| **ID** | T6 |
| **Stream** | A (Backend) |
| **Tempo est.** | 30 min |
| **Depende de** | T3, T5 (app.yml + Docker sobe) |
| **Paralelo com** | Nada (bloqueia T7) |
| **Arquivo** | `backend/src/main/resources/db/migration/V1__create_users.sql` a `V7__seed_data.sql` |
| **Teste** | `mvn flyway:migrate` |
| **Esperado** | 7 migrations aplicadas com sucesso |

**O que fazer:**
1. Criar V1__create_users.sql (tabela users com roles e manager_id)
2. Criar V2__create_leads.sql (tabela leads com status e enriched_data)
3. Criar V3__create_contacts.sql (tabela contacts com is_main)
4. Criar V4__create_interactions.sql (tabela interactions com tipos)
5. Criar V5__create_tasks.sql (tabela tasks com priority e status)
6. Criar V6__create_timeline_events.sql (tabela timeline_events com metadata JSONB)
7. Criar V7__seed_data.sql (diretor inicial com senha BCrypt)
8. Verificar: `mvn flyway:migrate`

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

- [ ] Backend compila sem erros
- [ ] Frontend compila sem erros  
- [ ] Docker sobe PostgreSQL
- [ ] Flyway migra as 7 tabelas
- [ ] Diretor inicial está no banco

**Quando tudo estiver ✅, criar PR para `main`:**

```bash
git checkout main
git merge feature/01-setup-projeto
git push origin main
```

---

**Próximo sprint:** [Sprint 02 — Auth](sprint-02-auth.md)