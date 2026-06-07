package com.commit.crm.dto.response;

import lombok.Builder;

@Builder
public record LoginResponse(
        String token,
        long expiresIn,
        UserResponse user
) {}
