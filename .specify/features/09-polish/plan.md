# Plano: 09 — Polish

**Branch**: `feature/09-polish` | **Data**: 2026-06-06
**Depende de**: TODAS as features anteriores
**Bloqueia**: — (última feature)
**Worktree**: `.worktrees/feature/09-polish`

---

## Objetivo

Finalizar o MVP: ajustes, testes de integração, documentação, e preparação
para deploy.

---

## Contexto

Polish é a fase de acabamento. Aqui não se adiciona funcionalidade, apenas:
- Refina o que existe
- Adiciona testes que faltam
- Documenta a API
- Prepara para deploy

---

## Tasks

### Task 1: Testes de integração

**Arquivos:**
- Criar: `backend/src/test/java/com/commit/crm/controller/AuthControllerTest.java`
- Criar: `backend/src/test/java/com/commit/crm/controller/LeadControllerTest.java`

**Passo 1: AuthControllerTest.java**

```java
package com.commit.crm.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AuthControllerTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
        .withDatabaseName("crm_test")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    MockMvc mockMvc;

    @Test
    void login_withValidCredentials_shouldReturnToken() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email": "diretor@commit.com", "password": "admin123"}
                    """))
            .andExpect(status().isOk());
    }

    @Test
    void login_withInvalidCredentials_shouldReturn401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email": "invalid@test.com", "password": "wrong"}
                    """))
            .andExpect(status().isUnauthorized());
    }
}
```

**Passo 2: Commit**

```bash
git add backend/src/test/java/com/commit/crm/controller/
git commit -m "test(polish): add integration tests for AuthController"
```

---

### Task 2: Testes de segurança (RBAC)

**Arquivos:**
- Criar: `backend/src/test/java/com/commit/crm/controller/SecurityTest.java`

**Passo 1: SecurityTest.java**

```java
package com.commit.crm.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "AQUISICAO")
    void createLead_withAquisicao_shouldSucceed() throws Exception {
        mockMvc.perform(post("/api/leads")
                .contentType("application/json")
                .content("""
                    {"companyName": "Test", "address": "Rua A", "segment": "Tech"}
                    """))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "PROSPECCAO")
    void createLead_withProspeccao_shouldReturn403() throws Exception {
        mockMvc.perform(post("/api/leads")
                .contentType("application/json")
                .content("""
                    {"companyName": "Test", "address": "Rua A", "segment": "Tech"}
                    """))
            .andExpect(status().isForbidden());
    }
}
```

**Passo 2: Commit**

```bash
git add backend/src/test/java/com/commit/crm/controller/SecurityTest.java
git commit -m "test(polish): add RBAC security tests"
```

---

### Task 3: Documentação da API

**Arquivos:**
- Criar: `docs/api/README.md`

**Passo 1: README.md**

```markdown
# API Documentation

## Authentication

### POST /api/auth/login
Login with email and password.

**Request:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "data": {
    "token": "eyJhbGci...",
    "expiresIn": 86400,
    "user": {
      "id": "uuid",
      "name": "User",
      "email": "user@example.com",
      "role": "AQUISICAO"
    }
  },
  "message": "Login realizado com sucesso",
  "timestamp": "2026-06-06T00:00:00Z"
}
```

## Leads

### GET /api/leads
List all leads with pagination.

**Query Parameters:**
- `page`: Page number (0-based)
- `size`: Page size
- `status`: Filter by status
- `segment`: Filter by segment
- `search`: Text search

## Users

### GET /api/users
List all users (DIRETOR only).

...
```

**Passo 2: Commit**

```bash
git add docs/api/README.md
git commit -m "docs(polish): add API documentation"
```

---

### Task 4: Health check endpoint

**Arquivos:**
- Criar: `backend/src/main/java/com/commit/crm/controller/HealthController.java`

**Passo 1: HealthController.java**

```java
package com.commit.crm.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/api/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "crm-comercial",
            "version", "1.0.0"
        ));
    }
}
```

**Passo 2: Commit**

```bash
git add backend/src/main/java/com/commit/crm/controller/HealthController.java
git commit -m "feat(polish): add health check endpoint"
```

---

### Task 5: Frontend — Login page

**Arquivos:**
- Criar: `frontend/src/pages/LoginPage.tsx`
- Criar: `frontend/src/services/authService.ts`
- Criar: `frontend/src/context/AuthContext.tsx`

**Passo 1: LoginPage.tsx**

```typescript
import React, { useState } from 'react';
import { useAuth } from '../hooks/useAuth';

export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const { login, error } = useAuth();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    await login(email, password);
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100">
      <form onSubmit={handleSubmit} className="bg-white p-8 rounded-lg shadow-md w-96">
        <h1 className="text-2xl font-bold mb-6">CRM Comercial</h1>
        {error && <p className="text-red-500 mb-4">{error}</p>}
        <input
          type="email"
          placeholder="Email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          className="w-full p-2 border rounded mb-4"
        />
        <input
          type="password"
          placeholder="Senha"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          className="w-full p-2 border rounded mb-4"
        />
        <button type="submit" className="w-full bg-blue-500 text-white p-2 rounded">
          Entrar
        </button>
      </form>
    </div>
  );
}
```

**Passo 2: Commit**

```bash
git add frontend/src/pages/LoginPage.tsx
git add frontend/src/services/authService.ts
git add frontend/src/context/AuthContext.tsx
git commit -m "feat(polish): add login page, auth service, and auth context"
```

---

## Resumo

| Task | O que entrega |
|------|---------------|
| 1 | Testes de integração (AuthController) |
| 2 | Testes de segurança (RBAC) |
| 3 | Documentação da API |
| 4 | Health check endpoint |
| 5 | Login page no frontend |

---

## Próximo Passo

→ PR para `main` → merge → projeto finalizado!
