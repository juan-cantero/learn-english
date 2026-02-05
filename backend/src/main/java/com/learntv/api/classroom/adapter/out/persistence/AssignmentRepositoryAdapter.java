package com.learntv.api.classroom.adapter.out.persistence;

import com.learntv.api.classroom.application.port.AssignmentRepository;
import com.learntv.api.classroom.domain.model.Assignment;
import com.learntv.api.classroom.domain.model.AssignmentId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AssignmentRepositoryAdapter implements AssignmentRepository {

    private final AssignmentJpaRepository jpaRepository;

    public AssignmentRepositoryAdapter(AssignmentJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Assignment> findById(AssignmentId id) {
        return findById(id.value());
    }

    @Override
    public Optional<Assignment> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(AssignmentJpaEntity::toDomain);
    }

    @Override
    public List<Assignment> findByClassroomId(UUID classroomId) {
        return jpaRepository.findByClassroomId(classroomId).stream()
                .map(AssignmentJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Assignment save(Assignment assignment) {
        AssignmentJpaEntity entity = AssignmentJpaEntity.fromDomain(assignment);
        AssignmentJpaEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public void delete(AssignmentId id) {
        jpaRepository.deleteById(id.value());
    }
}
