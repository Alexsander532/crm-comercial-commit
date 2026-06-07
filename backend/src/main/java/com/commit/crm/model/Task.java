package com.commit.crm.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tasks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id", nullable = false)
    private Lead lead;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    @Builder.Default
    private TaskPriority priority = TaskPriority.MEDIA;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    @Builder.Default
    private TaskStatus status = TaskStatus.PENDENTE;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public String getCompletionStatus() {
        if (status != TaskStatus.PENDENTE) return null;
        if (dueDate == null) return "SEM_PRAZO";
        if (completedAt != null) {
            return completedAt.isBefore(dueDate) || completedAt.isEqual(dueDate)
                    ? "NO_PRAZO" : "COM_ATRASO";
        }
        return dueDate.isBefore(LocalDateTime.now()) ? "VENCIDA" : "NO_PRAZO";
    }

    public long getDaysOverdue() {
        if (dueDate == null || completedAt == null) return 0;
        return java.time.Duration.between(dueDate, completedAt).toDays();
    }
}
