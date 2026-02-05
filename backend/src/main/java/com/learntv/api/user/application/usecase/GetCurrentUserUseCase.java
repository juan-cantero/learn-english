package com.learntv.api.user.application.usecase;

import com.learntv.api.user.application.port.UserRepository;
import com.learntv.api.user.domain.exception.UserNotFoundException;
import com.learntv.api.user.domain.model.User;

import java.util.UUID;

/**
 * Gets the current authenticated user.
 */
public class GetCurrentUserUseCase {

    private final UserRepository userRepository;

    public GetCurrentUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User execute(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }
}
