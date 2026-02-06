package com.learntv.api.classroom.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Assignment {

    private final AssignmentId id;
    private final UUID classroomId;
    private final UUID episodeId;
    private String title;
    private String instructions;
    private Instant dueDate;
    private final Instant createdAt;
    private Instant updatedAt;

    private Assignment(Builder builder) {
        this.id = builder.id != null ? builder.id : AssignmentId.generate();
        this.classroomId = Objects.requireNonNull(builder.classroomId, "classroomId is required");
        this.episodeId = Objects.requireNonNull(builder.episodeId, "episodeId is required");
        this.title = builder.title;
        this.instructions = builder.instructions;
        this.dueDate = builder.dueDate;
        this.createdAt = builder.createdAt != null ? builder.createdAt : Instant.now();
        this.updatedAt = builder.updatedAt != null ? builder.updatedAt : Instant.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public void update(String title, String instructions, Instant dueDate) {
        if (title != null) {
            this.title = title;
        }
        if (instructions != null) {
            this.instructions = instructions;
        }
        this.dueDate = dueDate; // Allow null to remove due date
        this.updatedAt = Instant.now();
    }

    public boolean isOverdue() {
        return dueDate != null && Instant.now().isAfter(dueDate);
    }

    // Getters
    public AssignmentId getId() {
        return id;
    }

    public UUID getClassroomId() {
        return classroomId;
    }

    public UUID getEpisodeId() {
        return episodeId;
    }

    public String getTitle() {
        return title;
    }

    public String getInstructions() {
        return instructions;
    }

    public Instant getDueDate() {
        return dueDate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public static class Builder {
        private AssignmentId id;
        private UUID classroomId;
        private UUID episodeId;
        private String title;
        private String instructions;
        private Instant dueDate;
        private Instant createdAt;
        private Instant updatedAt;

        public Builder id(AssignmentId id) {
            this.id = id;
            return this;
        }

        public Builder id(UUID id) {
            this.id = AssignmentId.of(id);
            return this;
        }

        public Builder classroomId(UUID classroomId) {
            this.classroomId = classroomId;
            return this;
        }

        public Builder episodeId(UUID episodeId) {
            this.episodeId = episodeId;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder instructions(String instructions) {
            this.instructions = instructions;
            return this;
        }

        public Builder dueDate(Instant dueDate) {
            this.dueDate = dueDate;
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

        public Assignment build() {
            return new Assignment(this);
        }
    }
}
