# R2 Storage Adapter Integration Example

This document shows how to integrate the R2StorageAdapter with the audio generation workflow.

## Complete Workflow Example

Here's a complete example showing how to generate audio and store it in R2:

```java
package com.learntv.api.generation.application.service;

import com.learntv.api.generation.application.port.out.AudioGenerationPort;
import com.learntv.api.generation.application.port.out.AudioStoragePort;
import com.learntv.api.learning.domain.model.Vocabulary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class VocabularyAudioGenerationService {
    private static final Logger log = LoggerFactory.getLogger(VocabularyAudioGenerationService.class);

    private final AudioGenerationPort audioGenerator;
    private final AudioStoragePort audioStorage;

    public VocabularyAudioGenerationService(
            AudioGenerationPort audioGenerator,
            AudioStoragePort audioStorage) {
        this.audioGenerator = audioGenerator;
        this.audioStorage = audioStorage;
    }

    /**
     * Generate audio for vocabulary and store it in R2.
     * Returns the public URL for the audio file.
     */
    public String generateAndStoreAudio(Vocabulary vocabulary, String episodeSlug) {
        log.info("Generating audio for vocabulary: {}", vocabulary.term());

        // Generate audio using Piper TTS or other provider
        byte[] audioData = audioGenerator.generateSpeech(vocabulary.term());

        // Construct storage key following naming convention
        String key = buildStorageKey(episodeSlug, "vocabulary", vocabulary.id().value());

        // Upload to R2 and get public URL
        String publicUrl = audioStorage.upload(key, audioData, "audio/mpeg");

        log.info("Audio stored successfully: {}", publicUrl);
        return publicUrl;
    }

    /**
     * Build a consistent storage key for audio files.
     * Format: {episode-slug}/vocabulary/{vocab-id}.mp3
     */
    private String buildStorageKey(String episodeSlug, String type, Long id) {
        return String.format("%s/%s/%d.mp3", episodeSlug, type, id);
    }

    /**
     * Delete audio file from R2 when vocabulary is removed.
     */
    public void deleteAudio(String episodeSlug, Long vocabularyId) {
        String key = buildStorageKey(episodeSlug, "vocabulary", vocabularyId);
        audioStorage.delete(key);
        log.info("Deleted audio: {}", key);
    }
}
```

## Batch Processing Example

For processing multiple vocabulary items in an episode:

```java
package com.learntv.api.generation.application.service;

import com.learntv.api.generation.application.port.out.AudioStoragePort;
import com.learntv.api.learning.domain.model.Vocabulary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EpisodeAudioBatchService {
    private static final Logger log = LoggerFactory.getLogger(EpisodeAudioBatchService.class);

    private final VocabularyAudioGenerationService audioService;
    private final AudioStoragePort audioStorage;

    public EpisodeAudioBatchService(
            VocabularyAudioGenerationService audioService,
            AudioStoragePort audioStorage) {
        this.audioService = audioService;
        this.audioStorage = audioStorage;
    }

    /**
     * Generate and store audio for all vocabulary in an episode.
     * Returns a map of vocabulary ID to public URL.
     */
    public Map<Long, String> generateEpisodeAudio(String episodeSlug, List<Vocabulary> vocabularyList) {
        log.info("Starting batch audio generation for episode: {}, items: {}",
                episodeSlug, vocabularyList.size());

        Map<Long, String> audioUrls = new HashMap<>();

        for (Vocabulary vocab : vocabularyList) {
            try {
                String publicUrl = audioService.generateAndStoreAudio(vocab, episodeSlug);
                audioUrls.put(vocab.id().value(), publicUrl);
            } catch (Exception e) {
                log.error("Failed to generate audio for vocabulary: {}", vocab.term(), e);
                // Continue with other items even if one fails
            }
        }

        log.info("Completed batch audio generation. Success: {}/{}",
                audioUrls.size(), vocabularyList.size());

        return audioUrls;
    }

    /**
     * Check if audio already exists for a vocabulary item.
     * Useful for avoiding regeneration of existing audio.
     */
    public boolean audioExists(String episodeSlug, Long vocabularyId) {
        String key = String.format("%s/vocabulary/%d.mp3", episodeSlug, vocabularyId);

        try {
            String publicUrl = audioStorage.getPublicUrl(key);
            // In production, you might want to actually check if the file exists
            // by making a HEAD request or checking R2 directly
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
```

## Use Case: Lesson Generation with Audio

Integration with the lesson generation workflow:

```java
package com.learntv.api.generation.application.service;

import com.learntv.api.generation.application.port.in.GenerateEpisodeLessonUseCase;
import com.learntv.api.generation.application.port.out.AudioStoragePort;
import com.learntv.api.generation.domain.model.ExtractedVocabulary;
import com.learntv.api.generation.domain.model.GeneratedLesson;
import com.learntv.api.learning.domain.model.Vocabulary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LessonGenerationWithAudioService {
    private static final Logger log = LoggerFactory.getLogger(LessonGenerationWithAudioService.class);

    private final GenerateEpisodeLessonUseCase lessonGenerator;
    private final VocabularyAudioGenerationService audioService;
    private final AudioStoragePort audioStorage;

    public LessonGenerationWithAudioService(
            GenerateEpisodeLessonUseCase lessonGenerator,
            VocabularyAudioGenerationService audioService,
            AudioStoragePort audioStorage) {
        this.lessonGenerator = lessonGenerator;
        this.audioService = audioService;
        this.audioStorage = audioStorage;
    }

    /**
     * Generate a complete lesson including audio files for all vocabulary.
     */
    public GeneratedLesson generateLessonWithAudio(Long episodeId, String episodeSlug) {
        log.info("Generating lesson with audio for episode: {}", episodeId);

        // Step 1: Generate lesson content (vocabulary, grammar, exercises)
        GeneratedLesson lesson = lessonGenerator.execute(episodeId);

        // Step 2: Generate and store audio for each vocabulary item
        List<Vocabulary> vocabularyWithAudio = lesson.vocabulary().stream()
                .map(vocab -> enrichWithAudio(vocab, episodeSlug))
                .toList();

        // Step 3: Return lesson with audio URLs included
        log.info("Lesson generation complete with {} audio files", vocabularyWithAudio.size());

        return new GeneratedLesson(
                vocabularyWithAudio,
                lesson.grammar(),
                lesson.expressions(),
                lesson.exercises()
        );
    }

    private Vocabulary enrichWithAudio(Vocabulary vocab, String episodeSlug) {
        try {
            String audioUrl = audioService.generateAndStoreAudio(vocab, episodeSlug);
            return vocab.withAudioUrl(audioUrl);
        } catch (Exception e) {
            log.error("Failed to generate audio for vocabulary: {}", vocab.term(), e);
            return vocab; // Return without audio URL
        }
    }
}
```

## REST Controller Example

Exposing audio generation through REST API:

```java
package com.learntv.api.generation.adapter.in.web;

import com.learntv.api.generation.application.service.VocabularyAudioGenerationService;
import com.learntv.api.learning.domain.model.Vocabulary;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/generation/audio")
@Tag(name = "Audio Generation", description = "Generate and manage audio files")
public class AudioGenerationController {

    private final VocabularyAudioGenerationService audioService;

    public AudioGenerationController(VocabularyAudioGenerationService audioService) {
        this.audioService = audioService;
    }

    @PostMapping("/vocabulary/{vocabularyId}")
    @Operation(summary = "Generate audio for vocabulary item")
    public ResponseEntity<AudioResponse> generateVocabularyAudio(
            @PathVariable Long vocabularyId,
            @RequestParam String episodeSlug) {

        // Fetch vocabulary from repository
        Vocabulary vocab = fetchVocabulary(vocabularyId);

        // Generate and store audio
        String publicUrl = audioService.generateAndStoreAudio(vocab, episodeSlug);

        return ResponseEntity.ok(new AudioResponse(publicUrl));
    }

    @DeleteMapping("/vocabulary/{vocabularyId}")
    @Operation(summary = "Delete audio for vocabulary item")
    public ResponseEntity<Void> deleteVocabularyAudio(
            @PathVariable Long vocabularyId,
            @RequestParam String episodeSlug) {

        audioService.deleteAudio(episodeSlug, vocabularyId);
        return ResponseEntity.noContent().build();
    }

    private Vocabulary fetchVocabulary(Long id) {
        // Implementation omitted - fetch from repository
        throw new UnsupportedOperationException("Not implemented");
    }

    record AudioResponse(String url) {}
}
```

## Configuration for Different Environments

### Development (.env.development)
```env
CLOUDFLARE_ACCOUNT_ID=your-dev-account-id
R2_ACCESS_KEY_ID=your-dev-access-key
R2_SECRET_ACCESS_KEY=your-dev-secret-key
R2_BUCKET_NAME=learntv-audio-dev
R2_PUBLIC_URL=https://audio-dev.learntv.test
```

### Production (.env.production)
```env
CLOUDFLARE_ACCOUNT_ID=your-prod-account-id
R2_ACCESS_KEY_ID=your-prod-access-key
R2_SECRET_ACCESS_KEY=your-prod-secret-key
R2_BUCKET_NAME=learntv-audio
R2_PUBLIC_URL=https://audio.learntv.com
```

## Error Handling Best Practices

```java
public String generateAudioWithRetry(Vocabulary vocab, String episodeSlug) {
    int maxRetries = 3;
    int attempt = 0;

    while (attempt < maxRetries) {
        try {
            return audioService.generateAndStoreAudio(vocab, episodeSlug);
        } catch (AudioStorageException e) {
            attempt++;
            if (attempt >= maxRetries) {
                log.error("Failed to generate audio after {} attempts", maxRetries, e);
                throw e;
            }
            log.warn("Audio generation failed (attempt {}), retrying...", attempt);
            try {
                Thread.sleep(1000 * attempt); // Exponential backoff
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new AudioStorageException("Interrupted during retry", ie);
            }
        }
    }

    throw new AudioStorageException("Unexpected error in retry logic");
}
```

## Testing Integration

```java
@SpringBootTest
class AudioIntegrationTest {

    @Autowired
    private VocabularyAudioGenerationService audioService;

    @MockBean
    private AudioStoragePort audioStorage;

    @Test
    void shouldGenerateAndStoreAudio() {
        // Given
        Vocabulary vocab = createTestVocabulary();
        String expectedUrl = "https://audio.test.com/episode/vocab/123.mp3";

        when(audioStorage.upload(any(), any(), any()))
            .thenReturn(expectedUrl);

        // When
        String actualUrl = audioService.generateAndStoreAudio(vocab, "episode-123");

        // Then
        assertEquals(expectedUrl, actualUrl);
        verify(audioStorage).upload(
            eq("episode-123/vocabulary/123.mp3"),
            any(byte[].class),
            eq("audio/mpeg")
        );
    }
}
```

## Monitoring and Metrics

Consider adding metrics for production:

```java
@Service
public class AudioMetricsService {
    private final MeterRegistry meterRegistry;

    public void recordUpload(long duration, boolean success) {
        Timer.builder("audio.upload")
            .tag("status", success ? "success" : "failure")
            .register(meterRegistry)
            .record(Duration.ofMillis(duration));
    }

    public void recordStorageSize(long bytes) {
        meterRegistry.gauge("audio.storage.bytes", bytes);
    }
}
```
