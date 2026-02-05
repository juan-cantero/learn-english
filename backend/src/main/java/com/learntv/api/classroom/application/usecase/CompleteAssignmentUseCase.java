package com.learntv.api.classroom.application.usecase;

import com.learntv.api.classroom.application.port.AssignmentRepository;
import com.learntv.api.classroom.application.port.SubmissionRepository;
import com.learntv.api.classroom.domain.exception.AssignmentNotFoundException;
import com.learntv.api.classroom.domain.model.Assignment;
import com.learntv.api.classroom.domain.model.AssignmentSubmission;
import com.learntv.api.progress.application.port.UserProgressRepository;
import com.learntv.api.progress.domain.model.UserProgress;

import java.util.UUID;

public class CompleteAssignmentUseCase {

    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final UserProgressRepository progressRepository;

    public CompleteAssignmentUseCase(AssignmentRepository assignmentRepository,
                                      SubmissionRepository submissionRepository,
                                      UserProgressRepository progressRepository) {
        this.assignmentRepository = assignmentRepository;
        this.submissionRepository = submissionRepository;
        this.progressRepository = progressRepository;
    }

    public AssignmentSubmission execute(UUID studentId, UUID assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AssignmentNotFoundException(assignmentId));

        // Get submission (must exist and be started)
        AssignmentSubmission submission = submissionRepository
                .findByAssignmentIdAndStudentId(assignmentId, studentId)
                .orElseThrow(() -> new IllegalStateException("Assignment not started"));

        // Get user's progress on the episode to calculate score
        int score = progressRepository
                .findByUserIdAndEpisodeId(studentId, assignment.getEpisodeId())
                .map(UserProgress::getTotalPoints)
                .orElse(0);

        submission.complete(score);

        return submissionRepository.save(submission);
    }
}
