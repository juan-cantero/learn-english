package com.learntv.api.classroom.application.port;

import com.learntv.api.classroom.domain.model.Assignment;
import com.learntv.api.classroom.domain.model.AssignmentId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssignmentRepository {

    Optional<Assignment> findById(AssignmentId id);

    Optional<Assignment> findById(UUID id);

    List<Assignment> findByClassroomId(UUID classroomId);

    Assignment save(Assignment assignment);

    void delete(AssignmentId id);
}
