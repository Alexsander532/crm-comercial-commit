# Plano: 06 — Tarefas

**Branch**: `feature/06-tarefas` | **Data**: 2026-06-06
**Depende de**: 01-setup-projeto, 02-auth, 03-leads-crud
**Bloqueia**: 07-timeline, 08-dashboard
**Worktree**: `.worktrees/feature/06-tarefas`

---

## Objetivo

Implementar CRUD de tarefas com accountability: gerentes criam, funcionários
concluem, sistema calcula status (no prazo / com atraso / vencida).

---

## Contexto

Tarefas são o diferencial do CRM. Cada tarefa tem:
- Título, descrição, prioridade
- Prazo (due_date)
- Quem criou (gerente) e quem faz (funcionário)
- Status calculado automaticamente

---

## Tasks

### Task 1: Criar Task entity e enums

**Arquivos:**
- Criar: `backend/src/main/java/com/commit/crm/model/Task.java`
- Criar: `backend/src/main/java/com/commit/crm/model/TaskPriority.java`
- Criar: `backend/src/main/java/com/commit/crm/model/TaskStatus.java`

**Passo 1: Task.java**

```java
package com.commit.crm.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tasks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id", nullable = false)
    private Lead lead;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private TaskPriority priority = TaskPriority.MEDIA;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    @Builder.Default
    private TaskStatus status = TaskStatus.PENDENTE;

    private LocalDateTime dueDate;

    private LocalDateTime completedAt;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
```

**Passo 2: TaskPriority.java**

```java
package com.commit.crm.model;

public enum TaskPriority {
    BAIXA, MEDIA, ALTA
}
```

**Passo 3: TaskStatus.java**

```java
package com.commit.crm.model;

public enum TaskStatus {
    PENDENTE, CONCLUIDA, CANCELADA
}
```

**Passo 4: Commit**

```bash
git add backend/src/main/java/com/commit/crm/model/Task.java
git add backend/src/main/java/com/commit/crm/model/TaskPriority.java
git add backend/src/main/java/com/commit/crm/model/TaskStatus.java
git commit -m "feat(tasks): add Task entity, TaskPriority and TaskStatus enums"
```

---

### Task 2: Criar TaskRepository

**Arquivos:**
- Criar: `backend/src/main/java/com/commit/crm/repository/TaskRepository.java`

**Passo 1: TaskRepository.java**

```java
package com.commit.crm.repository;

import com.commit.crm.model.Task;
import com.commit.crm.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findByLeadId(UUID leadId);
    List<Task> findByAssignedToId(UUID assignedTo);
    List<Task> findByAssignedToIdAndStatus(UUID assignedTo, TaskStatus status);
    
    @Query("SELECT t FROM Task t WHERE t.assignedTo.id = :userId AND t.status = 'PENDENTE' AND t.dueDate < CURRENT_TIMESTAMP")
    List<Task> findOverdueByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignedTo.id = :userId AND t.status = 'PENDENTE'")
    long countPendingByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignedTo.id = :userId AND t.status = 'PENDENTE' AND t.dueDate < CURRENT_TIMESTAMP")
    long countOverdueByUserId(@Param("userId") UUID userId);
}
```

**Passo 2: Commit**

```bash
git add backend/src/main/java/com/commit/crm/repository/TaskRepository.java
git commit -m "feat(tasks): add TaskRepository"
```

---

### Task 3: Criar DTOs

**Arquivos:**
- Criar: `backend/src/main/java/com/commit/crm/dto/request/TaskRequest.java`
- Criar: `backend/src/main/java/com/commit/crm/dto/response/TaskResponse.java`

**Passo 1: TaskRequest.java**

```java
package com.commit.crm.dto.request;

import com.commit.crm.model.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TaskRequest {
    @NotBlank(message = "Título é obrigatório")
    private String title;
    private String description;
    private TaskPriority priority;
    private LocalDateTime dueDate;
    @NotNull(message = "Responsável é obrigatório")
    private UUID assignedTo;
}
```

**Passo 2: Commit**

```bash
git add backend/src/main/java/com/commit/crm/dto/request/TaskRequest.java
git commit -m "feat(tasks): add TaskRequest DTO"
```

---

### Task 4: Criar TaskService

**Arquivos:**
- Criar: `backend/src/main/java/com/commit/crm/service/TaskService.java`

**Passo 1: TaskService.java**

```java
package com.commit.crm.service;

import com.commit.crm.dto.request.TaskRequest;
import com.commit.crm.model.Task;
import com.commit.crm.model.TaskStatus;
import com.commit.crm.model.User;
import com.commit.crm.repository.LeadRepository;
import com.commit.crm.repository.TaskRepository;
import com.commit.crm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final LeadRepository leadRepository;
    private final UserRepository userRepository;

    @Transactional
    public Task create(UUID leadId, TaskRequest request, UUID createdById) {
        var lead = leadRepository.findById(leadId)
            .orElseThrow(() -> new RuntimeException("Lead não encontrado"));
        var creator = userRepository.findById(createdById)
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        var assigned = userRepository.findById(request.getAssignedTo())
            .orElseThrow(() -> new RuntimeException("Responsável não encontrado"));

        Task task = Task.builder()
            .lead(lead)
            .createdBy(creator)
            .assignedTo(assigned)
            .title(request.getTitle())
            .description(request.getDescription())
            .priority(request.getPriority() != null ? request.getPriority() : com.commit.crm.model.TaskPriority.MEDIA)
            .dueDate(request.getDueDate())
            .status(TaskStatus.PENDENTE)
            .build();

        return taskRepository.save(task);
    }

    @Transactional
    public Task complete(UUID taskId) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new RuntimeException("Tarefa não encontrada"));
        
        task.setStatus(TaskStatus.CONCLUIDA);
        task.setCompletedAt(LocalDateTime.now());
        
        return taskRepository.save(task);
    }

    @Transactional
    public Task reopen(UUID taskId) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new RuntimeException("Tarefa não encontrada"));
        
        task.setStatus(TaskStatus.PENDENTE);
        task.setCompletedAt(null);
        
        return taskRepository.save(task);
    }

    @Transactional(readOnly = true)
    public List<Task> listByLead(UUID leadId) {
        return taskRepository.findByLeadId(leadId);
    }

    @Transactional(readOnly = true)
    public List<Task> listMyTasks(UUID userId) {
        return taskRepository.findByAssignedToId(userId);
    }

    @Transactional(readOnly = true)
    public List<Task> listOverdue(UUID userId) {
        return taskRepository.findOverdueByUserId(userId);
    }

    public String getCompletionStatus(Task task) {
        if (task.getStatus() == TaskStatus.CONCLUIDA && task.getCompletedAt() != null) {
            if (task.getDueDate() == null) {
                return "NO_PRAZO";
            }
            return task.getCompletedAt().isBefore(task.getDueDate()) || task.getCompletedAt().isEqual(task.getDueDate())
                ? "NO_PRAZO" : "COM_ATRASO";
        }
        if (task.getStatus() == TaskStatus.PENDENTE && task.getDueDate() != null) {
            return task.getDueDate().isBefore(LocalDateTime.now()) ? "VENCIDA" : "EM_DIA";
        }
        return "EM_DIA";
    }

    public long getDaysOverdue(Task task) {
        if (task.getDueDate() == null || task.getCompletedAt() == null) return 0;
        if (task.getCompletedAt().isAfter(task.getDueDate())) {
            return java.time.Duration.between(task.getDueDate(), task.getCompletedAt()).toDays();
        }
        return 0;
    }
}
```

**Passo 2: Commit**

```bash
git add backend/src/main/java/com/commit/crm/service/TaskService.java
git commit -m "feat(tasks): add TaskService with CRUD, completion, and accountability"
```

---

### Task 5: Criar TaskController

**Arquivos:**
- Criar: `backend/src/main/java/com/commit/crm/controller/TaskController.java`

**Passo 1: TaskController.java**

```java
package com.commit.crm.controller;

import com.commit.crm.dto.request.TaskRequest;
import com.commit.crm.dto.response.ApiResponse;
import com.commit.crm.model.Task;
import com.commit.crm.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @PostMapping("/leads/{leadId}/tasks")
    public ResponseEntity<ApiResponse<Task>> create(
        @PathVariable UUID leadId,
        @Valid @RequestBody TaskRequest request,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        Task task = taskService.create(leadId, request, userId);
        return ResponseEntity.ok(ApiResponse.success(task, "Tarefa criada"));
    }

    @GetMapping("/leads/{leadId}/tasks")
    public ResponseEntity<ApiResponse<List<Task>>> listByLead(@PathVariable UUID leadId) {
        List<Task> tasks = taskService.listByLead(leadId);
        return ResponseEntity.ok(ApiResponse.success(tasks, "Tarefas recuperadas"));
    }

    @PatchMapping("/leads/{leadId}/tasks/{taskId}/complete")
    public ResponseEntity<ApiResponse<Task>> complete(@PathVariable UUID taskId) {
        Task task = taskService.complete(taskId);
        return ResponseEntity.ok(ApiResponse.success(task, "Tarefa concluída"));
    }

    @PatchMapping("/leads/{leadId}/tasks/{taskId}/reopen")
    public ResponseEntity<ApiResponse<Task>> reopen(@PathVariable UUID taskId) {
        Task task = taskService.reopen(taskId);
        return ResponseEntity.ok(ApiResponse.success(task, "Tarefa reaberta"));
    }

    @GetMapping("/tasks/my")
    public ResponseEntity<ApiResponse<List<Task>>> myTasks(
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        List<Task> tasks = taskService.listMyTasks(userId);
        return ResponseEntity.ok(ApiResponse.success(tasks, "Minhas tarefas"));
    }

    @GetMapping("/tasks/overdue")
    public ResponseEntity<ApiResponse<List<Task>>> overdue(
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        List<Task> tasks = taskService.listOverdue(userId);
        return ResponseEntity.ok(ApiResponse.success(tasks, "Tarefas vencidas"));
    }

    @GetMapping("/tasks/{taskId}/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> status(@PathVariable UUID taskId) {
        Task task = taskService.listByLead(taskId).stream()
            .filter(t -> t.getId().equals(taskId))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Tarefa não encontrada"));
        
        Map<String, Object> result = Map.of(
            "completionStatus", taskService.getCompletionStatus(task),
            "daysOverdue", taskService.getDaysOverdue(task)
        );
        return ResponseEntity.ok(ApiResponse.success(result, "Status calculado"));
    }
}
```

**Passo 2: Commit**

```bash
git add backend/src/main/java/com/commit/crm/controller/TaskController.java
git commit -m "feat(tasks): add TaskController"
```

---

### Task 6: Testes

**Arquivos:**
- Criar: `backend/src/test/java/com/commit/crm/service/TaskServiceTest.java`

**Passo 1: Teste**

```java
package com.commit.crm.service;

import com.commit.crm.model.*;
import com.commit.crm.repository.LeadRepository;
import com.commit.crm.repository.TaskRepository;
import com.commit.crm.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {
    @Mock TaskRepository taskRepository;
    @Mock LeadRepository leadRepository;
    @Mock UserRepository userRepository;
    @InjectMocks TaskService taskService;

    @Test
    void getCompletionStatus_concluidaNoPrazo_shouldReturnNoPrazo() {
        Task task = Task.builder()
            .status(TaskStatus.CONCLUIDA)
            .completedAt(LocalDateTime.now().minusDays(1))
            .dueDate(LocalDateTime.now().plusDays(1))
            .build();

        assertEquals("NO_PRAZO", taskService.getCompletionStatus(task));
    }

    @Test
    void getCompletionStatus_concluidaAtrasada_shouldReturnComAtraso() {
        Task task = Task.builder()
            .status(TaskStatus.CONCLUIDA)
            .completedAt(LocalDateTime.now().plusDays(1))
            .dueDate(LocalDateTime.now().minusDays(1))
            .build();

        assertEquals("COM_ATRASO", taskService.getCompletionStatus(task));
    }

    @Test
    void getCompletionStatus_vencida_shouldReturnVencida() {
        Task task = Task.builder()
            .status(TaskStatus.PENDENTE)
            .dueDate(LocalDateTime.now().minusDays(2))
            .build();

        assertEquals("VENCIDA", taskService.getCompletionStatus(task));
    }
}
```

**Passo 2: Commit**

```bash
git add backend/src/test/java/com/commit/crm/service/TaskServiceTest.java
git commit -m "test(tasks): add TaskService tests for accountability"
```

---

## Resumo

| Task | O que entrega |
|------|---------------|
| 1 | Task entity, TaskPriority, TaskStatus |
| 2 | TaskRepository |
| 3 | TaskRequest DTO |
| 4 | TaskService com CRUD, completion, accountability |
| 5 | TaskController |
| 6 | Testes |

---

## Próxima Feature

→ [07-timeline/plan.md](07-timeline/plan.md)
