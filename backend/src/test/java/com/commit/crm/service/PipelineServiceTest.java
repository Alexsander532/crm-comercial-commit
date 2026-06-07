package com.commit.crm.service;

import com.commit.crm.model.*;
import com.commit.crm.repository.LeadRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PipelineServiceTest {

    @Mock
    private LeadRepository leadRepository;

    @InjectMocks
    private PipelineService pipelineService;

    private User diretor() {
        return User.builder().id(UUID.randomUUID()).name("Diretor").email("d@c.com")
                .passwordHash("h").role(UserRole.DIRETOR).build();
    }

    @Test
    void shouldMoveNovoToContato() {
        User user = diretor();
        Lead lead = Lead.builder().id(UUID.randomUUID()).companyName("Teste")
                .segment(LeadSegment.TECNOLOGIA).status(LeadStatus.NOVO).createdBy(user).build();
        when(leadRepository.findById(lead.getId())).thenReturn(Optional.of(lead));
        when(leadRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Lead result = pipelineService.moveStatus(lead.getId(), LeadStatus.CONTATO, user);

        assertEquals(LeadStatus.CONTATO, result.getStatus());
    }

    @Test
    void shouldThrowOnInvalidTransition() {
        User user = diretor();
        Lead lead = Lead.builder().id(UUID.randomUUID()).companyName("Teste")
                .segment(LeadSegment.TECNOLOGIA).status(LeadStatus.NOVO).createdBy(user).build();
        when(leadRepository.findById(lead.getId())).thenReturn(Optional.of(lead));

        assertThrows(IllegalArgumentException.class,
                () -> pipelineService.moveStatus(lead.getId(), LeadStatus.GANHO, user));
    }
}
