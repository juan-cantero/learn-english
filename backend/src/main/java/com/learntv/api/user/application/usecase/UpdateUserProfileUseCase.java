package com.learntv.api.user.application.usecase;

import com.learntv.api.user.application.port.UserRepository;
import com.learntv.api.user.domain.exception.UserNotFoundException;
import com.learntv.api.user.domain.model.User;

import java.util.UUID;

/**
 * Updates a user's profile information.
 */
public class UpdateUserProfileUseCase {

    private final UserRepository userRepository;

    public UpdateUserProfileUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User execute(UUID userId, UpdateProfileCommand command) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        user.updateProfile(
                command.displayName(),
                command.avatarUrl(),
                command.preferredDifficulty()
        );

        return userRepository.save(user);
    }

    public record UpdateProfileCommand(
            String displayName,
            String avatarUrl,
            String preferredDifficulty
    ) {}
}
