package com.learntv.api.classroom.domain.model;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

public class AssignmentSubmission {

    private final UUID id;
    private final UUID assignmentId;
    private final UUID studentId;
    private SubmissionStatus status;
    private Instant startedAt;
    private Instant completedAt;
    private Integer score;
    private Integer timeSpentMinutes;

    private AssignmentSubmission(Builder builder) {
        this.id = builder.id != null ? builder.id : UUID.randomUUID();
        this.assignmentId = Objects.requireNonNull(builder.assignmentId, "assignmentId is required");
        this.studentId = Objects.requireNonNull(builder.studentId, "studentId is required");
        this.status = builder.status != null ? builder.status : SubmissionStatus.NOT_STARTED;
        this.startedAt = builder.startedAt;
        this.completedAt = builder.completedAt;
        this.score = builder.score;
        this.timeSpentMinutes = builder.timeSpentMinutes;
    }

    public static Builder builder() {
        return new Builder();
    }

    public void start() {
        if (this.status == SubmissionStatus.NOT_STARTED) {
            this.status = SubmissionStatus.IN_PROGRESS;
            this.startedAt = Instant.now();
        }
    }

    public void complete(int score) {
        this.status = SubmissionStatus.COMPLETED;
        this.completedAt = Instant.now();
        this.score = score;
        if (startedAt != null) {
            this.timeSpentMinutes = (int) ChronoUnit.MINUTES.between(startedAt, completedAt);
        }
    }

    public boolean isCompleted() {
        return status == SubmissionStatus.COMPLETED;
    }

    public boolean isInProgress() {
        return status == SubmissionStatus.IN_PROGRESS;
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public UUID getAssignmentId() {
        return assignmentId;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public SubmissionStatus getStatus() {
        return status;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public Integer getScore() {
        return score;
    }

    public Integer getTimeSpentMinutes() {
        return timeSpentMinutes;
    }

    public static class Builder {
        private UUID id;
        private UUID assignmentId;
        private UUID studentId;
        private SubmissionStatus status;
        private Instant startedAt;
        private Instant completedAt;
        private Integer score;
        private Integer timeSpentMinutes;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder assignmentId(UUID assignmentId) {
            this.assignmentId = assignmentId;
            return this;
        }

        public Builder studentId(UUID studentId) {
            this.studentId = studentId;
            return this;
        }

        public Builder status(SubmissionStatus status) {
            this.status = status;
            return this;
        }

        public Builder startedAt(Instant startedAt) {
            this.startedAt = startedAt;
            return this;
        }

        public Builder completedAt(Instant completedAt) {
            this.completedAt = completedAt;
            return this;
        }

        public Builder score(Integer score) {
            this.score = score;
            return this;
        }

        public Builder timeSpentMinutes(Integer timeSpentMinutes) {
            this.timeSpentMinutes = timeSpentMinutes;
            return this;
        }

        public AssignmentSubmission build() {
            return new AssignmentSubmission(this);
        }
    }
}
