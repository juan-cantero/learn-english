package com.learntv.api.generation.adapter.out.openai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learntv.api.generation.application.port.out.ShadowingExtractionPort;
import com.learntv.api.generation.domain.model.ExtractedScene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ShadowingExtractionAdapter implements ShadowingExtractionPort {

    private static final Logger log = LoggerFactory.getLogger(ShadowingExtractionAdapter.class);

    private final OpenAiClient openAiClient;
    private final ObjectMapper objectMapper;

    public ShadowingExtractionAdapter(OpenAiClient openAiClient, ObjectMapper objectMapper) {
        this.openAiClient = openAiClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<ExtractedScene> extractShadowingScenes(String rawSrt,
                                                        List<String> vocabularyTerms,
                                                        List<String> expressions) {
        log.info("Extracting shadowing scenes from SRT script");

        String systemPrompt = """
            You are an expert English teacher designing shadowing practice exercises from TV show scripts.

            Given an SRT subtitle file (with timestamps and character names) and a list of vocabulary/expressions
            the student has been learning, select the 2-3 BEST dialogue scenes for shadowing practice.

            Each scene should:
            - Be 8-15 lines of dialogue (not too short, not too long)
            - Have clear character attribution (who says what)
            - Contain vocabulary or expressions from the lesson when possible
            - Be a natural, flowing conversation (not disconnected lines)
            - Have at least 2 characters speaking

            Important rules for parsing the SRT:
            - Character names appear at the start of lines, often in ALL CAPS followed by a colon
            - If a line has no character name, it continues the previous character's speech
            - Use the timestamp of the first line in each subtitle block as startTime
            - Format startTime as "HH:MM:SS" (drop milliseconds)

            Return a JSON object with a "scenes" array containing objects with these fields:
            - title: A descriptive title for the scene (e.g., "Emergency Room Confrontation")
            - lines: Array of objects with { "character": "CHARACTER NAME", "text": "what they say", "startTime": "HH:MM:SS" }
            - characters: Array of unique character names in the scene
            """;

        String vocabList = vocabularyTerms.isEmpty() ? "none" : String.join(", ", vocabularyTerms);
        String exprList = expressions.isEmpty() ? "none" : String.join(", ", expressions);

        String userPrompt = String.format("""
            Vocabulary terms from the lesson: %s

            Expressions from the lesson: %s

            Select 2-3 best shadowing scenes from this SRT script:

            %s
            """, vocabList, exprList, truncateSrt(rawSrt));

        String response = openAiClient.chatCompletion(systemPrompt, userPrompt);
        return parseScenesResponse(response);
    }

    private String truncateSrt(String srt) {
        int maxChars = 15000;
        if (srt.length() > maxChars) {
            log.warn("SRT truncated from {} to {} chars", srt.length(), maxChars);
            return srt.substring(0, maxChars) + "\n\n[Script truncated...]";
        }
        return srt;
    }

    private List<ExtractedScene> parseScenesResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode scenesArray = root.path("scenes");
            return objectMapper.convertValue(scenesArray, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Failed to parse shadowing scenes response: {}", response, e);
            throw new RuntimeException("Failed to parse shadowing scenes response", e);
        }
    }
}
