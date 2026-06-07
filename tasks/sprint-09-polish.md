# Sprint 9 — Polish (Finalização)

**Duração estimada**: 1 dia
**Depende de**: S1-S8 (tudo anterior completo)
**Bloqueia**: Nada (último sprint)

---

## 🎯 Objetivo

Finalizar o MVP: testes de integração, testes de segurança, documentação, health check, ajustes gerais.

---

## 📊 Dependências

```
T53 (Testes de integração) ──→ T55 (Documentação da API)
                                      │
T54 (Testes de segurança)  ──→ T55 ──┤
                                      │
T56 (Health check) ───────────────────→ T57 (Deploy check)
```

### Pode rodar em paralelo:
- **T53 + T54** (testes de integração e segurança são independentes)

---

## Tasks

### T53 — Testes de integração (Auth + Leads)

| Campo | Valor |
|-------|-------|
| **ID** | T53 |
| **Tempo est.** | 45 min |
| **Depende de** | S8 completo |
| **Arquivos** | `test/controller/AuthControllerTest.java`, `test/controller/LeadControllerTest.java` |
| **Teste** | `mvn test` |

Testes com Testcontainers (PostgreSQL real):
- Auth: login com credenciais válidas retorna 200 + JWT
- Auth: login com credenciais inválidas retorna 401
- Auth: registro de usuário funciona
- Leads: CRUD completo funciona com JWT
- Leads: lista filtrada por status funciona
- Leads: busca por nome funciona

Commit: `test(polish): add integration tests for auth and leads`

---

### T54 — Testes de segurança (RBAC)

| Campo | Valor |
|-------|-------|
| **ID** | T54 |
| **Tempo est.** | 45 min |
| **Depende de** | S8 completo |
| **Arquivos** | `test/controller/SecurityTest.java` |
| **Teste** | `mvn test` |

Testes com @WithMockUser:
- DIRETOR pode criar leads ✅
- GERENTE_AQUISICAO pode criar leads ✅
- AQUISICAO pode criar leads ✅
- PROSPECCAO não pode criar leads (403) ❌
- GERENTE_PROSPECCAO pode visualizar leads ✅
- DIRETOR pode criar tarefas ✅
- FUNCIONÁRIO não pode criar tarefas (403) ❌
- Usuário não autenticado recebe 401 em qualquer endpoint

Commit: `test(polish): add RBAC security tests`

---

### T55 — Documentação da API

| Campo | Valor |
|-------|-------|
| **ID** | T55 |
| **Tempo est.** | 30 min |
| **Depende de** | T53, T54 |
| **Arquivos** | `docs/api/README.md` |
| **Teste** | Revisão manual |

Documentação completa:
- Autenticação (login, register)
- Leads (CRUD, filtros, busca)
- Pipeline (mover, atribuir)
- Contatos (CRUD, principal)
- Tarefas (CRUD, completar, cancelar, atrasadas)
- Timeline (listar eventos)
- Dashboard (métricas por role)
- Health check

Commit: `docs(polish): add API documentation`

---

### T56 — Health check + CORS final

| Campo | Valor |
|-------|-------|
| **ID** | T56 |
| **Tempo est.** | 15 min |
| **Depende de** | S8 completo |
| **Arquivos** | `controller/HealthController.java`, ajustes em SecurityConfig |
| **Teste** | curl /api/health |

HealthController: GET /api/health → { status: "UP", service: "crm-comercial", version: "1.0.0" }
Ajustar CORS para aceitar frontend em produção.

Commit: `feat(polish): add health check endpoint and final CORS config`

---

### T57 — Verificação final (deploy check)

| Campo | Valor |
|-------|-------|
| **ID** | T57 |
| **Tempo est.** | 30 min |
| **Depende de** | T55, T56 |
| **Arquivos** | Nenhum (validação) |
| **Teste** | Checklist completo |

Verificar:
- [ ] `mvn clean test` passa sem erros
- [ ] `npm run build` passa sem erros
- [ ] `docker-compose up -d` sobe sem erros
- [ ] PostgreSQL aceita conexões
- [ ] Flyway migra todas as tabelas
- [ ] Login funciona (JWT)
- [ ] CRUD de leads funciona
- [ ] Kanban com drag & drop funciona
- [ ] Contatos funcionam
- [ ] Tarefas com accountability funcionam
- [ ] Timeline registra eventos
- [ ] Dashboard mostra métricas por role
- [ ] RBAC funciona (403 para roles erradas)

Commit: `chore(polish): final verification complete`

---

## ✅ Checklist de Finalização do MVP

- [ ] Todos os testes passam
- [ ] Documentação da API completa
- [ ] Health check responde
- [ ] CORS configurado para produção
- [ ] Build de produção funciona (Backend + Frontend)
- [ ] Docker Compose sobe tudo

**🎉 MVP COMPLETO!**

---

## 🚀 Pós-MVP (roadmap futuro)

| Feature | Prioridade |
|---------|-----------|
| Notificações por email | Alta |
| Exportação de relatórios (CSV/Excel) | Média |
| Dashboard com gráficos (Chart.js) | Média |
| WebSocket para atualizações em tempo real | Baixa |
| IA para enriched_data | Baixa |
| Validação de CNPJ (ReceitaWS) | Baixa |