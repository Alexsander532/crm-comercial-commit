# Feature Specification: MVP CRM Comercial

**Feature Branch**: `feature/mvp-crm`

**Created**: 2026-06-06

**Status**: Draft

**Input**: Design document `docs/plans/2026-06-06-crm-comercial-design.md`

## User Scenarios & Testing

### User Story 1 - Equipe de Aquisição cadastra leads (Priority: P1)

A equipe de aquisição pesquisa empresas manualmente e cadastra no sistema com
nome, site, instagram, whatsapp, endereço, segmento e observações relevantes.
O lead já nasce com status `NOVO` e fica disponível pra prospecção.

**Why this priority**: Sem leads cadastrados, não há pipeline. É a base do sistema.

**Independent Test**: Um usuário com role `AQUISICAO` faz login, acessa "Novo Lead",
preenche os campos obrigatórios, salva, e vê o lead na coluna "NOVO" do kanban.

**Acceptance Scenarios**:

1. **Given** um usuário logado como AQUISICAO, **When** ele preenche nome,
endereço e segmento (obrigatórios) e salva, **Then** o lead é criado com
status `NOVO` e aparece no kanban
2. **Given** um usuário logado como AQUISICAO, **When** ele tenta salvar sem
nome da empresa, **Then** o sistema rejeita com erro 400 "Nome da empresa é obrigatório"
3. **Given** um usuário logado como PROSPECCAO, **When** ele tenta acessar a
página de criar lead, **Then** o sistema retorna 403 (sem permissão)

---

### User Story 2 - Kanban de prospecção (Priority: P1)

A equipe de prospecção visualiza os leads em colunas (NOVO, CONTATO, NEGOCIAÇÃO,
GANHO, PERDIDO, ARQUIVADO) e move os cards entre as colunas conforme avança
o contato com a empresa.

**Why this priority**: O kanban é a interface principal do time de vendas.

**Independent Test**: Um usuário PROSPECCAO vê apenas seus leads atribuídos
no kanban, arrasta um card de NOVO para CONTATO, e o sistema confirma a mudança.

**Acceptance Scenarios**:

1. **Given** um lead na coluna NOVO, **When** o usuário PROSPECCAO arrasta
para CONTATO, **Then** o status muda e o card aparece na coluna CONTATO
2. **Given** um lead na coluna NOVO, **When** o usuário tenta arrastar direto
para GANHO, **Then** o sistema rejeita com erro 422 "Transição inválida"
3. **Given** um lead na coluna NEGOCIACAO, **When** o usuário arrasta para
PERDIDO, **Then** a transição é aceita
4. **Given** um lead arquivado, **When** o usuário reativa, **Then** o lead
volta a NOVO e a timeline mantém o histórico completo (arquivamento + reativação)
5. **Given** um lead na coluna GANHO, **When** o usuário tenta reativar,
**Then** o sistema rejeita com erro 422 (GANHO não pode ser reativado)

---

### User Story 3 - Hierarquia e permissões (Priority: P1)

O diretor gerencia usuários e seus gerentes. Gerentes acompanham o time.
Cada papel tem acesso específico: aquisição só cadastra, prospecção só vende.

**Why this priority**: A estrutura organizacional define quem faz o quê.

**Independent Test**: DIRETOR cria um gerente, gerente cria funcionários,
cada funcionário só vê e faz o que sua role permite.

**Acceptance Scenarios**:

1. **Given** um DIRETOR logado, **When** ele cria um usuário com role
`GERENTE_AQUISICAO`, **Then** o usuário é criado com manager_id = null
2. **Given** um GERENTE_AQUISICAO logado, **When** ele cria um usuário com
role `AQUISICAO`, **Then** o funcionário tem manager_id = gerente
3. **Given** um AQUISICAO logado, **When** ele tenta mover lead no kanban,
**Then** retorna 403
4. **Given** um PROSPECCAO logado, **When** ele tenta criar lead, **Then**
retorna 403
5. **Given** um GERENTE_PROSPECCAO logado, **When** ele acessa a lista de
leads do seu time, **Then** ele vê apenas os leads atribuídos aos
funcionários do seu time
6. **Given** um GERENTE logado, **When** ele desativa um funcionário do seu
time, **Then** o funcionário fica `is_active = false` e seus leads e
tarefas permanecem no banco (assigned_to preservado)
7. **Given** um funcionário desativado, **When** o gerente acessa o filtro
"leads não atribuídos", **Then** os leads do funcionário desativado
aparecem para reatribuição
8. **Given** um gerente logado, **When** ele altera o assigned_to de um lead
para outro funcionário ativo, **Then** o lead é reatribuído e a timeline
cria evento `ASSIGNED` com de/para

---

### User Story 4 - Múltiplos contatos por lead (Priority: P2)

Cada lead pode ter vários contatos (nome, cargo, telefone, email, whatsapp).
Um deles é marcado como principal. Útil pra empresa que tem diretor, gerente,
etc.

**Why this priority**: Enriquece o lead sem ser essencial pro MVP básico.

**Independent Test**: Um lead tem 3 contatos. O principal aparece no card
do kanban. Os demais ficam na aba de contatos.

**Acceptance Scenarios**:

1. **Given** um lead existente, **When** adiciono um contato com nome,
telefone e cargo, **Then** o contato aparece na lista de contatos do lead
2. **Given** uma lista de contatos, **When** marco um como principal,
**Then** o anterior deixa de ser principal
3. **Given** um lead sem contatos, **When** adiciono o primeiro contato,
**Then** ele é automaticamente o principal

---

### User Story 5 - Tarefas com accountability (Priority: P2)

Gerentes criam tarefas para os funcionários com prazo e prioridade.
Funcionários marcam como concluída. O sistema calcula se foi no prazo,
com atraso, ou se está vencida.

**Why this priority**: Diferencial do CRM — controle de entrega e prazos.

**Independent Test**: Gerente cria tarefa pra funcionário com prazo de 2 dias.
Funcionário conclui no prazo. Outra tarefa ele conclui atrasado. Ambas
aparecem na timeline com status correto.

**Acceptance Scenarios**:

1. **Given** um gerente logado, **When** ele cria tarefa com due_date e
assigned_to, **Then** a tarefa aparece nas pendências do funcionário
2. **Given** uma tarefa com due_date no futuro, **When** o funcionário
conclui, **Then** completionStatus = `NO_PRAZO`
3. **Given** uma tarefa com due_date no passado, **When** o funcionário
conclui, **Then** completionStatus = `COM_ATRASO` com daysOverdue calculado
4. **Given** uma tarefa com due_date vencida, **When** o funcionário não
concluiu, **Then** status calculado = `VENCIDA`
5. **Given** um funcionário logado, **When** ele tenta criar tarefa,
**Then** retorna 403

---

### User Story 6 - Timeline de atividades (Priority: P2)

Cada lead tem um feed cronológico mostrando tudo que aconteceu: criação,
mudanças de status, edições, interações, tarefas concluídas. O usuário vê
exatamente quem fez o quê e quando.

**Why this priority**: Transparência total — essencial pra accountability.

**Independent Test**: Crio um lead, edito o Instagram, mudo de status,
registro uma interação. A timeline mostra todos esses eventos em ordem.

**Acceptance Scenarios**:

1. **Given** um lead recem-criado, **When** acesso a timeline, **Then**
vejo o evento `CREATED` com nome do usuário que criou
2. **Given** que editei o Instagram do lead, **When** acesso a timeline,
**Then** vejo `FIELD_UPDATED` com campo, valor antigo e novo
3. **Given** que mudei o status, **When** acesso a timeline, **Then**
vejo `STATUS_CHANGED` com de/para
4. **Given** que registrei uma interação, **When** acesso a timeline,
**Then** vejo `INTERACTION` com tipo e descrição

---

### User Story 7 - Dashboard do diretor/gerente (Priority: P3)

O diretor vê um painel com métricas de todos os times. O gerente vê apenas
do seu time. Métricas: leads criados, taxa de conversão, tarefas atrasadas,
leads frios (sem interação há X dias).

**Why this priority**: Importante pra gestão, mas não bloqueia o uso do CRM.

**Independent Test**: Diretor acessa dashboard e vê total de leads, conversão
por time, tarefas atrasadas. Gerente vê só os dados do próprio time.

**Acceptance Scenarios**:

1. **Given** um DIRETOR logado, **When** acessa /dashboard, **Then** vê
métricas de todos os times
2. **Given** um GERENTE logado, **When** acessa /dashboard, **Then** vê
apenas métricas do seu time
3. **Given** um AQUISICAO logado, **When** acessa /dashboard, **Then** vê
apenas suas próprias estatísticas
4. **Given** um funcionário desativado, **When** o gerente busca leads com
filtro "não atribuído", **Then** os leads do funcionário desativado
aparecem disponíveis para reatribuição

---

### Edge Cases

- **O que acontece se um GERENTE for promovido a DIRETOR?**
  manager_id vira null (não tem mais chefe)

- **Funcionário desativado com leads e tarefas?**
  Os leads e tarefas mantêm o vínculo com o histórico do usuário,
  mas `assigned_to` e `created_by` são preservados para auditoria.
  O sistema DEVE permitir que um gerente/diretor reatribua os leads
  e tarefas pendentes a outro funcionário ativo.
  **Decisão:** Não remover automaticamente — o gerente reatribui
  manualmente quando quiser.

- **Due_date alterada após vencimento?**
  Se a tarefa já está vencida e o gerente altera a due_date, o
  `completionStatus` é recalculado com base na nova data.
  A timeline registra a alteração como `TASK_UPDATED`.

- **Leads duplicados?**
  **Decisão:** Sem validação de CNPJ no MVP. O sistema não impede
  duplicatas. Pode ser adicionado futuramente com busca de
  similaridade.

- **Lead arquivado e reativado?**
  O histórico permanece intacto. A timeline continua acumulando
  eventos normalmente. O lead volta ao status NOVO.

## Requirements

### Functional Requirements

- **FR-001**: Sistema DEVE autenticar usuários por email + senha com JWT
- **FR-002**: Sistema DEVE ter 5 papéis: DIRETOR, GERENTE_AQUISICAO,
GERENTE_PROSPECCAO, AQUISICAO, PROSPECCAO
- **FR-003**: Sistema DEVE permitir criar leads com campos obrigatórios
(company_name, address, segment) e opcionais (site, instagram, whatsapp, notes)
- **FR-004**: Sistema DEVE ter pipeline com 6 status: NOVO, CONTATO,
NEGOCIACAO, GANHO, PERDIDO, ARQUIVADO
- **FR-005**: Sistema DEVE validar transições de status (ex: NOVO não pula
direto pra GANHO)
- **FR-006**: Sistema DEVE permitir criar múltiplos contatos por lead
- **FR-007**: Sistema DEVE permitir criar tarefas com título, prioridade,
due_date e assigned_to
- **FR-008**: Sistema DEVE calcular completionStatus (NO_PRAZO, COM_ATRASO,
VENCIDA) automaticamente
- **FR-009**: Sistema DEVE registrar todo evento na timeline do lead
- **FR-010**: Sistema DEVE ter dashboard com métricas por role
- **FR-011**: Sistema DEVE ter busca textual em leads (company_name, site,
instagram, notes, contatos)
- **FR-012**: Sistema DEVE seguir modelo hierárquico: gerente só vê/age no
próprio time
- **FR-013**: Sistema DEVE exportar respostas no formato padronizado
{ data, message, timestamp }
- **FR-014**: Ao desativar um usuário, o sistema DEVE preservar os leads
e tarefas no banco (histórico de auditoria). Os campos assigned_to dos
leads e tarefas PERMANECEM intactos — o gerente reatribui manualmente.
- **FR-015**: Sistema DEVE permitir filtrar leads e tarefas onde
assigned_to IS NULL

### Key Entities

- **User**: Representa um membro da equipe. Tem role, manager_id (para
hierarquia), email e senha para login.
- **Lead**: Representa uma empresa no pipeline. Contém dados cadastrais
coletados pela aquisição e status atual no kanban.
- **Contact**: Pessoa física dentro de uma empresa lead. Múltiplos por lead,
um marcado como principal.
- **Task**: Atividade atribuída a um usuário com prazo. Status calculado
automaticamente (no prazo/atrasado/vencido).
- **TimelineEvent**: Registro imutável de tudo que aconteceu com um lead.
Cada tipo tem metadata específica em JSONB.
- **Interaction**: Interação humana com o lead (ligação, email, reunião,
observação, proposta). Tem `lead_id` (FK), `user_id` (quem registrou),
`type` (ENUM), `description` e `proposal_url` (só para tipo PROPOSTA).
Gera evento na timeline automaticamente.

## Success Criteria

### Measurable Outcomes

- **SC-001**: Usuário consegue cadastrar um lead completo em menos de 2 minutos
- **SC-002**: Kanban reflete mudanças de status em menos de 1 segundo após
arrastar o card
- **SC-003**: Timeline mostra eventos em até 1 segundo após a ação ser
realizada
- **SC-004**: Dashboard carrega métricas em menos de 2 segundos
- **SC-005**: 100% das transições de status inválidas são rejeitadas com
mensagem clara
- **SC-006**: Zero endpoints expõem dados que o papel do usuário não deveria ver

## Assumptions

- Usuários têm acesso à internet e navegador moderno (Chrome/Firefox/Edge)
- A equipe de aquisição digita os dados manualmente (IA será adicionada depois)
- O sistema começa com um único diretor que cadastra os demais usuários
- Não haverá integração com email (outlook/gmail) no MVP
- As notificações (email/SMS) ficam pra uma versão futura
- O banco PostgreSQL roda via Docker em desenvolvimento
- O deploy será feito via Coolify (como o Condogaia)
