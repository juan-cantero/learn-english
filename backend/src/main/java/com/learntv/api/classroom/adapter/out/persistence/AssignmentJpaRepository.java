package com.learntv.api.classroom.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AssignmentJpaRepository extends JpaRepository<AssignmentJpaEntity, UUID> {

    List<AssignmentJpaEntity> findByClassroomId(UUID classroomId);
}
