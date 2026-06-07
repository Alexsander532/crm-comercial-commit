package com.commit.crm.dto;

import com.commit.crm.dto.request.LeadRequest;
import com.commit.crm.dto.response.LeadListResponse;
import com.commit.crm.dto.response.LeadResponse;
import com.commit.crm.model.LeadSegment;
import com.commit.crm.model.LeadStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LeadDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void shouldValidateLeadRequest() {
        LeadRequest request = new LeadRequest("", null, null, null, null, null, null);
        Set<ConstraintViolation<LeadRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldAcceptValidLeadRequest() {
        LeadRequest request = new LeadRequest("Empresa", null, null, null, null, LeadSegment.TECNOLOGIA, null);
        Set<ConstraintViolation<LeadRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldBuildLeadResponse() {
        LeadResponse response = LeadResponse.builder()
                .id(UUID.randomUUID())
                .companyName("Empresa")
                .segment(LeadSegment.TECNOLOGIA)
                .status(LeadStatus.NOVO)
                .createdByName("Criador")
                .createdAt(LocalDateTime.now())
                .build();

        assertEquals("Empresa", response.companyName());
        assertEquals(LeadSegment.TECNOLOGIA, response.segment());
        assertEquals(LeadStatus.NOVO, response.status());
    }

    @Test
    void shouldBuildLeadListResponse() {
        LeadResponse lead = LeadResponse.builder()
                .id(UUID.randomUUID())
                .companyName("Empresa")
                .segment(LeadSegment.TECNOLOGIA)
                .status(LeadStatus.NOVO)
                .build();

        LeadListResponse list = LeadListResponse.builder()
                .content(List.of(lead))
                .page(0)
                .size(10)
                .totalElements(1)
                .totalPages(1)
                .build();

        assertEquals(1, list.content().size());
        assertEquals(0, list.page());
        assertEquals(10, list.size());
    }
}
