package com.commit.crm.dto.response;

import com.commit.crm.model.TaskPriority;
import com.commit.crm.model.TaskStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record TaskResponse(
        UUID id,
        UUID leadId,
        String title,
        String description,
        TaskPriority priority,
        TaskStatus status,
        String completionStatus,
        long daysOverdue,
        String assignedToName,
        String createdByName,
        LocalDateTime dueDate,
        LocalDateTime completedAt,
        LocalDateTime createdAt
) {}
