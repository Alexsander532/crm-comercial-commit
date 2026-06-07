package com.commit.crm.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LeadTest {

    @Test
    void shouldCreateLeadWithBuilder() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .name("Teste")
                .email("teste@teste.com")
                .passwordHash("hash")
                .role(UserRole.AQUISICAO)
                .build();

        Lead lead = Lead.builder()
                .companyName("Empresa Teste")
                .segment(LeadSegment.TECNOLOGIA)
                .createdBy(user)
                .build();

        assertNotNull(lead);
        assertEquals("Empresa Teste", lead.getCompanyName());
        assertEquals(LeadSegment.TECNOLOGIA, lead.getSegment());
        assertEquals(LeadStatus.NOVO, lead.getStatus());
        assertNull(lead.getAddress());
        assertNull(lead.getAssignedTo());
    }

    @Test
    void shouldDefaultStatusToNovo() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .name("Teste")
                .email("teste@teste.com")
                .passwordHash("hash")
                .role(UserRole.AQUISICAO)
                .build();

        Lead lead = Lead.builder()
                .companyName("Empresa")
                .segment(LeadSegment.FINANCAS)
                .createdBy(user)
                .build();

        assertEquals(LeadStatus.NOVO, lead.getStatus());
    }

    @Test
    void shouldAcceptAllSegments() {
        assertDoesNotThrow(() -> LeadSegment.valueOf("TECNOLOGIA"));
        assertDoesNotThrow(() -> LeadSegment.valueOf("FINANCAS"));
        assertDoesNotThrow(() -> LeadSegment.valueOf("SAUDE"));
        assertDoesNotThrow(() -> LeadSegment.valueOf("EDUCACAO"));
        assertDoesNotThrow(() -> LeadSegment.valueOf("VAREJO"));
        assertDoesNotThrow(() -> LeadSegment.valueOf("OUTRO"));
    }

    @Test
    void shouldAcceptAllStatuses() {
        assertDoesNotThrow(() -> LeadStatus.valueOf("NOVO"));
        assertDoesNotThrow(() -> LeadStatus.valueOf("CONTATO"));
        assertDoesNotThrow(() -> LeadStatus.valueOf("NEGOCIACAO"));
        assertDoesNotThrow(() -> LeadStatus.valueOf("GANHO"));
        assertDoesNotThrow(() -> LeadStatus.valueOf("PERDIDO"));
        assertDoesNotThrow(() -> LeadStatus.valueOf("ARQUIVADO"));
    }
}
