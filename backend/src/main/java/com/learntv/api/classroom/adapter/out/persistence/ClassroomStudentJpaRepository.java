package com.learntv.api.classroom.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClassroomStudentJpaRepository extends JpaRepository<ClassroomStudentJpaEntity, UUID> {

    Optional<ClassroomStudentJpaEntity> findByClassroomIdAndStudentId(UUID classroomId, UUID studentId);

    List<ClassroomStudentJpaEntity> findByClassroomId(UUID classroomId);

    List<ClassroomStudentJpaEntity> findByStudentId(UUID studentId);

    boolean existsByClassroomIdAndStudentId(UUID classroomId, UUID studentId);
}
