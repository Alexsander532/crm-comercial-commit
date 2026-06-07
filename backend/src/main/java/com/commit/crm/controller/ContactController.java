package com.commit.crm.controller;

import com.commit.crm.dto.request.ContactRequest;
import com.commit.crm.dto.response.ApiResponse;
import com.commit.crm.dto.response.ContactResponse;
import com.commit.crm.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/leads/{leadId}/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ContactResponse>>> list(
            @PathVariable UUID leadId
    ) {
        List<ContactResponse> contacts = contactService.getContacts(leadId);
        return ResponseEntity.ok(ApiResponse.success(contacts, "Contatos do lead"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ContactResponse>> create(
            @PathVariable UUID leadId,
            @Valid @RequestBody ContactRequest request
    ) {
        ContactResponse response = contactService.create(leadId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Contato adicionado"));
    }

    @DeleteMapping("/{contactId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable UUID leadId,
            @PathVariable UUID contactId
    ) {
        contactService.delete(leadId, contactId);
        return ResponseEntity.ok(ApiResponse.success(null, "Contato removido"));
    }

    @PatchMapping("/{contactId}/main")
    public ResponseEntity<ApiResponse<ContactResponse>> setAsMain(
            @PathVariable UUID leadId,
            @PathVariable UUID contactId
    ) {
        ContactResponse response = contactService.setAsMain(leadId, contactId);
        return ResponseEntity.ok(ApiResponse.success(response, "Contato principal atualizado"));
    }
}
