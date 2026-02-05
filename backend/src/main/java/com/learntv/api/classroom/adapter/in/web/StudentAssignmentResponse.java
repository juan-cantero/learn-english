package com.learntv.api.classroom.adapter.in.web;

import com.learntv.api.classroom.application.usecase.GetStudentAssignmentsUseCase;
import com.learntv.api.classroom.domain.model.SubmissionStatus;

import java.time.Instant;
import java.util.UUID;

public record StudentAssignmentResponse(
        UUID id,
        UUID classroomId,
        UUID episodeId,
        String title,
        String instructions,
        Instant dueDate,
        SubmissionStatus status,
        Integer score,
        boolean overdue,
        Instant createdAt
) {
    public static StudentAssignmentResponse fromDomain(GetStudentAssignmentsUseCase.StudentAssignment studentAssignment) {
        var assignment = studentAssignment.assignment();
        return new StudentAssignmentResponse(
                assignment.getId().value(),
                studentAssignment.classroomId(),
                assignment.getEpisodeId(),
                assignment.getTitle(),
                assignment.getInstructions(),
                assignment.getDueDate(),
                studentAssignment.status(),
                studentAssignment.score(),
                studentAssignment.overdue(),
                assignment.getCreatedAt()
        );
    }
}
