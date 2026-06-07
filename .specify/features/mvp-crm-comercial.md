# Feature Specification: MVP CRM Comercial

**Feature Branch**: `feature/mvp-crm`

**Created**: 2026-06-06

**Updated**: 2026-06-07 (refinamento após grilling session)

**Status**: Refined

**Input**: Design document `docs/plans/2026-06-06-crm-comercial-design.md`, `CONTEXT.md`

---

## Domain Glossary

> Full definitions in `CONTEXT.md`. Summary below:

- **Lead**: Empresa no pipeline com uma oportunidade ativa. Autônomo — se a mesma empresa voltar após PERDIDO, cria-se um novo lead.
- **Estado Terminal**: GANHO e PERDIDO — irreversíveis. Se a empresa voltar, novo lead.
- **Estado de Pausa**: ARQUIVADO — reversível, reativa sempre volta a NOVO.
- **Pipeline**: NOVO → CONTATO → NEGOCIACAO → {GANHO, PERDIDO} | ARQUIVADO (de qualquer estágio, exceto terminais)
- **TimelineEvent**: Registro imutável de qualquer mudança de estado, incluindo interações humanas (lingação, email, reunião, etc.). Não existe entidade separada "Interaction" — é um tipo de TimelineEvent.
- **Tarefa**: Atividade com prazo. Possui status de execução (PENDENTE, CONCLUIDA, CANCELADA) e status de cumprimento calculado em runtime (NO_PRAZO, COM_ATRASO, VENCIDA).
- **Contato**: Pessoa física vinculada a um lead. Múltiplos por lead, um principal.

---

## Business Rules

### Permission Matrix

| Ação | DIRETOR | GER_AQUIS | GER_PROSP | AQUISICAO | PROSPECCAO |
|------|---------|-----------|-----------|-----------|------------|
| Criar lead | ✅ | ✅ | ❌ | ✅ | ❌ |
| Mover no pipeline | ✅ | ❌ | ✅ | ❌ | ✅ (próprio) |
| Editar lead | ✅ (qualquer) | ✅ (time) | ✅ (time) | ✅ (próprio) | ✅ (atribuído) |
| Deletar/arquivar lead | ✅ | ❌ | ✅ (time) | ❌ | ✅ (atribuído) |
| Criar tarefa | ✅ (qualquer) | ✅ (time aquis.) | ✅ (time prosp.) | ❌ | ❌ |
| Completar tarefa | — | — | — | — | ✅ (atribuída) |
| Cancelar tarefa | ✅ | ✅ (time aquis.) | ✅ (time prosp.) | ❌ | ❌ |
| Registrar interação | ✅ (qualquer) | ✅ (time) | ✅ (time) | ✅ (próprio) | ✅ (atribuído) |
| Gerenciar contatos | ✅ (qualquer) | ✅ (time) | ✅ (time) | ✅ (próprio) | ✅ (atribuído) |
| Atribuir lead | ✅ | ❌ | ✅ | ❌ | ❌ |
| Reativar lead arquivado | ✅ | ❌ | ✅ | ❌ | ✅ (atribuído) |
| Criar usuário | ✅ | ✅ (time) | ✅ (time) | ❌ | ❌ |
| Desativar usuário | ✅ | ✅ (time) | ✅ (time) | ❌ | ❌ |

### Visibility Rules

| Papel | Leads visíveis | Tarefas visíveis |
|-------|---------------|-----------------|
| DIRETOR | Todos | Todas |
| GERENTE_AQUISICAO | Criados pelo time | Tarefas do time |
| GERENTE_PROSPECCAO | Atribuídos ao time | Tarefas do time |
| AQUISICAO | Que criou (qualquer estágio) | Apenas atribuídas a si |
| PROSPECCAO | Atribuídos a si | Apenas atribuídas a si |

### Lead Assignment Flow

1. AQUISICAO cria lead → `assigned_to = null`, status = NOVO
2. Lead sem atribuição fica visível para GERENTES e DIRETOR
3. GERENTE_PROSPECCAO atribui a um PROSPECCAO do time
4. PROSPECCAO só vê leads atribuídos a si

### Pipeline Transition Rules

- **Forward**: NOVO → CONTATO → NEGOCIACAO → GANHO | PERDIDO ✅
- **Backward**: NEGOCIACAO → CONTATO → NOVO ✅
- **Archive**: Qualquer estágio (exceto GANHO, PERDIDO) → ARQUIVADO ✅
- **Reactivate**: ARQUIVADO → NOVO ✅ (always NOVO, never previous stage)
- **Blocked**: Skip stages (NOVO ↛ NEGOCIACAO), terminal states (GANHO/PERDIDO ↛ any) ❌
- **Kanban order**: Sorted by last activity (most recent timeline event first), no drag-to-reorder in MVP

### Task Cancellation Rules

- Assigned user can **complete** a task, but **cannot cancel** it
- Only the task creator, the assigned user's manager, or DIRETOR can cancel
- Cancellation requires reason (optional field)

### Dashboard per Role

| Papel | Cards do Dashboard |
|-------|--------------------|
| DIRETOR | Total leads, leads por status, conversão geral, tarefas atrasadas por time, leads frios, atividade por usuário |
| GERENTE_AQUISICAO | Leads criados pelo time, avanço no pipeline, produtividade da equipe |
| GERENTE_PROSPECCAO | Leads do time, conversão do time, tarefas atrasadas, leads frios |
| AQUISICAO + PROSPECCAO | Dashboard simplificado compartilhado: meus leads, minhas tarefas (pendentes/atrasadas/concluídas) |

### Search Scope (MVP)

Busca textual apenas em campos do lead: `company_name`, `site`, `instagram`, `notes`. Busca em contatos será adicionada em versão futura.

### Required Fields

Lead: `company_name` (obrigatório), `segment` (obrigatório). `address` é recomendado mas opcional no MVP. Os demais campos (site, instagram, whatsapp, notes) são opcionais.

### Segments

Fixed enum: TECNOLOGIA, FINANCAS, SAUDE, EDUCACAO, VAREJO, OUTRO. Não há CRUD de segmentos no MVP.

---

## User Scenarios & Testing

### User Story 1 - Equipe de Aquisição cadastra leads (Priority: P1)

A equipe de aquisição pesquisa empresas manualmente e cadastra no sistema com
nome da empresa (obrigatório), segmento (obrigatório) e dados complementares.
O lead já nasce com status `NOVO` e aguarda atribuição pela GERENTE_PROSPECCAO.

**Why this priority**: Sem leads cadastrados, não há pipeline. É a base do sistema.

**Independent Test**: Um usuário com role `AQUISICAO` faz login, acessa "Novo Lead",
preenche company_name e segment, salva, e vê o lead na coluna "NOVO" do kanban
(com `assigned_to = null`, visível apenas para GERENTES e DIRETOR).

**Acceptance Scenarios**:

1. **Given** um usuário logado como AQUISICAO, **When** ele preenche
company_name e segment (obrigatórios) e salva, **Then** o lead é criado com
status `NOVO`, `assigned_to = null`, e aparece no kanban para GERENTES/DIRETOR
2. **Given** um usuário logado como AQUISICAO, **When** ele tenta salvar sem
company_name, **Then** o sistema rejeita com erro 400 "Nome da empresa é obrigatório"
3. **Given** um usuário logado como PROSPECCAO, **When** ele tenta criar lead,
**Then** retorna 403 (sem permissão)
4. **Given** um lead NOVO sem atribuição, **When** PROSPECCAO abre o kanban,
**Then** não vê o lead (apenas GERENTES e DIRETOR veem leads não atribuídos)

---

### User Story 2 - Kanban de prospecção (Priority: P1)

A equipe de prospecção visualiza os leads em colunas e move os cards entre
elas conforme avança o contato. Cards são ordenados por última atividade.

**Why this priority**: O kanban é a interface principal do time de vendas.

**Independent Test**: Um usuário PROSPECCAO vê seus leads atribuídos no kanban,
move um card de NOVO para CONTATO, e o sistema registra o evento na timeline.

**Acceptance Scenarios**:

1. **Given** um lead na coluna NOVO atribuído ao PROSPECCAO, **When** ele
move para CONTATO, **Then** o status muda e a timeline registra `STATUS_CHANGED`
2. **Given** um lead na coluna NOVO, **When** o usuário tenta mover direto
para GANHO, **Then** o sistema rejeita com erro 422 "Transição inválida"
3. **Given** um lead na coluna NEGOCIACAO, **When** move para PERDIDO,
**Then** a transição é aceita
4. **Given** um lead arquivado, **When** o GERENTE_PROSPECCAO ou DIRETOR reativa,
**Then** o lead volta a NOVO (never to previous stage) e a timeline mantém
o histórico completo
5. **Given** um lead no estado GANHO, **When** qualquer usuário tenta mover,
**Then** o sistema rejeita com erro 422 (estado terminal)
6. **Given** um lead no estado PERDIDO, **When** qualquer usuário tenta mover,
**Then** o sistema rejeita com erro 422 (estado terminal)
7. **Given** um AQUISICAO logado, **When** tenta mover lead no pipeline,
**Then** retorna 403
8. **Given** um GERENTE_AQUISICAO logado, **When** tenta mover lead no pipeline,
**Then** retorna 403

---

### User Story 3 - Hierarquia e permissões (Priority: P1)

O diretor gerencia usuários e seus gerentes. Gerentes acompanham o time.
Cada papel tem acesso específico conforme a Permission Matrix acima.

**Why this priority**: A estrutura organizacional define quem faz o quê.

**Independent Test**: DIRETOR cria um gerente com `manager_id = null`,
gerente cria funcionários com `manager_id = gerente_id`, cada funcionário
só vê e faz o que sua role permite.

**Acceptance Scenarios**:

1. **Given** um DIRETOR logado, **When** ele cria um usuário com role
`GERENTE_AQUISICAO`, **Then** o usuário é criado com `manager_id = DIRETOR`
2. **Given** um GERENTE_AQUISICAO logado, **When** ele cria um usuário com
role `AQUISICAO`, **Then** o funcionário tem `manager_id = GERENTE_AQUISICAO`
3. **Given** um AQUISICAO logado, **When** ele tenta mover lead no kanban,
**Then** retorna 403
4. **Given** um PROSPECCAO logado, **When** ele tenta criar lead, **Then**
retorna 403
5. **Given** um GERENTE_PROSPECCAO logado, **When** ele acessa a lista de
leads, **Then** ele vê apenas os leads atribuídos aos funcionários do seu time
6. **Given** um GERENTE logado, **When** ele desativa um funcionário do seu
time, **Then** o funcionário fica `is_active = false` e seus leads e
tarefas permanecem no banco com `assigned_to` preservado
7. **Given** um funcionário desativado, **When** o gerente acessa o filtro
"leads de usuários inativos", **Then** os leads do funcionário desativado
aparecem para reatribuição
8. **Given** um gerente logado, **When** ele altera o assigned_to de um lead,
**Then** o lead é reatribuído e a timeline cria evento `ASSIGNED` com de/para

---

### User Story 4 - Múltiplos contatos por lead (Priority: P2)

Cada lead pode ter vários contatos. Quem pode ver o lead pode gerenciar seus
contatos (adicionar, editar, remover, definir principal).

**Acceptance Scenarios**:

1. **Given** um lead existente, **When** adiciono um contato com nome,
telefone e cargo, **Then** o contato aparece na lista e gera `CONTACT_ADDED` na timeline
2. **Given** uma lista de contatos, **When** marco um como principal,
**Then** o anterior deixa de ser principal
3. **Given** um lead sem contatos, **When** adiciono o primeiro contato,
**Then** ele é automaticamente o principal

---

### User Story 5 - Tarefas com accountability (Priority: P2)

Gerentes e DIRETOR criam tarefas para funcionários. Funcionários completam.
O sistema calcula se foi no prazo, com atraso, ou vencida. Funcionários
não podem cancelar tarefas — só o criador, gerente ou DIRETOR pode cancelar.

**Acceptance Scenarios**:

1. **Given** um GERENTE_PROSPECCAO logado, **When** ele cria tarefa com
due_date e assigned_to para um PROSPECCAO do time, **Then** a tarefa aparece
nas pendências do funcionário e gera `TASK_CREATED` na timeline
2. **Given** uma tarefa com due_date no futuro, **When** o funcionário
conclui, **Then** completionStatus = `NO_PRAZO`
3. **Given** uma tarefa com due_date no passado, **When** o funcionário
conclui, **Then** completionStatus = `COM_ATRASO` com daysOverdue calculado
4. **Given** uma tarefa com due_date vencida e não concluída, **Then**
status calculado = `VENCIDA`
5. **Given** um AQUISICAO ou PROSPECCAO logado, **When** tenta criar tarefa,
**Then** retorna 403
6. **Given** um PROSPECCAO logado, **When** tenta cancelar tarefa atribuída
a si, **Then** retorna 403 (só criador/gerente/DIRETOR pode cancelar)

---

### User Story 6 - Timeline de atividades (Priority: P2)

Cada lead tem um feed cronológico mostrando tudo que aconteceu: criação,
mudanças de status, edições, interações humanas (ligação, email, reunião,
observação, proposta), tarefas concluídas. Tudo é um TimelineEvent — não
existe tabela separada de interações.

**Acceptance Scenarios**:

1. **Given** um lead recém-criado, **When** acesso a timeline, **Then**
vejo o evento `CREATED` com nome do usuário que criou
2. **Given** que editei o Instagram do lead, **When** acesso a timeline,
**Then** vejo `FIELD_UPDATED` com campo, valor antigo e novo
3. **Given** que mudei o status, **When** acesso a timeline, **Then**
vejo `STATUS_CHANGED` com de/para
4. **Given** que registrei uma ligação, **When** acesso a timeline,
**Then** vejo `INTERACTION` com type=LIGACAO e description

---

### User Story 7 - Dashboard por papel (Priority: P3)

O DIRETOR vê métricas globais. GERENTES vêem métricas do próprio time.
AQUISICAO e PROSPECCAO compartilham um dashboard simplificado com "meus leads"
e "minhas tarefas".

**Acceptance Scenarios**:

1. **Given** um DIRETOR logado, **When** acessa /dashboard, **Then** vê
métricas de todos os times
2. **Given** um GERENTE_AQUISICAO logado, **When** acessa /dashboard,
**Then** vê métricas do time de aquisição
3. **Given** um GERENTE_PROSPECCAO logado, **When** acessa /dashboard,
**Then** vê métricas do time de prospecção
4. **Given** um AQUISICAO logado, **When** acessa /dashboard, **Then**
vê dashboard simplificado com leads que criou e suas tarefas
5. **Given** um PROSPECCAO logado, **When** acessa /dashboard, **Then**
vê dashboard simplificado com leads atribuídos e suas tarefas
6. **Given** leads de um funcionário desativado, **When** o gerente filtra
"leads de usuários inativos", **Then** eles aparecem para reatribuição

---

### Edge Cases

- **Gerente promovido a DIRETOR**: `manager_id` vira null, vice-versa.
- **Funcionário desativado com leads/tarefas**: `assigned_to` e `created_by`
permanecem intactos. O gerente encontra os leads via filtro "usuários inativos"
(`WHERE assigned_to IN (SELECT id FROM users WHERE is_active = false)`) e
reatribui manualmente.
- **Due_date alterada após vencimento**: `completionStatus` recalculado
com base na nova data. Timeline registra `TASK_UPDATED`.
- **Leads duplicados**: Sem validação de CNPJ no MVP. O sistema não impede
duplicatas.
- **Lead arquivado e reativado**: Reativação SEMPRE volta a NOVO (nunca ao
estágio anterior). Histórico preservado na timeline.
- **Gerente de aquisição tentando mover lead**: Retorna 403. A aquisição não
gerencia o pipeline — é responsabilidade da prospecção.
- **AQUISICAO editando lead em qualquer estágio**: Permitido — a visibilidade
permite edição em qualquer campo, e a timeline registra `FIELD_UPDATED`.
- **Busca textual**: Apenas em campos do lead (company_name, site, instagram,
notes). Não inclui contatos no MVP.

---

## Requirements

### Functional Requirements

- **FR-001**: Sistema DEVE autenticar usuários por email + senha com JWT
- **FR-002**: Sistema DEVE ter 5 papéis: DIRETOR, GERENTE_AQUISICAO,
GERENTE_PROSPECCAO, AQUISICAO, PROSPECCAO, com `manager_id` sempre apontando
para o superior imediato (GERENTE_* → DIRETOR, FUNCIONARIO → GERENTE)
- **FR-003**: Sistema DEVE permitir criar leads com campos obrigatórios
(company_name, segment) e opcionais (address, site, instagram, whatsapp,
notes, enriched_data)
- **FR-004**: Sistema DEVE ter pipeline com 6 status: NOVO, CONTATO,
NEGOCIACAO, GANHO, PERDIDO, ARQUIVADO com transições válidas conforme
definido na seção Pipeline Transition Rules
- **FR-005**: Sistema DEVE validar transições de status (NOVO ↛ GANHO,
GANHO/PERDIDO são terminais, ARQUIVADO volta sempre a NOVO)
- **FR-006**: Sistema DEVE permitir criar múltiplos contatos por lead.
Quem pode ver o lead pode gerenciar seus contatos.
- **FR-007**: Sistema DEVE permitir criar tarefas com título, prioridade,
due_date e assigned_to. Apenas DIRETOR e GERENTES podem criar tarefas.
Funcionários (AQUISICAO, PROSPECCAO) só podem completar.
- **FR-008**: Sistema DEVE calcular completionStatus (NO_PRAZO, COM_ATRASO,
VENCIDA) automaticamente
- **FR-009**: Sistema DEVE registrar todo evento na timeline do lead,
incluindo interações humanas (sem tabela separada — interações são
TimelineEvent do tipo INTERACTION)
- **FR-010**: Sistema DEVE ter dashboard com métricas por role conforme
definido na seção Dashboard per Role
- **FR-011**: Sistema DEVE ter busca textual em campos do lead
(company_name, site, instagram, notes) usando ILIKE. Busca em contatos
ficará para versão futura.
- **FR-012**: Sistema DEVE seguir modelo hierárquico com `manager_id`
apontando sempre para o superior imediato
- **FR-013**: Sistema DEVE exportar respostas no formato padronizado
{ data, message, timestamp }
- **FR-014**: Ao desativar um usuário, o sistema DEVE preservar os leads
e tarefas no banco com `assigned_to` intacto. O gerente encontra leads
órãos via filtro "usuários inativos" e reatribui manualmente.
- **FR-015**: Sistema DEVE permitir filtrar leads onde `assigned_to IN
(SELECT id FROM users WHERE is_active = false)` para reatribuição
- **FR-016**: Atribuição de leads é manual pelo GERENTE_PROSPECCAO ou
DIRETOR. PROSPECCAO só vê leads atribuídos a si. Leads sem atribuição são
visíveis apenas para GERENTES e DIRETOR.
- **FR-017**: Sistema DEVE ordenar cards no kanban por última atividade
(timeline mais recente primeiro), sem drag-to-reorder no MVP.
- **FR-018**: Cancelamento de tarefa: apenas o criador, o gerente do
assigned user, ou DIRETOR pode cancelar. O assigned user não pode cancelar.
- **FR-019**: Sistema DEVE usar segmentos fixos (enum: TECNOLOGIA, FINANCAS,
SAUDE, EDUCACAO, VAREJO, OUTRO) sem CRUD no MVP.

### Key Entities

- **User**: Membro da equipe com role, `manager_id` (sempre aponta para o
superior imediato — GERENTE_* → DIRETOR, FUNCIONARIO → GERENTE), email e
senha para login, `is_active` para desativação.
- **Lead**: Empresa no pipeline com uma oportunidade ativa. Autônomo — se a
mesma empresa retornar após PERDIDO, novo lead é criado. Campos obrigatórios:
`company_name`, `segment`. Opcional: `address`, `site`, `instagram`, `whatsapp`,
`notes`, `enriched_data` (JSONB reservado para pós-MVP).
- **Contact**: Pessoa física vinculada a um lead. Múltiplos por lead,
um marcado como principal. Gerenciado por quem pode ver o lead.
- **Task**: Atividade com prazo e prioridade. Status de execução
(PENDENTE, CONCLUIDA, CANCELADA) e status de cumprimento calculado em runtime
(NO_PRAZO, COM_ATRASO, VENCIDA). Criação restrita a DIRETOR e GERENTES.
Cancelamento restrito ao criador, gerente do assigned, ou DIRETOR.
- **TimelineEvent**: Registro imutável de qualquer mudança de estado no lead.
Tipos: CREATED, STATUS_CHANGED, FIELD_UPDATED, INTERACTION (com sub-tipos:
LIGACAO, EMAIL, REUNIAO, OBSERVACAO, PROPOSTA), NOTE_ADDED, TASK_CREATED,
TASK_COMPLETED, ASSIGNED, CONTACT_ADDED, CONTACT_UPDATED. Não existe
entidade "Interaction" separada — é um tipo de TimelineEvent.

## Success Criteria

- **SC-001**: Usuário consegue cadastrar um lead completo em menos de 2 minutos
- **SC-002**: Kanban reflete mudanças de status em menos de 1 segundo
- **SC-003**: Timeline mostra eventos em até 1 segundo após a ação
- **SC-004**: Dashboard carrega métricas em menos de 2 segundos
- **SC-005**: 100% das transições de status inválidas são rejeitadas
- **SC-006**: Zero endpoints expõem dados que o papel do usuário não deveria ver
- **SC-007**: Leads de usuários desativados são encontráveis via filtro

## Assumptions

- Usuários têm acesso à internet e navegador moderno (Chrome/Firefox/Edge)
- A equipe de aquisição digita os dados manualmente (IA será adicionada depois)
- O sistema começa com um único diretor que cadastra os demais usuários
- Não haverá integração com email (outlook/gmail) no MVP
- As notificações (email/SMS) ficam pra uma versão futura
- O banco PostgreSQL roda via Docker em desenvolvimento
- O deploy será feito via Coolify
- `address` é opcional no MVP (recomendado mas não obrigatório)
- Segmentos são fixos (enum), sem CRUD no MVP
- Kanban não terá drag-to-reorder no MVP
- Busca textual não inclui contatos no MVP
- Interações (ligação, email, etc.) são TimelineEvents, não tabela separada