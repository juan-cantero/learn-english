package com.learntv.api.user.adapter.in.web;

public record UpdateProfileRequest(
        String displayName,
        String avatarUrl,
        String preferredDifficulty
) {
}
