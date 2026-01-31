# Lesson Generation Architecture

This document describes the hexagonal architecture implementation for lesson generation in the LearnTV backend.

## Architecture Overview

The lesson generation feature follows a **Use Case + Domain Service** pattern that strictly adheres to hexagonal architecture principles:

```
┌─────────────────────────────────────────────────────────────────┐
│  ADAPTER LAYER (Web)                                            │
│  GenerationController                                           │
│  - POST /api/v1/generation/lessons (async)                     │
│  - GET  /api/v1/generation/jobs/{id} (status)                  │
└──────────────────────┬──────────────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────────────┐
│  APPLICATION LAYER                                              │
│  GenerateEpisodeLessonService (Use Case)                       │
│  - Coordinates async job processing                             │
│  - Updates job progress at each step                            │
│  - Delegates to domain service for composition                  │
│  - Persists result via LessonPersistencePort                   │
└──────────────────────┬──────────────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────────────┐
│  DOMAIN LAYER                                                   │
│  EpisodeLessonGenerator (Domain Service)                       │
│  - Contains pedagogical business logic                          │
│  - Validates minimum content requirements                       │
│  - Composes GeneratedLesson from extracted content              │
│  - NO external dependencies, NO framework annotations           │
└─────────────────────────────────────────────────────────────────┘
```

## Pedagogical Order

The domain service enforces this learning sequence:

1. **Vocabulary** - Foundation (understand words first)
2. **Grammar** - Structure (understand patterns)
3. **Expressions** - Context (understand idioms and usage)
4. **Exercises** - Practice (reinforce learning)
5. **Audio** - Pronunciation (hear correct pronunciation)

## Component Responsibilities

### 1. Domain Service: `EpisodeLessonGenerator`

**Location:** `domain/service/EpisodeLessonGenerator.java`

**Pure Domain Logic:**
- NO framework annotations (`@Service`, `@Component`)
- NO port dependencies (no injected dependencies)
- All data passed as method parameters
- Returns domain models only

**Responsibilities:**
- Validate minimum content requirements
- Apply business rules for lesson quality
- Compose immutable `GeneratedLesson` domain model
- Calculate lesson metrics (total points, quality checks)

**Business Rules:**
- Minimum 10 vocabulary items
- Minimum 3 grammar points
- Minimum 5 expressions
- Minimum 10 exercises
- High quality: 15+ vocab, 4+ grammar, 6+ expressions, 12+ exercises

### 2. Use Case: `GenerateEpisodeLessonService`

**Location:** `application/service/GenerateEpisodeLessonService.java`

**Orchestration Responsibilities:**
- Create and track `GenerationJob`
- Start async processing with `@Async`
- Update progress at each step using `GenerationProgressStep` enum
- Call ports to fetch/extract content
- Delegate composition to `EpisodeLessonGenerator`
- Persist result via `LessonPersistencePort`
- Handle errors and update job status

**Does NOT:**
- Contain business logic (delegated to domain service)
- Know the pedagogical order (domain service handles that)
- Make decisions about content quality (domain service validates)

### 3. Use Case: `GetGenerationStatusService`

**Location:** `application/service/GetGenerationStatusService.java`

Simple query use case for job status retrieval.

### 4. Adapter: `LessonPersistenceAdapter`

**Location:** `adapter/out/persistence/LessonPersistenceAdapter.java`

**Responsibilities:**
- Transform `GeneratedLesson` to JPA entities
- Find or create show from metadata
- Save lesson content to database
- Handle regeneration (delete existing content)

## Progress Tracking

Uses `GenerationProgressStep` enum for consistent progress updates:

```java
FETCHING_SCRIPT(10, "Fetching script")
PARSING_SCRIPT(20, "Parsing script")
EXTRACTING_VOCABULARY(40, "Extracting vocabulary...")
EXTRACTING_GRAMMAR(55, "Extracting grammar...")
EXTRACTING_EXPRESSIONS(70, "Extracting expressions...")
GENERATING_EXERCISES(80, "Generating exercises...")
GENERATING_AUDIO(90, "Generating audio...")
SAVING(95, "Saving...")
COMPLETED(100, "Completed")
```

## API Endpoints

### Start Async Generation

```http
POST /api/v1/generation/lessons
Content-Type: application/x-www-form-urlencoded

tmdbId=1396&season=1&episode=1&genre=drama
```

**Response (202 Accepted):**
```json
{
  "jobId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "PENDING",
  "progress": 0,
  "currentStep": null,
  "errorMessage": null,
  "episodeId": null
}
```

### Poll Job Status

```http
GET /api/v1/generation/jobs/{jobId}
```

**Response (200 OK):**
```json
{
  "jobId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "PROCESSING",
  "progress": 55,
  "currentStep": "Extracting grammar...",
  "errorMessage": null,
  "episodeId": null
}
```

**When Completed:**
```json
{
  "jobId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "COMPLETED",
  "progress": 100,
  "currentStep": "Completed",
  "errorMessage": null,
  "episodeId": "a8f2c3b1-4d5e-6f7a-8b9c-0d1e2f3a4b5c"
}
```

## Async Processing

### Configuration

**AsyncConfiguration.java:**
- Enables `@Async` annotation
- Configures thread pool for lesson generation
- Core pool: 2 threads
- Max pool: 5 threads
- Queue capacity: 10 jobs

### Execution Flow

1. Controller receives request
2. Use case creates job in PENDING state
3. Job ID returned immediately (202 Accepted)
4. Async method starts processing in background
5. Client polls `/jobs/{id}` for status updates
6. On completion, episodeId is available in job

## Domain Models

### GeneratedLesson

```java
public record GeneratedLesson(
    List<ExtractedVocabulary> vocabulary,
    List<ExtractedGrammar> grammarPoints,
    List<ExtractedExpression> expressions,
    List<GeneratedExercise> exercises
)
```

### GenerationJob

```java
public record GenerationJob(
    UUID id,
    GenerationStatus status,    // PENDING, PROCESSING, COMPLETED, FAILED
    int progress,               // 0-100
    String currentStep,
    String errorMessage,
    UUID episodeId,
    Instant createdAt,
    Instant completedAt
)
```

## Testing

### Unit Tests

**EpisodeLessonGeneratorTest:**
- Tests business rule validation
- Tests minimum content requirements
- Tests quality checks
- Tests immutability
- Tests point calculation

Run tests:
```bash
./gradlew test --tests "EpisodeLessonGeneratorTest"
```

### Integration Tests

For full integration testing:
```bash
./gradlew test
```

## Design Principles

### 1. Separation of Concerns

- **Domain Service** = Business logic only
- **Use Case** = Orchestration and coordination
- **Adapter** = External system integration

### 2. Dependency Inversion

- Use Case depends on ports (abstractions)
- Domain service has NO dependencies
- Adapters implement ports

### 3. Single Responsibility

Each component has ONE clear responsibility:
- Domain service: compose lesson with business rules
- Use case: coordinate async processing
- Adapter: persist to database

### 4. Immutability

- Domain models are immutable records
- `GenerationJob` uses state transition methods
- `GeneratedLesson` uses defensive copying

## Future Enhancements

1. **Retry Logic**: Add retry for transient failures
2. **Job Expiration**: Clean up old completed jobs
3. **Webhooks**: Notify clients when job completes
4. **Batch Generation**: Generate multiple episodes
5. **Quality Metrics**: Track lesson quality over time
6. **A/B Testing**: Test different extraction strategies

## Files Created/Modified

### New Files

1. `domain/service/EpisodeLessonGenerator.java` - Domain service
2. `application/service/GenerateEpisodeLessonService.java` - Use case
3. `application/service/GetGenerationStatusService.java` - Query use case
4. `adapter/out/persistence/LessonPersistenceAdapter.java` - Persistence adapter
5. `shared/config/AsyncConfiguration.java` - Async configuration
6. `test/.../EpisodeLessonGeneratorTest.java` - Unit tests

### Modified Files

1. `application/port/out/LessonPersistencePort.java` - Added method signature
2. `adapter/in/web/GenerationController.java` - Added async endpoint
3. `shared/config/BeanConfiguration.java` - Registered domain service bean
4. `learning/adapter/out/persistence/*JpaRepository.java` - Added delete methods

## Migration from Old Architecture

The old `LessonGenerationService` is still available for backward compatibility:

- **Old (Sync):** `POST /api/v1/generation/lessons/create`
- **New (Async):** `POST /api/v1/generation/lessons`

Eventually, the old synchronous endpoint can be deprecated.
