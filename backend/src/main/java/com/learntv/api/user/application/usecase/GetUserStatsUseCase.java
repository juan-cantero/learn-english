package com.learntv.api.user.application.usecase;

import com.learntv.api.user.application.port.UserStatsRepository;
import com.learntv.api.user.domain.model.UserStats;

import java.util.UUID;

/**
 * Gets user statistics.
 */
public class GetUserStatsUseCase {

    private final UserStatsRepository userStatsRepository;

    public GetUserStatsUseCase(UserStatsRepository userStatsRepository) {
        this.userStatsRepository = userStatsRepository;
    }

    public UserStats execute(UUID userId) {
        return userStatsRepository.findByUserId(userId)
                .orElseGet(() -> UserStats.createEmpty(userId));
    }
}
