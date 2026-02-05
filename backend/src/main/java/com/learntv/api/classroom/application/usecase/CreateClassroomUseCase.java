package com.learntv.api.classroom.application.usecase;

import com.learntv.api.classroom.application.port.ClassroomRepository;
import com.learntv.api.classroom.domain.exception.NotTeacherException;
import com.learntv.api.classroom.domain.model.Classroom;
import com.learntv.api.classroom.domain.model.JoinCode;
import com.learntv.api.user.application.port.UserRepository;
import com.learntv.api.user.domain.exception.UserNotFoundException;
import com.learntv.api.user.domain.model.User;

import java.util.UUID;

public class CreateClassroomUseCase {

    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;

    public CreateClassroomUseCase(ClassroomRepository classroomRepository, UserRepository userRepository) {
        this.classroomRepository = classroomRepository;
        this.userRepository = userRepository;
    }

    public Classroom execute(UUID teacherId, CreateClassroomCommand command) {
        User user = userRepository.findById(teacherId)
                .orElseThrow(() -> new UserNotFoundException(teacherId));

        if (!user.isTeacher()) {
            throw new NotTeacherException("Only teachers can create classrooms");
        }

        // Generate unique join code
        JoinCode joinCode = generateUniqueJoinCode();

        Classroom classroom = Classroom.builder()
                .teacherId(teacherId)
                .name(command.name())
                .description(command.description())
                .joinCode(joinCode)
                .build();

        return classroomRepository.save(classroom);
    }

    private JoinCode generateUniqueJoinCode() {
        JoinCode code;
        int attempts = 0;
        do {
            code = JoinCode.generate();
            attempts++;
            if (attempts > 100) {
                throw new RuntimeException("Failed to generate unique join code");
            }
        } while (classroomRepository.existsByJoinCode(code.value()));
        return code;
    }

    public record CreateClassroomCommand(String name, String description) {}
}
