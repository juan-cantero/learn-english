package com.learntv.api.classroom.adapter.out.persistence;

import com.learntv.api.classroom.domain.model.Assignment;
import com.learntv.api.classroom.domain.model.AssignmentId;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "assignments")
public class AssignmentJpaEntity {

    @Id
    private UUID id;

    @Column(name = "classroom_id", nullable = false)
    private UUID classroomId;

    @Column(name = "episode_id", nullable = false)
    private UUID episodeId;

    private String title;

    private String instructions;

    @Column(name = "due_date")
    private Instant dueDate;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected AssignmentJpaEntity() {
    }

    public static AssignmentJpaEntity fromDomain(Assignment assignment) {
        AssignmentJpaEntity entity = new AssignmentJpaEntity();
        entity.id = assignment.getId().value();
        entity.classroomId = assignment.getClassroomId();
        entity.episodeId = assignment.getEpisodeId();
        entity.title = assignment.getTitle();
        entity.instructions = assignment.getInstructions();
        entity.dueDate = assignment.getDueDate();
        entity.createdAt = assignment.getCreatedAt();
        entity.updatedAt = assignment.getUpdatedAt();
        return entity;
    }

    public Assignment toDomain() {
        return Assignment.builder()
                .id(AssignmentId.of(id))
                .classroomId(classroomId)
                .episodeId(episodeId)
                .title(title)
                .instructions(instructions)
                .dueDate(dueDate)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public UUID getId() {
        return id;
    }
}
