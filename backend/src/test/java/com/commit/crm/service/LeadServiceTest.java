package com.commit.crm.service;

import com.commit.crm.dto.request.LeadRequest;
import com.commit.crm.dto.response.LeadListResponse;
import com.commit.crm.dto.response.LeadResponse;
import com.commit.crm.model.*;
import com.commit.crm.repository.LeadRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
class LeadServiceTest {

    @Mock
    private LeadRepository leadRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private LeadService leadService;

    private User aquisicao() {
        return User.builder().id(UUID.randomUUID()).name("Aquisição").email("a@c.com")
                .passwordHash("h").role(UserRole.AQUISICAO).build();
    }

    private User prospeccao() {
        return User.builder().id(UUID.randomUUID()).name("Prospecção").email("p@c.com")
                .passwordHash("h").role(UserRole.PROSPECCAO).build();
    }

    private User diretor() {
        return User.builder().id(UUID.randomUUID()).name("Diretor").email("d@c.com")
                .passwordHash("h").role(UserRole.DIRETOR).build();
    }

    private Lead lead(User creator) {
        return Lead.builder()
                .id(UUID.randomUUID())
                .companyName("Empresa")
                .segment(LeadSegment.TECNOLOGIA)
                .status(LeadStatus.NOVO)
                .createdBy(creator)
                .build();
    }

    @Test
    void shouldCreateLead() {
        User creator = aquisicao();
        Lead saved = lead(creator);
        when(leadRepository.save(any(Lead.class))).thenReturn(saved);

        LeadRequest request = new LeadRequest("Empresa", null, null, null, null, LeadSegment.TECNOLOGIA, null);
        LeadResponse response = leadService.create(request, creator);

        assertEquals("Empresa", response.companyName());
        assertEquals(LeadSegment.TECNOLOGIA, response.segment());
    }

    @Test
    void shouldThrowWhenProspeccaoCreatesLead() {
        User user = prospeccao();
        LeadRequest request = new LeadRequest("Empresa", null, null, null, null, LeadSegment.TECNOLOGIA, null);

        assertThrows(SecurityException.class, () -> leadService.create(request, user));
    }

    @Test
    void shouldUpdateLead() {
        User user = aquisicao();
        Lead existing = lead(user);
        when(leadRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(leadRepository.save(any(Lead.class))).thenReturn(existing);

        LeadRequest request = new LeadRequest("Novo Nome", "http://site.com", null, null, null, LeadSegment.FINANCAS, null);
        LeadResponse response = leadService.update(existing.getId(), request, user);

        assertEquals("Novo Nome", response.companyName());
    }

    @Test
    void shouldArchiveLead() {
        User user = diretor();
        Lead existing = lead(aquisicao());
        when(leadRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(leadRepository.save(any(Lead.class))).thenReturn(existing);

        leadService.archive(existing.getId(), user);

        assertEquals(LeadStatus.ARQUIVADO, existing.getStatus());
    }

    @Test
    void shouldAssignLead() {
        User diretor = diretor();
        Lead existing = lead(aquisicao());
        User assignedUser = prospeccao();
        when(leadRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(userService.findById(assignedUser.getId())).thenReturn(assignedUser);
        when(leadRepository.save(any(Lead.class))).thenReturn(existing);

        LeadResponse response = leadService.assignTo(existing.getId(), assignedUser.getId(), diretor);

        assertNotNull(response);
        verify(leadRepository).save(any(Lead.class));
    }

    @Test
    void shouldFindLeadsByRole() {
        User user = aquisicao();
        Lead l = lead(user);
        Page<Lead> page = new PageImpl<>(List.of(l));
        when(leadRepository.findByCreatedById(user.getId(), PageRequest.of(0, 10))).thenReturn(page);

        LeadListResponse result = leadService.findAll(PageRequest.of(0, 10), user);

        assertEquals(1, result.content().size());
        verify(leadRepository).findByCreatedById(user.getId(), PageRequest.of(0, 10));
    }

    @Test
    void shouldSearchLeads() {
        User user = diretor();
        Lead l = lead(aquisicao());
        Page<Lead> page = new PageImpl<>(List.of(l));
        when(leadRepository.search(eq("Empresa"), any(PageRequest.class))).thenReturn(page);

        LeadListResponse result = leadService.search("Empresa", PageRequest.of(0, 10), user);

        assertEquals(1, result.content().size());
    }
}
