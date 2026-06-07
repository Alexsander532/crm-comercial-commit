package com.commit.crm.service;

import com.commit.crm.dto.request.ContactRequest;
import com.commit.crm.dto.response.ContactResponse;
import com.commit.crm.model.Contact;
import com.commit.crm.model.Lead;
import com.commit.crm.repository.ContactRepository;
import com.commit.crm.repository.LeadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactRepository contactRepository;
    private final LeadRepository leadRepository;

    @Transactional(readOnly = true)
    public List<ContactResponse> getContacts(UUID leadId) {
        return contactRepository.findByLeadIdOrderByCreatedAtAsc(leadId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional
    public ContactResponse create(UUID leadId, ContactRequest request) {
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new IllegalArgumentException("Lead não encontrado"));

        boolean isFirst = contactRepository.countByLeadId(leadId) == 0;

        Contact contact = Contact.builder()
                .lead(lead)
                .name(request.name())
                .role(request.role())
                .phone(request.phone())
                .email(request.email())
                .whatsapp(request.whatsapp())
                .notes(request.notes())
                .isMain(isFirst)
                .build();

        return toResponse(contactRepository.save(contact));
    }

    @Transactional
    public void delete(UUID leadId, UUID contactId) {
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new IllegalArgumentException("Contato não encontrado"));
        if (!contact.getLead().getId().equals(leadId)) {
            throw new IllegalArgumentException("Contato não pertence a este lead");
        }
        contactRepository.delete(contact);
    }

    @Transactional
    public ContactResponse setAsMain(UUID leadId, UUID contactId) {
        // Desmarca todos como principal
        List<Contact> contacts = contactRepository.findByLeadIdOrderByCreatedAtAsc(leadId);
        contacts.forEach(c -> c.setIsMain(false));
        contactRepository.saveAll(contacts);

        // Marca o selecionado
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new IllegalArgumentException("Contato não encontrado"));
        contact.setIsMain(true);
        return toResponse(contactRepository.save(contact));
    }

    private ContactResponse toResponse(Contact contact) {
        return ContactResponse.builder()
                .id(contact.getId())
                .leadId(contact.getLead().getId())
                .name(contact.getName())
                .role(contact.getRole())
                .phone(contact.getPhone())
                .email(contact.getEmail())
                .whatsapp(contact.getWhatsapp())
                .isMain(contact.getIsMain())
                .notes(contact.getNotes())
                .createdAt(contact.getCreatedAt())
                .build();
    }
}
