package com.learntv.api.user.adapter.in.web;

import com.learntv.api.user.domain.model.User;

import java.time.Instant;

public record UserResponse(
        String id,
        String email,
        String displayName,
        String avatarUrl,
        String role,
        String preferredDifficulty,
        Instant createdAt,
        Instant updatedAt
) {
    public static UserResponse fromDomain(User user) {
        return new UserResponse(
                user.getId().toString(),
                user.getEmail(),
                user.getDisplayName(),
                user.getAvatarUrl(),
                user.getRole().name(),
                user.getPreferredDifficulty(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
