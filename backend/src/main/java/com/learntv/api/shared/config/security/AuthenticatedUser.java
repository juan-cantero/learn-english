package com.learntv.api.shared.config.security;

import java.security.Principal;
import java.util.UUID;

public record AuthenticatedUser(
        UUID id,
        String email,
        UserRole role
) implements Principal {

    @Override
    public String getName() {
        return id.toString();
    }

    public boolean isTeacher() {
        return role == UserRole.TEACHER;
    }

    public boolean isLearner() {
        return role == UserRole.LEARNER;
    }
}
