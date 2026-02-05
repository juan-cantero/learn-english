package com.learntv.api.classroom.domain.model;

import java.util.Objects;
import java.util.UUID;

public record ClassroomId(UUID value) {

    public ClassroomId {
        Objects.requireNonNull(value, "ClassroomId value cannot be null");
    }

    public static ClassroomId of(UUID value) {
        return new ClassroomId(value);
    }

    public static ClassroomId generate() {
        return new ClassroomId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
