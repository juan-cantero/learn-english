package com.learntv.api.learning.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learntv.api.generation.adapter.out.openai.OpenAiClient;
import com.learntv.api.learning.adapter.out.openai.WhisperAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for evaluating pronunciation using Whisper API transcription.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PronunciationService {

    private static final double SIMILARITY_THRESHOLD = 0.80;

    private final WhisperAdapter whisperAdapter;
    private final OpenAiClient openAiClient;
    private final ObjectMapper objectMapper;

    /**
     * Evaluate pronunciation by comparing audio transcription to expected text.
     *
     * @param audioData    Audio file bytes
     * @param filename     Original filename
     * @param expectedText The text the user was supposed to say
     * @return Evaluation result with transcription and similarity score
     */
    public PronunciationResult evaluate(byte[] audioData, String filename, String expectedText) {
        log.debug("Evaluating pronunciation, expected: {}", expectedText);

        String transcription = whisperAdapter.transcribe(audioData, filename, expectedText);
        log.debug("Received transcription: {}", transcription);

        String normalizedTranscription = normalize(transcription);
        String normalizedExpected = normalize(expectedText);

        double similarity = calculateSimilarity(normalizedTranscription, normalizedExpected);
        boolean passed = similarity >= SIMILARITY_THRESHOLD;

        log.info("Pronunciation evaluation - Expected: '{}', Transcribed: '{}', Similarity: {}, Passed: {}",
                expectedText, transcription, similarity, passed);

        List<String> suggestions = generateFeedback(transcription, expectedText, similarity);

        return new PronunciationResult(transcription, expectedText, similarity, passed, suggestions);
    }

    /**
     * Calculate similarity between two strings using Levenshtein distance.
     *
     * @param a First string
     * @param b Second string
     * @return Similarity score between 0.0 and 1.0
     */
    private double calculateSimilarity(String a, String b) {
        if (a.equals(b)) {
            return 1.0;
        }

        int distance = levenshteinDistance(a, b);
        int maxLength = Math.max(a.length(), b.length());

        if (maxLength == 0) {
            return 1.0;
        }

        return 1.0 - ((double) distance / maxLength);
    }

    /**
     * Normalize text for comparison: lowercase, trim, remove punctuation.
     */
    private String normalize(String text) {
        return text.toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", " ");
    }

    /**
     * Calculate Levenshtein distance between two strings.
     * Standard dynamic programming algorithm.
     */
    private int levenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= b.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }

        return dp[a.length()][b.length()];
    }

    private List<String> generateFeedback(String transcription, String expectedText, double similarity) {
        if (similarity >= 0.95) {
            return List.of();
        }

        String normalizedTranscription = normalize(transcription);
        String normalizedExpected = normalize(expectedText);
        int spokenWords = normalizedTranscription.split("\\s+").length;
        int expectedWords = normalizedExpected.split("\\s+").length;

        try {
            String systemPrompt = """
                You are an English pronunciation coach for a non-native speaker (Spanish speaker).
                The student tried to say a word or phrase and an AI transcriber heard something different.
                Note: the transcription may be inaccurate — it's from speech recognition, not perfect.

                Your job:
                1. Infer what the student likely mispronounced based on what was heard vs expected.
                2. Give 1-3 specific, actionable tips focused on the EXPECTED word/phrase.
                3. Explain HOW to physically produce the correct sounds (tongue position, lip shape, etc.).
                4. If relevant, mention common mistakes Spanish speakers make with these sounds.

                Important observations to consider:
                - If the word count differs (e.g. heard 1 word but expected 2), the student may have merged words together.
                - If the transcription is completely unrelated, the student's pronunciation was likely very far off — focus tips on the expected text.

                Respond in JSON: {"suggestions": ["tip1", "tip2"]}
                Keep each tip under 25 words. Be specific, not generic.
                """;

            String userPrompt = String.format(
                    "Heard: \"%s\" (%d word%s)\nExpected: \"%s\" (%d word%s)\nSimilarity: %.0f%%",
                    transcription, spokenWords, spokenWords != 1 ? "s" : "",
                    expectedText, expectedWords, expectedWords != 1 ? "s" : "",
                    similarity * 100);

            String response = openAiClient.chatCompletion(systemPrompt, userPrompt);
            JsonNode root = objectMapper.readTree(response);
            JsonNode suggestionsNode = root.path("suggestions");

            List<String> suggestions = new ArrayList<>();
            if (suggestionsNode.isArray()) {
                for (JsonNode node : suggestionsNode) {
                    suggestions.add(node.asText());
                }
            }
            return suggestions;
        } catch (Exception e) {
            log.warn("Failed to generate pronunciation feedback: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * Result of pronunciation evaluation.
     *
     * @param transcription What was actually said (from Whisper)
     * @param expectedText  What should have been said
     * @param similarity    Similarity score (0.0 to 1.0)
     * @param passed        Whether similarity meets threshold
     * @param suggestions   Pronunciation improvement tips from AI
     */
    public record PronunciationResult(
            String transcription,
            String expectedText,
            double similarity,
            boolean passed,
            List<String> suggestions
    ) {}
}
