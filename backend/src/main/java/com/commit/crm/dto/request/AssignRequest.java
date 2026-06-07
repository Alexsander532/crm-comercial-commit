package com.commit.crm.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AssignRequest(
        @NotNull(message = "ID do usuário é obrigatório")
        UUID userId
) {}
