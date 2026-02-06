package com.learntv.api.classroom.application.usecase;

import com.learntv.api.classroom.application.port.ClassroomRepository;
import com.learntv.api.classroom.application.port.ClassroomStudentRepository;
import com.learntv.api.classroom.domain.exception.ClassroomNotFoundException;
import com.learntv.api.classroom.domain.exception.NotInClassroomException;
import com.learntv.api.classroom.domain.model.Classroom;
import com.learntv.api.classroom.domain.model.ClassroomStudent;

import java.util.UUID;

public class LeaveClassroomUseCase {

    private final ClassroomRepository classroomRepository;
    private final ClassroomStudentRepository studentRepository;

    public LeaveClassroomUseCase(ClassroomRepository classroomRepository,
                                  ClassroomStudentRepository studentRepository) {
        this.classroomRepository = classroomRepository;
        this.studentRepository = studentRepository;
    }

    public void execute(UUID studentId, UUID classroomId) {
        // Verify classroom exists
        classroomRepository.findById(classroomId)
                .orElseThrow(() -> new ClassroomNotFoundException(classroomId));

        // Find and deactivate membership
        ClassroomStudent membership = studentRepository.findByClassroomIdAndStudentId(classroomId, studentId)
                .orElseThrow(NotInClassroomException::new);

        membership.deactivate();
        studentRepository.save(membership);
    }
}
