package com.learntv.api.progress.adapter.in.web;

import com.learntv.api.learning.application.port.EpisodeRepository;
import com.learntv.api.learning.domain.model.Episode;
import com.learntv.api.progress.application.usecase.GetUserProgressUseCase;
import com.learntv.api.progress.application.usecase.UpdateProgressUseCase;
import com.learntv.api.progress.domain.model.ProgressSnapshot;
import com.learntv.api.progress.domain.model.UserProgress;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/progress")
@Tag(name = "Progress", description = "User progress tracking operations")
public class ProgressController {

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String DEFAULT_USER = "anonymous";

    private final GetUserProgressUseCase getUserProgressUseCase;
    private final UpdateProgressUseCase updateProgressUseCase;
    private final EpisodeRepository episodeRepository;

    public ProgressController(GetUserProgressUseCase getUserProgressUseCase,
                              UpdateProgressUseCase updateProgressUseCase,
                              EpisodeRepository episodeRepository) {
        this.getUserProgressUseCase = getUserProgressUseCase;
        this.updateProgressUseCase = updateProgressUseCase;
        this.episodeRepository = episodeRepository;
    }

    @GetMapping
    @Operation(summary = "Get user progress", description = "Returns all progress for the current user")
    public ResponseEntity<ProgressSnapshotResponse> getUserProgress(
            @RequestHeader(value = USER_ID_HEADER, defaultValue = DEFAULT_USER) String userId) {
        ProgressSnapshot snapshot = getUserProgressUseCase.execute(userId);

        // Fetch episode metadata for all progress entries
        List<UUID> episodeIds = snapshot.episodeProgress().stream()
                .map(UserProgress::getEpisodeId)
                .toList();

        Map<UUID, Episode> episodesById = episodeRepository.findAllByIds(episodeIds).stream()
                .collect(Collectors.toMap(
                        episode -> episode.getId().value(),
                        Function.identity()
                ));

        return ResponseEntity.ok(ProgressSnapshotResponse.fromDomain(snapshot, episodesById));
    }

    @GetMapping("/{episodeId}")
    @Operation(summary = "Get episode progress", description = "Returns progress for a specific episode")
    public ResponseEntity<UserProgressResponse> getEpisodeProgress(
            @RequestHeader(value = USER_ID_HEADER, defaultValue = DEFAULT_USER) String userId,
            @PathVariable UUID episodeId) {
        return getUserProgressUseCase.execute(userId, episodeId)
                .map(progress -> {
                    Episode episode = episodeRepository.findById(episodeId).orElse(null);
                    return UserProgressResponse.fromDomain(progress, episode);
                })
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{episodeId}")
    @Operation(summary = "Update progress", description = "Updates progress for a specific episode")
    public ResponseEntity<UserProgressResponse> updateProgress(
            @RequestHeader(value = USER_ID_HEADER, defaultValue = DEFAULT_USER) String userId,
            @PathVariable UUID episodeId,
            @RequestBody SaveProgressRequest request) {

        UpdateProgressUseCase.ProgressUpdate update = new UpdateProgressUseCase.ProgressUpdate(
                request.category(),
                request.points(),
                request.completed() != null && request.completed()
        );

        UserProgress saved = updateProgressUseCase.execute(userId, episodeId, update);
        Episode episode = episodeRepository.findById(episodeId).orElse(null);
        return ResponseEntity.ok(UserProgressResponse.fromDomain(saved, episode));
    }
}
