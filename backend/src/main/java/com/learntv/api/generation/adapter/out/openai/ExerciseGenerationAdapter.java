package com.learntv.api.generation.adapter.out.openai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learntv.api.generation.application.port.out.ExerciseGenerationPort;
import com.learntv.api.generation.domain.model.ExtractedExpression;
import com.learntv.api.generation.domain.model.ExtractedGrammar;
import com.learntv.api.generation.domain.model.ExtractedVocabulary;
import com.learntv.api.generation.domain.model.GeneratedExercise;
import com.learntv.api.shared.config.PromptSanitizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.learntv.api.shared.config.PromptSanitizer.sanitizeShortInput;

/**
 * OpenAI implementation of ExerciseGenerationPort.
 * Generates exercises based on extracted vocabulary, grammar, and expressions.
 */
@Component
public class ExerciseGenerationAdapter implements ExerciseGenerationPort {

    private static final Logger log = LoggerFactory.getLogger(ExerciseGenerationAdapter.class);
    private static final int MAX_FIELD_LENGTH = 500;

    private final OpenAiClient openAiClient;
    private final ObjectMapper objectMapper;

    public ExerciseGenerationAdapter(OpenAiClient openAiClient, ObjectMapper objectMapper) {
        this.openAiClient = openAiClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<GeneratedExercise> generateExercises(
            List<ExtractedVocabulary> vocabulary,
            List<ExtractedGrammar> grammar,
            List<ExtractedExpression> expressions) {

        log.info("Generating exercises from {} vocabulary, {} grammar, {} expressions",
                vocabulary.size(), grammar.size(), expressions.size());

        String systemPrompt = """
            You are an expert English teacher creating exercises for intermediate learners.
            Generate 12-15 varied exercises based on the provided vocabulary, grammar points, and expressions.

            Exercise types to include:
            1. FILL_IN_BLANK (5-6 exercises): Sentences with a blank to fill with vocabulary words
               - Provide the sentence with "___" for the blank
               - correctAnswer is the word that fills the blank
               - options should include 4 choices (including the correct one)

            2. MULTIPLE_CHOICE (4-5 exercises): Questions about definitions or grammar usage
               - Question asks about meaning or correct usage
               - 4 options with one correct answer

            3. MATCHING (1-2 exercises): Match terms with their definitions
               - question is a description like "Match the vocabulary terms with their definitions"
               - matchingPairs is an array of objects with "term" and "definition" fields (3-5 pairs per exercise)
               - correctAnswer and options should be null for MATCHING type

            4. LISTENING (2-3 exercises): Type what you hear exercises
               - question is "Listen and type what you hear: [word]"
               - correctAnswer is the vocabulary term
               - options should be null or empty for listening exercises

            Each exercise should have:
            - type: one of "FILL_IN_BLANK", "MULTIPLE_CHOICE", "MATCHING", "LISTENING"
            - question: the exercise prompt
            - correctAnswer: the correct response (null for MATCHING)
            - options: array of 4 choices (null for LISTENING and MATCHING)
            - matchingPairs: array of {"term": "...", "definition": "..."} objects (only for MATCHING, null for others)
            - points: difficulty score (1-3 points)

            Return a JSON object with an "exercises" array.
            """;

        String userPrompt = buildUserPrompt(vocabulary, grammar, expressions);

        String response = openAiClient.chatCompletion(systemPrompt, userPrompt);
        return parseExercisesResponse(response);
    }

    private String buildUserPrompt(
            List<ExtractedVocabulary> vocabulary,
            List<ExtractedGrammar> grammar,
            List<ExtractedExpression> expressions) {

        StringBuilder sb = new StringBuilder();
        sb.append("Generate exercises using this content:\n\n");

        sb.append("## Vocabulary\n");
        for (ExtractedVocabulary v : vocabulary) {
            sb.append(String.format("- %s: %s (Example: %s)\n",
                    sanitizeShortInput(v.term(), MAX_FIELD_LENGTH),
                    sanitizeShortInput(v.definition(), MAX_FIELD_LENGTH),
                    sanitizeShortInput(v.exampleSentence(), MAX_FIELD_LENGTH)));
        }

        sb.append("\n## Grammar Points\n");
        for (ExtractedGrammar g : grammar) {
            sb.append(String.format("- %s: %s\n  Structure: %s\n  Examples: %s\n",
                    sanitizeShortInput(g.title(), MAX_FIELD_LENGTH),
                    sanitizeShortInput(g.explanation(), MAX_FIELD_LENGTH),
                    sanitizeShortInput(g.structure(), MAX_FIELD_LENGTH),
                    sanitizeShortInput(String.join("; ", g.examples()), MAX_FIELD_LENGTH)));
        }

        sb.append("\n## Expressions\n");
        for (ExtractedExpression e : expressions) {
            sb.append(String.format("- \"%s\": %s\n  Usage: %s\n",
                    sanitizeShortInput(e.phrase(), MAX_FIELD_LENGTH),
                    sanitizeShortInput(e.meaning(), MAX_FIELD_LENGTH),
                    sanitizeShortInput(e.usageNote(), MAX_FIELD_LENGTH)));
        }

        return sb.toString();
    }

    private List<GeneratedExercise> parseExercisesResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode exercisesArray = root.path("exercises");
            return objectMapper.convertValue(exercisesArray, new TypeReference<List<GeneratedExercise>>() {});
        } catch (Exception e) {
            log.error("Failed to parse exercises response: {}", response, e);
            throw new RuntimeException("Failed to parse exercise generation response", e);
        }
    }
}
