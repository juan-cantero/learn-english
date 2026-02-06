package com.learntv.api.classroom.application.usecase;

import com.learntv.api.classroom.application.port.ClassroomRepository;
import com.learntv.api.classroom.application.port.ClassroomStudentRepository;
import com.learntv.api.classroom.domain.model.Classroom;
import com.learntv.api.classroom.domain.model.ClassroomStudent;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class GetStudentClassroomsUseCase {

    private final ClassroomRepository classroomRepository;
    private final ClassroomStudentRepository studentRepository;

    public GetStudentClassroomsUseCase(ClassroomRepository classroomRepository,
                                        ClassroomStudentRepository studentRepository) {
        this.classroomRepository = classroomRepository;
        this.studentRepository = studentRepository;
    }

    public List<Classroom> execute(UUID studentId) {
        List<ClassroomStudent> memberships = studentRepository.findByStudentId(studentId);

        return memberships.stream()
                .filter(ClassroomStudent::isActive)
                .map(membership -> classroomRepository.findById(membership.getClassroomId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(Classroom::isActive)
                .toList();
    }
}
