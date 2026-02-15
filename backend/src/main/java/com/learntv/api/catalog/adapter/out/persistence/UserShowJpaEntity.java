package com.learntv.api.catalog.adapter.out.persistence;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_shows")
public class UserShowJpaEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "show_id", nullable = false)
    private UUID showId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected UserShowJpaEntity() {
    }

    public static UserShowJpaEntity create(UUID userId, UUID showId) {
        UserShowJpaEntity entity = new UserShowJpaEntity();
        entity.id = UUID.randomUUID();
        entity.userId = userId;
        entity.showId = showId;
        entity.createdAt = Instant.now();
        return entity;
    }

    // Getters for JPA
    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getShowId() {
        return showId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
