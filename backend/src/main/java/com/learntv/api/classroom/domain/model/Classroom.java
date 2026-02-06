package com.learntv.api.classroom.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Classroom {

    private final ClassroomId id;
    private final UUID teacherId;
    private String name;
    private String description;
    private JoinCode joinCode;
    private boolean active;
    private final Instant createdAt;
    private Instant updatedAt;

    private Classroom(Builder builder) {
        this.id = builder.id != null ? builder.id : ClassroomId.generate();
        this.teacherId = Objects.requireNonNull(builder.teacherId, "teacherId is required");
        this.name = Objects.requireNonNull(builder.name, "name is required");
        this.description = builder.description;
        this.joinCode = builder.joinCode != null ? builder.joinCode : JoinCode.generate();
        this.active = builder.active;
        this.createdAt = builder.createdAt != null ? builder.createdAt : Instant.now();
        this.updatedAt = builder.updatedAt != null ? builder.updatedAt : Instant.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public void update(String name, String description) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (description != null) {
            this.description = description;
        }
        this.updatedAt = Instant.now();
    }

    public void regenerateJoinCode() {
        this.joinCode = JoinCode.generate();
        this.updatedAt = Instant.now();
    }

    public void deactivate() {
        this.active = false;
        this.updatedAt = Instant.now();
    }

    public void activate() {
        this.active = true;
        this.updatedAt = Instant.now();
    }

    public boolean isOwnedBy(UUID userId) {
        return teacherId.equals(userId);
    }

    // Getters
    public ClassroomId getId() {
        return id;
    }

    public UUID getTeacherId() {
        return teacherId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public JoinCode getJoinCode() {
        return joinCode;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public static class Builder {
        private ClassroomId id;
        private UUID teacherId;
        private String name;
        private String description;
        private JoinCode joinCode;
        private boolean active = true;
        private Instant createdAt;
        private Instant updatedAt;

        public Builder id(ClassroomId id) {
            this.id = id;
            return this;
        }

        public Builder id(UUID id) {
            this.id = ClassroomId.of(id);
            return this;
        }

        public Builder teacherId(UUID teacherId) {
            this.teacherId = teacherId;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder joinCode(JoinCode joinCode) {
            this.joinCode = joinCode;
            return this;
        }

        public Builder joinCode(String joinCode) {
            this.joinCode = JoinCode.of(joinCode);
            return this;
        }

        public Builder active(boolean active) {
            this.active = active;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Classroom build() {
            return new Classroom(this);
        }
    }
}
