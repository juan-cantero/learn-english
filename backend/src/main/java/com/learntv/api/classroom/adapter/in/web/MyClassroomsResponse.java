package com.learntv.api.classroom.adapter.in.web;

import com.learntv.api.classroom.application.usecase.GetTeacherClassroomsUseCase;
import com.learntv.api.classroom.domain.model.Classroom;

import java.util.List;

public record MyClassroomsResponse(
        List<ClassroomResponse> teaching,
        List<ClassroomResponse> enrolled
) {
    public static MyClassroomsResponse from(
            List<GetTeacherClassroomsUseCase.ClassroomWithCount> teacherClassrooms,
            List<Classroom> studentClassrooms
    ) {
        List<ClassroomResponse> teaching = teacherClassrooms.stream()
                .map(c -> ClassroomResponse.fromDomainWithCount(c.classroom(), c.studentCount()))
                .toList();

        List<ClassroomResponse> enrolled = studentClassrooms.stream()
                .map(ClassroomResponse::fromDomain)
                .toList();

        return new MyClassroomsResponse(teaching, enrolled);
    }
}
