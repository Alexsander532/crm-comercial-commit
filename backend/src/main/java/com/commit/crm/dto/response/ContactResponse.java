package com.commit.crm.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ContactResponse(
        UUID id,
        UUID leadId,
        String name,
        String role,
        String phone,
        String email,
        String whatsapp,
        Boolean isMain,
        String notes,
        LocalDateTime createdAt
) {}
