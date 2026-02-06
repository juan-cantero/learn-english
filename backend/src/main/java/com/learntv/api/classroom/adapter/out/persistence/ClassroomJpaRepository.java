package com.learntv.api.classroom.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClassroomJpaRepository extends JpaRepository<ClassroomJpaEntity, UUID> {

    Optional<ClassroomJpaEntity> findByJoinCode(String joinCode);

    List<ClassroomJpaEntity> findByTeacherId(UUID teacherId);

    boolean existsByJoinCode(String joinCode);
}
