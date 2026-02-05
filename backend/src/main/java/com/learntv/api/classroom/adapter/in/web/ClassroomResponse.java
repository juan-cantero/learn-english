package com.learntv.api.classroom.adapter.in.web;

import com.learntv.api.classroom.domain.model.Classroom;

import java.time.Instant;

public record ClassroomResponse(
        String id,
        String teacherId,
        String name,
        String description,
        String joinCode,
        boolean active,
        Integer studentCount,
        Instant createdAt,
        Instant updatedAt
) {
    public static ClassroomResponse fromDomain(Classroom classroom) {
        return fromDomainWithCount(classroom, null);
    }

    public static ClassroomResponse fromDomainWithCount(Classroom classroom, Integer studentCount) {
        return new ClassroomResponse(
                classroom.getId().toString(),
                classroom.getTeacherId().toString(),
                classroom.getName(),
                classroom.getDescription(),
                classroom.getJoinCode().value(),
                classroom.isActive(),
                studentCount,
                classroom.getCreatedAt(),
                classroom.getUpdatedAt()
        );
    }
}
