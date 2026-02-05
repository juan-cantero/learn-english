package com.learntv.api.classroom.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssignmentSubmissionJpaRepository extends JpaRepository<AssignmentSubmissionJpaEntity, UUID> {

    Optional<AssignmentSubmissionJpaEntity> findByAssignmentIdAndStudentId(UUID assignmentId, UUID studentId);

    List<AssignmentSubmissionJpaEntity> findByAssignmentId(UUID assignmentId);

    List<AssignmentSubmissionJpaEntity> findByStudentId(UUID studentId);

    boolean existsByAssignmentIdAndStudentId(UUID assignmentId, UUID studentId);
}
