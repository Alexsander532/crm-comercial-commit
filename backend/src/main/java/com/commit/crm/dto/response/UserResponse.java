package com.commit.crm.dto.response;

import com.commit.crm.model.UserRole;
import lombok.Builder;

import java.util.UUID;

@Builder
public record UserResponse(
        UUID id,
        String name,
        String email,
        UserRole role
) {}
