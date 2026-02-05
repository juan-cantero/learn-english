package com.learntv.api.classroom.domain.exception;

import java.util.UUID;

public class ClassroomNotFoundException extends RuntimeException {

    public ClassroomNotFoundException(UUID classroomId) {
        super("Classroom not found: " + classroomId);
    }

    public ClassroomNotFoundException(String joinCode) {
        super("Classroom not found with join code: " + joinCode);
    }
}
