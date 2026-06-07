package com.commit.crm.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ContactRequest(
        @NotBlank(message = "Nome do contato é obrigatório")
        String name,

        String role,
        String phone,
        String email,
        String whatsapp,
        String notes
) {}
