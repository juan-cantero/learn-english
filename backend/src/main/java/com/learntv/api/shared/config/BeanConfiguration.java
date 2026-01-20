package com.learntv.api.shared.config;

import com.learntv.api.catalog.application.port.ShowRepository;
import com.learntv.api.catalog.application.usecase.BrowseCatalogUseCase;
import com.learntv.api.catalog.application.usecase.ViewShowDetailsUseCase;
import com.learntv.api.learning.application.port.EpisodeRepository;
import com.learntv.api.learning.application.port.LessonQueryPort;
import com.learntv.api.learning.application.usecase.CheckExerciseAnswerUseCase;
import com.learntv.api.learning.application.usecase.ViewEpisodeLessonUseCase;
import com.learntv.api.progress.application.port.UserProgressRepository;
import com.learntv.api.progress.application.usecase.GetUserProgressUseCase;
import com.learntv.api.progress.application.usecase.UpdateProgressUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    // ==================== Catalog Use Cases ====================

    @Bean
    public BrowseCatalogUseCase browseCatalogUseCase(ShowRepository showRepository) {
        return new BrowseCatalogUseCase(showRepository);
    }

    @Bean
    public ViewShowDetailsUseCase viewShowDetailsUseCase(ShowRepository showRepository,
                                                          EpisodeRepository episodeRepository) {
        return new ViewShowDetailsUseCase(showRepository, episodeRepository);
    }

    // ==================== Learning Use Cases ====================

    @Bean
    public ViewEpisodeLessonUseCase viewEpisodeLessonUseCase(LessonQueryPort lessonQueryPort,
                                                              UserProgressRepository progressRepository) {
        return new ViewEpisodeLessonUseCase(lessonQueryPort, progressRepository);
    }

    @Bean
    public CheckExerciseAnswerUseCase checkExerciseAnswerUseCase(LessonQueryPort lessonQueryPort,
                                                                   UserProgressRepository progressRepository) {
        return new CheckExerciseAnswerUseCase(lessonQueryPort, progressRepository);
    }

    // ==================== Progress Use Cases ====================

    @Bean
    public GetUserProgressUseCase getUserProgressUseCase(UserProgressRepository progressRepository) {
        return new GetUserProgressUseCase(progressRepository);
    }

    @Bean
    public UpdateProgressUseCase updateProgressUseCase(UserProgressRepository progressRepository) {
        return new UpdateProgressUseCase(progressRepository);
    }
}
