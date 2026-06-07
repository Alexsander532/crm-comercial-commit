# Plano: 05 — Contatos

**Branch**: `feature/05-contatos` | **Data**: 2026-06-06
**Depende de**: 01-setup-projeto, 02-auth, 03-leads-crud
**Bloqueia**: 07-timeline
**Worktree**: `.worktrees/feature/05-contatos`

---

## Objetivo

Implementar CRUD de contatos por lead. Cada lead pode ter múltiplos contatos
(nome, cargo, telefone, email, whatsapp). Um é marcado como principal.

---

## Contexto

Contatos enriquecem o lead. É um diferencial do Ploomes — saber com quem
falar dentro da empresa. O contato principal aparece no card do kanban.

---

## Tasks

### Task 1: Criar Contact entity

**Arquivos:**
- Criar: `backend/src/main/java/com/commit/crm/model/Contact.java`

**Passo 1: Contact.java**

```java
package com.commit.crm.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "contacts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Contact {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id", nullable = false)
    private Lead lead;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 100)
    private String role;

    @Column(length = 20)
    private String phone;

    @Column(length = 255)
    private String email;

    @Column(length = 20)
    private String whatsapp;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isMain = false;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
```

**Passo 2: Commit**

```bash
git add backend/src/main/java/com/commit/crm/model/Contact.java
git commit -m "feat(contacts): add Contact entity"
```

---

### Task 2: Criar ContactRepository

**Arquivos:**
- Criar: `backend/src/main/java/com/commit/crm/repository/ContactRepository.java`

**Passo 1: ContactRepository.java**

```java
package com.commit.crm.repository;

import com.commit.crm.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ContactRepository extends JpaRepository<Contact, UUID> {
    List<Contact> findByLeadId(UUID leadId);
    
    @Query("SELECT c FROM Contact c WHERE c.lead.id = :leadId AND c.isMain = true")
    Contact findMainByLeadId(@Param("leadId") UUID leadId);
    
    @Modifying
    @Query("UPDATE Contact c SET c.isMain = false WHERE c.lead.id = :leadId")
    void clearMainByLeadId(@Param("leadId") UUID leadId);
    
    long countByLeadId(UUID leadId);
}
```

**Passo 2: Commit**

```bash
git add backend/src/main/java/com/commit/crm/repository/ContactRepository.java
git commit -m "feat(contacts): add ContactRepository"
```

---

### Task 3: Criar DTOs

**Arquivos:**
- Criar: `backend/src/main/java/com/commit/crm/dto/request/ContactRequest.java`
- Criar: `backend/src/main/java/com/commit/crm/dto/response/ContactResponse.java`

**Passo 1: ContactRequest.java**

```java
package com.commit.crm.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ContactRequest {
    @NotBlank(message = "Nome do contato é obrigatório")
    private String name;
    private String role;
    private String phone;
    private String email;
    private String whatsapp;
    private Boolean isMain;
    private String notes;
}
```

**Passo 2: Commit**

```bash
git add backend/src/main/java/com/commit/crm/dto/request/ContactRequest.java
git commit -m "feat(contacts): add ContactRequest DTO"
```

---

### Task 4: Criar ContactService

**Arquivos:**
- Criar: `backend/src/main/java/com/commit/crm/service/ContactService.java`

**Passo 1: ContactService.java**

```java
package com.commit.crm.service;

import com.commit.crm.dto.request.ContactRequest;
import com.commit.crm.model.Contact;
import com.commit.crm.model.Lead;
import com.commit.crm.repository.ContactRepository;
import com.commit.crm.repository.LeadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContactService {
    private final ContactRepository contactRepository;
    private final LeadRepository leadRepository;

    @Transactional
    public Contact create(UUID leadId, ContactRequest request) {
        Lead lead = leadRepository.findById(leadId)
            .orElseThrow(() -> new RuntimeException("Lead não encontrado"));

        boolean isMain = request.getIsMain() != null ? request.getIsMain() : false;
        
        // Se for o primeiro contato, automaticamente é principal
        long count = contactRepository.countByLeadId(leadId);
        if (count == 0) {
            isMain = true;
        }
        
        // Se este for principal, limpar os outros
        if (isMain) {
            contactRepository.clearMainByLeadId(leadId);
        }

        Contact contact = Contact.builder()
            .lead(lead)
            .name(request.getName())
            .role(request.getRole())
            .phone(request.getPhone())
            .email(request.getEmail())
            .whatsapp(request.getWhatsapp())
            .isMain(isMain)
            .notes(request.getNotes())
            .build();

        return contactRepository.save(contact);
    }

    @Transactional(readOnly = true)
    public List<Contact> listByLead(UUID leadId) {
        return contactRepository.findByLeadId(leadId);
    }

    @Transactional
    public Contact setMain(UUID leadId, UUID contactId) {
        contactRepository.clearMainByLeadId(leadId);
        
        Contact contact = contactRepository.findById(contactId)
            .orElseThrow(() -> new RuntimeException("Contato não encontrado"));
        contact.setIsMain(true);
        
        return contactRepository.save(contact);
    }

    @Transactional
    public void delete(UUID contactId) {
        contactRepository.deleteById(contactId);
    }
}
```

**Passo 2: Commit**

```bash
git add backend/src/main/java/com/commit/crm/service/ContactService.java
git commit -m "feat(contacts): add ContactService with CRUD"
```

---

### Task 5: Criar ContactController

**Arquivos:**
- Criar: `backend/src/main/java/com/commit/crm/controller/ContactController.java`

**Passo 1: ContactController.java**

```java
package com.commit.crm.controller;

import com.commit.crm.dto.request.ContactRequest;
import com.commit.crm.dto.response.ApiResponse;
import com.commit.crm.model.Contact;
import com.commit.crm.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/leads/{leadId}/contacts")
@RequiredArgsConstructor
public class ContactController {
    private final ContactService contactService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Contact>>> list(@PathVariable UUID leadId) {
        List<Contact> contacts = contactService.listByLead(leadId);
        return ResponseEntity.ok(ApiResponse.success(contacts, "Contatos recuperados"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Contact>> create(
        @PathVariable UUID leadId,
        @Valid @RequestBody ContactRequest request
    ) {
        Contact contact = contactService.create(leadId, request);
        return ResponseEntity.ok(ApiResponse.success(contact, "Contato criado"));
    }

    @PatchMapping("/{contactId}/main")
    public ResponseEntity<ApiResponse<Contact>> setMain(
        @PathVariable UUID leadId,
        @PathVariable UUID contactId
    ) {
        Contact contact = contactService.setMain(leadId, contactId);
        return ResponseEntity.ok(ApiResponse.success(contact, "Contato principal definido"));
    }

    @DeleteMapping("/{contactId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID contactId) {
        contactService.delete(contactId);
        return ResponseEntity.ok(ApiResponse.success(null, "Contato removido"));
    }
}
```

**Passo 2: Commit**

```bash
git add backend/src/main/java/com/commit/crm/controller/ContactController.java
git commit -m "feat(contacts): add ContactController"
```

---

## Resumo

| Task | O que entrega |
|------|---------------|
| 1 | Contact entity |
| 2 | ContactRepository |
| 3 | ContactRequest DTO |
| 4 | ContactService com CRUD |
| 5 | ContactController |

---

## Próxima Feature

→ [06-tarefas/plan.md](06-tarefas/plan.md)
