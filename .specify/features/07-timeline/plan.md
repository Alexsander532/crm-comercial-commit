# Plano: 07 — Timeline

**Branch**: `feature/07-timeline` | **Data**: 2026-06-06
**Depende de**: 01-setup-projeto, 02-auth, 03-leads-crud, 04-pipeline-kanban, 05-contatos, 06-tarefas
**Bloqueia**: 08-dashboard
**Worktree**: `.worktrees/feature/07-timeline`

---

## Objetivo

Implementar timeline de eventos para cada lead. Todo estado gera um evento
imutável: criação, edição, mudança de status, interação, tarefa, contato.

---

## Contexto

A timeline é o feed de atividades do lead. É a prova do accountability.
Cada evento mostra: quem, o quê, quando, e metadados específicos.

---

## Eventos Gerados

| Evento | Quando gera | Metadata |
|--------|-------------|----------|
| CREATED | Lead criado | — |
| STATUS_CHANGED | Status muda | `{from, to}` |
| FIELD_UPDATED | Campo editado | `{field, oldValue, newValue}` |
| INTERACTION | Interação registrada | `{type, description}` |
| NOTE_ADDED | Nota adicionada | `{content}` |
| TASK_CREATED | Tarefa criada | `{taskId, title}` |
| TASK_COMPLETED | Tarefa concluída | `{taskId, completionStatus, daysOverdue}` |
| ASSIGNED | Lead reatribuído | `{from, to}` |
| CONTACT_ADDED | Contato adicionado | `{contactId, name}` |
| CONTACT_UPDATED | Contato editado | `{contactId, field}` |

---

## Tasks

### Task 1: Criar TimelineEvent entity

**Arquivos:**
- Criar: `backend/src/main/java/com/commit/crm/model/TimelineEvent.java`
- Criar: `backend/src/main/java/com/commit/crm/model/TimelineEventType.java`

**Passo 1: TimelineEvent.java**

```java
package com.commit.crm.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "timeline_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimelineEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id", nullable = false)
    private Lead lead;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TimelineEventType type;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Object metadata;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
```

**Passo 2: TimelineEventType.java**

```java
package com.commit.crm.model;

public enum TimelineEventType {
    CREATED,
    STATUS_CHANGED,
    FIELD_UPDATED,
    INTERACTION,
    NOTE_ADDED,
    TASK_CREATED,
    TASK_COMPLETED,
    ASSIGNED,
    CONTACT_ADDED,
    CONTACT_UPDATED
}
```

**Passo 3: Commit**

```bash
git add backend/src/main/java/com/commit/crm/model/TimelineEvent.java
git add backend/src/main/java/com/commit/crm/model/TimelineEventType.java
git commit -m "feat(timeline): add TimelineEvent entity and TimelineEventType enum"
```

---

### Task 2: Criar TimelineEventRepository

**Arquivos:**
- Criar: `backend/src/main/java/com/commit/crm/repository/TimelineEventRepository.java`

**Passo 1: TimelineEventRepository.java**

```java
package com.commit.crm.repository;

import com.commit.crm.model.TimelineEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TimelineEventRepository extends JpaRepository<TimelineEvent, UUID> {
    List<TimelineEvent> findByLeadIdOrderByCreatedAtDesc(UUID leadId);
    Page<TimelineEvent> findByLeadIdOrderByCreatedAtDesc(UUID leadId, Pageable pageable);
}
```

**Passo 2: Commit**

```bash
git add backend/src/main/java/com/commit/crm/repository/TimelineEventRepository.java
git commit -m "feat(timeline): add TimelineEventRepository"
```

---

### Task 3: Criar TimelineService

**Arquivos:**
- Criar: `backend/src/main/java/com/commit/crm/service/TimelineService.java`

**Passo 1: TimelineService.java**

```java
package com.commit.crm.service;

import com.commit.crm.model.*;
import com.commit.crm.repository.TimelineEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TimelineService {
    private final TimelineEventRepository timelineRepository;

    @Transactional
    public void recordCreated(Lead lead, User user) {
        createEvent(lead, user, TimelineEventType.CREATED, null);
    }

    @Transactional
    public void recordStatusChanged(Lead lead, User user, LeadStatus from, LeadStatus to) {
        createEvent(lead, user, TimelineEventType.STATUS_CHANGED,
            Map.of("from", from.name(), "to", to.name()));
    }

    @Transactional
    public void recordFieldUpdated(Lead lead, User user, String field, Object oldValue, Object newValue) {
        createEvent(lead, user, TimelineEventType.FIELD_UPDATED,
            Map.of("field", field, "oldValue", oldValue, "newValue", newValue));
    }

    @Transactional
    public void recordInteraction(Lead lead, User user, InteractionType type, String description) {
        createEvent(lead, user, TimelineEventType.INTERACTION,
            Map.of("type", type.name(), "description", description));
    }

    @Transactional
    public void recordTaskCreated(Lead lead, User user, Task task) {
        createEvent(lead, user, TimelineEventType.TASK_CREATED,
            Map.of("taskId", task.getId(), "title", task.getTitle()));
    }

    @Transactional
    public void recordTaskCompleted(Lead lead, User user, Task task, String completionStatus, long daysOverdue) {
        createEvent(lead, user, TimelineEventType.TASK_COMPLETED,
            Map.of("taskId", task.getId(), "title", task.getTitle(),
                   "completionStatus", completionStatus, "daysOverdue", daysOverdue));
    }

    @Transactional
    public void recordAssigned(Lead lead, User user, UUID fromUserId, UUID toUserId) {
        createEvent(lead, user, TimelineEventType.ASSIGNED,
            Map.of("from", fromUserId, "to", toUserId));
    }

    @Transactional
    public void recordContactAdded(Lead lead, User user, Contact contact) {
        createEvent(lead, user, TimelineEventType.CONTACT_ADDED,
            Map.of("contactId", contact.getId(), "name", contact.getName()));
    }

    @Transactional
    public void recordContactUpdated(Lead lead, User user, Contact contact, String field) {
        createEvent(lead, user, TimelineEventType.CONTACT_UPDATED,
            Map.of("contactId", contact.getId(), "name", contact.getName(), "field", field));
    }

    @Transactional(readOnly = true)
    public List<TimelineEvent> getTimeline(UUID leadId) {
        return timelineRepository.findByLeadIdOrderByCreatedAtDesc(leadId);
    }

    private void createEvent(Lead lead, User user, TimelineEventType type, Object metadata) {
        TimelineEvent event = TimelineEvent.builder()
            .lead(lead)
            .user(user)
            .type(type)
            .metadata(metadata)
            .build();
        timelineRepository.save(event);
    }
}
```

**Passo 2: Commit**

```bash
git add backend/src/main/java/com/commit/crm/service/TimelineService.java
git commit -m "feat(timeline): add TimelineService with event recording methods"
```

---

### Task 4: Integrar Timeline nos outros services

**Arquivos:**
- Modificar: `backend/src/main/java/com/commit/crm/service/LeadService.java`
- Modificar: `backend/src/main/java/com/commit/crm/service/PipelineService.java`
- Modificar: `backend/src/main/java/com/commit/crm/service/TaskService.java`
- Modificar: `backend/src/main/java/com/commit/crm/service/ContactService.java`

**Passo 1: Adicionar TimelineService em LeadService**

```java
// No construtor:
private final TimelineService timelineService;

// No create, após salvar:
timelineService.recordCreated(saved, creator);

// No update, antes de salvar:
timelineService.recordFieldUpdated(lead, currentUser, "companyName", oldName, newName);
// (simplificado - registrar apenas campos que mudaram)
```

**Passo 2: Adicionar em PipelineService**

```java
// No moveStatus:
timelineService.recordStatusChanged(lead, currentUser, current, newStatus);
```

**Passo 3: Adicionar em TaskService**

```java
// No create:
timelineService.recordTaskCreated(lead, creator, saved);

// No complete:
timelineService.recordTaskCompleted(lead, user, task, completionStatus, daysOverdue);
```

**Passo 4: Adicionar em ContactService**

```java
// No create:
timelineService.recordContactAdded(lead, user, saved);
```

**Passo 5: Commit**

```bash
git add -A
git commit -m "feat(timeline): integrate TimelineService into Lead, Pipeline, Task, and Contact services"
```

---

### Task 5: Criar TimelineController

**Arquivos:**
- Criar: `backend/src/main/java/com/commit/crm/controller/TimelineController.java`

**Passo 1: TimelineController.java**

```java
package com.commit.crm.controller;

import com.commit.crm.dto.response.ApiResponse;
import com.commit.crm.model.TimelineEvent;
import com.commit.crm.service.TimelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/leads/{leadId}/timeline")
@RequiredArgsConstructor
public class TimelineController {
    private final TimelineService timelineService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TimelineEvent>>> getTimeline(
        @PathVariable UUID leadId
    ) {
        List<TimelineEvent> events = timelineService.getTimeline(leadId);
        return ResponseEntity.ok(ApiResponse.success(events, "Timeline recuperada"));
    }
}
```

**Passo 2: Commit**

```bash
git add backend/src/main/java/com/commit/crm/controller/TimelineController.java
git commit -m "feat(timeline): add TimelineController"
```

---

## Resumo

| Task | O que entrega |
|------|---------------|
| 1 | TimelineEvent entity, TimelineEventType enum |
| 2 | TimelineEventRepository |
| 3 | TimelineService com métodos de registro |
| 4 | Integração em Lead, Pipeline, Task, Contact services |
| 5 | TimelineController |

---

## Próxima Feature

→ [08-dashboard/plan.md](08-dashboard/plan.md)
