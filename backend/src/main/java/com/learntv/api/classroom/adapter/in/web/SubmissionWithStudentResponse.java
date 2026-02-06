package com.learntv.api.classroom.adapter.in.web;

import com.learntv.api.classroom.application.usecase.GetAssignmentSubmissionsUseCase;
import com.learntv.api.classroom.domain.model.SubmissionStatus;

import java.time.Instant;
import java.util.UUID;

public record SubmissionWithStudentResponse(
        UUID id,
        UUID assignmentId,
        UUID studentId,
        String studentName,
        String studentEmail,
        SubmissionStatus status,
        Instant startedAt,
        Instant completedAt,
        Integer score,
        Integer timeSpentMinutes
) {
    public static SubmissionWithStudentResponse fromDomain(GetAssignmentSubmissionsUseCase.SubmissionWithStudent data) {
        var submission = data.submission();
        return new SubmissionWithStudentResponse(
                submission.getId(),
                submission.getAssignmentId(),
                submission.getStudentId(),
                data.studentName(),
                data.studentEmail(),
                submission.getStatus(),
                submission.getStartedAt(),
                submission.getCompletedAt(),
                submission.getScore(),
                submission.getTimeSpentMinutes()
        );
    }
}
