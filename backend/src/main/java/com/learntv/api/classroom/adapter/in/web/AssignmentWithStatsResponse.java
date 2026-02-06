package com.learntv.api.classroom.adapter.in.web;

import com.learntv.api.classroom.application.usecase.GetClassroomAssignmentsUseCase;

import java.time.Instant;
import java.util.UUID;

public record AssignmentWithStatsResponse(
        UUID id,
        UUID classroomId,
        UUID episodeId,
        String title,
        String instructions,
        Instant dueDate,
        boolean overdue,
        int totalSubmissions,
        int completedSubmissions,
        Instant createdAt
) {
    public static AssignmentWithStatsResponse fromDomain(GetClassroomAssignmentsUseCase.AssignmentWithStats stats) {
        var assignment = stats.assignment();
        return new AssignmentWithStatsResponse(
                assignment.getId().value(),
                assignment.getClassroomId(),
                assignment.getEpisodeId(),
                assignment.getTitle(),
                assignment.getInstructions(),
                assignment.getDueDate(),
                assignment.isOverdue(),
                stats.totalSubmissions(),
                stats.completedSubmissions(),
                assignment.getCreatedAt()
        );
    }
}
