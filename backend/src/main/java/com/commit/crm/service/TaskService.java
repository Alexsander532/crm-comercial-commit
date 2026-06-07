package com.commit.crm.service;

import com.commit.crm.dto.request.TaskRequest;
import com.commit.crm.dto.response.TaskResponse;
import com.commit.crm.model.*;
import com.commit.crm.repository.LeadRepository;
import com.commit.crm.repository.TaskRepository;
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

    @Transactional
    public TaskResponse create(UUID leadId, TaskRequest request, User creator) {
        validateCanCreate(creator);

        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new IllegalArgumentException("Lead não encontrado"));

        Task task = Task.builder()
                .lead(lead)
                .createdBy(creator)
                .assignedTo(User.builder().id(request.assignedToId()).build())
                .title(request.title())
                .description(request.description())
                .priority(request.priority())
                .dueDate(request.dueDate())
                .build();

        return toResponse(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse complete(UUID taskId, User user) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Tarefa não encontrada"));

        if (task.getAssignedTo() != null && !task.getAssignedTo().getId().equals(user.getId())) {
            throw new SecurityException("Apenas o usuário atribuído pode concluir esta tarefa");
        }

        task.setStatus(TaskStatus.CONCLUIDA);
        task.setCompletedAt(LocalDateTime.now());
        return toResponse(taskRepository.save(task));
    }

    @Transactional
    public void cancel(UUID taskId, User user) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Tarefa não encontrada"));

        if (user.getRole() != UserRole.DIRETOR
                && !task.getCreatedBy().getId().equals(user.getId())) {
            throw new SecurityException("Apenas o criador ou diretor pode cancelar tarefas");
        }

        task.setStatus(TaskStatus.CANCELADA);
        taskRepository.save(task);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getByLead(UUID leadId) {
        return taskRepository.findByLeadIdOrderByCreatedAtDesc(leadId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getMyTasks(UUID userId) {
        return taskRepository.findByAssignedToIdOrderByDueDateAsc(userId).stream()
                .map(this::toResponse).toList();
    }

    private void validateCanCreate(User user) {
        if (user.getRole() == UserRole.AQUISICAO || user.getRole() == UserRole.PROSPECCAO) {
            throw new SecurityException("Apenas gerentes e diretor podem criar tarefas");
        }
    }

    private TaskResponse toResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .leadId(task.getLead().getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .priority(task.getPriority())
                .status(task.getStatus())
                .completionStatus(task.getCompletionStatus())
                .daysOverdue(task.getDaysOverdue())
                .assignedToName(task.getAssignedTo() != null ? task.getAssignedTo().getName() : null)
                .createdByName(task.getCreatedBy().getName())
                .dueDate(task.getDueDate())
                .completedAt(task.getCompletedAt())
                .createdAt(task.getCreatedAt())
                .build();
    }
}
