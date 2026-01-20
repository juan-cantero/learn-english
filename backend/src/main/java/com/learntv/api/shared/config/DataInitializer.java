package com.learntv.api.shared.config;

import com.learntv.api.catalog.application.port.ShowRepository;
import com.learntv.api.catalog.domain.model.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

@Configuration
@Profile("!production")
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(ShowRepository showRepository, JdbcTemplate jdbcTemplate) {
        return args -> {
            // Check if data already exists (idempotent)
            if (showRepository.findBySlug("the-pitt").isPresent()) {
                System.out.println("Sample data already exists, skipping initialization");
                return;
            }

            System.out.println("Initializing sample data...");

            // Create The Pitt show
            Show thePitt = Show.builder()
                    .id(ShowId.generate())
                    .title("The Pitt")
                    .slug("the-pitt")
                    .description("A medical drama set in a Pittsburgh hospital, following the staff through intense 15-hour shifts in the emergency room.")
                    .genre(Genre.MEDICAL)
                    .accent(AccentType.AMERICAN)
                    .difficulty(DifficultyLevel.INTERMEDIATE)
                    .imageUrl("/images/the-pitt.jpg")
                    .totalSeasons(1)
                    .totalEpisodes(15)
                    .build();

            Show savedShow = showRepository.save(thePitt);
            UUID showId = savedShow.getId().value();
            UUID episodeId = UUID.randomUUID();

            // Insert episode
            String showSlug = "the-pitt";
            jdbcTemplate.update("""
                INSERT INTO episodes (id, show_id, show_slug, season_number, episode_number, title, slug, synopsis, duration_minutes)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                episodeId, showId, showSlug, 1, 1, "7:00 AM - 8:00 AM", "s01e01",
                "The pilot episode follows Dr. Michael \"Robby\" Rabinavitch through the first hour of his 15-hour shift in the ER.",
                45);

            // Insert vocabulary
            insertVocabulary(jdbcTemplate, episodeId);

            // Insert grammar points
            insertGrammarPoints(jdbcTemplate, episodeId);

            // Insert expressions
            insertExpressions(jdbcTemplate, episodeId);

            // Insert exercises
            insertExercises(jdbcTemplate, episodeId);

            System.out.println("Sample data initialized for The Pitt S01E01");
        };
    }

    private void insertVocabulary(JdbcTemplate jdbc, UUID episodeId) {
        jdbc.update("""
            INSERT INTO vocabulary (id, episode_id, term, definition, phonetic, category, example_sentence, context_timestamp)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """, UUID.randomUUID(), episodeId, "triage",
            "The process of sorting patients by urgency of care needed",
            "/ˈtriːɑːʒ/", "MEDICAL",
            "We need to triage these patients immediately.", "00:05:30");

        jdbc.update("""
            INSERT INTO vocabulary (id, episode_id, term, definition, phonetic, category, example_sentence, context_timestamp)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """, UUID.randomUUID(), episodeId, "code blue",
            "Emergency alert for cardiac or respiratory arrest",
            null, "MEDICAL",
            "Code blue in room 4!", "00:12:45");

        jdbc.update("""
            INSERT INTO vocabulary (id, episode_id, term, definition, phonetic, category, example_sentence, context_timestamp)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """, UUID.randomUUID(), episodeId, "stat",
            "Immediately, without delay (from Latin 'statim')",
            "/stæt/", "MEDICAL",
            "I need those labs stat!", "00:08:20");

        jdbc.update("""
            INSERT INTO vocabulary (id, episode_id, term, definition, phonetic, category, example_sentence, context_timestamp)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """, UUID.randomUUID(), episodeId, "intubate",
            "To insert a tube into the trachea to help patient breathe",
            "/ˈɪntjubeɪt/", "MEDICAL",
            "We need to intubate now!", "00:15:10");
    }

    private void insertGrammarPoints(JdbcTemplate jdbc, UUID episodeId) {
        jdbc.update("""
            INSERT INTO grammar_points (id, episode_id, title, explanation, structure, example, context_quote)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """, UUID.randomUUID(), episodeId,
            "Present Continuous for Ongoing Actions",
            "Used to describe actions happening right now, especially in urgent medical situations.",
            "Subject + am/is/are + verb-ing",
            "The patient is crashing!",
            "He's coding! Get the crash cart!");

        jdbc.update("""
            INSERT INTO grammar_points (id, episode_id, title, explanation, structure, example, context_quote)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """, UUID.randomUUID(), episodeId,
            "Imperative for Commands",
            "Direct commands without subject, common in emergency situations.",
            "Base verb + object",
            "Push one of epi!",
            "Get me a central line kit!");
    }

    private void insertExpressions(JdbcTemplate jdbc, UUID episodeId) {
        jdbc.update("""
            INSERT INTO expressions (id, episode_id, phrase, meaning, context_quote, usage_note)
            VALUES (?, ?, ?, ?, ?, ?)
            """, UUID.randomUUID(), episodeId,
            "push meds",
            "To administer medication quickly through an IV",
            "Push the epi now!",
            "Common in emergency medical contexts");

        jdbc.update("""
            INSERT INTO expressions (id, episode_id, phrase, meaning, context_quote, usage_note)
            VALUES (?, ?, ?, ?, ?, ?)
            """, UUID.randomUUID(), episodeId,
            "the patient is crashing",
            "The patient's vital signs are rapidly deteriorating",
            "She's crashing! We need to move now!",
            "Informal but standard medical terminology");
    }

    private void insertExercises(JdbcTemplate jdbc, UUID episodeId) {
        jdbc.update("""
            INSERT INTO exercises (id, episode_id, type, question, correct_answer, options, matching_pairs, points)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """, UUID.randomUUID(), episodeId, "FILL_IN_BLANK",
            "We need to _____ these patients by urgency.",
            "triage", null, null, 10);

        jdbc.update("""
            INSERT INTO exercises (id, episode_id, type, question, correct_answer, options, matching_pairs, points)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """, UUID.randomUUID(), episodeId, "MULTIPLE_CHOICE",
            "What does 'stat' mean in medical context?",
            "Immediately",
            "[\"Immediately\",\"Later\",\"Sometimes\",\"Never\"]",
            null, 10);

        jdbc.update("""
            INSERT INTO exercises (id, episode_id, type, question, correct_answer, options, matching_pairs, points)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """, UUID.randomUUID(), episodeId, "MATCHING",
            "Match the medical terms with their meanings",
            null, null,
            "[{\"term\":\"triage\",\"definition\":\"sorting patients by urgency\"},{\"term\":\"intubate\",\"definition\":\"insert breathing tube\"},{\"term\":\"code blue\",\"definition\":\"cardiac arrest alert\"}]",
            15);
    }
}
