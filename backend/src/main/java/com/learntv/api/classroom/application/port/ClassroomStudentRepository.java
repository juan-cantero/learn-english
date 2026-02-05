package com.learntv.api.classroom.application.port;

import com.learntv.api.classroom.domain.model.ClassroomStudent;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClassroomStudentRepository {

    Optional<ClassroomStudent> findByClassroomIdAndStudentId(UUID classroomId, UUID studentId);

    List<ClassroomStudent> findByClassroomId(UUID classroomId);

    List<ClassroomStudent> findByStudentId(UUID studentId);

    ClassroomStudent save(ClassroomStudent student);

    void delete(UUID id);

    boolean existsByClassroomIdAndStudentId(UUID classroomId, UUID studentId);
}
