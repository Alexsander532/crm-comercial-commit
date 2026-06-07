# CRM Comercial

Sistema interno da Commit para gestão do pipeline comercial, com kanban, tarefas com accountability, timeline, hierarquia de usuários e dashboard por papel.

---

## Language

**Lead**:
Uma empresa no pipeline com uma única oportunidade ativa. Cada lead é autônomo e independente.

**Campos do Lead**: company_name (obrigatório), segment (obrigatório), address (recomendado, opcional no MVP), site (opcional), instagram (opcional), whatsapp (opcional), notes (opcional), enriched_data (JSONB — reservado para pós-MVP).
_Avoid_: Oportunidade, negócio, empresa (quando significar lead), cliente potencial

**Estado Terminal (PERDIDO, GANHO)**:
Ciclo de venda encerrado. O lead não pode ser reativado. Se a mesma empresa for contatada novamente, um novo lead é criado.
_Avoid_: Reativar, reabrir

**Estado de Pausa (ARQUIVADO)**:
Pausa reversível. O lead pode ser reativado e volta ao estado NOVO preservando todo o histórico. Útil para leads arquivados por engano, ou que não são o momento agora mas podem voltar.

**Atribuição de Lead**:
Leads NOVO criados pela Aquisição nascem com `assigned_to = null`. Ficam visíveis apenas para GERENTES e DIRETOR. O GERENTE_PROSPECCAO distribui manualmente para os PROSPECCAO do seu time. PROSPECCAO só vê leads atribuídos a si.
_Avoid_: Auto-atribuição, pool aberto, distribuição automática

**Visibilidade de Leads**:
Cada papel enxerga um subconjunto diferente de leads:
- **DIRETOR**: todos os leads, de todos os times, em qualquer estágio.
- **GERENTE_AQUISICAO**: leads criados pelo seu time de aquisição, em qualquer estágio do pipeline.
- **GERENTE_PROSPECCAO**: leads atribuídos ao seu time de prospecção, em qualquer estágio.
- **AQUISICAO**: leads que criou, em qualquer estágio do pipeline.
- **PROSPECCAO**: apenas leads atribuídos a si.

**Edição de leads**: quem enxerga o lead pode editar qualquer campo. A timeline registra `FIELD_UPDATED` com de/para.
_Avoid_: Visão cega, muro entre aquisição e prospecção

**Hierarquia de Usuário**:
A hierarquia é uma árvore com `manager_id` sempre apontando para o superior imediato:
- DIRETOR: `manager_id = null`
- GERENTE_*: `manager_id = DIRETOR`
- AQUISICAO: `manager_id = GERENTE_AQUISICAO`
- PROSPECCAO: `manager_id = GERENTE_PROSPECCAO`

O `manager_id` é usado em consultas para determinar o escopo de visibilidade em dashboards e filtros.
_Avoid_: manager_id null para gerentes, hierarquia plana

**Desativação de Usuário**:
Ao desativar um usuário (`is_active = false`), seus leads e tarefas mantêm `assigned_to` intacto (histórico de auditoria). O gerente encontra os leads órfãos pelo filtro **"Leads de usuários inativos"** (`assigned_to IN (SELECT id FROM users WHERE is_active = false)`). Pode reatribuir individualmente ou em massa para usuários ativos do time.
_Avoid_: Limpar assigned_to ao desativar, deletar usuário, filtro "não atribuído"

**Contato**:
Uma pessoa física vinculada a um lead. Múltiplos contatos por lead, um deles marcado como principal.

**Gerenciamento de contatos**: quem enxerga o lead pode gerenciar seus contatos (adicionar, editar, remover, definir principal). A timeline registra as alterações.

**Pipeline**:
A sequência ordenada de estágios que um lead percorre: NOVO → CONTATO → NEGOCIACAO → GANHO/PERDIDO/ARQUIVADO.

**Estágios do Pipeline**:
- **NOVO**: Lead cadastrado pela aquisição, aguardando primeiro contato da prospecção.
- **CONTATO**: Prospecção iniciou o contato com o lead.
- **NEGOCIACAO**: Prospecção está em negociação ativa.
- **GANHO**: Negócio fechado (estado terminal, irreversível).
- **PERDIDO**: Negócio perdido (estado terminal, irreversível).
- **ARQUIVADO**: Lead pausado (reversível — reativação sempre volta a NOVO).

**Transições válidas**:
- Avanço: NOVO → CONTATO → NEGOCIACAO → {GANHO, PERDIDO}
- Pausa: qualquer estágio (exceto GANHO, PERDIDO) → ARQUIVADO
- Reativação: ARQUIVADO → NOVO
- Retrocesso permitido: NEGOCIACAO → CONTATO, CONTATO → NOVO
- Bloqueado: pular estágio (NOVO ↛ NEGOCIACAO), GANHO/PERDIDO ↛ qualquer

**Quem pode mover no pipeline**: PROSPECCAO (lead atribuído a si), GERENTE_PROSPECCAO (time), DIRETOR (qualquer). AQUISICAO e GERENTE_AQUISICAO não movem.

**Ordenação no kanban**: cards ordenados por última atividade (timeline mais recente) decrescente. Sem drag to reorder manual no MVP.

**Timeline Event**:
Registro imutável de uma mudança de estado no sistema. Todo evento é atribuído a um usuário e armazenado em JSONB com metadados específicos do tipo.
_Avoid_: Log, histórico, feed (quando referir ao dado técnico)

**Tarefa**:
Atividade atribuída a um usuário com prazo e prioridade. Possui status de execução (PENDENTE, CONCLUIDA, CANCELADA) e um status de cumprimento de prazo calculado em runtime (NO_PRAZO, COM_ATRASO, VENCIDA).

**Quem pode criar tarefas**: DIRETOR (para qualquer um), GERENTE_PROSPECCAO (para PROSPECCAO do seu time, em leads do time), GERENTE_AQUISICAO (para AQUISICAO do seu time, em leads do time). AQUISICAO e PROSPECCAO não criam tarefas (403).

**Cancelamento**: criador, gerente do assigned user, ou DIRETOR. O assigned user não pode cancelar — só completa.
_Avoid_: Atividade, job, ticket

**Dashboard por Papel**:
- **DIRETOR**: total leads, leads por status, conversão geral, tarefas atrasadas por time, leads frios, atividade por usuário.
- **GERENTE_AQUISICAO**: leads criados pelo time, avanço no pipeline, produtividade da equipe de aquisição.
- **GERENTE_PROSPECCAO**: leads do time, conversão do time, tarefas atrasadas do time, leads frios.
- **AQUISICAO e PROSPECCAO**: Dashboard simplificado compartilhado com: meus leads, minhas tarefas (pendentes/atrasadas/concluídas). AQUISICAO vê leads que criou, PROSPECCAO vê leads atribuídos a si.

**TimelineEvent — tipo INTERACTION**:
Registro manual de um contato humano com o lead. É um TimelineEvent como qualquer outro — não existe entidade separada. O metadado contém `type` (LIGACAO, EMAIL, REUNIAO, OBSERVACAO, PROPOSTA) e `description`.

**Quem pode registrar interação**: PROSPECCAO (no lead atribuído a si), GERENTE_PROSPECCAO (no time), GERENTE_AQUISICAO (nos leads criados pelo time), AQUISICAO (nos leads que criou, em qualquer estágio), DIRETOR (qualquer lead).
_Avoid_: Atividade, nota, contato (quando referir ao registro), tabela interactions

---

## Example Dialogue

**Dev**: Quando a Commit perde um negócio e 3 meses depois o mesmo cliente volta, o que fazer?

**Domain Expert**: O lead antigo fica como PERDIDO — não se mexe. Cria um lead novo com os dados atualizados da empresa. Cada lead é um ciclo de venda independente.

**Dev**: E um lead que foi arquivado porque o cliente disse "não é o momento"? Ele pode voltar?

**Domain Expert**: Esse é o ARQUIVADO — é uma pausa. Pode reativar ele de volta a NOVO, o histórico todo continua intacto. PERDIDO é terminal, ARQUIVADO não.

**Dev**: Então existem duas categorias de estado final?

**Domain Expert**: Exato. Terminais (GANHO, PERDIDO) e Pausa (ARQUIVADO).

**Dev**: Então se eu buscar "Empresa X" no sistema, posso ver dois leads: um PERDIDO e um NOVO?

**Domain Expert**: Exato. Cada um com sua própria timeline, contatos e tarefas. O histórico do ciclo anterior fica preservado no lead PERDIDO.
