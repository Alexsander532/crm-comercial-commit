package com.commit.crm.repository;

import com.commit.crm.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContactRepository extends JpaRepository<Contact, UUID> {

    List<Contact> findByLeadIdOrderByCreatedAtAsc(UUID leadId);

    Optional<Contact> findByLeadIdAndIsMainTrue(UUID leadId);

    long countByLeadId(UUID leadId);
}
