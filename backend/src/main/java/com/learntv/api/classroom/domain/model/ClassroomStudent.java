package com.learntv.api.classroom.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class ClassroomStudent {

    private final UUID id;
    private final UUID classroomId;
    private final UUID studentId;
    private final Instant joinedAt;
    private boolean active;

    private ClassroomStudent(Builder builder) {
        this.id = builder.id != null ? builder.id : UUID.randomUUID();
        this.classroomId = Objects.requireNonNull(builder.classroomId, "classroomId is required");
        this.studentId = Objects.requireNonNull(builder.studentId, "studentId is required");
        this.joinedAt = builder.joinedAt != null ? builder.joinedAt : Instant.now();
        this.active = builder.active;
    }

    public static Builder builder() {
        return new Builder();
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public UUID getClassroomId() {
        return classroomId;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public boolean isActive() {
        return active;
    }

    public static class Builder {
        private UUID id;
        private UUID classroomId;
        private UUID studentId;
        private Instant joinedAt;
        private boolean active = true;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder classroomId(UUID classroomId) {
            this.classroomId = classroomId;
            return this;
        }

        public Builder studentId(UUID studentId) {
            this.studentId = studentId;
            return this;
        }

        public Builder joinedAt(Instant joinedAt) {
            this.joinedAt = joinedAt;
            return this;
        }

        public Builder active(boolean active) {
            this.active = active;
            return this;
        }

        public ClassroomStudent build() {
            return new ClassroomStudent(this);
        }
    }
}
