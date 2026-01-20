package com.learntv.api.learning.domain.exception;

import java.util.UUID;

public class ExerciseNotFoundException extends RuntimeException {

    public ExerciseNotFoundException(UUID exerciseId) {
        super("Exercise not found: " + exerciseId);
    }
}
