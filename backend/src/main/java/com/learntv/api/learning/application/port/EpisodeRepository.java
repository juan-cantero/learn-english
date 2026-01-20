package com.learntv.api.learning.application.port;

import com.learntv.api.learning.domain.model.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EpisodeRepository {

    Optional<Episode> findByShowSlugAndEpisodeSlug(String showSlug, String episodeSlug);

    List<Episode> findByShowId(UUID showId);

    List<Vocabulary> findVocabularyByEpisodeId(UUID episodeId);

    List<GrammarPoint> findGrammarPointsByEpisodeId(UUID episodeId);

    List<Expression> findExpressionsByEpisodeId(UUID episodeId);

    List<Exercise> findExercisesByEpisodeId(UUID episodeId);

    Optional<Exercise> findExerciseById(UUID exerciseId);

    Episode save(Episode episode);
}
