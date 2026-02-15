package com.learntv.api.learning.adapter.in.web;

import com.learntv.api.learning.application.service.PhonemeService;
import com.learntv.api.learning.application.usecase.CheckExerciseAnswerUseCase;
import com.learntv.api.learning.application.usecase.ViewEpisodeLessonUseCase;
import com.learntv.api.shared.config.security.AuthenticatedUser;
import com.learntv.api.shared.config.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shows/{showSlug}/episodes")
@Tag(name = "Episodes", description = "Episode lesson operations")
public class EpisodeController {

    private final ViewEpisodeLessonUseCase viewEpisodeLessonUseCase;
    private final CheckExerciseAnswerUseCase checkExerciseAnswerUseCase;
    private final PhonemeService phonemeService;

    public EpisodeController(ViewEpisodeLessonUseCase viewEpisodeLessonUseCase,
                             CheckExerciseAnswerUseCase checkExerciseAnswerUseCase,
                             PhonemeService phonemeService) {
        this.viewEpisodeLessonUseCase = viewEpisodeLessonUseCase;
        this.checkExerciseAnswerUseCase = checkExerciseAnswerUseCase;
        this.phonemeService = phonemeService;
    }

    @GetMapping("/{episodeSlug}")
    @Operation(summary = "View episode lesson",
               description = "Returns full lesson content with user's progress")
    public ResponseEntity<LessonWithProgressResponse> getLesson(
            @CurrentUser AuthenticatedUser authUser,
            @PathVariable String showSlug,
            @PathVariable String episodeSlug) {

        ViewEpisodeLessonUseCase.LessonWithProgress result =
                viewEpisodeLessonUseCase.execute(authUser.id(), showSlug, episodeSlug);

        return ResponseEntity.ok(LessonWithProgressResponse.fromDomain(result, phonemeService));
    }

    @PostMapping("/{episodeSlug}/exercises/{exerciseId}/check")
    @Operation(summary = "Check exercise answer",
               description = "Validates user answer and updates progress")
    public ResponseEntity<AnswerResultResponse> checkAnswer(
            @CurrentUser AuthenticatedUser authUser,
            @PathVariable String showSlug,
            @PathVariable String episodeSlug,
            @PathVariable UUID exerciseId,
            @RequestBody CheckAnswerRequest request) {

        CheckExerciseAnswerUseCase.AnswerResult result =
                checkExerciseAnswerUseCase.execute(authUser.id(), showSlug, episodeSlug, exerciseId, request.answer());

        return ResponseEntity.ok(AnswerResultResponse.fromDomain(result));
    }
}
