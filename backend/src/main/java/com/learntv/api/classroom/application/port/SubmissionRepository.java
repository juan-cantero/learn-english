package com.learntv.api.classroom.application.port;

import com.learntv.api.classroom.domain.model.AssignmentSubmission;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubmissionRepository {

    Optional<AssignmentSubmission> findById(UUID id);

    Optional<AssignmentSubmission> findByAssignmentIdAndStudentId(UUID assignmentId, UUID studentId);

    List<AssignmentSubmission> findByAssignmentId(UUID assignmentId);

    List<AssignmentSubmission> findByStudentId(UUID studentId);

    AssignmentSubmission save(AssignmentSubmission submission);

    boolean existsByAssignmentIdAndStudentId(UUID assignmentId, UUID studentId);
}
