package com.commit.crm.controller;

import com.commit.crm.dto.request.LeadRequest;
import com.commit.crm.dto.response.ApiResponse;
import com.commit.crm.dto.response.LeadListResponse;
import com.commit.crm.dto.response.LeadResponse;
import com.commit.crm.model.User;
import com.commit.crm.service.LeadService;
import com.commit.crm.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/leads")
@RequiredArgsConstructor
public class LeadController {

    private final LeadService leadService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<LeadResponse>> create(
            @Valid @RequestBody LeadRequest request,
            @AuthenticationPrincipal String userId
    ) {
        User creator = userService.findById(UUID.fromString(userId));
        LeadResponse response = leadService.create(request, creator);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Lead criado com sucesso"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<LeadListResponse>> list(
            Pageable pageable,
            @AuthenticationPrincipal String userId
    ) {
        User user = userService.findById(UUID.fromString(userId));
        LeadListResponse response = leadService.findAll(pageable, user);
        return ResponseEntity.ok(ApiResponse.success(response, "Lista de leads"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LeadResponse>> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal String userId
    ) {
        User user = userService.findById(UUID.fromString(userId));
        LeadResponse response = leadService.findById(id, user);
        return ResponseEntity.ok(ApiResponse.success(response, "Lead encontrado"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LeadResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody LeadRequest request,
            @AuthenticationPrincipal String userId
    ) {
        User user = userService.findById(UUID.fromString(userId));
        LeadResponse response = leadService.update(id, request, user);
        return ResponseEntity.ok(ApiResponse.success(response, "Lead atualizado"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> archive(
            @PathVariable UUID id,
            @AuthenticationPrincipal String userId
    ) {
        User user = userService.findById(UUID.fromString(userId));
        leadService.archive(id, user);
        return ResponseEntity.ok(ApiResponse.success(null, "Lead arquivado"));
    }

    @PutMapping("/{id}/assign")
    public ResponseEntity<ApiResponse<LeadResponse>> assign(
            @PathVariable UUID id,
            @RequestParam UUID userId,
            @AuthenticationPrincipal String currentUserId
    ) {
        User currentUser = userService.findById(UUID.fromString(currentUserId));
        LeadResponse response = leadService.assignTo(id, userId, currentUser);
        return ResponseEntity.ok(ApiResponse.success(response, "Lead atribuído"));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<LeadListResponse>> search(
            @RequestParam String q,
            Pageable pageable,
            @AuthenticationPrincipal String userId
    ) {
        User user = userService.findById(UUID.fromString(userId));
        LeadListResponse response = leadService.search(q, pageable, user);
        return ResponseEntity.ok(ApiResponse.success(response, "Resultado da busca"));
    }
}
