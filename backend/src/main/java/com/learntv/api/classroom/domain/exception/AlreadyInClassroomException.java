package com.learntv.api.classroom.domain.exception;

public class AlreadyInClassroomException extends RuntimeException {

    public AlreadyInClassroomException() {
        super("You are already a member of this classroom");
    }
}
