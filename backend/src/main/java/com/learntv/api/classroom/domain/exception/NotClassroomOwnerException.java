package com.learntv.api.classroom.domain.exception;

public class NotClassroomOwnerException extends RuntimeException {

    public NotClassroomOwnerException() {
        super("You are not the owner of this classroom");
    }
}
