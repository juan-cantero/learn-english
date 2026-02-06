package com.learntv.api.user.application.port;

import com.learntv.api.user.domain.model.UserStats;

import java.util.Optional;
import java.util.UUID;

public interface UserStatsRepository {

    Optional<UserStats> findByUserId(UUID userId);

    UserStats save(UserStats stats);
}
