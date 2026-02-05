package com.learntv.api.classroom.adapter.out.persistence;

import com.learntv.api.classroom.domain.model.AssignmentSubmission;
import com.learntv.api.classroom.domain.model.SubmissionStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "assignment_submissions", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"assignment_id", "student_id"})
})
public class AssignmentSubmissionJpaEntity {

    @Id
    private UUID id;

    @Column(name = "assignment_id", nullable = false)
    private UUID assignmentId;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SubmissionStatus status;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    private Integer score;

    @Column(name = "time_spent_minutes")
    private Integer timeSpentMinutes;

    protected AssignmentSubmissionJpaEntity() {
    }

    public static AssignmentSubmissionJpaEntity fromDomain(AssignmentSubmission submission) {
        AssignmentSubmissionJpaEntity entity = new AssignmentSubmissionJpaEntity();
        entity.id = submission.getId();
        entity.assignmentId = submission.getAssignmentId();
        entity.studentId = submission.getStudentId();
        entity.status = submission.getStatus();
        entity.startedAt = submission.getStartedAt();
        entity.completedAt = submission.getCompletedAt();
        entity.score = submission.getScore();
        entity.timeSpentMinutes = submission.getTimeSpentMinutes();
        return entity;
    }

    public AssignmentSubmission toDomain() {
        return AssignmentSubmission.builder()
                .id(id)
                .assignmentId(assignmentId)
                .studentId(studentId)
                .status(status)
                .startedAt(startedAt)
                .completedAt(completedAt)
                .score(score)
                .timeSpentMinutes(timeSpentMinutes)
                .build();
    }

    public UUID getId() {
        return id;
    }
}
