package com.learntv.api.classroom.application.usecase;

import com.learntv.api.classroom.application.port.AssignmentRepository;
import com.learntv.api.classroom.application.port.ClassroomRepository;
import com.learntv.api.classroom.domain.exception.AssignmentNotFoundException;
import com.learntv.api.classroom.domain.exception.NotClassroomOwnerException;
import com.learntv.api.classroom.domain.model.Assignment;
import com.learntv.api.classroom.domain.model.Classroom;

import java.time.Instant;
import java.util.UUID;

public class UpdateAssignmentUseCase {

    private final AssignmentRepository assignmentRepository;
    private final ClassroomRepository classroomRepository;

    public UpdateAssignmentUseCase(AssignmentRepository assignmentRepository,
                                    ClassroomRepository classroomRepository) {
        this.assignmentRepository = assignmentRepository;
        this.classroomRepository = classroomRepository;
    }

    public Assignment execute(UUID teacherId, UUID assignmentId, UpdateAssignmentCommand command) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AssignmentNotFoundException(assignmentId));

        Classroom classroom = classroomRepository.findById(assignment.getClassroomId())
                .orElseThrow(() -> new AssignmentNotFoundException(assignmentId));

        if (!classroom.isOwnedBy(teacherId)) {
            throw new NotClassroomOwnerException();
        }

        assignment.update(command.title(), command.instructions(), command.dueDate());

        return assignmentRepository.save(assignment);
    }

    public record UpdateAssignmentCommand(
            String title,
            String instructions,
            Instant dueDate
    ) {}
}
