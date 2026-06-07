<!--
  Sync Impact Report:
  - Version change: 0.1.0 (initial)
  - Created from template with 7 principles + 3 additional sections
  - Templates reviewed: plan-template.md (✅ aligned), spec-template.md (✅ aligned), tasks-template.md (✅ aligned)
  - No placeholders intentionally deferred
-->

# CRM Comercial Constitution

## Core Principles

### I. Spring Boot 3 + React Stack
The backend MUST be built with Spring Boot 3 (Java 17+) using the MVC pattern with
well-defined layers: Controller → Service → Repository. The frontend MUST use
React 18+ with TypeScript, Vite, and Tailwind CSS. PostgreSQL is the database.
This stack is non-negotiable — the project serves as portfolio for Java/Spring
roles.

### II. API-First Design (NON-NEGOTIABLE)
Every feature MUST expose a well-defined REST API before any frontend
implementation begins. All endpoints MUST follow the patterns defined in the
API specification:
- Standardized response wrappers (`{ data, message, timestamp }`)
- Pagination for list endpoints
- Proper HTTP status codes and error responses
- JWT authentication via Spring Security
- Role-based access control enforced at the controller level

### III. Hierarchical Authorization
Access control MUST follow the organizational hierarchy: DIRETOR > GERENTE >
FUNCIONARIO. Every endpoint MUST verify the caller's role AND their
relationship to the resource (own leads vs team leads vs all leads).
Authorization logic MUST live in Service layer, never in controllers.

### IV. Accountability-by-Default
Every state change in the system MUST be recorded as a timeline event. Tasks
MUST track completion status against due dates (on time / late / overdue).
All modifications MUST be attributed to a specific user. The system MUST
provide visibility into who did what, when, and whether it was within
expected timeframes.

### V. Test-First (NON-NEGOTIABLE)
TDD is required for all business logic:
- Service layer: Unit tests with Mockito
- Controllers: Integration tests with @SpringBootTest + Testcontainers
- Security: Tests verifying role-based access (401/403 scenarios)
- Frontend: Component tests with Vitest + Testing Library, API mocks with MSW
Tests MUST pass before any PR is merged.

### VI. Ploomes-Inspired UX
The user experience SHOULD follow Ploomes conventions where applicable:
- Kanban pipeline as the primary lead management view
- Timeline feed showing all activity chronologically
- Cards showing key indicators (last activity, pending tasks, time in stage)
- Clean, modern UI with consistent design system (shadcn/ui)

### VII. YAGNI & Incremental Delivery
The MVP MUST include only: lead CRUD, pipeline kanban, contacts, tasks with
accountability, timeline, hierarchical users, dashboard. AI automation,
Google Maps integration, and advanced reporting are explicitly deferred to
post-MVP. The `enriched_data` JSONB field exists but will remain unused
until Phase 2.

## Technology & Constraints

### Stack Requirements
| Layer | Technology | Version |
|-------|-----------|---------|
| Backend | Spring Boot | 3.x |
| Language | Java | 17+ |
| Database | PostgreSQL | 15+ |
| Migrations | Flyway | Latest |
| Auth | Spring Security + JWT | Latest |
| Frontend | React + TypeScript | 18+ |
| Build (FE) | Vite | Latest |
| Styling | Tailwind CSS + shadcn/ui | Latest |
| API Client | Axios | Latest |
| Testing (BE) | JUnit 5 + Mockito + Testcontainers | Latest |
| Testing (FE) | Vitest + Testing Library + MSW | Latest |

### Deployment & Infrastructure
- Docker Compose for local development (PostgreSQL + app)
- Coolify for production deployment (matching Condogaia infra)
- Environment variables for all configuration (never hardcoded)
- Health check endpoint at `/api/health`

## Development Workflow

### Git & Branching
- `main` branch is production-ready — only reviewed code merges
- Every feature gets its own branch: `feature/<feature-name>`
- Git worktrees MUST be used for isolated development
- Conventional Commits format: `feat:`, `fix:`, `docs:`, `test:`, `refactor:`
- PRs MUST include: description, test results, and timeline of changes

### SDD Process
1. **Constitution** — Establish principles (this document)
2. **Specify** — Define requirements for each feature
3. **Plan** — Technical implementation design
4. **Tasks** — Breakdown into actionable TDD tasks
5. **Implement** — Execute with TDD cycle
6. **Review** — Code review before merging

### Quality Gates
- All tests MUST pass before PR
- No hardcoded secrets or credentials
- API responses MUST follow the standardized format
- Every new endpoint MUST include security tests
- Frontend components MUST have loading, empty, and error states

## Governance

This constitution supersedes ad-hoc development practices. Amendments require:
1. Update this document with rationale
2. Increment version according to semver rules
3. Update dependent templates if needed
4. Commit with message `docs: amend constitution to vX.Y.Z`

All PRs MUST verify compliance with these principles during review.
Violations MUST be flagged and resolved before merge.

**Version**: 0.1.0 | **Ratified**: 2026-06-06 | **Last Amended**: 2026-06-06
