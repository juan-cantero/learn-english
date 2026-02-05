package com.learntv.api.classroom.adapter.in.web;

import com.learntv.api.classroom.domain.model.AssignmentSubmission;
import com.learntv.api.classroom.domain.model.SubmissionStatus;

import java.time.Instant;
import java.util.UUID;

public record SubmissionResponse(
        UUID id,
        UUID assignmentId,
        UUID studentId,
        SubmissionStatus status,
        Instant startedAt,
        Instant completedAt,
        Integer score,
        Integer timeSpentMinutes
) {
    public static SubmissionResponse fromDomain(AssignmentSubmission submission) {
        return new SubmissionResponse(
                submission.getId(),
                submission.getAssignmentId(),
                submission.getStudentId(),
                submission.getStatus(),
                submission.getStartedAt(),
                submission.getCompletedAt(),
                submission.getScore(),
                submission.getTimeSpentMinutes()
        );
    }
}
