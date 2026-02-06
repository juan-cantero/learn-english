package com.learntv.api.classroom.application.port;

import com.learntv.api.classroom.domain.model.Classroom;
import com.learntv.api.classroom.domain.model.ClassroomId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClassroomRepository {

    Optional<Classroom> findById(ClassroomId id);

    Optional<Classroom> findById(UUID id);

    Optional<Classroom> findByJoinCode(String joinCode);

    List<Classroom> findByTeacherId(UUID teacherId);

    Classroom save(Classroom classroom);

    void delete(ClassroomId id);

    boolean existsByJoinCode(String joinCode);
}
