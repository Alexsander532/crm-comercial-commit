package com.commit.crm.repository;

import com.commit.crm.model.Lead;
import com.commit.crm.model.LeadStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LeadRepository extends JpaRepository<Lead, UUID> {

    List<Lead> findByStatus(LeadStatus status);

    List<Lead> findBySegment(String segment);

    List<Lead> findByAssignedToId(UUID userId);

    long countByStatus(LeadStatus status);

    @Query("SELECT l FROM Lead l WHERE " +
           "LOWER(l.companyName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(l.site) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(l.instagram) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(l.notes) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Lead> search(@Param("search") String search, Pageable pageable);

    List<Lead> findByAssignedToIdAndStatus(UUID userId, LeadStatus status);

    Page<Lead> findByCreatedById(UUID userId, Pageable pageable);

    Page<Lead> findByAssignedToId(UUID userId, Pageable pageable);
}
