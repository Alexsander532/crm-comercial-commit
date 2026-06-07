package com.commit.crm.dto.request;

import com.commit.crm.model.LeadSegment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LeadRequest(
        @NotBlank(message = "Nome da empresa é obrigatório")
        String companyName,

        String site,

        String instagram,

        String whatsapp,

        String address,

        @NotNull(message = "Segmento é obrigatório")
        LeadSegment segment,

        String notes
) {}
