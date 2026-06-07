# Plano: 01 — Setup do Projeto

**Branch**: `feature/01-setup-projeto` | **Data**: 2026-06-06
**Depende de**: — (primeira feature)
**Bloqueia**: 02-auth, 03-leads-crud
**Worktree**: `.worktrees/feature/01-setup-projeto`

---

## Objetivo

Criar a fundação do projeto: estrutura de pastas, configuração do Spring Boot,
React, Docker, banco de dados, e Flyway migrations.

---

## Contexto

Esta é a primeira feature. Tudo que vem depois depende dela. Precisamos de:
- Projeto Spring Boot configurado com dependências
- Projeto React configurado com TypeScript
- Docker Compose com PostgreSQL
- Flyway migrations (schema completo)
- Seed data (usuário diretor inicial)

---

## Tasks

### Task 1: Criar projeto Spring Boot

**Arquivos:**
- Criar: `backend/pom.xml`
- Criar: `backend/src/main/java/com/commit/crm/Application.java`
- Criar: `backend/src/main/resources/application.yml`

**Passo 1: Criar pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.5</version>
        <relativePath/>
    </parent>
    <groupId>com.commit</groupId>
    <artifactId>crm-comercial</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>CRM Comercial</name>
    <properties>
        <java.version>17</java.version>
    </properties>
    <dependencies>
        <!-- Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <!-- Data JPA -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <!-- Security -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <!-- Validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <!-- PostgreSQL -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <!-- Flyway -->
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        <!-- JWT -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>0.12.5</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>0.12.5</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>0.12.5</version>
            <scope>runtime</scope>
        </dependency>
        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <!-- Tests -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>junit-jupiter</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>postgresql</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

**Passo 2: Criar Application.java**

```java
package com.commit.crm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

**Passo 3: Criar application.yml**

```yaml
spring:
  application:
    name: crm-comercial
  datasource:
    url: jdbc:postgresql://localhost:5432/crm_comercial
    username: crm
    password: crm_password
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
    show-sql: true
  flyway:
    enabled: true
    locations: classpath:db/migration

server:
  port: 8080

jwt:
  secret: ${JWT_SECRET:crm-comercial-secret-key-change-in-production}
  expiration: 86400000
```

**Passo 4: Testar build**

```bash
cd backend
mvn clean compile
```

**Esperado**: BUILD SUCCESS

**Passo 5: Commit**

```bash
git add backend/
git commit -m "feat(setup): add Spring Boot project with dependencies"
```

---

### Task 2: Criar projeto React

**Arquivos:**
- Criar: `frontend/package.json`
- Criar: `frontend/vite.config.ts`
- Criar: `frontend/tsconfig.json`
- Criar: `frontend/tailwind.config.js`
- Criar: `frontend/index.html`
- Criar: `frontend/src/main.tsx`
- Criar: `frontend/src/App.tsx`

**Passo 1: Criar package.json**

```json
{
  "name": "crm-comercial-web",
  "private": true,
  "version": "0.0.0",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "tsc && vite build",
    "preview": "vite preview",
    "test": "vitest",
    "test:ui": "vitest --ui"
  },
  "dependencies": {
    "react": "^18.2.0",
    "react-dom": "^18.2.0",
    "react-router-dom": "^6.23.0",
    "axios": "^1.6.8",
    "tailwindcss": "^3.4.3",
    "autoprefixer": "^10.4.19",
    "postcss": "^8.4.38",
    "@radix-ui/react-dialog": "^1.0.5",
    "@radix-ui/react-dropdown-menu": "^2.0.6",
    "lucide-react": "^0.378.0",
    "class-variance-authority": "^0.7.0",
    "clsx": "^2.1.1",
    "tailwind-merge": "^2.3.0"
  },
  "devDependencies": {
    "@types/react": "^18.2.66",
    "@types/react-dom": "^18.2.22",
    "@vitejs/plugin-react": "^4.2.1",
    "typescript": "^5.2.2",
    "vite": "^5.2.0",
    "vitest": "^1.6.0",
    "@testing-library/react": "^15.0.7",
    "@testing-library/jest-dom": "^6.4.5",
    "msw": "^2.3.0",
    "jsdom": "^24.0.0"
  }
}
```

**Passo 2: Criar vite.config.ts**

```typescript
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

**Passo 3: Criar tsconfig.json**

```json
{
  "compilerOptions": {
    "target": "ES2020",
    "useDefineForClassFields": true,
    "lib": ["ES2020", "DOM", "DOM.Iterable"],
    "module": "ESNext",
    "skipLibCheck": true,
    "moduleResolution": "bundler",
    "allowImportingTsExtensions": true,
    "resolveJsonModule": true,
    "isolatedModules": true,
    "noEmit": true,
    "jsx": "react-jsx",
    "strict": true,
    "noUnusedLocals": true,
    "noUnusedParameters": true,
    "noFallthroughCasesInSwitch": true,
    "baseUrl": ".",
    "paths": {
      "@/*": ["src/*"]
    }
  },
  "include": ["src"],
  "references": [{ "path": "./tsconfig.node.json" }]
}
```

**Passo 4: Criar tailwind.config.js**

```javascript
/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {},
  },
  plugins: [],
}
```

**Passo 5: Criar index.html**

```html
<!DOCTYPE html>
<html lang="pt-BR">
  <head>
    <meta charset="UTF-8" />
    <link rel="icon" type="image/svg+xml" href="/vite.svg" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>CRM Comercial - Commit</title>
  </head>
  <body>
    <div id="root"></div>
    <script type="module" src="/src/main.tsx"></script>
  </body>
</html>
```

**Passo 6: Criar main.tsx**

```typescript
import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App'
import './index.css'

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>,
)
```

**Passo 7: Criar App.tsx**

```typescript
function App() {
  return (
    <div className="min-h-screen bg-gray-100">
      <h1 className="text-3xl font-bold text-center py-8">
        CRM Comercial - Commit
      </h1>
    </div>
  )
}

export default App
```

**Passo 8: Criar index.css**

```css
@tailwind base;
@tailwind components;
@tailwind utilities;
```

**Passo 9: Testar build**

```bash
cd frontend
npm install
npm run build
```

**Esperado**: build completo sem erros

**Passo 10: Commit**

```bash
git add frontend/
git commit -m "feat(setup): add React project with TypeScript, Tailwind, Vite"
```

---

### Task 3: Criar Docker Compose

**Arquivos:**
- Criar: `docker-compose.yml`
- Criar: `.env.example`
- Criar: `backend/Dockerfile`
- Criar: `frontend/Dockerfile`

**Passo 1: Criar docker-compose.yml**

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    container_name: crm-postgres
    environment:
      POSTGRES_DB: crm_comercial
      POSTGRES_USER: crm
      POSTGRES_PASSWORD: crm_password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U crm -d crm_comercial"]
      interval: 5s
      timeout: 5s
      retries: 5

  backend:
    build: ./backend
    container_name: crm-backend
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/crm_comercial
      SPRING_DATASOURCE_USERNAME: crm
      SPRING_DATASOURCE_PASSWORD: crm_password
      SPRING_FLYWAY_ENABLED: "true"
      JWT_SECRET: crm-comercial-jwt-secret-change-in-production
    depends_on:
      postgres:
        condition: service_healthy

  frontend:
    build: ./frontend
    container_name: crm-frontend
    ports:
      - "3000:80"
    depends_on:
      - backend

volumes:
  postgres_data:
```

**Passo 2: Criar .env.example**

```
# Database
POSTGRES_DB=crm_comercial
POSTGRES_USER=crm
POSTGRES_PASSWORD=crm_password

# JWT
JWT_SECRET=your-jwt-secret-here

# Ports
BACKEND_PORT=8080
FRONTEND_PORT=3000
```

**Passo 3: Criar backend/Dockerfile**

```dockerfile
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Passo 4: Criar frontend/Dockerfile**

```dockerfile
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
```

**Passo 5: Testar**

```bash
docker-compose up -d postgres
```

**Esperado**: PostgreSQL sobe sem erros

**Passo 6: Commit**

```bash
git add docker-compose.yml .env.example backend/Dockerfile frontend/Dockerfile
git commit -m "feat(setup): add Docker Compose with PostgreSQL, backend, frontend"
```

---

### Task 4: Criar Flyway Migrations

**Arquivos:**
- Criar: `backend/src/main/resources/db/migration/V1__create_users.sql`
- Criar: `backend/src/main/resources/db/migration/V2__create_leads.sql`
- Criar: `backend/src/main/resources/db/migration/V3__create_contacts.sql`
- Criar: `backend/src/main/resources/db/migration/V4__create_interactions.sql`
- Criar: `backend/src/main/resources/db/migration/V5__create_tasks.sql`
- Criar: `backend/src/main/resources/db/migration/V6__create_timeline_events.sql`
- Criar: `backend/src/main/resources/db/migration/V7__seed_data.sql`

**Passo 1: Criar V1__create_users.sql**

```sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(150) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL CHECK (role IN (
        'DIRETOR', 'GERENTE_AQUISICAO', 'GERENTE_PROSPECCAO',
        'AQUISICAO', 'PROSPECCAO'
    )),
    manager_id UUID REFERENCES users(id),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_manager ON users(manager_id);
CREATE INDEX idx_users_active ON users(is_active);
```

**Passo 2: Criar V2__create_leads.sql**

```sql
CREATE TABLE leads (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_name VARCHAR(200) NOT NULL,
    site VARCHAR(500),
    instagram VARCHAR(200),
    whatsapp VARCHAR(20),
    address TEXT NOT NULL,
    segment VARCHAR(100) NOT NULL,
    notes TEXT,
    enriched_data JSONB,
    status VARCHAR(20) NOT NULL DEFAULT 'NOVO' CHECK (status IN (
        'NOVO', 'CONTATO', 'NEGOCIACAO', 'GANHO', 'PERDIDO', 'ARQUIVADO'
    )),
    created_by UUID NOT NULL REFERENCES users(id),
    assigned_to UUID REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_leads_status ON leads(status);
CREATE INDEX idx_leads_assigned ON leads(assigned_to);
CREATE INDEX idx_leads_created_by ON leads(created_by);
CREATE INDEX idx_leads_segment ON leads(segment);
```

**Passo 3: Criar V3__create_contacts.sql**

```sql
CREATE TABLE contacts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lead_id UUID NOT NULL REFERENCES leads(id) ON DELETE CASCADE,
    name VARCHAR(150) NOT NULL,
    role VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(255),
    whatsapp VARCHAR(20),
    is_main BOOLEAN DEFAULT FALSE,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_contacts_lead ON contacts(lead_id);
```

**Passo 4: Criar V4__create_interactions.sql**

```sql
CREATE TABLE interactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lead_id UUID NOT NULL REFERENCES leads(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id),
    type VARCHAR(20) NOT NULL CHECK (type IN (
        'LIGACAO', 'EMAIL', 'REUNIAO', 'OBSERVACAO', 'PROPOSTA'
    )),
    description TEXT NOT NULL,
    proposal_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_interactions_lead ON interactions(lead_id);
CREATE INDEX idx_interactions_user ON interactions(user_id);
```

**Passo 5: Criar V5__create_tasks.sql**

```sql
CREATE TABLE tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lead_id UUID NOT NULL REFERENCES leads(id) ON DELETE CASCADE,
    created_by UUID NOT NULL REFERENCES users(id),
    assigned_to UUID REFERENCES users(id),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    priority VARCHAR(10) DEFAULT 'MEDIA' CHECK (priority IN (
        'BAIXA', 'MEDIA', 'ALTA'
    )),
    status VARCHAR(15) DEFAULT 'PENDENTE' CHECK (status IN (
        'PENDENTE', 'CONCLUIDA', 'CANCELADA'
    )),
    due_date TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_tasks_assigned ON tasks(assigned_to);
CREATE INDEX idx_tasks_lead ON tasks(lead_id);
CREATE INDEX idx_tasks_status ON tasks(status);
```

**Passo 6: Criar V6__create_timeline_events.sql**

```sql
CREATE TABLE timeline_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lead_id UUID NOT NULL REFERENCES leads(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id),
    type VARCHAR(20) NOT NULL CHECK (type IN (
        'CREATED', 'STATUS_CHANGED', 'FIELD_UPDATED',
        'INTERACTION', 'NOTE_ADDED', 'TASK_CREATED',
        'TASK_COMPLETED', 'ASSIGNED', 'CONTACT_ADDED',
        'CONTACT_UPDATED'
    )),
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_timeline_lead ON timeline_events(lead_id);
CREATE INDEX idx_timeline_created ON timeline_events(created_at);
```

**Passo 7: Criar V7__seed_data.sql**

```sql
-- Diretor inicial
INSERT INTO users (id, name, email, password_hash, role, is_active)
VALUES (
    '550e8400-e29b-41d4-a716-446655440000',
    'Diretor',
    'diretor@commit.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqBtF6zOqTdpA6wYJp7zXxL1O0k2a',
    'DIRETOR',
    TRUE
);
```

**Passo 8: Testar migrations**

```bash
cd backend
mvn flyway:migrate
```

**Esperado**: 7 migrations aplicadas com sucesso

**Passo 9: Commit**

```bash
git add backend/src/main/resources/db/migration/
git commit -m "feat(setup): add Flyway migrations with complete schema + seed data"
```

---

## Resumo

| Task | O que entrega |
|------|---------------|
| 1 | Spring Boot configurado com todas dependências |
| 2 | React configurado com TypeScript, Tailwind, Vite |
| 3 | Docker Compose com PostgreSQL + containers |
| 4 | Schema completo com Flyway + seed data |

---

## Próxima Feature

→ [02-auth/plan.md](02-auth/plan.md)
