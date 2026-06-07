package com.commit.crm.service;

import com.commit.crm.dto.response.DashboardResponse;
import com.commit.crm.model.*;
import com.commit.crm.repository.LeadRepository;
import com.commit.crm.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final LeadRepository leadRepository;
    private final TaskRepository taskRepository;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(User user) {
        List<Lead> leads = switch (user.getRole()) {
            case DIRETOR, GERENTE_AQUISICAO, GERENTE_PROSPECCAO -> leadRepository.findAll();
            case AQUISICAO -> leadRepository.findByCreatedById(user.getId(), org.springframework.data.domain.Pageable.unpaged()).getContent();
            case PROSPECCAO -> leadRepository.findByAssignedToId(user.getId());
        };

        long totalLeads = leads.size();
        long leadsThisMonth = leads.stream()
                .filter(l -> l.getCreatedAt() != null && l.getCreatedAt().isAfter(LocalDateTime.now().minusDays(30)))
                .count();

        Map<String, Long> pipelineChart = leads.stream()
                .collect(Collectors.groupingBy(
                        l -> l.getStatus().name(),
                        Collectors.counting()
                ));

        long tasksPending = taskRepository.countByAssignedToIdAndStatus(user.getId(), "PENDENTE");
        long tasksOverdue = taskRepository.countOverdueByUserId(user.getId());

        List<DashboardResponse.TaskSummary> recentTasks = taskRepository
                .findByAssignedToIdOrderByDueDateAsc(user.getId()).stream()
                .filter(t -> t.getStatus() == TaskStatus.PENDENTE)
                .limit(5)
                .map(t -> DashboardResponse.TaskSummary.builder()
                        .title(t.getTitle())
                        .status(t.getCompletionStatus())
                        .leadName(t.getLead().getCompanyName())
                        .daysOverdue(t.getDaysOverdue())
                        .build())
                .toList();

        return DashboardResponse.builder()
                .summary(new DashboardResponse.Summary(totalLeads, leadsThisMonth, tasksPending, tasksOverdue))
                .pipelineChart(pipelineChart)
                .recentTasks(recentTasks)
                .build();
    }
}
