package com.commit.crm.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record LeadListResponse(
        List<LeadResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {}
