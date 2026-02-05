package com.learntv.api.classroom.application.usecase;

import com.learntv.api.classroom.application.port.ClassroomRepository;
import com.learntv.api.classroom.application.port.ClassroomStudentRepository;
import com.learntv.api.classroom.domain.model.Classroom;

import java.util.List;
import java.util.UUID;

public class GetTeacherClassroomsUseCase {

    private final ClassroomRepository classroomRepository;
    private final ClassroomStudentRepository studentRepository;

    public GetTeacherClassroomsUseCase(ClassroomRepository classroomRepository,
                                        ClassroomStudentRepository studentRepository) {
        this.classroomRepository = classroomRepository;
        this.studentRepository = studentRepository;
    }

    public List<ClassroomWithCount> execute(UUID teacherId) {
        List<Classroom> classrooms = classroomRepository.findByTeacherId(teacherId);

        return classrooms.stream()
                .map(classroom -> {
                    int studentCount = studentRepository.findByClassroomId(classroom.getId().value()).size();
                    return new ClassroomWithCount(classroom, studentCount);
                })
                .toList();
    }

    public record ClassroomWithCount(Classroom classroom, int studentCount) {}
}
