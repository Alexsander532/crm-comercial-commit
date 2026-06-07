package com.commit.crm.service;

import com.commit.crm.dto.request.LeadRequest;
import com.commit.crm.dto.response.LeadListResponse;
import com.commit.crm.dto.response.LeadResponse;
import com.commit.crm.model.Lead;
import com.commit.crm.model.LeadStatus;
import com.commit.crm.model.User;
import com.commit.crm.model.UserRole;
import com.commit.crm.repository.LeadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LeadService {

    private final LeadRepository leadRepository;
    private final UserService userService;

    @Transactional
    public LeadResponse create(LeadRequest request, User creator) {
        validateCanCreate(creator);

        Lead lead = Lead.builder()
                .companyName(request.companyName())
                .site(request.site())
                .instagram(request.instagram())
                .whatsapp(request.whatsapp())
                .address(request.address())
                .segment(request.segment())
                .notes(request.notes())
                .status(LeadStatus.NOVO)
                .createdBy(creator)
                .build();

        Lead saved = leadRepository.save(lead);
        return toResponse(saved);
    }

    @Transactional
    public LeadResponse update(UUID id, LeadRequest request, User currentUser) {
        Lead lead = findLeadById(id);
        validateCanEdit(lead, currentUser);

        lead.setCompanyName(request.companyName());
        lead.setSite(request.site());
        lead.setInstagram(request.instagram());
        lead.setWhatsapp(request.whatsapp());
        lead.setAddress(request.address());
        lead.setSegment(request.segment());
        lead.setNotes(request.notes());

        Lead saved = leadRepository.save(lead);
        return toResponse(saved);
    }

    @Transactional
    public void archive(UUID id, User currentUser) {
        Lead lead = findLeadById(id);
        validateCanArchive(lead, currentUser);
        lead.setStatus(LeadStatus.ARQUIVADO);
        leadRepository.save(lead);
    }

    @Transactional
    public LeadResponse assignTo(UUID leadId, UUID userId, User currentUser) {
        Lead lead = findLeadById(leadId);
        validateCanAssign(currentUser);

        User assignedUser = userService.findById(userId);
        lead.setAssignedTo(assignedUser);
        Lead saved = leadRepository.save(lead);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public LeadResponse findById(UUID id, User currentUser) {
        Lead lead = findLeadById(id);
        validateCanView(lead, currentUser);
        return toResponse(lead);
    }

    @Transactional(readOnly = true)
    public LeadListResponse findAll(Pageable pageable, User currentUser) {
        Page<Lead> page = findAllByRole(pageable, currentUser);
        return toListResponse(page);
    }

    @Transactional(readOnly = true)
    public LeadListResponse search(String query, Pageable pageable, User currentUser) {
        Page<Lead> page = leadRepository.search(query, pageable);
        return toListResponse(page);
    }

    // ─── Validações de permissão ───

    private void validateCanCreate(User user) {
        if (user.getRole() == UserRole.PROSPECCAO || user.getRole() == UserRole.GERENTE_PROSPECCAO) {
            throw new SecurityException("Apenas aquisição pode criar leads");
        }
    }

    private void validateCanEdit(Lead lead, User user) {
        if (user.getRole() == UserRole.DIRETOR) return;
        if (user.getRole().name().startsWith("GERENTE")) {
            // Gerente pode editar leads do time
            return;
        }
        if (lead.getCreatedBy().getId().equals(user.getId())) return;
        throw new SecurityException("Você não tem permissão para editar este lead");
    }

    private void validateCanArchive(Lead lead, User user) {
        if (user.getRole() == UserRole.DIRETOR) return;
        if (user.getRole() == UserRole.GERENTE_PROSPECCAO) return;
        if (lead.getAssignedTo() != null && lead.getAssignedTo().getId().equals(user.getId())) return;
        throw new SecurityException("Você não tem permissão para arquivar este lead");
    }

    private void validateCanAssign(User user) {
        if (user.getRole() == UserRole.DIRETOR) return;
        if (user.getRole() == UserRole.GERENTE_PROSPECCAO) return;
        throw new SecurityException("Apenas diretor ou gerente de prospecção pode atribuir leads");
    }

    private void validateCanView(Lead lead, User user) {
        if (user.getRole() == UserRole.DIRETOR) return;
        if (user.getRole() == UserRole.GERENTE_AQUISICAO) return; // simplified
        if (user.getRole() == UserRole.GERENTE_PROSPECCAO) return; // simplified
        if (user.getRole() == UserRole.AQUISICAO && lead.getCreatedBy().getId().equals(user.getId())) return;
        if (user.getRole() == UserRole.PROSPECCAO && lead.getAssignedTo() != null
                && lead.getAssignedTo().getId().equals(user.getId())) return;
        throw new SecurityException("Você não tem permissão para visualizar este lead");
    }

    private Lead findLeadById(UUID id) {
        return leadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lead não encontrado: " + id));
    }

    private Page<Lead> findAllByRole(Pageable pageable, User user) {
        return switch (user.getRole()) {
            case DIRETOR, GERENTE_AQUISICAO, GERENTE_PROSPECCAO -> leadRepository.findAll(pageable);
            case AQUISICAO -> leadRepository.findByCreatedById(user.getId(), pageable);
            case PROSPECCAO -> leadRepository.findByAssignedToId(user.getId(), pageable);
        };
    }

    private LeadResponse toResponse(Lead lead) {
        return LeadResponse.builder()
                .id(lead.getId())
                .companyName(lead.getCompanyName())
                .site(lead.getSite())
                .instagram(lead.getInstagram())
                .whatsapp(lead.getWhatsapp())
                .address(lead.getAddress())
                .segment(lead.getSegment())
                .notes(lead.getNotes())
                .status(lead.getStatus())
                .createdByName(lead.getCreatedBy().getName())
                .assignedToName(lead.getAssignedTo() != null ? lead.getAssignedTo().getName() : null)
                .createdAt(lead.getCreatedAt())
                .updatedAt(lead.getUpdatedAt())
                .build();
    }

    private LeadListResponse toListResponse(Page<Lead> page) {
        return LeadListResponse.builder()
                .content(page.getContent().stream().map(this::toResponse).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }
}
