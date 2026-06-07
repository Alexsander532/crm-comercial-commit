package com.commit.crm.service;

import com.commit.crm.model.Lead;
import com.commit.crm.model.LeadStatus;
import com.commit.crm.model.User;
import com.commit.crm.model.UserRole;
import com.commit.crm.repository.LeadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PipelineService {

    private final LeadRepository leadRepository;

    @Transactional
    public Lead moveStatus(UUID leadId, LeadStatus newStatus, User currentUser) {
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new IllegalArgumentException("Lead não encontrado"));

        validateCanMove(lead, currentUser);
        validateTransition(lead.getStatus(), newStatus);

        lead.setStatus(newStatus);
        return leadRepository.save(lead);
    }

    public List<Lead> getKanbanView(User currentUser) {
        return leadRepository.findAll().stream()
                .filter(lead -> canView(lead, currentUser))
                .toList();
    }

    private void validateCanMove(Lead lead, User user) {
        if (user.getRole() == UserRole.DIRETOR) return;
        if (user.getRole() == UserRole.GERENTE_PROSPECCAO) return;
        if (user.getRole() == UserRole.PROSPECCAO
                && lead.getAssignedTo() != null
                && lead.getAssignedTo().getId().equals(user.getId())) return;
        throw new SecurityException("Você não tem permissão para mover este lead");
    }

    private void validateTransition(LeadStatus current, LeadStatus target) {
        if (!current.canTransitionTo(target)) {
            throw new IllegalArgumentException(
                    "Transição inválida: " + current + " → " + target
            );
        }
    }

    private boolean canView(Lead lead, User user) {
        return switch (user.getRole()) {
            case DIRETOR, GERENTE_AQUISICAO, GERENTE_PROSPECCAO -> true;
            case AQUISICAO -> lead.getCreatedBy().getId().equals(user.getId());
            case PROSPECCAO -> lead.getAssignedTo() != null
                    && lead.getAssignedTo().getId().equals(user.getId());
        };
    }
}
