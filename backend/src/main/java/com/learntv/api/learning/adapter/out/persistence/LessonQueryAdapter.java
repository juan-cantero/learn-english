package com.learntv.api.learning.adapter.out.persistence;

import com.learntv.api.learning.application.port.LessonQueryPort;
import com.learntv.api.learning.domain.model.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * Optimized query adapter for loading lessons.
 * Uses native SQL to load all lesson content in minimal queries,
 * avoiding the N+1 problem.
 */
@Repository
public class LessonQueryAdapter implements LessonQueryPort {

    private final JdbcTemplate jdbcTemplate;

    public LessonQueryAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Lesson> loadFullLesson(String showSlug, String episodeSlug) {
        // First, load the episode
        Optional<Episode> episodeOpt = loadEpisode(showSlug, episodeSlug);
        if (episodeOpt.isEmpty()) {
            return Optional.empty();
        }

        Episode episode = episodeOpt.get();
        UUID episodeId = episode.getId().value();

        // Load all related content in parallel-friendly single queries each
        // (Could be further optimized with a single mega-query if needed)
        List<Vocabulary> vocabulary = loadVocabulary(episodeId);
        List<GrammarPoint> grammarPoints = loadGrammarPoints(episodeId);
        List<Expression> expressions = loadExpressions(episodeId);
        List<Exercise> exercises = loadExercises(episodeId);

        return Optional.of(new Lesson(episode, vocabulary, grammarPoints, expressions, exercises));
    }

    private Optional<Episode> loadEpisode(String showSlug, String episodeSlug) {
        String sql = """
            SELECT id, show_id, show_slug, season_number, episode_number, title, slug, synopsis, duration_minutes
            FROM episodes
            WHERE show_slug = ? AND slug = ?
            """;

        List<Episode> episodes = jdbcTemplate.query(sql, (rs, rowNum) ->
                Episode.builder()
                        .id(EpisodeId.of(UUID.fromString(rs.getString("id"))))
                        .showId(UUID.fromString(rs.getString("show_id")))
                        .showSlug(rs.getString("show_slug"))
                        .seasonNumber(rs.getInt("season_number"))
                        .episodeNumber(rs.getInt("episode_number"))
                        .title(rs.getString("title"))
                        .slug(rs.getString("slug"))
                        .synopsis(rs.getString("synopsis"))
                        .durationMinutes(rs.getInt("duration_minutes"))
                        .build(),
                showSlug, episodeSlug);

        return episodes.isEmpty() ? Optional.empty() : Optional.of(episodes.get(0));
    }

    private List<Vocabulary> loadVocabulary(UUID episodeId) {
        String sql = """
            SELECT id, episode_id, term, definition, phonetic, category, example_sentence, context_timestamp, audio_url
            FROM vocabulary
            WHERE episode_id = ?
            ORDER BY term
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                Vocabulary.builder()
                        .id(UUID.fromString(rs.getString("id")))
                        .episodeId(UUID.fromString(rs.getString("episode_id")))
                        .term(rs.getString("term"))
                        .definition(rs.getString("definition"))
                        .phonetic(rs.getString("phonetic"))
                        .category(VocabularyCategory.valueOf(rs.getString("category")))
                        .exampleSentence(rs.getString("example_sentence"))
                        .contextTimestamp(rs.getString("context_timestamp"))
                        .audioUrl(rs.getString("audio_url"))
                        .build(),
                episodeId);
    }

    private List<GrammarPoint> loadGrammarPoints(UUID episodeId) {
        String sql = """
            SELECT id, episode_id, title, explanation, structure, example, context_quote
            FROM grammar_points
            WHERE episode_id = ?
            ORDER BY title
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                GrammarPoint.builder()
                        .id(UUID.fromString(rs.getString("id")))
                        .episodeId(UUID.fromString(rs.getString("episode_id")))
                        .title(rs.getString("title"))
                        .explanation(rs.getString("explanation"))
                        .structure(rs.getString("structure"))
                        .example(rs.getString("example"))
                        .contextQuote(rs.getString("context_quote"))
                        .build(),
                episodeId);
    }

    private List<Expression> loadExpressions(UUID episodeId) {
        String sql = """
            SELECT id, episode_id, phrase, meaning, context_quote, usage_note, audio_url
            FROM expressions
            WHERE episode_id = ?
            ORDER BY phrase
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                Expression.builder()
                        .id(UUID.fromString(rs.getString("id")))
                        .episodeId(UUID.fromString(rs.getString("episode_id")))
                        .phrase(rs.getString("phrase"))
                        .meaning(rs.getString("meaning"))
                        .contextQuote(rs.getString("context_quote"))
                        .usageNote(rs.getString("usage_note"))
                        .audioUrl(rs.getString("audio_url"))
                        .build(),
                episodeId);
    }

    private List<Exercise> loadExercises(UUID episodeId) {
        String sql = """
            SELECT id, episode_id, type, question, correct_answer, options, matching_pairs, points, audio_url
            FROM exercises
            WHERE episode_id = ?
            ORDER BY type, question
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                Exercise.builder()
                        .id(UUID.fromString(rs.getString("id")))
                        .episodeId(UUID.fromString(rs.getString("episode_id")))
                        .type(ExerciseType.valueOf(rs.getString("type")))
                        .question(rs.getString("question"))
                        .correctAnswer(rs.getString("correct_answer"))
                        .options(rs.getString("options"))
                        .matchingPairs(rs.getString("matching_pairs"))
                        .points(rs.getInt("points"))
                        .audioUrl(rs.getString("audio_url"))
                        .build(),
                episodeId);
    }
}
