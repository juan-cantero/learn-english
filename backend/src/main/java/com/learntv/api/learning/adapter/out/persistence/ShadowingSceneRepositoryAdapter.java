package com.learntv.api.learning.adapter.out.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learntv.api.generation.domain.model.DialogueLine;
import com.learntv.api.generation.domain.model.ExtractedScene;
import com.learntv.api.learning.application.port.ShadowingSceneRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
public class ShadowingSceneRepositoryAdapter implements ShadowingSceneRepository {

    private static final Logger log = LoggerFactory.getLogger(ShadowingSceneRepositoryAdapter.class);

    private final ShadowingSceneJpaRepository jpaRepository;
    private final ObjectMapper objectMapper;

    public ShadowingSceneRepositoryAdapter(ShadowingSceneJpaRepository jpaRepository,
                                            ObjectMapper objectMapper) {
        this.jpaRepository = jpaRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<ShadowingScene> findByEpisodeId(UUID episodeId) {
        return jpaRepository.findByEpisodeIdOrderBySceneIndex(episodeId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public boolean existsByEpisodeId(UUID episodeId) {
        return jpaRepository.existsByEpisodeId(episodeId);
    }

    @Override
    public void saveAll(UUID episodeId, List<ExtractedScene> scenes) {
        for (int i = 0; i < scenes.size(); i++) {
            ExtractedScene scene = scenes.get(i);
            String linesJson;
            try {
                linesJson = objectMapper.writeValueAsString(scene.lines());
            } catch (Exception e) {
                log.error("Failed to serialize scene lines", e);
                throw new RuntimeException("Failed to serialize scene lines", e);
            }

            String charactersStr = String.join(",", scene.characters());

            ShadowingSceneJpaEntity entity = new ShadowingSceneJpaEntity(
                    UUID.randomUUID(),
                    episodeId,
                    i,
                    scene.title(),
                    linesJson,
                    charactersStr
            );
            jpaRepository.save(entity);
        }
    }

    private ShadowingScene toDomain(ShadowingSceneJpaEntity entity) {
        List<DialogueLine> lines;
        try {
            lines = objectMapper.readValue(entity.getLines(), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Failed to deserialize scene lines for scene {}", entity.getId(), e);
            throw new RuntimeException("Failed to deserialize scene lines", e);
        }

        List<String> characters = Arrays.asList(entity.getCharacters().split(","));

        return new ShadowingScene(
                entity.getId(),
                entity.getEpisodeId(),
                entity.getSceneIndex(),
                entity.getTitle(),
                lines,
                characters
        );
    }
}
