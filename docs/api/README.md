# CRM Comercial — API Documentation

## Base URL
```
http://localhost:8080/api
```

## Auth

### POST /auth/login
Authenticate and receive JWT token.

```json
// Request
{ "email": "diretor@commit.com", "password": "admin123" }

// Response 200
{ "data": { "token": "eyJ...", "expiresIn": 86400000, "user": { "id": "...", "name": "Diretor", "email": "...", "role": "DIRETOR" } }, "message": "Login realizado com sucesso" }
```

### POST /auth/register (DIRETOR only)
```json
// Request
{ "name": "Nome", "email": "n@c.com", "password": "123", "role": "AQUISICAO" }
```

### GET /auth/me
Returns current user data. Requires Bearer token.

## Leads

| Method | Path | Description |
|--------|------|-------------|
| POST | /leads | Create lead |
| GET | /leads | List (paginated) |
| GET | /leads/{id} | Get by ID |
| PUT | /leads/{id} | Update |
| DELETE | /leads/{id} | Archive |
| PUT | /leads/{id}/assign?userId=X | Assign |
| GET | /leads/search?q= | Search |

### POST /leads
```json
{ "companyName": "Empresa", "segment": "TECNOLOGIA" }
```

## Pipeline

| Method | Path | Description |
|--------|------|-------------|
| PATCH | /leads/{id}/status | Move status |
| PATCH | /leads/{id}/assign | Assign user |
| GET | /leads/kanban | Grouped by status |

### PATCH /leads/{id}/status
```json
{ "newStatus": "CONTATO" }
```

## Contacts

| Method | Path | Description |
|--------|------|-------------|
| GET | /leads/{id}/contacts | List |
| POST | /leads/{id}/contacts | Create |
| DELETE | /leads/{id}/contacts/{cid} | Delete |
| PATCH | /leads/{id}/contacts/{cid}/main | Set as main |

## Tasks

| Method | Path | Description |
|--------|------|-------------|
| GET | /leads/{id}/tasks | By lead |
| POST | /leads/{id}/tasks | Create |
| GET | /tasks/my | My tasks |
| PATCH | /tasks/{id}/complete | Complete |
| PATCH | /tasks/{id}/cancel | Cancel |

## Timeline

| Method | Path | Description |
|--------|------|-------------|
| GET | /leads/{id}/timeline | Get events |
| POST | /leads/{id}/timeline | Record interaction |

## Dashboard

| Method | Path | Description |
|--------|------|-------------|
| GET | /dashboard | Metrics by role |

## Health

| Method | Path | Description |
|--------|------|-------------|
| GET | /api/health | Health check |
