package com.learntv.api.classroom.domain.exception;

public class NotTeacherException extends RuntimeException {

    public NotTeacherException() {
        super("Only teachers can perform this action");
    }

    public NotTeacherException(String message) {
        super(message);
    }
}
