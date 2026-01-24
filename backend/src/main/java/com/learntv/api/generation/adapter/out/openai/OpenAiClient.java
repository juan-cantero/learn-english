package com.learntv.api.generation.adapter.out.openai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Low-level client for OpenAI Chat Completions API.
 */
@Component
public class OpenAiClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAiClient.class);

    private final WebClient openAiWebClient;
    private final OpenAiConfig config;
    private final ObjectMapper objectMapper;

    public OpenAiClient(WebClient openAiWebClient, OpenAiConfig config, ObjectMapper objectMapper) {
        this.openAiWebClient = openAiWebClient;
        this.config = config;
        this.objectMapper = objectMapper;
    }

    /**
     * Send a chat completion request to OpenAI.
     *
     * @param systemPrompt System message setting the AI's behavior
     * @param userPrompt User message with the actual request
     * @return The assistant's response content
     */
    public String chatCompletion(String systemPrompt, String userPrompt) {
        log.debug("Sending chat completion request, model: {}", config.getModel());

        Map<String, Object> requestBody = Map.of(
                "model", config.getModel(),
                "max_tokens", config.getMaxTokens(),
                "temperature", 0.7,
                "response_format", Map.of("type", "json_object"),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                )
        );

        try {
            String response = openAiWebClient.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                            .filter(this::isRetryableError)
                            .doBeforeRetry(signal -> log.warn("Retrying OpenAI request, attempt {}", signal.totalRetries() + 1)))
                    .block(Duration.ofSeconds(60));

            return extractContent(response);
        } catch (WebClientResponseException e) {
            log.error("OpenAI API error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("OpenAI API error: " + e.getMessage(), e);
        }
    }

    private boolean isRetryableError(Throwable throwable) {
        if (throwable instanceof WebClientResponseException e) {
            int status = e.getStatusCode().value();
            // Retry on rate limits (429) and server errors (5xx)
            return status == 429 || status >= 500;
        }
        return false;
    }

    private String extractContent(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode choices = root.path("choices");
            if (choices.isArray() && !choices.isEmpty()) {
                return choices.get(0).path("message").path("content").asText();
            }
            throw new RuntimeException("No choices in OpenAI response");
        } catch (Exception e) {
            log.error("Failed to parse OpenAI response: {}", response);
            throw new RuntimeException("Failed to parse OpenAI response", e);
        }
    }
}
