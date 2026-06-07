package com.commit.crm.service;

import com.commit.crm.dto.response.TimelineEventResponse;
import com.commit.crm.model.*;
import com.commit.crm.repository.TimelineEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TimelineService {

    private final TimelineEventRepository repository;

    @Transactional
    public void recordCreated(Lead lead, User user) {
        save(lead, user, TimelineEventType.CREATED, null);
    }

    @Transactional
    public void recordStatusChanged(Lead lead, User user, LeadStatus from, LeadStatus to) {
        save(lead, user, TimelineEventType.STATUS_CHANGED, Map.of("from", from.name(), "to", to.name()));
    }

    @Transactional
    public void recordFieldUpdated(Lead lead, User user, String field, Object oldVal, Object newVal) {
        save(lead, user, TimelineEventType.FIELD_UPDATED, Map.of("field", field, "oldValue", oldVal, "newValue", newVal));
    }

    @Transactional
    public void recordInteraction(Lead lead, User user, String type, String description) {
        save(lead, user, TimelineEventType.INTERACTION, Map.of("type", type, "description", description));
    }

    @Transactional
    public void recordTaskCreated(Lead lead, User user, Task task) {
        save(lead, user, TimelineEventType.TASK_CREATED, Map.of("taskId", task.getId().toString(), "title", task.getTitle()));
    }

    @Transactional
    public void recordTaskCompleted(Lead lead, User user, Task task) {
        save(lead, user, TimelineEventType.TASK_COMPLETED, Map.of("taskId", task.getId().toString(), "title", task.getTitle(), "completionStatus", task.getCompletionStatus()));
    }

    @Transactional
    public void recordAssigned(Lead lead, User user, UUID fromUserId, UUID toUserId) {
        save(lead, user, TimelineEventType.ASSIGNED, Map.of("from", fromUserId != null ? fromUserId.toString() : null, "to", toUserId.toString()));
    }

    @Transactional
    public void recordContactAdded(Lead lead, User user, Contact contact) {
        save(lead, user, TimelineEventType.CONTACT_ADDED, Map.of("contactId", contact.getId().toString(), "name", contact.getName()));
    }

    @Transactional(readOnly = true)
    public List<TimelineEventResponse> getTimeline(UUID leadId) {
        return repository.findByLeadIdOrderByCreatedAtDesc(leadId).stream()
                .map(e -> TimelineEventResponse.builder()
                        .id(e.getId())
                        .type(e.getType().name())
                        .userName(e.getUser().getName())
                        .metadata(e.getMetadata())
                        .createdAt(e.getCreatedAt())
                        .build())
                .toList();
    }

    private void save(Lead lead, User user, TimelineEventType type, Object metadata) {
        repository.save(TimelineEvent.builder().lead(lead).user(user).type(type).metadata(metadata).build());
    }
}
