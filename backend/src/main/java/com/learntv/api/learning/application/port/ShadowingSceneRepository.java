package com.learntv.api.learning.application.port;

import com.learntv.api.generation.domain.model.DialogueLine;
import com.learntv.api.generation.domain.model.ExtractedScene;

import java.util.List;
import java.util.UUID;

public interface ShadowingSceneRepository {

    List<ShadowingScene> findByEpisodeId(UUID episodeId);

    boolean existsByEpisodeId(UUID episodeId);

    void saveAll(UUID episodeId, List<ExtractedScene> scenes);

    record ShadowingScene(
            UUID id,
            UUID episodeId,
            int sceneIndex,
            String title,
            List<DialogueLine> lines,
            List<String> characters
    ) {}
}
