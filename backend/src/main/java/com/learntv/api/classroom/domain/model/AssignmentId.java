package com.learntv.api.classroom.domain.model;

import java.util.Objects;
import java.util.UUID;

public record AssignmentId(UUID value) {

    public AssignmentId {
        Objects.requireNonNull(value, "AssignmentId value cannot be null");
    }

    public static AssignmentId of(UUID value) {
        return new AssignmentId(value);
    }

    public static AssignmentId generate() {
        return new AssignmentId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
