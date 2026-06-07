package com.commit.crm.dto.request;

import jakarta.validation.constraints.NotBlank;

public record StatusUpdateRequest(
        @NotBlank(message = "Novo status é obrigatório")
        String newStatus,

        String reason
) {}
