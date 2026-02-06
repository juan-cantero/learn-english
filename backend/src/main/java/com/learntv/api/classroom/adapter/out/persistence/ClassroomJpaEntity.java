package com.learntv.api.classroom.adapter.out.persistence;

import com.learntv.api.classroom.domain.model.Classroom;
import com.learntv.api.classroom.domain.model.ClassroomId;
import com.learntv.api.classroom.domain.model.JoinCode;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "classrooms")
public class ClassroomJpaEntity {

    @Id
    private UUID id;

    @Column(name = "teacher_id", nullable = false)
    private UUID teacherId;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "join_code", nullable = false, unique = true)
    private String joinCode;

    @Column(name = "is_active")
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ClassroomJpaEntity() {
    }

    public static ClassroomJpaEntity fromDomain(Classroom classroom) {
        ClassroomJpaEntity entity = new ClassroomJpaEntity();
        entity.id = classroom.getId().value();
        entity.teacherId = classroom.getTeacherId();
        entity.name = classroom.getName();
        entity.description = classroom.getDescription();
        entity.joinCode = classroom.getJoinCode().value();
        entity.active = classroom.isActive();
        entity.createdAt = classroom.getCreatedAt();
        entity.updatedAt = classroom.getUpdatedAt();
        return entity;
    }

    public Classroom toDomain() {
        return Classroom.builder()
                .id(ClassroomId.of(id))
                .teacherId(teacherId)
                .name(name)
                .description(description)
                .joinCode(JoinCode.of(joinCode))
                .active(active)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public UUID getId() {
        return id;
    }

    public String getJoinCode() {
        return joinCode;
    }
}
