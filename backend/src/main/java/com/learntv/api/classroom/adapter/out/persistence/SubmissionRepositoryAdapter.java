package com.learntv.api.classroom.adapter.out.persistence;

import com.learntv.api.classroom.application.port.SubmissionRepository;
import com.learntv.api.classroom.domain.model.AssignmentSubmission;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class SubmissionRepositoryAdapter implements SubmissionRepository {

    private final AssignmentSubmissionJpaRepository jpaRepository;

    public SubmissionRepositoryAdapter(AssignmentSubmissionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<AssignmentSubmission> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(AssignmentSubmissionJpaEntity::toDomain);
    }

    @Override
    public Optional<AssignmentSubmission> findByAssignmentIdAndStudentId(UUID assignmentId, UUID studentId) {
        return jpaRepository.findByAssignmentIdAndStudentId(assignmentId, studentId)
                .map(AssignmentSubmissionJpaEntity::toDomain);
    }

    @Override
    public List<AssignmentSubmission> findByAssignmentId(UUID assignmentId) {
        return jpaRepository.findByAssignmentId(assignmentId).stream()
                .map(AssignmentSubmissionJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<AssignmentSubmission> findByStudentId(UUID studentId) {
        return jpaRepository.findByStudentId(studentId).stream()
                .map(AssignmentSubmissionJpaEntity::toDomain)
                .toList();
    }

    @Override
    public AssignmentSubmission save(AssignmentSubmission submission) {
        AssignmentSubmissionJpaEntity entity = AssignmentSubmissionJpaEntity.fromDomain(submission);
        AssignmentSubmissionJpaEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public boolean existsByAssignmentIdAndStudentId(UUID assignmentId, UUID studentId) {
        return jpaRepository.existsByAssignmentIdAndStudentId(assignmentId, studentId);
    }
}
