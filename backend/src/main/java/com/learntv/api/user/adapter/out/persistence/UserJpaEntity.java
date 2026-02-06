package com.learntv.api.user.adapter.out.persistence;

import com.learntv.api.user.domain.model.User;
import com.learntv.api.user.domain.model.UserId;
import com.learntv.api.user.domain.model.UserRole;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(name = "preferred_difficulty")
    private String preferredDifficulty;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected UserJpaEntity() {
    }

    public static UserJpaEntity fromDomain(User user) {
        UserJpaEntity entity = new UserJpaEntity();
        entity.id = user.getId().value();
        entity.email = user.getEmail();
        entity.displayName = user.getDisplayName();
        entity.avatarUrl = user.getAvatarUrl();
        entity.role = user.getRole();
        entity.preferredDifficulty = user.getPreferredDifficulty();
        entity.createdAt = user.getCreatedAt();
        entity.updatedAt = user.getUpdatedAt();
        return entity;
    }

    public User toDomain() {
        return User.builder()
                .id(UserId.of(id))
                .email(email)
                .displayName(displayName)
                .avatarUrl(avatarUrl)
                .role(role)
                .preferredDifficulty(preferredDifficulty)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    // Getters for JPA
    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }
}
