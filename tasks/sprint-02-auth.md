# Sprint 2 — Autenticação e Autorização

**Duração estimada**: 1 dia
**Depende de**: S1 (Setup completo)
**Bloqueia**: S3, S4, S5, S6, S7, S8, S9
**Status**: ✅ Concluído (PR #3)

---

## 🎯 Objetivo

Login JWT funcional + roles hierárquicas (5 papéis) + cadastro de usuário + frontend de login.

---

## 📊 Dependências entre Tasks

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

---

## Tasks

### T8 — User entity + UserRole ✅

| Campo | Valor |
|-------|-------|
| **ID** | T8 |
| **Stream** | Backend |
| **Tempo real** | 15 min |
| **Depende de** | S1 |
| **Arquivos** | `model/User.java`, `model/UserRole.java`, `model/UserTest.java` |
| **Testes** | 3/3 ✅ |
| **Status** | ✅ Concluído |

**O que foi feito:**
- Criado `UserRole.java` — enum com 5 papéis: DIRETOR, GERENTE_AQUISICAO, GERENTE_PROSPECCAO, AQUISICAO, PROSPECCAO
- Criado `User.java` — JPA entity mapeando a tabela `users` com:
  - `id` (UUID, auto-generate)
  - `name`, `email` (unique), `passwordHash`
  - `role` (UserRole enum, armazenado como String)
  - `managerId` (UUID, nullable — self-reference para hierarquia)
  - `isActive` (default true)
  - `createdAt`, `updatedAt` (timestamps automáticos)
  - Builder pattern (Lombok @Builder), equals/hashCode por UUID

**Edge cases considerados:**
- managerId pode ser null (diretor não tem chefe)
- isActive default true mesmo se não especificado no builder
- email é unique no banco (constraint UNIQUE)

**Testes:**
- shouldCreateUserWithBuilder — valida criação com builder e campos
- shouldDefaultIsActiveToTrue — valida default de isActive
- shouldAcceptAllRoles — valida que os 5 papéis existem no enum

**Commit:** `feat(auth): add User entity and UserRole enum`

---

### T9 — UserRepository ✅

| Campo | Valor |
|-------|-------|
| **ID** | T9 |
| **Stream** | Backend |
| **Tempo real** | 10 min |
| **Depende de** | T8 |
| **Arquivos** | `repository/UserRepository.java`, `repository/UserRepositoryTest.java` |
| **Testes** | 3/3 ✅ |
| **Status** | ✅ Concluído |

**O que foi feito:**
- Criado `UserRepository.java` — interface JPA com:
  - `findByEmail(email)` — Optional<User>, usado no login
  - `existsByEmail(email)` — boolean, usado no register para validar unicidade

**Edge cases considerados:**
- Email não encontrado → retorna Optional.empty() (não lança exceção)
- existsByEmail retorna false para emails inexistentes

**Testes:** Mockito unit tests (sem dependência de Docker/Testcontainers)
- shouldFindByEmail — busca por email existente retorna usuário
- shouldReturnEmptyWhenEmailNotFound — busca por email inexistente retorna empty
- shouldCheckIfEmailExists — existsByEmail true/false

**Commit:** `feat(auth): add UserRepository with findByEmail and existsByEmail`

---

### T10 — Auth DTOs ✅

| Campo | Valor |
|-------|-------|
| **ID** | T10 |
| **Stream** | Backend |
| **Tempo real** | 20 min |
| **Depende de** | T8 |
| **Arquivos** | `dto/request/LoginRequest.java`, `dto/request/RegisterRequest.java`, `dto/response/LoginResponse.java`, `dto/response/UserResponse.java`, `dto/response/ApiResponse.java`, `dto/AuthDtoTest.java` |
| **Testes** | 6/6 ✅ |
| **Status** | ✅ Concluído |

**O que foi feito:**
- `LoginRequest` — record com validação (@NotBlank email, @NotBlank password)
- `RegisterRequest` — record com validação (@NotBlank name, @Email email, @NotBlank password, @NotNull role)
- `LoginResponse` — record com token, expiresIn, user (UserResponse)
- `UserResponse` — record com id, name, email, role (sem expor passwordHash)
- `ApiResponse<T>` — wrapper genérico com `data`, `message`, `timestamp` + factory methods `success()` e `error()`

**Edge cases considerados:**
- ApiResponse.error() retorna data=null para erros
- LoginResponse inclui UserResponse (nunca expõe senha)
- Validação de campos vazios/vazios no LoginRequest/RegisterRequest

**Testes:**
- shouldValidateLoginRequest — campos vazios geram violações
- shouldAcceptValidLoginRequest — campos preenchidos passam
- shouldAcceptValidRegisterRequest — registro com todos campos
- shouldBuildApiResponseSuccess — success() com data
- shouldBuildApiResponseError — error() com data=null
- shouldBuildLoginResponse — construção completa de LoginResponse

**Commit:** `feat(auth): add auth DTOs with validation`

---

### T11 — JWT Provider + Filter + UserDetails ✅

| Campo | Valor |
|-------|-------|
| **ID** | T11 |
| **Stream** | Backend |
| **Tempo real** | 30 min |
| **Depende de** | T8 |
| **Arquivos** | `config/security/JwtTokenProvider.java`, `config/security/JwtAuthenticationFilter.java`, `config/security/CustomUserDetailsService.java`, `config/security/JwtTokenProviderTest.java` |
| **Testes** | 5/5 ✅ |
| **Status** | ✅ Concluído |

**O que foi feito:**
- `JwtTokenProvider` — componente que:
  - Gera JWT com subject=userId, claim=role, issuedAt, expiration (jjwt 0.12.x)
  - Valida token (assinatura + expiração)
  - Extrai userId e role do token
  - Expõe getExpirationMs() para o AuthService
- `JwtAuthenticationFilter` — OncePerRequestFilter:
  - Extrai token do header Authorization: Bearer <token>
  - Valida o token via JwtTokenProvider
  - Seta SecurityContextHolder com UsernamePasswordAuthenticationToken
  - Authorities = ROLE_ + role (ex: ROLE_DIRETOR)
- `CustomUserDetailsService` — implementa UserDetailsService:
  - loadUserByUsername(email) busca no banco por email
  - Retorna UserDetails com id.toString() como principal, passwordHash como credentials, authorities pela role

**⚠️ Bug encontrado e corrigido:**
- Originalmente loadUserByUsername tentava UUID.fromString(username), mas o username recebido é o email (vindo do form de login)
- Corrigido para buscar por email: `userRepository.findByEmail(email)`

**Testes:**
- shouldGenerateAndValidateToken — gera e valida token
- shouldExtractUserIdFromToken — extrai UUID correto
- shouldExtractRoleFromToken — extrai role correta
- shouldReturnFalseForInvalidToken — token inválido rejeitado
- shouldReturnFalseForExpiredToken — token expirado rejeitado

**Commit:** `feat(auth): implement JWT token provider, filter, and user details service`

---

### T12 — Security Config ✅

| Campo | Valor |
|-------|-------|
| **ID** | T12 |
| **Stream** | Backend |
| **Tempo real** | 15 min |
| **Depende de** | T11 |
| **Arquivos** | `config/security/SecurityConfig.java` |
| **Teste** | `mvn compile` ✅ |
| **Status** | ✅ Concluído |

**O que foi feito:**
- SecurityFilterChain com:
  - CSRF disabled (API stateless)
  - CORS liberado para localhost:3000 e localhost:5173 (Vite)
  - SessionCreationPolicy.STATELESS
  - `/api/auth/**` e `/api/health` — permitAll
  - Demais rotas — authenticated
  - JwtAuthenticationFilter antes do UsernamePasswordAuthenticationFilter
- Beans: PasswordEncoder (BCrypt), AuthenticationManager
- `@EnableMethodSecurity` — ativa @PreAuthorize em controllers

**Edge cases considerados:**
- CORS configurado para desenvolvimento (localhost:3000 e 5173)
- /api/health público (útil para healthcheck)
- /api/auth/** público (login e register não exigem token)

**Commit:** `feat(auth): configure Spring Security with JWT, CORS, and stateless session`

---

### T13 — AuthService ✅

| Campo | Valor |
|-------|-------|
| **ID** | T13 |
| **Stream** | Backend |
| **Tempo real** | 30 min |
| **Depende de** | T10, T12 |
| **Arquivos** | `service/AuthService.java`, `service/UserService.java`, `service/AuthServiceTest.java` |
| **Testes** | 3/3 ✅ |
| **Status** | ✅ Concluído |

**O que foi feito:**
- `AuthService`:
  - `login(LoginRequest)` — autentica via AuthenticationManager, gera JWT, retorna LoginResponse
  - `register(RegisterRequest)` — valida unicidade de email, codifica senha com BCrypt, salva, gera JWT
  - Ambos retornam LoginResponse padronizado
- `UserService`:
  - `findById(UUID)` — busca usuário por ID, útil para outros services
  - `getCurrentUser()` — extrai usuário autenticado do SecurityContextHolder

**Edge cases considerados:**
- Email duplicado no register → IllegalArgumentException "Email já cadastrado"
- Usuário não encontrado no login → BadCredentialsException (authentication)
- Senha incorreta → BadCredentialsException (authentication)
- getCurrentUser sem autenticação → IllegalStateException

**Testes:**
- shouldRegisterUser — registro completo gera token com dados corretos
- shouldThrowWhenEmailAlreadyExists — email duplicado lança exceção e não salva
- shouldLoginUser — login com credenciais válidas gera token

**Commit:** `feat(auth): add AuthService and UserService`

---

### T14 — AuthController ✅

| Campo | Valor |
|-------|-------|
| **ID** | T14 |
| **Stream** | Backend |
| **Tempo real** | 20 min |
| **Depende de** | T13 |
| **Arquivos** | `controller/AuthController.java`, `exception/GlobalExceptionHandler.java` |
| **Teste** | Testado via curl ✅ |
| **Status** | ✅ Concluído |

**O que foi feito:**
- `AuthController` com 3 endpoints:
  - `POST /api/auth/login` — público, recebe email+senha, retorna JWT + user
  - `POST /api/auth/register` — @PreAuthorize("hasRole('DIRETOR')"), só diretor cria usuários
  - `GET /api/auth/me` — autenticado, retorna dados do usuário logado
- `GlobalExceptionHandler` com @RestControllerAdvice para erros padronizados:
  - IllegalArgumentException → 400 Bad Request
  - AccessDeniedException → 403 Forbidden
  - MethodArgumentNotValidException → 400 com detalhes da validação
  - Exception genérica → 500 Internal Server Error + log do stack trace

**⚠️ Bug encontrado e corrigido:**
- AuthController.me() usava @AuthenticationPrincipal UserDetails, mas o principal é String (userId)
- Corrigido para @AuthenticationPrincipal String userId direto

**Commits:** `feat(auth): add AuthController with login, register, and me endpoints`

---

### T15 — Testar login com curl ✅

| Campo | Valor |
|-------|-------|
| **ID** | T15 |
| **Stream** | Backend |
| **Tempo real** | Varias tentativas |
| **Depende de** | T14 |
| **Testes** | curl validado ✅ |
| **Status** | ✅ Concluído |

**O que foi testado:**

#### 1. Login com seed data
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "diretor@commit.com", "password": "admin123"}'
# RESULTADO: 200 OK, JWT token retornado
```

#### 2. Dados do usuário autenticado
```bash
curl http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer eyJhbG..."
# RESULTADO: 200 OK, { id, name, email, role }
```

#### 3. Register sem token (deve bloquear)
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Teste","email":"t@t.com","password":"123","role":"AQUISICAO"}'
# RESULTADO: 403 Forbidden (só DIRETOR cria usuários)
```

#### 4. Register com token de diretor
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Authorization: Bearer eyJhbG..." \
  -H "Content-Type: application/json" \
  -d '{"name":"Funcionario","email":"func@commit.com","password":"senha123","role":"AQUISICAO"}'
# RESULTADO: 201 Created, usuário criado com JWT
```

#### 5. Login com senha errada
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "diretor@commit.com", "password": "senha_errada"}'
# RESULTADO: 401 BadCredentialsException
```

#### 6. Login com email inexistente
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "naoexiste@commit.com", "password": "123"}'
# RESULTADO: 401 BadCredentialsException
```

**⚠️ Problemas encontrados e corrigidos durante T15:**

| Problema | Causa | Solução |
|----------|-------|---------|
| 500 erro no login | CustomUserDetailsService tentava UUID.fromString(email) | Buscar por email: `repository.findByEmail(email)` |
| 500 erro no login | BCrypt hash no seed não correspondia a 'admin123' | Gerado hash correto via PasswordHashGenerator, atualizado V6 |
| Flyway falhou ao iniciar | Migration V6 modificada após aplicada (checksum mismatch) | `flyway:repair` + V7 migration para corrigir hash |
| Erro não aparecia no log | GlobalExceptionHandler sem logging | Adicionado @Slf4j e log.error com stack trace |
| App não respondia | Curl executou antes da app terminar startup | Loop de espera com verificação de HTTP status |

**Commits:**
- `feat(auth): fix login with correct BCrypt hash and test endpoints`
- `feat(auth): add AuthService and UserService` (já incluía as correções do login)

---

### T16 — Frontend Login Page ✅

| Campo | Valor |
|-------|-------|
| **ID** | T16 |
| **Stream** | Frontend |
| **Tempo real** | 45 min |
| **Depende de** | T14 |
| **Arquivos** | `pages/LoginPage.tsx`, `services/authService.ts`, `services/api.ts`, `context/AuthContext.tsx`, `hooks/useAuth.ts`, `types/user.ts`, `components/ProtectedRoute.tsx` |
| **Teste** | `npm run build` ✅ |
| **Status** | ✅ Concluído |

**O que foi feito:**

- **LoginPage.tsx** — formulário de login com:
  - Campos de email e senha com validação HTML5
  - Estado de loading (botão "Entrando..." desabilitado)
  - Tratamento de erro (mensagem de erro do backend ou fallback)
  - Design responsivo, centralizado, com a identidade "CRM Comercial"

- **api.ts** — instância Axios configurada:
  - Base URL: /api (proxy Vite para backend)
  - Interceptor de request: adiciona Authorization Bearer token
  - Interceptor de response: trata 401 limpando token e redirecionando

- **authService.ts** — chamadas à API:
  - login(data) → POST /api/auth/login
  - getMe() → GET /api/auth/me

- **AuthContext.tsx** — contexto de autenticação:
  - user, isAuthenticated, isLoading, login(), logout()
  - Carrega token do localStorage ao iniciar
  - login() salva token + user no localStorage

- **ProtectedRoute.tsx** — wrapper de rota:
  - Se isLoading → mostra "Carregando..."
  - Se não autenticado → redirect para /login
  - Se autenticado → renderiza children

- **types/user.ts** — tipos TypeScript:
  - User, UserRole enum, LoginRequest, RegisterRequest, LoginResponse, ApiResponse

- **App.tsx** atualizado:
  - AuthProvider envolvendo todas as rotas
  - /login → LoginPage (pública)
  - /dashboard → ProtectedRoute (autenticada)
  - /* → redirect para /dashboard

**Edge cases considerados:**
- Token expirado → 401 → limpa storage → redirect login
- App iniciando → isLoading true → tela de carregamento
- Erro de rede → mensagem "Erro ao conectar ao servidor"
- Credenciais inválidas → mensagem do backend ou fallback

**Build:** `npm run build` → tsc + vite (3.44s) ✅

**Commit:** `feat(auth): add frontend login page with auth context`

---

## ✅ Checklist de Finalização

- [x] POST /api/auth/login retorna JWT
- [x] POST /api/auth/register cria usuário (só DIRETOR)
- [x] POST /api/auth/register sem token → 403
- [x] POST /api/auth/login com senha errada → 401
- [x] GET /api/auth/me retorna dados do usuário
- [x] Frontend: login funciona e redireciona ao dashboard
- [x] Token JWT expira em 24h

### Builds verificados

| Comando | Resultado |
|---------|-----------|
| `mvn clean test` | ✅ 20/20 testes |
| `npm run build` | ✅ tsc + vite |
| `curl POST /api/auth/login` | ✅ 200 + JWT |
| `curl POST /api/auth/register sem token` | ✅ 403 |
| `curl POST /api/auth/register com token` | ✅ 201 |
| `curl POST /api/auth/login senha errada` | ✅ 401 |
| `curl GET /api/auth/me` | ✅ 200 + user data |
| `docker compose up` | ✅ (se quiser testar local) |

### PR

**PR #3:** https://github.com/Alexsander532/crm-comercial-commit/pull/3

---

**Próximo sprint:** [Sprint 03 — Leads CRUD](sprint-03-leads.md)
