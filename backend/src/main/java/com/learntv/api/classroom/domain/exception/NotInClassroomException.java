package com.learntv.api.classroom.domain.exception;

public class NotInClassroomException extends RuntimeException {

    public NotInClassroomException() {
        super("You are not a member of this classroom");
    }
}
