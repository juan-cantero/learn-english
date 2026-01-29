# Piper TTS Adapter - Usage Example

This document shows how to use the `PiperTtsAdapter` in a real service.

## Example Service: Vocabulary Audio Generation

```java
package com.learntv.api.generation.application.service;

import com.learntv.api.generation.application.port.out.AudioGenerationPort;
import com.learntv.api.generation.application.port.out.AudioStoragePort;
import com.learntv.api.generation.domain.exception.AudioGenerationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class VocabularyAudioGenerationService {

    private static final Logger log = LoggerFactory.getLogger(VocabularyAudioGenerationService.class);

    private final AudioGenerationPort audioGeneration;
    private final AudioStoragePort audioStorage;

    public VocabularyAudioGenerationService(
            AudioGenerationPort audioGeneration,
            AudioStoragePort audioStorage) {
        this.audioGeneration = audioGeneration;
        this.audioStorage = audioStorage;
    }

    /**
     * Generates and stores audio pronunciation for a vocabulary term.
     *
     * @param vocabularyId The vocabulary ID
     * @param term The word/phrase to pronounce
     * @return URL to the stored MP3 file
     */
    public String generateAndStoreAudio(Long vocabularyId, String term) {
        log.info("Generating audio for vocabulary: {} (term: {})", vocabularyId, term);

        try {
            // Step 1: Generate WAV audio
            byte[] wavData = audioGeneration.generateWav(term);
            log.debug("Generated WAV: {} bytes", wavData.length);

            // Step 2: Convert to MP3
            byte[] mp3Data = audioGeneration.convertToMp3(wavData);
            log.debug("Converted to MP3: {} bytes", mp3Data.length);

            // Step 3: Store in cloud storage (e.g., Cloudflare R2)
            String audioUrl = audioStorage.storeAudio(
                vocabularyId,
                "vocabulary",
                mp3Data
            );

            log.info("Successfully generated and stored audio: {}", audioUrl);
            return audioUrl;

        } catch (AudioGenerationException e) {
            log.error("Failed to generate audio for vocabulary: {}", vocabularyId, e);
            // You might want to return null or a default audio URL
            // depending on your business requirements
            throw e;
        }
    }

    /**
     * Batch generation for multiple vocabulary items.
     *
     * @param vocabularyItems List of (id, term) pairs
     * @return Map of vocabulary ID to audio URL
     */
    public Map<Long, String> generateBatchAudio(List<VocabularyItem> vocabularyItems) {
        log.info("Generating audio for {} vocabulary items", vocabularyItems.size());

        Map<Long, String> results = new HashMap<>();
        int successCount = 0;
        int failureCount = 0;

        for (VocabularyItem item : vocabularyItems) {
            try {
                String audioUrl = generateAndStoreAudio(item.id(), item.term());
                results.put(item.id(), audioUrl);
                successCount++;
            } catch (Exception e) {
                log.warn("Failed to generate audio for vocabulary {}: {}",
                    item.id(), e.getMessage());
                failureCount++;
                // Continue with next item instead of failing the whole batch
            }
        }

        log.info("Batch audio generation complete: {} success, {} failures",
            successCount, failureCount);

        return results;
    }

    public record VocabularyItem(Long id, String term) {}
}
```

## Example Controller Endpoint

```java
package com.learntv.api.generation.adapter.in.web;

import com.learntv.api.generation.application.service.VocabularyAudioGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/generation/audio")
public class AudioGenerationController {

    private final VocabularyAudioGenerationService audioService;

    public AudioGenerationController(VocabularyAudioGenerationService audioService) {
        this.audioService = audioService;
    }

    @PostMapping("/vocabulary/{vocabularyId}")
    @Operation(summary = "Generate audio for vocabulary term")
    public ResponseEntity<AudioResponse> generateVocabularyAudio(
            @PathVariable Long vocabularyId,
            @RequestParam String term) {

        String audioUrl = audioService.generateAndStoreAudio(vocabularyId, term);

        return ResponseEntity.ok(new AudioResponse(audioUrl));
    }

    public record AudioResponse(String audioUrl) {}
}
```

## Testing the Adapter Locally

### Manual Test (requires piper-tts and ffmpeg installed)

```java
package com.learntv.api.generation.adapter.out.piper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PiperTtsAdapterIntegrationTest {

    @Autowired
    private PiperTtsAdapter piperTtsAdapter;

    @Test
    void shouldGenerateWavAudio() {
        // Given
        String text = "Hello, world!";

        // When
        byte[] wavData = piperTtsAdapter.generateWav(text);

        // Then
        assertThat(wavData).isNotEmpty();
        assertThat(wavData.length).isGreaterThan(1000); // WAV files are typically large
    }

    @Test
    void shouldConvertWavToMp3() {
        // Given
        String text = "vocabulary";
        byte[] wavData = piperTtsAdapter.generateWav(text);

        // When
        byte[] mp3Data = piperTtsAdapter.convertToMp3(wavData);

        // Then
        assertThat(mp3Data).isNotEmpty();
        assertThat(mp3Data.length).isLessThan(wavData.length); // MP3 should be smaller
        assertThat(mp3Data.length).isGreaterThan(100); // But still substantial
    }
}
```

### Mock Test (no dependencies required)

```java
package com.learntv.api.generation.application.service;

import com.learntv.api.generation.application.port.out.AudioGenerationPort;
import com.learntv.api.generation.application.port.out.AudioStoragePort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VocabularyAudioGenerationServiceTest {

    @Mock
    private AudioGenerationPort audioGeneration;

    @Mock
    private AudioStoragePort audioStorage;

    @InjectMocks
    private VocabularyAudioGenerationService service;

    @Test
    void shouldGenerateAndStoreAudio() {
        // Given
        Long vocabularyId = 123L;
        String term = "serendipity";
        byte[] wavData = new byte[]{1, 2, 3};
        byte[] mp3Data = new byte[]{4, 5, 6};
        String expectedUrl = "https://storage.example.com/audio/123.mp3";

        when(audioGeneration.generateWav(term)).thenReturn(wavData);
        when(audioGeneration.convertToMp3(wavData)).thenReturn(mp3Data);
        when(audioStorage.storeAudio(anyLong(), anyString(), any())).thenReturn(expectedUrl);

        // When
        String result = service.generateAndStoreAudio(vocabularyId, term);

        // Then
        assertThat(result).isEqualTo(expectedUrl);
        verify(audioGeneration).generateWav(term);
        verify(audioGeneration).convertToMp3(wavData);
        verify(audioStorage).storeAudio(vocabularyId, "vocabulary", mp3Data);
    }
}
```

## Performance Considerations

For production use, consider:

1. **Async Processing**: Generate audio asynchronously to avoid blocking HTTP requests
2. **Caching**: Store generated audio files and reuse for common words
3. **Queue-based Processing**: Use a message queue for batch processing
4. **Error Recovery**: Implement retry logic for transient failures

Example async implementation:

```java
@Async
public CompletableFuture<String> generateAndStoreAudioAsync(Long vocabularyId, String term) {
    return CompletableFuture.completedFuture(
        generateAndStoreAudio(vocabularyId, term)
    );
}
```
