# Sprint 2 — Autenticação e Autorização

**Duração estimada**: 1 dia
**Depende de**: S1 (Setup completo)
**Bloqueia**: S3, S4, S5, S6, S7, S8, S9

---

## 🎯 Objetivo

Login JWT funcional + roles hierárquicas (DIRETOR, GERENTE, FUNCIONÁRIO) + cadastro de usuário.

---

## 📊 Dependências

```
T8 (User entity) ──┬── T9 (UserRepository) ──┬── T10 (Auth DTOs) ──┐
                    │                           │                      │
                    └── T11 (JWT Provider) ──┬── T12 (Security Config) │
                                                    │                  │
                                             ┌─────┘                  │
                                             │                        │
                                        T13 (AuthService) ───────────┘
                                                    │
                                        T14 (AuthController)
                                                    │
                                             ┌──────┴──────┐
                                        T15 (Testar)    T16 (Frontend Login)
```

### Pode rodar em paralelo:
- **T9 + T11** — Repository e JWT são independentes
- **T15 + T16** — Testar backend e fazer frontend login são independentes

### Sequencial:
- **T13** depende de T10 e T12
- **T14** depende de T13

---

## Tasks

### T8 — User entity + UserRole

| Campo | Valor |
|-------|-------|
| **ID** | T8 |
| **Tempo est.** | 15 min |
| **Depende de** | S1 |
| **Arquivos** | `model/User.java`, `model/UserRole.java` |
| **Teste** | `mvn compile` |

Criar User com id, name, email, passwordHash, role, managerId, isActive, timestamps.
Criar UserRole enum com DIRETOR, GERENTE_AQUISICAO, GERENTE_PROSPECCAO, AQUISICAO, PROSPECCAO.

Commit: `feat(auth): add User entity and UserRole enum`

---

### T9 — UserRepository

| Campo | Valor |
|-------|-------|
| **ID** | T9 |
| **Tempo est.** | 10 min |
| **Depende de** | T8 |
| **Arquivos** | `repository/UserRepository.java` |
| **Teste** | `mvn compile` |

Criar UserRepository com findByEmail, existsByEmail.

Commit: `feat(auth): add UserRepository`

---

### T10 — Auth DTOs

| Campo | Valor |
|-------|-------|
| **ID** | T10 |
| **Tempo est.** | 20 min |
| **Depende de** | T8 |
| **Arquivos** | `dto/request/LoginRequest.java`, `dto/request/RegisterRequest.java`, `dto/response/LoginResponse.java`, `dto/response/ApiResponse.java` |
| **Teste** | `mvn compile` |

Criar DTOs com validação (@NotBlank, @Email, etc).
ApiResponse genérico com data, message, timestamp.

Commit: `feat(auth): add auth DTOs`

---

### T11 — JWT (Token Provider + Filter + UserDetailsService)

| Campo | Valor |
|-------|-------|
| **ID** | T11 |
| **Tempo est.** | 30 min |
| **Depende de** | T8 |
| **Arquivos** | `config/security/JwtTokenProvider.java`, `config/security/JwtAuthenticationFilter.java`, `config/security/CustomUserDetailsService.java` |
| **Teste** | `mvn compile` |

JwtTokenProvider: generateToken, validateToken, getUserIdFromToken, getRoleFromToken.
JwtAuthenticationFilter: extrair token do header, validar, setar SecurityContext.
CustomUserDetailsService: loadUserByUsername (busca por UUID).

Commit: `feat(auth): implement JWT token provider, filter, and user details service`

---

### T12 — Security Config

| Campo | Valor |
|-------|-------|
| **ID** | T12 |
| **Tempo est.** | 30 min |
| **Depende de** | T11 |
| **Arquivos** | `config/security/SecurityConfig.java` |
| **Teste** | `mvn compile` |

SecurityConfig com:
- CSRF disabled (stateless JWT)
- CORS (localhost:3000)
- Session stateless
- Role-based authorization por endpoint
- JWT filter antes de UsernamePasswordAuthenticationFilter

Commit: `feat(auth): configure Spring Security with JWT, CORS, and role-based access`

---

### T13 — AuthService

| Campo | Valor |
|-------|-------|
| **ID** | T13 |
| **Tempo est.** | 30 min |
| **Depende de** | T10, T12 |
| **Arquivos** | `service/AuthService.java` |
| **Teste** | `mvn test` |

AuthService com login (authenticate + generate token) e register (create user with BCrypt).

Commit: `feat(auth): add AuthService with login and register`

---

### T14 — AuthController

| Campo | Valor |
|-------|-------|
| **ID** | T14 |
| **Tempo est.** | 20 min |
| **Depende de** | T13 |
| **Arquivos** | `controller/AuthController.java` |
| **Teste** | curl POST /api/auth/login |

Endpoints:
- POST /api/auth/login → Login (JWT)
- POST /api/auth/register → Criar usuário (DIRETOR only)

Commit: `feat(auth): add AuthController with login and register`

---

### T15 — Testar login com curl

| Campo | Valor |
|-------|-------|
| **ID** | T15 |
| **Tempo est.** | 10 min |
| **Depende de** | T14 |
| **Arquivos** | Nenhum |
| **Teste** | curl POST /api/auth/login |

```bash
# Criar diretor via migration (V7)
# Testar login:
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "diretor@commit.com", "password": "admin123"}'
# Esperado: JWT token na resposta
```

---

### T16 — Frontend Login Page

| Campo | Valor |
|-------|-------|
| **ID** | T16 |
| **Tempo est.** | 45 min |
| **Depende de** | T14 (API funcionando) |
| **Arquivos** | `pages/LoginPage.tsx`, `services/authService.ts`, `context/AuthContext.tsx`, `hooks/useAuth.ts`, `types/user.ts` |
| **Teste** | Navegador: login funciona |

Criar:
- LoginPage.tsx com formulário de email/senha
- authService.ts com login() e register()
- AuthContext.tsx com estado de autenticação
- useAuth.ts hook
- types/user.ts

Commit: `feat(auth): add frontend login page with auth context`

---

## ✅ Checklist de Finalização

- [ ] POST /api/auth/login retorna JWT
- [ ] POST /api/auth/register cria usuário (só DIRETOR)
- [ ] GET /api/auth/me retorna dados do usuário
- [ ] Frontend: login funciona e redireciona ao dashboard
- [ ] Token JWT expira em 24h

**Criar PR para main quando tudo estiver ✅.**

---

**Próximo sprint:** [Sprint 03 — Leads CRUD](sprint-03-leads.md)