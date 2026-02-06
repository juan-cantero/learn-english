package com.learntv.api.classroom.application.usecase;

import com.learntv.api.classroom.application.port.AssignmentRepository;
import com.learntv.api.classroom.application.port.SubmissionRepository;
import com.learntv.api.classroom.domain.exception.AssignmentNotFoundException;
import com.learntv.api.classroom.domain.model.Assignment;
import com.learntv.api.classroom.domain.model.AssignmentSubmission;

import java.util.UUID;

public class StartAssignmentUseCase {

    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;

    public StartAssignmentUseCase(AssignmentRepository assignmentRepository,
                                   SubmissionRepository submissionRepository) {
        this.assignmentRepository = assignmentRepository;
        this.submissionRepository = submissionRepository;
    }

    public AssignmentSubmission execute(UUID studentId, UUID assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AssignmentNotFoundException(assignmentId));

        // Get or create submission
        AssignmentSubmission submission = submissionRepository
                .findByAssignmentIdAndStudentId(assignmentId, studentId)
                .orElseGet(() -> AssignmentSubmission.builder()
                        .assignmentId(assignmentId)
                        .studentId(studentId)
                        .build());

        submission.start();

        return submissionRepository.save(submission);
    }
}
