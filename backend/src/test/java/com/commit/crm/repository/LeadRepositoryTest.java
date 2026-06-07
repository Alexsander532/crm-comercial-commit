package com.commit.crm.repository;

import com.commit.crm.model.Lead;
import com.commit.crm.model.LeadSegment;
import com.commit.crm.model.LeadStatus;
import com.commit.crm.model.UserRole;
import com.commit.crm.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeadRepositoryTest {

    @Mock
    private LeadRepository leadRepository;

    private User createUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .name("Teste")
                .email("teste@teste.com")
                .passwordHash("hash")
                .role(UserRole.AQUISICAO)
                .build();
    }

    private Lead createLead(User user) {
        return Lead.builder()
                .id(UUID.randomUUID())
                .companyName("Empresa Teste")
                .segment(LeadSegment.TECNOLOGIA)
                .status(LeadStatus.NOVO)
                .createdBy(user)
                .build();
    }

    @Test
    void shouldFindByStatus() {
        User user = createUser();
        Lead lead = createLead(user);
        when(leadRepository.findByStatus(LeadStatus.NOVO)).thenReturn(List.of(lead));

        List<Lead> result = leadRepository.findByStatus(LeadStatus.NOVO);

        assertEquals(1, result.size());
        assertEquals(LeadStatus.NOVO, result.get(0).getStatus());
    }

    @Test
    void shouldCountByStatus() {
        when(leadRepository.countByStatus(LeadStatus.NOVO)).thenReturn(5L);

        long count = leadRepository.countByStatus(LeadStatus.NOVO);

        assertEquals(5L, count);
    }

    @Test
    void shouldSearchByCompanyName() {
        User user = createUser();
        Lead lead = createLead(user);
        Page<Lead> page = new PageImpl<>(List.of(lead));
        when(leadRepository.search(eq("Empresa"), any(PageRequest.class))).thenReturn(page);

        Page<Lead> result = leadRepository.search("Empresa", PageRequest.of(0, 10));

        assertFalse(result.isEmpty());
        assertEquals("Empresa Teste", result.getContent().get(0).getCompanyName());
    }
}
