package com.learntv.api.classroom.adapter.in.web;

import com.learntv.api.classroom.application.usecase.GetClassroomStudentsUseCase;

import java.time.Instant;

public record StudentResponse(
        String id,
        String email,
        String displayName,
        Instant joinedAt
) {
    public static StudentResponse fromDomain(GetClassroomStudentsUseCase.StudentInfo info) {
        return new StudentResponse(
                info.id().toString(),
                info.email(),
                info.displayName(),
                info.joinedAt()
        );
    }
}
