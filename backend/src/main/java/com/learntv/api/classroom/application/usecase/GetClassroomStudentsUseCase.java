package com.learntv.api.classroom.application.usecase;

import com.learntv.api.classroom.application.port.ClassroomRepository;
import com.learntv.api.classroom.application.port.ClassroomStudentRepository;
import com.learntv.api.classroom.domain.exception.ClassroomNotFoundException;
import com.learntv.api.classroom.domain.exception.NotClassroomOwnerException;
import com.learntv.api.classroom.domain.model.Classroom;
import com.learntv.api.classroom.domain.model.ClassroomStudent;
import com.learntv.api.user.application.port.UserRepository;
import com.learntv.api.user.domain.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class GetClassroomStudentsUseCase {

    private final ClassroomRepository classroomRepository;
    private final ClassroomStudentRepository studentRepository;
    private final UserRepository userRepository;

    public GetClassroomStudentsUseCase(ClassroomRepository classroomRepository,
                                        ClassroomStudentRepository studentRepository,
                                        UserRepository userRepository) {
        this.classroomRepository = classroomRepository;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
    }

    public List<StudentInfo> execute(UUID userId, UUID classroomId) {
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new ClassroomNotFoundException(classroomId));

        boolean isOwner = classroom.isOwnedBy(userId);
        boolean isEnrolled = studentRepository.existsByClassroomIdAndStudentId(classroomId, userId);

        if (!isOwner && !isEnrolled) {
            throw new NotClassroomOwnerException();
        }

        List<ClassroomStudent> memberships = studentRepository.findByClassroomId(classroomId);

        return memberships.stream()
                .filter(ClassroomStudent::isActive)
                .map(membership -> {
                    Optional<User> userOpt = userRepository.findById(membership.getStudentId());
                    return userOpt.map(user -> new StudentInfo(
                            user.getId().value(),
                            user.getEmail(),
                            user.getDisplayName(),
                            membership.getJoinedAt()
                    )).orElse(null);
                })
                .filter(info -> info != null)
                .toList();
    }

    public record StudentInfo(UUID id, String email, String displayName, java.time.Instant joinedAt) {}
}
