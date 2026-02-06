package com.learntv.api.classroom.adapter.in.web;

import com.learntv.api.classroom.domain.model.Assignment;

import java.time.Instant;
import java.util.UUID;

public record AssignmentResponse(
        UUID id,
        UUID classroomId,
        UUID episodeId,
        String title,
        String instructions,
        Instant dueDate,
        boolean overdue,
        Instant createdAt,
        Instant updatedAt
) {
    public static AssignmentResponse fromDomain(Assignment assignment) {
        return new AssignmentResponse(
                assignment.getId().value(),
                assignment.getClassroomId(),
                assignment.getEpisodeId(),
                assignment.getTitle(),
                assignment.getInstructions(),
                assignment.getDueDate(),
                assignment.isOverdue(),
                assignment.getCreatedAt(),
                assignment.getUpdatedAt()
        );
    }
}
