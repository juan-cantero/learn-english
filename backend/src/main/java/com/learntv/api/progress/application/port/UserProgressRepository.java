package com.learntv.api.progress.application.port;

import com.learntv.api.progress.domain.model.UserProgress;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserProgressRepository {

    Optional<UserProgress> findByUserIdAndEpisodeId(String userId, UUID episodeId);

    List<UserProgress> findByUserId(String userId);

    UserProgress save(UserProgress progress);
}
