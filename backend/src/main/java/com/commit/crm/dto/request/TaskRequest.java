package com.commit.crm.dto.request;

import com.commit.crm.model.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record TaskRequest(
        @NotBlank(message = "Título é obrigatório")
        String title,

        String description,

        @NotNull
        TaskPriority priority,

        @NotNull
        UUID assignedToId,

        LocalDateTime dueDate
) {}
