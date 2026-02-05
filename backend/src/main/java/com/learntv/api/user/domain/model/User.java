package com.learntv.api.user.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class User {

    private final UserId id;
    private final String email;
    private String displayName;
    private String avatarUrl;
    private UserRole role;
    private String preferredDifficulty;
    private final Instant createdAt;
    private Instant updatedAt;

    private User(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "id is required");
        this.email = Objects.requireNonNull(builder.email, "email is required");
        this.displayName = builder.displayName;
        this.avatarUrl = builder.avatarUrl;
        this.role = builder.role != null ? builder.role : UserRole.LEARNER;
        this.preferredDifficulty = builder.preferredDifficulty;
        this.createdAt = builder.createdAt != null ? builder.createdAt : Instant.now();
        this.updatedAt = builder.updatedAt != null ? builder.updatedAt : Instant.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public void updateProfile(String displayName, String avatarUrl, String preferredDifficulty) {
        if (displayName != null) {
            this.displayName = displayName;
        }
        if (avatarUrl != null) {
            this.avatarUrl = avatarUrl;
        }
        if (preferredDifficulty != null) {
            this.preferredDifficulty = preferredDifficulty;
        }
        this.updatedAt = Instant.now();
    }

    public void upgradeToTeacher() {
        if (this.role == UserRole.TEACHER) {
            throw new IllegalStateException("User is already a teacher");
        }
        this.role = UserRole.TEACHER;
        this.updatedAt = Instant.now();
    }

    public boolean isTeacher() {
        return role == UserRole.TEACHER;
    }

    public boolean isLearner() {
        return role == UserRole.LEARNER;
    }

    // Getters
    public UserId getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public UserRole getRole() {
        return role;
    }

    public String getPreferredDifficulty() {
        return preferredDifficulty;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public static class Builder {
        private UserId id;
        private String email;
        private String displayName;
        private String avatarUrl;
        private UserRole role;
        private String preferredDifficulty;
        private Instant createdAt;
        private Instant updatedAt;

        public Builder id(UserId id) {
            this.id = id;
            return this;
        }

        public Builder id(UUID id) {
            this.id = UserId.of(id);
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder avatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
            return this;
        }

        public Builder role(UserRole role) {
            this.role = role;
            return this;
        }

        public Builder preferredDifficulty(String preferredDifficulty) {
            this.preferredDifficulty = preferredDifficulty;
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

        public User build() {
            return new User(this);
        }
    }
}
