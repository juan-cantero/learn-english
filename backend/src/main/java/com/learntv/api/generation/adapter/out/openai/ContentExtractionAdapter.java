package com.learntv.api.generation.adapter.out.openai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learntv.api.generation.application.port.out.ContentExtractionPort;
import com.learntv.api.generation.domain.model.ExtractedExpression;
import com.learntv.api.generation.domain.model.ExtractedGrammar;
import com.learntv.api.generation.domain.model.ExtractedVocabulary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * OpenAI implementation of ContentExtractionPort.
 * Uses GPT to extract vocabulary, grammar, and expressions from TV show scripts.
 */
@Component
public class ContentExtractionAdapter implements ContentExtractionPort {

    private static final Logger log = LoggerFactory.getLogger(ContentExtractionAdapter.class);

    private final OpenAiClient openAiClient;
    private final ObjectMapper objectMapper;

    public ContentExtractionAdapter(OpenAiClient openAiClient, ObjectMapper objectMapper) {
        this.openAiClient = openAiClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<ExtractedVocabulary> extractVocabulary(String script, String genre) {
        log.info("Extracting vocabulary from script, genre: {}", genre);

        String systemPrompt = """
            You are an expert English teacher creating vocabulary lessons from TV show scripts.
            Extract 15-25 interesting vocabulary items that would help intermediate English learners.
            Focus on words that are:
            - Used in natural conversation
            - Relevant to the genre/context
            - Not too basic (avoid "the", "is", "go") but not too obscure
            - Include a mix of nouns, verbs, adjectives, and phrasal verbs

            Return a JSON object with a "vocabulary" array containing objects with these fields:
            - term: the word or phrase
            - definition: clear, concise definition
            - phonetic: IPA pronunciation (e.g., "/ˈdɒktər/")
            - category: one of "medical", "colloquial", "professional", "emotional", "action"
            - exampleSentence: a natural example sentence using the word
            - audioUrl: leave as null (will be generated later)
            """;

        String userPrompt = String.format("""
            Genre: %s

            Extract vocabulary from this TV show script:

            %s
            """, genre, truncateScript(script));

        String response = openAiClient.chatCompletion(systemPrompt, userPrompt);
        return parseVocabularyResponse(response);
    }

    @Override
    public List<ExtractedGrammar> extractGrammar(String script) {
        log.info("Extracting grammar points from script");

        String systemPrompt = """
            You are an expert English teacher creating grammar lessons from TV show scripts.
            Identify 4-6 interesting grammar patterns that appear in the dialogue.
            Focus on:
            - Common conversational structures
            - Patterns that intermediate learners often struggle with
            - Grammar used naturally in context (not textbook examples)

            Return a JSON object with a "grammar" array containing objects with these fields:
            - title: brief name of the grammar point (e.g., "Present Perfect for Experience")
            - explanation: clear explanation of when/how to use it
            - structure: the grammatical pattern (e.g., "have/has + past participle")
            - examples: array of 2-3 example sentences from or inspired by the script
            """;

        String userPrompt = String.format("""
            Extract grammar points from this TV show script:

            %s
            """, truncateScript(script));

        String response = openAiClient.chatCompletion(systemPrompt, userPrompt);
        return parseGrammarResponse(response);
    }

    @Override
    public List<ExtractedExpression> extractExpressions(String script) {
        log.info("Extracting expressions from script");

        String systemPrompt = """
            You are an expert English teacher identifying idiomatic expressions and phrases from TV shows.
            Extract 6-10 interesting expressions, idioms, or colloquial phrases that learners should know.
            Focus on:
            - Natural spoken expressions (not formal written language)
            - Phrases that might confuse non-native speakers
            - Common idioms and phrasal expressions

            Return a JSON object with an "expressions" array containing objects with these fields:
            - phrase: the expression exactly as used
            - meaning: what it actually means
            - context: brief description of when/how it's used in the script
            - usageNote: when/how to use this expression appropriately
            """;

        String userPrompt = String.format("""
            Extract expressions and idioms from this TV show script:

            %s
            """, truncateScript(script));

        String response = openAiClient.chatCompletion(systemPrompt, userPrompt);
        return parseExpressionsResponse(response);
    }

    private String truncateScript(String script) {
        // OpenAI has token limits, truncate very long scripts
        int maxChars = 15000;
        if (script.length() > maxChars) {
            log.warn("Script truncated from {} to {} chars", script.length(), maxChars);
            return script.substring(0, maxChars) + "\n\n[Script truncated...]";
        }
        return script;
    }

    private List<ExtractedVocabulary> parseVocabularyResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode vocabArray = root.path("vocabulary");
            return objectMapper.convertValue(vocabArray, new TypeReference<List<ExtractedVocabulary>>() {});
        } catch (Exception e) {
            log.error("Failed to parse vocabulary response: {}", response, e);
            throw new RuntimeException("Failed to parse vocabulary extraction response", e);
        }
    }

    private List<ExtractedGrammar> parseGrammarResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode grammarArray = root.path("grammar");
            return objectMapper.convertValue(grammarArray, new TypeReference<List<ExtractedGrammar>>() {});
        } catch (Exception e) {
            log.error("Failed to parse grammar response: {}", response, e);
            throw new RuntimeException("Failed to parse grammar extraction response", e);
        }
    }

    private List<ExtractedExpression> parseExpressionsResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode expressionsArray = root.path("expressions");
            return objectMapper.convertValue(expressionsArray, new TypeReference<List<ExtractedExpression>>() {});
        } catch (Exception e) {
            log.error("Failed to parse expressions response: {}", response, e);
            throw new RuntimeException("Failed to parse expressions extraction response", e);
        }
    }
}
