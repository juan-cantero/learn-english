package com.learntv.api.classroom.application.usecase;

import com.learntv.api.classroom.application.port.ClassroomRepository;
import com.learntv.api.classroom.domain.exception.ClassroomNotFoundException;
import com.learntv.api.classroom.domain.exception.NotClassroomOwnerException;
import com.learntv.api.classroom.domain.model.Classroom;
import com.learntv.api.classroom.domain.model.JoinCode;

import java.util.UUID;

public class RegenerateJoinCodeUseCase {

    private final ClassroomRepository classroomRepository;

    public RegenerateJoinCodeUseCase(ClassroomRepository classroomRepository) {
        this.classroomRepository = classroomRepository;
    }

    public Classroom execute(UUID teacherId, UUID classroomId) {
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new ClassroomNotFoundException(classroomId));

        if (!classroom.isOwnedBy(teacherId)) {
            throw new NotClassroomOwnerException();
        }

        // Generate new unique code
        JoinCode newCode;
        int attempts = 0;
        do {
            newCode = JoinCode.generate();
            attempts++;
            if (attempts > 100) {
                throw new RuntimeException("Failed to generate unique join code");
            }
        } while (classroomRepository.existsByJoinCode(newCode.value()));

        classroom.regenerateJoinCode();

        return classroomRepository.save(classroom);
    }
}
