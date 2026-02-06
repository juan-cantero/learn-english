package com.learntv.api.classroom.adapter.out.persistence;

import com.learntv.api.classroom.domain.model.ClassroomStudent;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "classroom_students", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"classroom_id", "student_id"})
})
public class ClassroomStudentJpaEntity {

    @Id
    private UUID id;

    @Column(name = "classroom_id", nullable = false)
    private UUID classroomId;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;

    @Column(name = "is_active")
    private boolean active;

    protected ClassroomStudentJpaEntity() {
    }

    public static ClassroomStudentJpaEntity fromDomain(ClassroomStudent student) {
        ClassroomStudentJpaEntity entity = new ClassroomStudentJpaEntity();
        entity.id = student.getId();
        entity.classroomId = student.getClassroomId();
        entity.studentId = student.getStudentId();
        entity.joinedAt = student.getJoinedAt();
        entity.active = student.isActive();
        return entity;
    }

    public ClassroomStudent toDomain() {
        return ClassroomStudent.builder()
                .id(id)
                .classroomId(classroomId)
                .studentId(studentId)
                .joinedAt(joinedAt)
                .active(active)
                .build();
    }

    public UUID getId() {
        return id;
    }
}
