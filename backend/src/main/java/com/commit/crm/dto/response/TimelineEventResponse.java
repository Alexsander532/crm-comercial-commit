package com.commit.crm.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record TimelineEventResponse(
        UUID id,
        String type,
        String userName,
        Object metadata,
        LocalDateTime createdAt
) {}
