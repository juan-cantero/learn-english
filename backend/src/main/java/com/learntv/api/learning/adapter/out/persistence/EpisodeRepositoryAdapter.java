package com.learntv.api.learning.adapter.out.persistence;

import com.learntv.api.learning.application.port.EpisodeRepository;
import com.learntv.api.learning.domain.model.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class EpisodeRepositoryAdapter implements EpisodeRepository {

    private final EpisodeJpaRepository episodeJpaRepository;
    private final VocabularyJpaRepository vocabularyJpaRepository;
    private final GrammarPointJpaRepository grammarPointJpaRepository;
    private final ExpressionJpaRepository expressionJpaRepository;
    private final ExerciseJpaRepository exerciseJpaRepository;

    public EpisodeRepositoryAdapter(EpisodeJpaRepository episodeJpaRepository,
                                    VocabularyJpaRepository vocabularyJpaRepository,
                                    GrammarPointJpaRepository grammarPointJpaRepository,
                                    ExpressionJpaRepository expressionJpaRepository,
                                    ExerciseJpaRepository exerciseJpaRepository) {
        this.episodeJpaRepository = episodeJpaRepository;
        this.vocabularyJpaRepository = vocabularyJpaRepository;
        this.grammarPointJpaRepository = grammarPointJpaRepository;
        this.expressionJpaRepository = expressionJpaRepository;
        this.exerciseJpaRepository = exerciseJpaRepository;
    }

    @Override
    public Optional<Episode> findByShowSlugAndEpisodeSlug(String showSlug, String episodeSlug) {
        return episodeJpaRepository.findByShowSlugAndSlug(showSlug, episodeSlug)
                .map(EpisodeJpaEntity::toDomain);
    }

    @Override
    public List<Episode> findByShowId(UUID showId) {
        return episodeJpaRepository.findByShowIdOrderBySeasonNumberAscEpisodeNumberAsc(showId).stream()
                .map(EpisodeJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<Vocabulary> findVocabularyByEpisodeId(UUID episodeId) {
        return vocabularyJpaRepository.findByEpisodeId(episodeId).stream()
                .map(VocabularyJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<GrammarPoint> findGrammarPointsByEpisodeId(UUID episodeId) {
        return grammarPointJpaRepository.findByEpisodeId(episodeId).stream()
                .map(GrammarPointJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<Expression> findExpressionsByEpisodeId(UUID episodeId) {
        return expressionJpaRepository.findByEpisodeId(episodeId).stream()
                .map(ExpressionJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<Exercise> findExercisesByEpisodeId(UUID episodeId) {
        return exerciseJpaRepository.findByEpisodeId(episodeId).stream()
                .map(ExerciseJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<Exercise> findExerciseById(UUID exerciseId) {
        return exerciseJpaRepository.findById(exerciseId)
                .map(ExerciseJpaEntity::toDomain);
    }

    @Override
    public Episode save(Episode episode) {
        EpisodeJpaEntity entity = EpisodeJpaEntity.fromDomain(episode);
        EpisodeJpaEntity saved = episodeJpaRepository.save(entity);
        return saved.toDomain();
    }
}
