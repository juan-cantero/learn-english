# AudioGenerationService Integration Guide

## Overview

The `AudioGenerationService` orchestrates audio generation for vocabulary items. It:
1. Generates WAV audio using Piper TTS
2. Converts WAV to MP3 using ffmpeg
3. Uploads MP3 files to Cloudflare R2 storage
4. Returns vocabulary items with populated `audioUrl` fields

## Implementation Status

**Completed:**
- `AudioGenerationService` - Core orchestration service
- Full unit test coverage with 10 test cases
- Parallel processing (max 10 concurrent operations)
- Graceful error handling (sets `audioUrl` to null on failure)
- Proper timeout handling (15 minutes total)

**Ready to Use:**
The service is fully implemented and tested. It can be injected into any service that needs to generate audio.

## Integration Steps

### 1. Add audio_url Column to Database

Create a new Flyway migration `V7__add_audio_url_to_vocabulary.sql`:

```sql
-- V7__add_audio_url_to_vocabulary.sql
-- Add audio_url column to vocabulary table

ALTER TABLE vocabulary
ADD COLUMN audio_url VARCHAR(500);

-- Index for potential queries filtering by audio availability
CREATE INDEX idx_vocabulary_audio_url ON vocabulary(audio_url)
WHERE audio_url IS NOT NULL;
```

### 2. Update VocabularyJpaEntity

Add audio URL field and update factory method:

```java
@Entity
@Table(name = "vocabulary")
public class VocabularyJpaEntity {
    // ... existing fields ...

    @Column(name = "audio_url", length = 500)
    private String audioUrl;

    public static VocabularyJpaEntity create(
            UUID episodeId,
            String term,
            String definition,
            String phonetic,
            VocabularyCategory category,
            String exampleSentence,
            String audioUrl) {  // Add this parameter
        VocabularyJpaEntity entity = new VocabularyJpaEntity();
        entity.id = UUID.randomUUID();
        entity.episodeId = episodeId;
        entity.term = term;
        entity.definition = definition;
        entity.phonetic = phonetic;
        entity.category = category;
        entity.exampleSentence = exampleSentence;
        entity.audioUrl = audioUrl;  // Add this line
        return entity;
    }

    // Add getter
    public String getAudioUrl() {
        return audioUrl;
    }
}
```

### 3. Integrate into LessonGenerationService

Update the vocabulary extraction section:

```java
@Service
public class LessonGenerationService {

    private final AudioGenerationService audioGenerationService;  // Add this field

    public LessonGenerationService(
            // ... existing parameters ...
            AudioGenerationService audioGenerationService) {  // Add this parameter
        // ... existing assignments ...
        this.audioGenerationService = audioGenerationService;
    }

    @Transactional
    public GeneratedLessonResult generateAndSaveLesson(LessonGenerationRequest request) {
        // ... existing code ...

        // 5. Extract vocabulary
        List<ExtractedVocabulary> extractedVocab =
            contentExtractionPort.extractVocabulary(script, request.genre());

        // 5a. Generate audio for vocabulary (NEW)
        List<ExtractedVocabulary> vocabWithAudio =
            audioGenerationService.generateAudioForVocabulary(extractedVocab);

        // 5b. Save vocabulary with audio URLs
        for (ExtractedVocabulary v : vocabWithAudio) {
            VocabularyJpaEntity vocab = VocabularyJpaEntity.create(
                    episode.getId(),
                    v.term(),
                    v.definition(),
                    v.phonetic(),
                    VocabularyCategory.fromString(v.category()),
                    v.exampleSentence(),
                    v.audioUrl()  // Add this parameter
            );
            vocabularyRepository.save(vocab);
        }
        log.info("Saved {} vocabulary items with audio", vocabWithAudio.size());

        // ... rest of the method ...
    }
}
```

## Usage Example

```java
@Service
public class MyService {
    private final AudioGenerationService audioGenerationService;

    public void processVocabulary(List<ExtractedVocabulary> vocabulary) {
        // Generate audio for all vocabulary items
        List<ExtractedVocabulary> withAudio =
            audioGenerationService.generateAudioForVocabulary(vocabulary);

        // Items that succeed will have audioUrl populated
        // Items that fail will have audioUrl = null
        withAudio.forEach(vocab -> {
            if (vocab.audioUrl() != null) {
                log.info("Audio available for: {} at {}", vocab.term(), vocab.audioUrl());
            } else {
                log.warn("Audio generation failed for: {}", vocab.term());
            }
        });
    }
}
```

## Key Features

### Parallel Processing
- Uses a fixed thread pool with 10 concurrent workers
- Processes vocabulary items in parallel for faster generation
- Total timeout: 15 minutes

### Error Handling
- Individual failures don't break the entire batch
- Failed items get `audioUrl = null`
- Logs warnings for each failure with term name
- Returns all items (successful + failed)

### Storage Key Generation
- Format: `audio/vocab/{slugified-term}.mp3`
- Slugification: lowercase, removes special chars, hyphens for spaces
- Examples:
  - "triage" → `audio/vocab/triage.mp3`
  - "ICU (Intensive Care Unit)" → `audio/vocab/icu-intensive-care-unit.mp3`
  - "burn out" → `audio/vocab/burn-out.mp3`

### Timeout Behavior
- Uses existing adapter timeouts (configured in PiperTtsConfig)
- Overall batch timeout: 15 minutes
- Individual operations timeout based on adapter settings

## Configuration

The service uses existing adapter configurations:

**Piper TTS** (`application.yml`):
```yaml
piper:
  model-path: /path/to/model.onnx
  timeout-seconds: 30
  ffmpeg-quality: 2
```

**R2 Storage** (`application.yml`):
```yaml
cloudflare:
  r2:
    account-id: your-account-id
    access-key-id: your-access-key
    secret-access-key: your-secret-key
    bucket-name: learntv-audio
    public-url: https://audio.learntv.com
```

## Testing

The service has comprehensive test coverage:
- ✅ Success case with multiple items
- ✅ Empty/null input handling
- ✅ Generation failures (sets audioUrl to null)
- ✅ Conversion failures (sets audioUrl to null)
- ✅ Storage failures (sets audioUrl to null)
- ✅ Slugification of complex terms
- ✅ Field preservation
- ✅ Multiple failures in same batch

Run tests:
```bash
./gradlew test --tests AudioGenerationServiceTest
```

## Next Steps

1. Create database migration `V7__add_audio_url_to_vocabulary.sql`
2. Update `VocabularyJpaEntity` with `audioUrl` field
3. Update `VocabularyJpaEntity.create()` method signature
4. Inject `AudioGenerationService` into `LessonGenerationService`
5. Call `generateAudioForVocabulary()` before saving vocabulary
6. Update frontend to display audio players for vocabulary

## Architecture Notes

The service follows hexagonal architecture principles:
- **Application Layer**: `AudioGenerationService` (orchestration)
- **Port Interfaces**: `AudioGenerationPort`, `AudioStoragePort`
- **Adapters**: `PiperTtsAdapter`, `R2StorageAdapter`
- **Domain Model**: `ExtractedVocabulary` (framework-agnostic)

Dependencies flow inward: Service → Ports ← Adapters
