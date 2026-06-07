package com.commit.crm.repository;

import com.commit.crm.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    List<Task> findByLeadIdOrderByCreatedAtDesc(UUID leadId);

    List<Task> findByAssignedToIdOrderByDueDateAsc(UUID userId);

    long countByAssignedToIdAndStatus(UUID userId, String status);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignedTo.id = :userId AND t.status = 'PENDENTE' AND t.dueDate < CURRENT_TIMESTAMP")
    long countOverdueByUserId(@Param("userId") UUID userId);
}
