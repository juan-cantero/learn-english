package com.learntv.api.classroom.application.usecase;

import com.learntv.api.classroom.application.port.ClassroomRepository;
import com.learntv.api.classroom.domain.exception.ClassroomNotFoundException;
import com.learntv.api.classroom.domain.exception.NotClassroomOwnerException;
import com.learntv.api.classroom.domain.model.Classroom;

import java.util.UUID;

public class DeleteClassroomUseCase {

    private final ClassroomRepository classroomRepository;

    public DeleteClassroomUseCase(ClassroomRepository classroomRepository) {
        this.classroomRepository = classroomRepository;
    }

    public void execute(UUID teacherId, UUID classroomId) {
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new ClassroomNotFoundException(classroomId));

        if (!classroom.isOwnedBy(teacherId)) {
            throw new NotClassroomOwnerException();
        }

        // Soft delete by deactivating
        classroom.deactivate();
        classroomRepository.save(classroom);
    }
}
