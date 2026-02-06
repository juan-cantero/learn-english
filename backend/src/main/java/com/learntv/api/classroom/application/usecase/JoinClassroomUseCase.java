package com.learntv.api.classroom.application.usecase;

import com.learntv.api.classroom.application.port.ClassroomRepository;
import com.learntv.api.classroom.application.port.ClassroomStudentRepository;
import com.learntv.api.classroom.domain.exception.AlreadyInClassroomException;
import com.learntv.api.classroom.domain.exception.ClassroomNotFoundException;
import com.learntv.api.classroom.domain.model.Classroom;
import com.learntv.api.classroom.domain.model.ClassroomStudent;

import java.util.UUID;

public class JoinClassroomUseCase {

    private final ClassroomRepository classroomRepository;
    private final ClassroomStudentRepository studentRepository;

    public JoinClassroomUseCase(ClassroomRepository classroomRepository,
                                 ClassroomStudentRepository studentRepository) {
        this.classroomRepository = classroomRepository;
        this.studentRepository = studentRepository;
    }

    public Classroom execute(UUID studentId, String joinCode) {
        Classroom classroom = classroomRepository.findByJoinCode(joinCode.toUpperCase())
                .orElseThrow(() -> new ClassroomNotFoundException(joinCode));

        if (!classroom.isActive()) {
            throw new ClassroomNotFoundException(joinCode);
        }

        // Check if already a member
        if (studentRepository.existsByClassroomIdAndStudentId(classroom.getId().value(), studentId)) {
            throw new AlreadyInClassroomException();
        }

        // Can't join your own classroom as a student
        if (classroom.isOwnedBy(studentId)) {
            throw new AlreadyInClassroomException();
        }

        ClassroomStudent membership = ClassroomStudent.builder()
                .classroomId(classroom.getId().value())
                .studentId(studentId)
                .build();

        studentRepository.save(membership);

        return classroom;
    }
}
