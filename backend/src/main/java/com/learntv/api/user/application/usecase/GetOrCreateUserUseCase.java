package com.learntv.api.user.application.usecase;

import com.learntv.api.user.application.port.UserRepository;
import com.learntv.api.user.application.port.UserStatsRepository;
import com.learntv.api.user.domain.model.User;
import com.learntv.api.user.domain.model.UserId;
import com.learntv.api.user.domain.model.UserRole;
import com.learntv.api.user.domain.model.UserStats;

import java.util.UUID;

/**
 * Gets an existing user or creates a new one from authentication data.
 * This is called on every authenticated request to ensure the user exists in our database.
 */
public class GetOrCreateUserUseCase {

    private final UserRepository userRepository;
    private final UserStatsRepository userStatsRepository;

    public GetOrCreateUserUseCase(UserRepository userRepository, UserStatsRepository userStatsRepository) {
        this.userRepository = userRepository;
        this.userStatsRepository = userStatsRepository;
    }

    public User execute(UUID userId, String email, UserRole role) {
        return userRepository.findById(userId)
                .orElseGet(() -> createUser(userId, email, role));
    }

    private User createUser(UUID userId, String email, UserRole role) {
        User newUser = User.builder()
                .id(UserId.of(userId))
                .email(email)
                .role(role)
                .build();

        User savedUser = userRepository.save(newUser);

        // Also create empty stats for the user
        UserStats stats = UserStats.createEmpty(userId);
        userStatsRepository.save(stats);

        return savedUser;
    }
}
