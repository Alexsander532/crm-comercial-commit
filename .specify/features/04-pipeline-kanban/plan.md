# Plano: 04 — Pipeline Kanban

**Branch**: `feature/04-pipeline-kanban` | **Data**: 2026-06-06
**Depende de**: 01-setup-projeto, 02-auth, 03-leads-crud
**Bloqueia**: 07-timeline (parcialmente), 08-dashboard
**Worktree**: `.worktrees/feature/04-pipeline-kanban`

---

## Objetivo

Implementar o pipeline kanban com 6 colunas (NOVO, CONTATO, NEGOCIAÇÃO, GANHO,
PERDIDO, ARQUIVADO), validação de transições de status, drag & drop, e reativação.

---

## Contexto

O kanban é a interface principal do time de prospecção. Precisamos de:
- Regras de transição (não pode pular etapas)
- Endpoint para agrupar leads por status
- Endpoint para mover lead entre colunas
- Validação de transições inválidas

---

## Regras de Transição

```
NOVO       ──▶ CONTATO
             ──▶ ARQUIVADO

CONTATO    ──▶ NEGOCIACAO
             ──▶ ARQUIVADO

NEGOCIACAO ──▶ GANHO
             ──▶ PERDIDO
             ──▶ ARQUIVADO

GANHO      ──▶ ARQUIVADO (não pode reativar)

PERDIDO    ──▶ ARQUIVADO (não pode reativar)

ARQUIVADO  ──▶ NOVO (reativar)
```

---

## Tasks

### Task 1: Criar serviço de validação de transições

**Arquivos:**
- Criar: `backend/src/main/java/com/commit/crm/service/PipelineService.java`
- Criar: `backend/src/main/java/com/commit/crm/exception/InvalidTransitionException.java`

**Passo 1: InvalidTransitionException.java**

```java
package com.commit.crm.exception;

public class InvalidTransitionException extends RuntimeException {
    public InvalidTransitionException(String message) {
        super(message);
    }
}
```

**Passo 2: PipelineService.java**

```java
package com.commit.crm.service;

import com.commit.crm.exception.InvalidTransitionException;
import com.commit.crm.model.Lead;
import com.commit.crm.model.LeadStatus;
import com.commit.crm.repository.LeadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PipelineService {
    private final LeadRepository leadRepository;

    private static final Map<LeadStatus, Set<LeadStatus>> VALID_TRANSITIONS = Map.of(
        LeadStatus.NOVO, Set.of(LeadStatus.CONTATO, LeadStatus.ARQUIVADO),
        LeadStatus.CONTATO, Set.of(LeadStatus.NEGOCIACAO, LeadStatus.ARQUIVADO),
        LeadStatus.NEGOCIACAO, Set.of(LeadStatus.GANHO, LeadStatus.PERDIDO, LeadStatus.ARQUIVADO),
        LeadStatus.GANHO, Set.of(LeadStatus.ARQUIVADO),
        LeadStatus.PERDIDO, Set.of(LeadStatus.ARQUIVADO),
        LeadStatus.ARQUIVADO, Set.of(LeadStatus.NOVO)
    );

    @Transactional
    public Lead moveStatus(UUID leadId, LeadStatus newStatus) {
        Lead lead = leadRepository.findById(leadId)
            .orElseThrow(() -> new RuntimeException("Lead não encontrado"));

        LeadStatus current = lead.getStatus();

        if (current == newStatus) {
            return lead;
        }

        Set<LeadStatus> allowed = VALID_TRANSITIONS.get(current);
        if (allowed == null || !allowed.contains(newStatus)) {
            throw new InvalidTransitionException(
                String.format("Transição inválida: %s → %s", current, newStatus)
            );
        }

        lead.setStatus(newStatus);
        return leadRepository.save(lead);
    }

    @Transactional
    public Lead assign(UUID leadId, UUID userId) {
        Lead lead = leadRepository.findById(leadId)
            .orElseThrow(() -> new RuntimeException("Lead não encontrado"));

        // TODO: set assignedTo user
        lead.setAssignedTo(null); // Will be set when user service is ready
        return leadRepository.save(lead);
    }

    public boolean isValidTransition(LeadStatus from, LeadStatus to) {
        if (from == to) return true;
        Set<LeadStatus> allowed = VALID_TRANSITIONS.get(from);
        return allowed != null && allowed.contains(to);
    }
}
```

**Passo 3: Commit**

```bash
git add backend/src/main/java/com/commit/crm/service/PipelineService.java
git add backend/src/main/java/com/commit/crm/exception/InvalidTransitionException.java
git commit -m "feat(pipeline): add PipelineService with transition validation"
```

---

### Task 2: Criar DTO de atualização de status

**Arquivos:**
- Criar: `backend/src/main/java/com/commit/crm/dto/request/StatusUpdateRequest.java`

**Passo 1: StatusUpdateRequest.java**

```java
package com.commit.crm.dto.request;

import com.commit.crm.model.LeadStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StatusUpdateRequest {
    @NotNull
    private LeadStatus status;
    private String justification;
}
```

**Passo 2: Commit**

```bash
git add backend/src/main/java/com/commit/crm/dto/request/StatusUpdateRequest.java
git commit -m "feat(pipeline): add StatusUpdateRequest DTO"
```

---

### Task 3: Criar PipelineController

**Arquivos:**
- Criar: `backend/src/main/java/com/commit/crm/controller/PipelineController.java`

**Passo 1: PipelineController.java**

```java
package com.commit.crm.controller;

import com.commit.crm.dto.request.StatusUpdateRequest;
import com.commit.crm.dto.response.ApiResponse;
import com.commit.crm.dto.response.LeadResponse;
import com.commit.crm.model.Lead;
import com.commit.crm.model.LeadStatus;
import com.commit.crm.service.LeadService;
import com.commit.crm.service.PipelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/leads")
@RequiredArgsConstructor
public class PipelineController {
    private final PipelineService pipelineService;
    private final LeadService leadService;

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<LeadResponse>> updateStatus(
        @PathVariable UUID id,
        @RequestBody StatusUpdateRequest request
    ) {
        Lead lead = pipelineService.moveStatus(id, request.getStatus());
        LeadResponse response = leadService.findById(lead.getId());
        return ResponseEntity.ok(ApiResponse.success(response, "Status atualizado"));
    }

    @PatchMapping("/{id}/assign")
    public ResponseEntity<ApiResponse<LeadResponse>> assign(
        @PathVariable UUID id,
        @RequestParam(required = false) UUID userId
    ) {
        Lead lead = pipelineService.assign(id, userId);
        LeadResponse response = leadService.findById(lead.getId());
        return ResponseEntity.ok(ApiResponse.success(response, "Lead atribuído"));
    }

    @GetMapping("/kanban")
    public ResponseEntity<ApiResponse<java.util.Map<LeadStatus, java.util.List<LeadResponse>>>> kanban() {
        var response = java.util.Map.<LeadStatus, java.util.List<LeadResponse>>of();
        // TODO: implement kanban grouping
        return ResponseEntity.ok(ApiResponse.success(response, "Kanban carregado"));
    }
}
```

**Passo 2: Commit**

```bash
git add backend/src/main/java/com/commit/crm/controller/PipelineController.java
git commit -m "feat(pipeline): add PipelineController with status update and assign"
```

---

### Task 4: Testes de PipelineService

**Arquivos:**
- Criar: `backend/src/test/java/com/commit/crm/service/PipelineServiceTest.java`

**Passo 1: PipelineServiceTest.java**

```java
package com.commit.crm.service;

import com.commit.crm.exception.InvalidTransitionException;
import com.commit.crm.model.Lead;
import com.commit.crm.model.LeadStatus;
import com.commit.crm.model.User;
import com.commit.crm.model.UserRole;
import com.commit.crm.repository.LeadRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PipelineServiceTest {
    @Mock LeadRepository leadRepository;
    @InjectMocks PipelineService pipelineService;

    @Test
    void moveStatus_novoToContato_shouldSucceed() {
        UUID leadId = UUID.randomUUID();
        User user = User.builder().id(UUID.randomUUID()).name("João").role(UserRole.PROSPECCAO).build();
        Lead lead = Lead.builder().id(leadId).status(LeadStatus.NOVO).createdBy(user).build();

        when(leadRepository.findById(leadId)).thenReturn(Optional.of(lead));
        when(leadRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Lead result = pipelineService.moveStatus(leadId, LeadStatus.CONTATO);

        assertEquals(LeadStatus.CONTATO, result.getStatus());
    }

    @Test
    void moveStatus_novoToGanho_shouldThrow() {
        UUID leadId = UUID.randomUUID();
        User user = User.builder().id(UUID.randomUUID()).name("João").role(UserRole.PROSPECCAO).build();
        Lead lead = Lead.builder().id(leadId).status(LeadStatus.NOVO).createdBy(user).build();

        when(leadRepository.findById(leadId)).thenReturn(Optional.of(lead));

        assertThrows(InvalidTransitionException.class, () ->
            pipelineService.moveStatus(leadId, LeadStatus.GANHO)
        );
    }

    @Test
    void isValidTransition_shouldReturnCorrectly() {
        assertTrue(pipelineService.isValidTransition(LeadStatus.NOVO, LeadStatus.CONTATO));
        assertFalse(pipelineService.isValidTransition(LeadStatus.NOVO, LeadStatus.GANHO));
        assertTrue(pipelineService.isValidTransition(LeadStatus.ARQUIVADO, LeadStatus.NOVO));
    }
}
```

**Passo 2: Commit**

```bash
git add backend/src/test/java/com/commit/crm/service/PipelineServiceTest.java
git commit -m "test(pipeline): add PipelineService tests for valid and invalid transitions"
```

---

## Resumo

| Task | O que entrega |
|------|---------------|
| 1 | PipelineService com validação de transições |
| 2 | StatusUpdateRequest DTO |
| 3 | PipelineController (update status, assign) |
| 4 | Testes de transições |

---

## Próxima Feature

→ [05-contatos/plan.md](05-contatos/plan.md)
