# Plano: 03 — CRUD de Leads

**Branch**: `feature/03-leads-crud` | **Data**: 2026-06-06
**Depende de**: 01-setup-projeto, 02-auth
**Bloqueia**: 04-pipeline-kanban, 05-contatos, 06-tarefas, 07-timeline, 08-dashboard
**Worktree**: `.worktrees/feature/03-leads-crud`

---

## Objetivo

Implementar o CRUD completo de leads: criação (equipe de aquisição), edição,
listagem paginada com filtros, busca textual, e visualização de detalhes.

---

## Contexto

Leads são o núcleo do sistema. Sem leads, não há pipeline, não há contatos,
não há tarefas. Precisamos de:
- Entidade Lead com status `NOVO` por padrão
- Validação de campos obrigatórios
- Filtros por status, segmento, assigned_to
- Busca textual em company_name, site, instagram, notes
- Relacionamento com User (created_by, assigned_to)

---

## Tasks

### Task 1: Criar Lead entity e enums

**Arquivos:**
- Criar: `backend/src/main/java/com/commit/crm/model/Lead.java`
- Criar: `backend/src/main/java/com/commit/crm/model/LeadStatus.java`

**Passo 1: Lead.java**

```java
package com.commit.crm.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "leads")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lead {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String companyName;

    @Column(length = 500)
    private String site;

    @Column(length = 200)
    private String instagram;

    @Column(length = 20)
    private String whatsapp;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false, length = 100)
    private String segment;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Object enrichedData;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private LeadStatus status = LeadStatus.NOVO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
```

**Passo 2: LeadStatus.java**

```java
package com.commit.crm.model;

public enum LeadStatus {
    NOVO, CONTATO, NEGOCIACAO, GANHO, PERDIDO, ARQUIVADO
}
```

**Passo 3: Commit**

```bash
git add backend/src/main/java/com/commit/crm/model/Lead.java
git add backend/src/main/java/com/commit/crm/model/LeadStatus.java
git commit -m "feat(leads): add Lead entity and LeadStatus enum"
```

---

### Task 2: Criar LeadRepository

**Arquivos:**
- Criar: `backend/src/main/java/com/commit/crm/repository/LeadRepository.java`

**Passo 1: LeadRepository.java**

```java
package com.commit.crm.repository;

import com.commit.crm.model.Lead;
import com.commit.crm.model.LeadStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LeadRepository extends JpaRepository<Lead, UUID> {

    Page<Lead> findByStatus(LeadStatus status, Pageable pageable);

    Page<Lead> findByAssignedToId(UUID assignedTo, Pageable pageable);

    Page<Lead> findByCreatedById(UUID createdBy, Pageable pageable);

    Page<Lead> findBySegment(String segment, Pageable pageable);

    @Query("SELECT l FROM Lead l WHERE " +
           "LOWER(l.companyName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(l.site) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(l.instagram) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(l.notes) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Lead> searchByText(@Param("search") String search, Pageable pageable);

    @Query("SELECT l FROM Lead l WHERE l.status IN :statuses")
    Page<Lead> findByStatusIn(@Param("statuses") List<LeadStatus> statuses, Pageable pageable);

    @Query("SELECT l FROM Lead l WHERE l.assignedTo.id = :userId OR l.createdBy.id = :userId")
    Page<Lead> findByAssignedToOrCreatedBy(@Param("userId") UUID userId, Pageable pageable);

    long countByStatus(LeadStatus status);

    long countByAssignedToId(UUID assignedTo);
}
```

**Passo 2: Commit**

```bash
git add backend/src/main/java/com/commit/crm/repository/LeadRepository.java
git commit -m "feat(leads): add LeadRepository with custom queries"
```

---

### Task 3: Criar DTOs de Lead

**Arquivos:**
- Criar: `backend/src/main/java/com/commit/crm/dto/request/LeadRequest.java`
- Criar: `backend/src/main/java/com/commit/crm/dto/response/LeadResponse.java`

**Passo 1: LeadRequest.java**

```java
package com.commit.crm.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LeadRequest {
    @NotBlank(message = "Nome da empresa é obrigatório")
    private String companyName;

    private String site;
    private String instagram;
    private String whatsapp;

    @NotBlank(message = "Endereço é obrigatório")
    private String address;

    @NotBlank(message = "Segmento é obrigatório")
    private String segment;

    private String notes;
}
```

**Passo 2: LeadResponse.java**

```java
package com.commit.crm.dto.response;

import com.commit.crm.model.LeadStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class LeadResponse {
    private UUID id;
    private String companyName;
    private String site;
    private String instagram;
    private String whatsapp;
    private String address;
    private String segment;
    private String notes;
    private LeadStatus status;
    private UserSummary createdBy;
    private UserSummary assignedTo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    public static class UserSummary {
        private UUID id;
        private String name;
    }
}
```

**Passo 3: Commit**

```bash
git add backend/src/main/java/com/commit/crm/dto/request/LeadRequest.java
git add backend/src/main/java/com/commit/crm/dto/response/LeadResponse.java
git commit -m "feat(leads): add LeadRequest and LeadResponse DTOs"
```

---

### Task 4: Criar LeadService

**Arquivos:**
- Criar: `backend/src/main/java/com/commit/crm/service/LeadService.java`

**Passo 1: LeadService.java**

```java
package com.commit.crm.service;

import com.commit.crm.dto.request.LeadRequest;
import com.commit.crm.dto.response.LeadResponse;
import com.commit.crm.model.Lead;
import com.commit.crm.model.LeadStatus;
import com.commit.crm.model.User;
import com.commit.crm.repository.LeadRepository;
import com.commit.crm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LeadService {
    private final LeadRepository leadRepository;
    private final UserRepository userRepository;

    @Transactional
    public LeadResponse create(LeadRequest request, UUID createdById) {
        User creator = userRepository.findById(createdById)
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Lead lead = Lead.builder()
            .companyName(request.getCompanyName())
            .site(request.getSite())
            .instagram(request.getInstagram())
            .whatsapp(request.getWhatsapp())
            .address(request.getAddress())
            .segment(request.getSegment())
            .notes(request.getNotes())
            .status(LeadStatus.NOVO)
            .createdBy(creator)
            .build();

        Lead saved = leadRepository.save(lead);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<LeadResponse> listAll(Pageable pageable) {
        return leadRepository.findAll(pageable)
            .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<LeadResponse> listByStatus(LeadStatus status, Pageable pageable) {
        return leadRepository.findByStatus(status, pageable)
            .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<LeadResponse> search(String query, Pageable pageable) {
        return leadRepository.searchByText(query, pageable)
            .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public LeadResponse findById(UUID id) {
        Lead lead = leadRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Lead não encontrado"));
        return mapToResponse(lead);
    }

    @Transactional
    public LeadResponse update(UUID id, LeadRequest request) {
        Lead lead = leadRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Lead não encontrado"));

        lead.setCompanyName(request.getCompanyName());
        lead.setSite(request.getSite());
        lead.setInstagram(request.getInstagram());
        lead.setWhatsapp(request.getWhatsapp());
        lead.setAddress(request.getAddress());
        lead.setSegment(request.getSegment());
        lead.setNotes(request.getNotes());

        Lead updated = leadRepository.save(lead);
        return mapToResponse(updated);
    }

    @Transactional
    public void delete(UUID id) {
        Lead lead = leadRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Lead não encontrado"));
        lead.setStatus(LeadStatus.ARQUIVADO);
        leadRepository.save(lead);
    }

    private LeadResponse mapToResponse(Lead lead) {
        return LeadResponse.builder()
            .id(lead.getId())
            .companyName(lead.getCompanyName())
            .site(lead.getSite())
            .instagram(lead.getInstagram())
            .whatsapp(lead.getWhatsapp())
            .address(lead.getAddress())
            .segment(lead.getSegment())
            .notes(lead.getNotes())
            .status(lead.getStatus())
            .createdBy(LeadResponse.UserSummary.builder()
                .id(lead.getCreatedBy().getId())
                .name(lead.getCreatedBy().getName())
                .build())
            .assignedTo(lead.getAssignedTo() != null ?
                LeadResponse.UserSummary.builder()
                    .id(lead.getAssignedTo().getId())
                    .name(lead.getAssignedTo().getName())
                    .build() : null)
            .createdAt(lead.getCreatedAt())
            .updatedAt(lead.getUpdatedAt())
            .build();
    }
}
```

**Passo 2: Commit**

```bash
git add backend/src/main/java/com/commit/crm/service/LeadService.java
git commit -m "feat(leads): add LeadService with CRUD operations"
```

---

### Task 5: Criar LeadController

**Arquivos:**
- Criar: `backend/src/main/java/com/commit/crm/controller/LeadController.java`

**Passo 1: LeadController.java**

```java
package com.commit.crm.controller;

import com.commit.crm.dto.request.LeadRequest;
import com.commit.crm.dto.response.ApiResponse;
import com.commit.crm.dto.response.LeadResponse;
import com.commit.crm.model.LeadStatus;
import com.commit.crm.service.LeadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/leads")
@RequiredArgsConstructor
public class LeadController {
    private final LeadService leadService;

    @PostMapping
    public ResponseEntity<ApiResponse<LeadResponse>> create(
        @Valid @RequestBody LeadRequest request,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        LeadResponse response = leadService.create(request, userId);
        return ResponseEntity.ok(ApiResponse.success(response, "Lead criado com sucesso"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<LeadResponse>>> list(Pageable pageable) {
        Page<LeadResponse> response = leadService.listAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Leads recuperados"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LeadResponse>> findById(@PathVariable UUID id) {
        LeadResponse response = leadService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Lead encontrado"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LeadResponse>> update(
        @PathVariable UUID id,
        @Valid @RequestBody LeadRequest request
    ) {
        LeadResponse response = leadService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Lead atualizado"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        leadService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Lead arquivado"));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<LeadResponse>>> search(
        @RequestParam String q,
        Pageable pageable
    ) {
        Page<LeadResponse> response = leadService.search(q, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Busca realizada"));
    }

    @GetMapping("/kanban")
    public ResponseEntity<ApiResponse<Page<LeadResponse>>> kanban(
        @RequestParam(required = false) LeadStatus status,
        Pageable pageable
    ) {
        Page<LeadResponse> response = status != null
            ? leadService.listByStatus(status, pageable)
            : leadService.listAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Kanban carregado"));
    }
}
```

**Passo 2: Commit**

```bash
git add backend/src/main/java/com/commit/crm/controller/LeadController.java
git commit -m "feat(leads): add LeadController with CRUD endpoints"
```

---

### Task 6: Testes

**Arquivos:**
- Criar: `backend/src/test/java/com/commit/crm/service/LeadServiceTest.java`

**Passo 1: Teste unitário**

```java
package com.commit.crm.service;

import com.commit.crm.dto.request.LeadRequest;
import com.commit.crm.dto.response.LeadResponse;
import com.commit.crm.model.Lead;
import com.commit.crm.model.LeadStatus;
import com.commit.crm.model.User;
import com.commit.crm.model.UserRole;
import com.commit.crm.repository.LeadRepository;
import com.commit.crm.repository.UserRepository;
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
class LeadServiceTest {
    @Mock LeadRepository leadRepository;
    @Mock UserRepository userRepository;
    @InjectMocks LeadService leadService;

    @Test
    void create_shouldReturnLeadWithStatusNovo() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).name("João").role(UserRole.AQUISICAO).build();

        LeadRequest request = new LeadRequest();
        request.setCompanyName("Empresa X");
        request.setAddress("Rua A");
        request.setSegment("Tecnologia");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(leadRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        LeadResponse result = leadService.create(request, userId);

        assertNotNull(result);
        assertEquals("Empresa X", result.getCompanyName());
        assertEquals(LeadStatus.NOVO, result.getStatus());
    }
}
```

**Passo 2: Commit**

```bash
git add backend/src/test/java/com/commit/crm/service/LeadServiceTest.java
git commit -m "test(leads): add LeadService unit tests"
```

---

## Resumo

| Task | O que entrega |
|------|---------------|
| 1 | Lead entity, LeadStatus enum |
| 2 | LeadRepository com queries customizadas |
| 3 | LeadRequest e LeadResponse DTOs |
| 4 | LeadService com CRUD |
| 5 | LeadController com endpoints REST |
| 6 | Testes unitários |

---

## Próxima Feature

→ [04-pipeline-kanban/plan.md](04-pipeline-kanban/plan.md)
