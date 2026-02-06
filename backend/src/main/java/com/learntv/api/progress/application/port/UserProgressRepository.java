package com.learntv.api.progress.application.port;

import com.learntv.api.progress.domain.model.UserProgress;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserProgressRepository {

    Optional<UserProgress> findByUserIdAndEpisodeId(UUID userId, UUID episodeId);

    List<UserProgress> findByUserId(UUID userId);

    UserProgress save(UserProgress progress);
}
