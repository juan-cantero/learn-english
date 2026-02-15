package com.learntv.api.learning.adapter.out.openai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;

/**
 * Adapter for OpenAI Whisper API (audio transcription).
 */
@Component
@Slf4j
public class WhisperAdapter {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public WhisperAdapter(
            @Value("${external-apis.openai.api-key}") String apiKey,
            @Value("${external-apis.openai.base-url}") String baseUrl,
            ObjectMapper objectMapper) {

        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();

        this.objectMapper = objectMapper;
    }

    /**
     * Transcribe audio using OpenAI Whisper API.
     *
     * @param audioData Audio file bytes
     * @param filename  Original filename (for multipart upload)
     * @return Transcribed text
     */
    public String transcribe(byte[] audioData, String filename, String promptHint) {
        log.debug("Sending audio transcription request, file: {}, size: {} bytes", filename, audioData.length);

        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();

        // Add file part with custom ByteArrayResource to provide filename
        bodyBuilder.part("file", new ByteArrayResource(audioData) {
            @Override
            public String getFilename() {
                return filename;
            }
        });

        bodyBuilder.part("model", "whisper-1");
        bodyBuilder.part("language", "en");
        bodyBuilder.part("temperature", "0");
        if (promptHint != null && !promptHint.isBlank()) {
            bodyBuilder.part("prompt", promptHint);
        }

        try {
            String response = webClient.post()
                    .uri("/audio/transcriptions")
                    .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                    .retrieve()
                    .bodyToMono(String.class)
                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                            .filter(this::isRetryableError)
                            .doBeforeRetry(signal -> log.warn("Retrying Whisper request, attempt {}", signal.totalRetries() + 1)))
                    .block(Duration.ofSeconds(60));

            return extractText(response);
        } catch (WebClientResponseException e) {
            log.error("Whisper API error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Whisper API error: " + e.getMessage(), e);
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

    private String extractText(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            String text = root.path("text").asText();
            if (text == null || text.isBlank()) {
                throw new RuntimeException("No text in Whisper response");
            }
            return text;
        } catch (Exception e) {
            log.error("Failed to parse Whisper response: {}", response);
            throw new RuntimeException("Failed to parse Whisper response", e);
        }
    }
}
