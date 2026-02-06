package com.learntv.api.classroom.application.usecase;

import com.learntv.api.classroom.application.port.AssignmentRepository;
import com.learntv.api.classroom.application.port.ClassroomRepository;
import com.learntv.api.classroom.domain.exception.ClassroomNotFoundException;
import com.learntv.api.classroom.domain.exception.NotClassroomOwnerException;
import com.learntv.api.classroom.domain.model.Assignment;
import com.learntv.api.classroom.domain.model.Classroom;

import java.time.Instant;
import java.util.UUID;

public class CreateAssignmentUseCase {

    private final AssignmentRepository assignmentRepository;
    private final ClassroomRepository classroomRepository;

    public CreateAssignmentUseCase(AssignmentRepository assignmentRepository,
                                    ClassroomRepository classroomRepository) {
        this.assignmentRepository = assignmentRepository;
        this.classroomRepository = classroomRepository;
    }

    public Assignment execute(UUID teacherId, UUID classroomId, CreateAssignmentCommand command) {
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new ClassroomNotFoundException(classroomId));

        if (!classroom.isOwnedBy(teacherId)) {
            throw new NotClassroomOwnerException();
        }

        Assignment assignment = Assignment.builder()
                .classroomId(classroomId)
                .episodeId(command.episodeId())
                .title(command.title())
                .instructions(command.instructions())
                .dueDate(command.dueDate())
                .build();

        return assignmentRepository.save(assignment);
    }

    public record CreateAssignmentCommand(
            UUID episodeId,
            String title,
            String instructions,
            Instant dueDate
    ) {}
}
