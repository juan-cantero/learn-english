package com.learntv.api.learning.adapter.in.web;

import com.learntv.api.learning.application.usecase.CheckExerciseAnswerUseCase;
import com.learntv.api.learning.application.usecase.ViewEpisodeLessonUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shows/{showSlug}/episodes")
@Tag(name = "Episodes", description = "Episode lesson operations")
public class EpisodeController {

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String DEFAULT_USER = "anonymous";

    private final ViewEpisodeLessonUseCase viewEpisodeLessonUseCase;
    private final CheckExerciseAnswerUseCase checkExerciseAnswerUseCase;

    public EpisodeController(ViewEpisodeLessonUseCase viewEpisodeLessonUseCase,
                             CheckExerciseAnswerUseCase checkExerciseAnswerUseCase) {
        this.viewEpisodeLessonUseCase = viewEpisodeLessonUseCase;
        this.checkExerciseAnswerUseCase = checkExerciseAnswerUseCase;
    }

    @GetMapping("/{episodeSlug}")
    @Operation(summary = "View episode lesson",
               description = "Returns full lesson content with user's progress")
    public ResponseEntity<LessonWithProgressResponse> getLesson(
            @RequestHeader(value = USER_ID_HEADER, defaultValue = DEFAULT_USER) String userId,
            @PathVariable String showSlug,
            @PathVariable String episodeSlug) {

        ViewEpisodeLessonUseCase.LessonWithProgress result =
                viewEpisodeLessonUseCase.execute(userId, showSlug, episodeSlug);

        return ResponseEntity.ok(LessonWithProgressResponse.fromDomain(result));
    }

    @PostMapping("/{episodeSlug}/exercises/{exerciseId}/check")
    @Operation(summary = "Check exercise answer",
               description = "Validates user answer and updates progress")
    public ResponseEntity<AnswerResultResponse> checkAnswer(
            @RequestHeader(value = USER_ID_HEADER, defaultValue = DEFAULT_USER) String userId,
            @PathVariable String showSlug,
            @PathVariable String episodeSlug,
            @PathVariable UUID exerciseId,
            @RequestBody CheckAnswerRequest request) {

        CheckExerciseAnswerUseCase.AnswerResult result =
                checkExerciseAnswerUseCase.execute(userId, showSlug, episodeSlug, exerciseId, request.answer());

        return ResponseEntity.ok(AnswerResultResponse.fromDomain(result));
    }
}
