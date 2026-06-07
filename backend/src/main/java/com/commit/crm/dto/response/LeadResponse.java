package com.commit.crm.dto.response;

import com.commit.crm.model.LeadSegment;
import com.commit.crm.model.LeadStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record LeadResponse(
        UUID id,
        String companyName,
        String site,
        String instagram,
        String whatsapp,
        String address,
        LeadSegment segment,
        String notes,
        LeadStatus status,
        String createdByName,
        String assignedToName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
