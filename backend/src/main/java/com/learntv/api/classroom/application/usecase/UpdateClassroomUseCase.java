package com.learntv.api.classroom.application.usecase;

import com.learntv.api.classroom.application.port.ClassroomRepository;
import com.learntv.api.classroom.domain.exception.ClassroomNotFoundException;
import com.learntv.api.classroom.domain.exception.NotClassroomOwnerException;
import com.learntv.api.classroom.domain.model.Classroom;

import java.util.UUID;

public class UpdateClassroomUseCase {

    private final ClassroomRepository classroomRepository;

    public UpdateClassroomUseCase(ClassroomRepository classroomRepository) {
        this.classroomRepository = classroomRepository;
    }

    public Classroom execute(UUID teacherId, UUID classroomId, UpdateClassroomCommand command) {
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new ClassroomNotFoundException(classroomId));

        if (!classroom.isOwnedBy(teacherId)) {
            throw new NotClassroomOwnerException();
        }

        classroom.update(command.name(), command.description());

        return classroomRepository.save(classroom);
    }

    public record UpdateClassroomCommand(String name, String description) {}
}
