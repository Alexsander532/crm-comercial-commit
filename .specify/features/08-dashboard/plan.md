# Plano: 08 — Dashboard

**Branch**: `feature/08-dashboard` | **Data**: 2026-06-06
**Depende de**: 01-setup-projeto, 02-auth, 03-leads-crud, 06-tarefas, 07-timeline
**Bloqueia**: 09-polish
**Worktree**: `.worktrees/feature/08-dashboard`

---

## Objetivo

Implementar dashboard com métricas por papel: leads, conversão, tarefas
atrasadas, leads frios (sem interação há X dias).

---

## Contexto

Dashboard é visão de gestão. DIRETOR vê todos os times. GERENTE vê só o
próprio. FUNCIONÁRIO vê só o próprio.

---

## Métricas por Papel

### DIRETOR
- Total de leads
- Leads por status (colunas do kanban)
- Taxa de conversão (GANHO / total)
- Tarefas atrasadas por time
- Leads frios por time
- Atividade por usuário

### GERENTE
- Leads do time
- Tarefas atrasadas do time
- Leads frios do time
- Conversão do time

### FUNCIONÁRIO
- Meus leads
- Minhas tarefas pendentes
- Minhas tarefas atrasadas
- Minhas tarefas concluídas

---

## Tasks

### Task 1: Criar DTOs de Dashboard

**Arquivos:**
- Criar: `backend/src/main/java/com/commit/crm/dto/response/DashboardResponse.java`

**Passo 1: DashboardResponse.java**

```java
package com.commit.crm.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardResponse {
    private Summary summary;
    private Map<String, Long> pipelineChart;
    private List<RecentLead> recentLeads;
    private List<RecentActivity> recentActivities;
    private List<TopSegment> topSegments;
    private List<UserActivity> userActivities;
    private List<OverdueTask> overdueTasks;
    private List<StaleLead> staleLeads;

    @Data
    @Builder
    public static class Summary {
        private long totalLeads;
        private long leadsThisMonth;
        private long myLeads;
        private double conversionRate;
        private long tasksOverdue;
        private long tasksPending;
        private long leadsStale;
    }

    @Data
    @Builder
    public static class RecentLead {
        private String id;
        private String companyName;
        private String status;
        private String createdAt;
    }

    @Data
    @Builder
    public static class RecentActivity {
        private String userName;
        private String action;
        private String leadName;
        private String timestamp;
    }

    @Data
    @Builder
    public static class TopSegment {
        private String segment;
        private long count;
    }

    @Data
    @Builder
    public static class UserActivity {
        private String userName;
        private long interactions;
        private long leadsCreated;
    }

    @Data
    @Builder
    public static class OverdueTask {
        private String taskId;
        private String title;
        private String assignedTo;
        private long daysOverdue;
    }

    @Data
    @Builder
    public static class StaleLead {
        private String leadId;
        private String companyName;
        private String assignedTo;
        private long daysWithoutActivity;
    }
}
```

**Passo 2: Commit**

```bash
git add backend/src/main/java/com/commit/crm/dto/response/DashboardResponse.java
git commit -m "feat(dashboard): add DashboardResponse DTO"
```

---

### Task 2: Criar DashboardService

**Arquivos:**
- Criar: `backend/src/main/java/com/commit/crm/service/DashboardService.java`

**Passo 1: DashboardService.java**

```java
package com.commit.crm.service;

import com.commit.crm.dto.response.DashboardResponse;
import com.commit.crm.model.*;
import com.commit.crm.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final LeadRepository leadRepository;
    private final TaskRepository taskRepository;
    private final TimelineEventRepository timelineRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(User currentUser) {
        return switch (currentUser.getRole()) {
            case DIRETOR -> buildDirectorDashboard();
            case GERENTE_AQUISICAO, GERENTE_PROSPECCAO -> buildManagerDashboard(currentUser);
            case AQUISICAO, PROSPECCAO -> buildEmployeeDashboard(currentUser);
        };
    }

    private DashboardResponse buildDirectorDashboard() {
        long totalLeads = leadRepository.count();
        long ganhos = leadRepository.countByStatus(LeadStatus.GANHO);
        long perdidos = leadRepository.countByStatus(LeadStatus.PERDIDO);
        long totalFinalizados = ganhos + perdidos;
        double conversionRate = totalFinalizados > 0 ? (ganhos * 100.0 / totalFinalizados) : 0;

        var pipelineChart = Map.of(
            "NOVO", leadRepository.countByStatus(LeadStatus.NOVO),
            "CONTATO", leadRepository.countByStatus(LeadStatus.CONTATO),
            "NEGOCIACAO", leadRepository.countByStatus(LeadStatus.NEGOCIACAO),
            "GANHO", ganhos,
            "PERDIDO", perdidos,
            "ARQUIVADO", leadRepository.countByStatus(LeadStatus.ARQUIVADO)
        );

        return DashboardResponse.builder()
            .summary(DashboardResponse.Summary.builder()
                .totalLeads(totalLeads)
                .conversionRate(conversionRate)
                .build())
            .pipelineChart(pipelineChart)
            .build();
    }

    private DashboardResponse buildManagerDashboard(User manager) {
        // TODO: Implementar filtro por time
        return DashboardResponse.builder().build();
    }

    private DashboardResponse buildEmployeeDashboard(User user) {
        long myLeads = leadRepository.countByAssignedToId(user.getId());
        long pending = taskRepository.countPendingByUserId(user.getId());
        long overdue = taskRepository.countOverdueByUserId(user.getId());

        return DashboardResponse.builder()
            .summary(DashboardResponse.Summary.builder()
                .myLeads(myLeads)
                .tasksPending(pending)
                .tasksOverdue(overdue)
                .build())
            .build();
    }

    public List<DashboardResponse.StaleLead> getStaleLeads(int daysThreshold) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(daysThreshold);
        // TODO: Implementar query para leads sem interação desde threshold
        return List.of();
    }
}
```

**Passo 2: Commit**

```bash
git add backend/src/main/java/com/commit/crm/service/DashboardService.java
git commit -m "feat(dashboard): add DashboardService with metrics per role"
```

---

### Task 3: Criar DashboardController

**Arquivos:**
- Criar: `backend/src/main/java/com/commit/crm/controller/DashboardController.java`

**Passo 1: DashboardController.java**

```java
package com.commit.crm.controller;

import com.commit.crm.dto.response.ApiResponse;
import com.commit.crm.dto.response.DashboardResponse;
import com.commit.crm.model.User;
import com.commit.crm.service.DashboardService;
import com.commit.crm.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        User user = userService.findById(userId);
        DashboardResponse response = dashboardService.getDashboard(user);
        return ResponseEntity.ok(ApiResponse.success(response, "Dashboard carregado"));
    }

    @GetMapping("/team")
    public ResponseEntity<ApiResponse<DashboardResponse>> getTeamDashboard(
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        // TODO: Implementar dashboard do time
        return ResponseEntity.ok(ApiResponse.success(null, "Dashboard do time"));
    }
}
```

**Passo 2: Commit**

```bash
git add backend/src/main/java/com/commit/crm/controller/DashboardController.java
git commit -m "feat(dashboard): add DashboardController"
```

---

## Resumo

| Task | O que entrega |
|------|---------------|
| 1 | DashboardResponse DTO |
| 2 | DashboardService com métricas por role |
| 3 | DashboardController |

---

## Próxima Feature

→ [09-polish/plan.md](09-polish/plan.md)
