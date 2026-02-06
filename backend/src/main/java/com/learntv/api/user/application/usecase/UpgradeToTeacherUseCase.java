package com.learntv.api.user.application.usecase;

import com.learntv.api.user.application.port.UserRepository;
import com.learntv.api.user.domain.exception.UserNotFoundException;
import com.learntv.api.user.domain.model.User;

import java.util.UUID;

/**
 * Upgrades a user from LEARNER to TEACHER role.
 */
public class UpgradeToTeacherUseCase {

    private final UserRepository userRepository;

    public UpgradeToTeacherUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User execute(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        user.upgradeToTeacher();

        return userRepository.save(user);
    }
}
