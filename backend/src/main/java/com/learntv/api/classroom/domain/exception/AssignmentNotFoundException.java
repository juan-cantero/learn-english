package com.learntv.api.classroom.domain.exception;

import java.util.UUID;

public class AssignmentNotFoundException extends RuntimeException {

    public AssignmentNotFoundException(UUID assignmentId) {
        super("Assignment not found: " + assignmentId);
    }
}
