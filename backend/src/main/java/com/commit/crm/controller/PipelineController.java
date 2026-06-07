package com.commit.crm.controller;

import com.commit.crm.dto.request.AssignRequest;
import com.commit.crm.dto.request.StatusUpdateRequest;
import com.commit.crm.dto.response.ApiResponse;
import com.commit.crm.dto.response.LeadResponse;
import com.commit.crm.model.Lead;
import com.commit.crm.model.LeadStatus;
import com.commit.crm.model.User;
import com.commit.crm.service.LeadService;
import com.commit.crm.service.PipelineService;
import com.commit.crm.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/leads")
@RequiredArgsConstructor
public class PipelineController {

    private final PipelineService pipelineService;
    private final LeadService leadService;
    private final UserService userService;

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<LeadResponse>> moveStatus(
            @PathVariable UUID id,
            @Valid @RequestBody StatusUpdateRequest request,
            @AuthenticationPrincipal String userId
    ) {
        User user = userService.findById(UUID.fromString(userId));
        LeadStatus newStatus = LeadStatus.valueOf(request.newStatus());
        Lead lead = pipelineService.moveStatus(id, newStatus, user);
        LeadResponse response = toResponse(lead);
        return ResponseEntity.ok(ApiResponse.success(response, "Status atualizado"));
    }

    @PatchMapping("/{id}/assign")
    public ResponseEntity<ApiResponse<LeadResponse>> assign(
            @PathVariable UUID id,
            @Valid @RequestBody AssignRequest request,
            @AuthenticationPrincipal String userId
    ) {
        User user = userService.findById(UUID.fromString(userId));
        LeadResponse response = leadService.assignTo(id, request.userId(), user);
        return ResponseEntity.ok(ApiResponse.success(response, "Lead atribuído"));
    }

    @GetMapping("/kanban")
    public ResponseEntity<ApiResponse<Map<String, List<LeadResponse>>>> kanban(
            @AuthenticationPrincipal String userId
    ) {
        User user = userService.findById(UUID.fromString(userId));
        List<Lead> leads = pipelineService.getKanbanView(user);

        Map<String, List<LeadResponse>> grouped = leads.stream()
                .collect(Collectors.groupingBy(
                        l -> l.getStatus().name(),
                        Collectors.mapping(this::toResponse, Collectors.toList())
                ));

        return ResponseEntity.ok(ApiResponse.success(grouped, "Kanban carregado"));
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
}
