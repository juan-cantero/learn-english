# ADR-001: Episode Script Fetching and Content Generation System

**Status**: Proposed
**Date**: 2026-01-21
**Authors**: Architecture Team

---

## Problem Statement

LearnTV needs to automatically generate learning content (vocabulary, grammar points, expressions, and exercises) from TV show episodes. Currently, content is manually created, which is time-consuming and doesn't scale. We need a system that:

1. Allows users to search and select TV shows and episodes
2. Fetches episode scripts/transcripts from external sources
3. Processes the script through AI to extract educational content
4. Stores the generated content in the database for immediate use

The "Generate Episode" button should orchestrate this entire flow with proper error handling, progress feedback, and caching.

---

## Proposed Solution

### High-Level Architecture

```
+-------------------+      +------------------+      +---------------------+
|                   |      |                  |      |                     |
|  React Frontend   |----->|  Spring Boot     |----->|  External APIs      |
|  (User Interface) |      |  Backend         |      |  - TMDB (metadata)  |
|                   |      |  (Orchestration) |      |  - OpenSubtitles    |
+-------------------+      +------------------+      |  - OpenAI (AI)      |
                                   |                +---------------------+
                                   |
                                   v
                           +------------------+
                           |                  |
                           |  PostgreSQL      |
                           |  (Persistence)   |
                           |                  |
                           +------------------+
```

### Data Flow: "Generate Episode" Button

```
User clicks "Generate Episode"
        |
        v
+------------------+
| 1. SHOW SEARCH   |  TMDB API: Search shows by name
+------------------+  Response: Show ID, title, poster, seasons
        |
        v
+------------------+
| 2. EPISODE LIST  |  TMDB API: Get episodes for season
+------------------+  Response: Episode ID, title, air date
        |
        v
+------------------+
| 3. FETCH SCRIPT  |  OpenSubtitles API: Search by IMDB ID
+------------------+  Response: SRT/VTT subtitle file
        |
        v
+------------------+
| 4. PARSE SCRIPT  |  Internal: Convert SRT to plain text
+------------------+  Output: Clean dialogue text
        |
        v
+------------------+
| 5. AI EXTRACTION |  OpenAI API: Extract learning content
+------------------+  Output: Vocabulary, Grammar, Expressions
        |
        v
+------------------+
| 6. GENERATE      |  OpenAI API: Create exercises
| EXERCISES        |  Output: Fill-in-blank, matching, quiz
+------------------+
        |
        v
+------------------+
| 7. PERSIST       |  PostgreSQL: Save all content
+------------------+  Episode + related entities
        |
        v
+------------------+
| 8. RETURN        |  Response: Complete lesson data
+------------------+
```

---

## API Research Summary

### 1. TMDB (The Movie Database) - Show/Episode Metadata

**Purpose**: Search for TV shows, get episode lists, retrieve metadata (posters, descriptions).

| Aspect | Details |
|--------|---------|
| **Base URL** | `https://api.themoviedb.org/3` |
| **Authentication** | API Key (query param or Bearer token) |
| **Rate Limits** | ~40 requests/second per IP (very generous) |
| **Cost** | Free for non-commercial use |
| **Key Endpoints** | `/search/tv`, `/tv/{id}`, `/tv/{id}/season/{n}` |

**Why TMDB?**
- Free tier with generous limits
- Comprehensive metadata including IMDB IDs (needed for OpenSubtitles)
- High-quality poster images
- Active community-maintained database

**Example Flow**:
```
GET /search/tv?query=The+Pitt
    -> Returns show_id, imdb_id

GET /tv/{show_id}/season/1
    -> Returns episodes with episode numbers, titles, air dates
```

### 2. OpenSubtitles.com - Script/Subtitle Retrieval

**Purpose**: Download subtitles/scripts for episodes using IMDB ID.

| Aspect | Details |
|--------|---------|
| **Base URL** | `https://api.opensubtitles.com/api/v1` |
| **Authentication** | API Key (header) + JWT for downloads |
| **Rate Limits** | 5 requests/second, 1 request/second for login |
| **Download Limits** | Free: 5/day, Registered: 10/day, VIP: 1000/day |
| **Key Endpoints** | `/subtitles` (search), `/download` (get file) |

**Why OpenSubtitles?**
- Largest subtitle database (7M+ subtitles)
- REST API with JSON responses
- Supports search by IMDB ID (most accurate)
- SRT format is easy to parse

**Important Considerations**:
- Download limits require caching strategy
- JWT tokens expire and need refresh
- Some subtitles may have quality issues (fan-made)

**Example Flow**:
```
POST /login (get JWT token)

GET /subtitles?imdb_id=tt1234567&season_number=1&episode_number=1
    -> Returns subtitle file IDs

POST /download (with file_id)
    -> Returns download link
```

### 3. OpenAI API - Content Extraction and Generation

**Purpose**: Extract vocabulary, grammar, expressions from script text. Generate exercises.

| Aspect | Details |
|--------|---------|
| **Model** | GPT-4o or GPT-4o-mini (for cost efficiency) |
| **Rate Limits** | Token-based, varies by tier |
| **Cost** | ~$0.01 per 1K tokens (GPT-4o-mini) |
| **Key Endpoints** | `/chat/completions` |

**Why OpenAI?**
- Best-in-class language understanding
- Structured output support (JSON mode)
- Can be prompted for specific educational formats

---

## Components Design

### Backend Components (Hexagonal Architecture)

```
com.learntv.api/
+-- generation/                          # New bounded context
|   +-- domain/
|   |   +-- model/
|   |   |   +-- ScriptSource.java        # Enum: OPENSUBTITLES, MANUAL
|   |   |   +-- GenerationJob.java       # Tracks generation status
|   |   |   +-- GenerationStatus.java    # PENDING, PROCESSING, COMPLETED, FAILED
|   |   |   +-- RawScript.java           # Parsed subtitle content
|   |   |   +-- ExtractedContent.java    # AI extraction result
|   |   +-- exception/
|   |       +-- ScriptNotFoundException.java
|   |       +-- GenerationFailedException.java
|   |       +-- RateLimitExceededException.java
|   |
|   +-- application/
|   |   +-- port/
|   |   |   +-- in/
|   |   |   |   +-- GenerateEpisodeContentUseCase.java
|   |   |   |   +-- SearchShowsUseCase.java
|   |   |   |   +-- GetGenerationStatusUseCase.java
|   |   |   +-- out/
|   |   |       +-- ShowMetadataPort.java       # TMDB adapter interface
|   |   |       +-- SubtitleFetchPort.java      # OpenSubtitles adapter interface
|   |   |       +-- ContentExtractionPort.java  # OpenAI adapter interface
|   |   |       +-- GenerationJobRepository.java
|   |   +-- service/
|   |       +-- EpisodeGenerationService.java   # Orchestrates the flow
|   |       +-- ScriptParserService.java        # SRT/VTT to plain text
|   |
|   +-- adapter/
|       +-- in/
|       |   +-- web/
|       |       +-- GenerationController.java
|       |       +-- dto/
|       |           +-- SearchShowsRequest.java
|       |           +-- SearchShowsResponse.java
|       |           +-- GenerateEpisodeRequest.java
|       |           +-- GenerationStatusResponse.java
|       +-- out/
|           +-- tmdb/
|           |   +-- TmdbClient.java
|           |   +-- TmdbShowMetadataAdapter.java
|           |   +-- dto/
|           |       +-- TmdbShowSearchResult.java
|           |       +-- TmdbEpisodeDetails.java
|           +-- opensubtitles/
|           |   +-- OpenSubtitlesClient.java
|           |   +-- OpenSubtitlesAdapter.java
|           |   +-- dto/
|           |       +-- SubtitleSearchResult.java
|           |       +-- DownloadResponse.java
|           +-- openai/
|           |   +-- OpenAiClient.java
|           |   +-- ContentExtractionAdapter.java
|           |   +-- prompts/
|           |       +-- VocabularyExtractionPrompt.java
|           |       +-- GrammarExtractionPrompt.java
|           |       +-- ExerciseGenerationPrompt.java
|           +-- persistence/
|               +-- GenerationJobJpaEntity.java
|               +-- GenerationJobJpaRepository.java
|               +-- GenerationJobRepositoryAdapter.java
```

### Port Interfaces

```java
// ShowMetadataPort.java - TMDB integration
public interface ShowMetadataPort {
    List<ShowSearchResult> searchShows(String query);
    ShowDetails getShowDetails(String tmdbId);
    List<EpisodeMetadata> getSeasonEpisodes(String tmdbId, int season);
    Optional<String> getImdbId(String tmdbId);
}

// SubtitleFetchPort.java - OpenSubtitles integration
public interface SubtitleFetchPort {
    List<SubtitleInfo> searchSubtitles(String imdbId, int season, int episode, String language);
    String downloadSubtitle(String subtitleId);
    boolean isAvailable(); // Check rate limit status
}

// ContentExtractionPort.java - OpenAI integration
public interface ContentExtractionPort {
    ExtractedVocabulary extractVocabulary(String scriptText, String showGenre);
    ExtractedGrammar extractGrammarPoints(String scriptText);
    ExtractedExpressions extractExpressions(String scriptText);
    GeneratedExercises generateExercises(ExtractedContent content);
}
```

---

## API Design

### REST Endpoints

```yaml
# Show Search (uses TMDB)
GET /api/v1/generation/shows/search?q={query}
Response:
  {
    "shows": [
      {
        "tmdbId": "12345",
        "imdbId": "tt1234567",
        "title": "The Pitt",
        "posterUrl": "https://image.tmdb.org/...",
        "year": 2025,
        "seasons": 1
      }
    ]
  }

# Get Episodes for Season
GET /api/v1/generation/shows/{tmdbId}/seasons/{season}/episodes
Response:
  {
    "episodes": [
      {
        "episodeNumber": 1,
        "title": "7:00 A.M.",
        "airDate": "2025-01-09",
        "hasSubtitles": true
      }
    ]
  }

# Trigger Episode Generation (async)
POST /api/v1/generation/episodes
Request:
  {
    "tmdbShowId": "12345",
    "imdbId": "tt1234567",
    "season": 1,
    "episode": 1,
    "language": "en"
  }
Response:
  {
    "jobId": "uuid",
    "status": "PENDING",
    "estimatedSeconds": 60
  }

# Check Generation Status
GET /api/v1/generation/jobs/{jobId}
Response:
  {
    "jobId": "uuid",
    "status": "PROCESSING",
    "progress": 45,
    "currentStep": "Extracting vocabulary...",
    "episodeId": null
  }

# On completion:
  {
    "jobId": "uuid",
    "status": "COMPLETED",
    "progress": 100,
    "currentStep": "Done",
    "episodeId": "episode-uuid",
    "summary": {
      "vocabularyCount": 25,
      "grammarPointsCount": 5,
      "expressionsCount": 8,
      "exercisesCount": 15
    }
  }
```

### WebSocket for Progress Updates (Optional Enhancement)

```yaml
WS /api/v1/generation/jobs/{jobId}/progress
Messages:
  { "step": "fetching_script", "progress": 10 }
  { "step": "parsing_script", "progress": 20 }
  { "step": "extracting_vocabulary", "progress": 40 }
  { "step": "extracting_grammar", "progress": 55 }
  { "step": "extracting_expressions", "progress": 70 }
  { "step": "generating_exercises", "progress": 85 }
  { "step": "saving", "progress": 95 }
  { "step": "completed", "progress": 100, "episodeId": "..." }
```

---

## AI Prompts Design

### Vocabulary Extraction Prompt

```
You are an expert English language teacher analyzing a TV script for vocabulary learning.

Context:
- Show genre: {genre} (e.g., Medical Drama)
- Target audience: Intermediate to Advanced English learners
- Script excerpt: {script_text}

Extract 15-25 interesting vocabulary items. For each item provide:
1. term: The word or phrase
2. definition: Clear, learner-friendly definition
3. phonetic: IPA pronunciation
4. category: One of [MEDICAL, LEGAL, WORKPLACE, INFORMAL, SLANG, TECHNICAL, EVERYDAY]
5. exampleSentence: A sentence from the script using this word
6. contextTimestamp: Approximate position in script (early/middle/late)

Focus on:
- Domain-specific terminology (medical, legal, etc.)
- Idiomatic expressions
- Phrasal verbs
- Colloquialisms
- Words with interesting connotations

Output as JSON array.
```

### Grammar Points Extraction Prompt

```
You are an expert English grammar teacher analyzing dialogue from a TV script.

Script excerpt: {script_text}

Identify 4-6 interesting grammar patterns used in natural speech. For each:
1. title: Grammar point name (e.g., "Present Perfect Continuous")
2. explanation: Clear explanation for intermediate learners
3. structure: The grammatical pattern (e.g., "have/has been + verb-ing")
4. example: Direct quote from the script
5. contextQuote: The full dialogue line for context

Focus on:
- Grammar used in natural conversation (not textbook examples)
- Contractions and informal structures
- Question formations
- Conditional sentences
- Reported speech
- Modal verbs in context

Output as JSON array.
```

### Exercise Generation Prompt

```
You are creating practice exercises for English learners based on extracted content.

Vocabulary items: {vocabulary_json}
Grammar points: {grammar_json}
Expressions: {expressions_json}

Generate 12-15 exercises:

1. Fill-in-the-blank (5-6):
   - Use vocabulary terms in context
   - Provide sentence with blank
   - correctAnswer: the missing word

2. Multiple Choice (4-5):
   - Test understanding of definitions or grammar
   - Provide 4 options, one correct
   - options: JSON array ["A", "B", "C", "D"]
   - correctAnswer: the letter

3. Matching (2-3):
   - Match terms to definitions OR expressions to meanings
   - matchingPairs: JSON array of {term, definition}

Output as JSON array with fields:
- type: "FILL_IN_BLANK" | "MULTIPLE_CHOICE" | "MATCHING"
- question: The exercise prompt
- correctAnswer: Expected answer
- options: (for multiple choice)
- matchingPairs: (for matching)
- points: 10 for basic, 15 for harder
```

---

## Caching Strategy

### Cache Levels

```
+----------------------+     +-------------------+     +------------------+
|  Response Cache      |     |  Script Cache     |     |  AI Result Cache |
|  (Redis/Caffeine)    |     |  (Database)       |     |  (Database)      |
+----------------------+     +-------------------+     +------------------+
       |                            |                         |
  TTL: 1 hour               TTL: 30 days                 TTL: Permanent
  What: TMDB responses      What: Downloaded SRT        What: Generated content
  Why: Reduce API calls     Why: OpenSubtitles limits   Why: Don't re-generate
```

### Database Tables for Caching

```sql
-- Cache downloaded scripts to avoid re-downloading
CREATE TABLE cached_scripts (
    id UUID PRIMARY KEY,
    imdb_id VARCHAR(20) NOT NULL,
    season_number INT NOT NULL,
    episode_number INT NOT NULL,
    language VARCHAR(10) NOT NULL,
    source VARCHAR(50) NOT NULL,  -- 'OPENSUBTITLES', 'MANUAL'
    raw_content TEXT NOT NULL,
    parsed_text TEXT NOT NULL,
    downloaded_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    UNIQUE(imdb_id, season_number, episode_number, language)
);

-- Track generation jobs for async processing
CREATE TABLE generation_jobs (
    id UUID PRIMARY KEY,
    imdb_id VARCHAR(20) NOT NULL,
    tmdb_id VARCHAR(20),
    season_number INT NOT NULL,
    episode_number INT NOT NULL,
    status VARCHAR(20) NOT NULL,  -- PENDING, PROCESSING, COMPLETED, FAILED
    current_step VARCHAR(100),
    progress INT DEFAULT 0,
    error_message TEXT,
    result_episode_id UUID REFERENCES episodes(id),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP
);
```

---

## Error Handling Strategy

### Error Types and Responses

| Error Scenario | HTTP Status | User Message | System Action |
|----------------|-------------|--------------|---------------|
| Show not found in TMDB | 404 | "Show not found. Try a different search term." | Log, suggest alternatives |
| Subtitle not available | 404 | "No subtitles available for this episode." | Offer manual upload option |
| OpenSubtitles rate limit | 429 | "Too many requests. Please try again in X minutes." | Queue request, backoff |
| OpenSubtitles down | 503 | "Subtitle service temporarily unavailable." | Retry with exponential backoff |
| OpenAI rate limit | 429 | "Content generation busy. Retrying..." | Auto-retry with backoff |
| OpenAI API error | 500 | "Content generation failed. Please try again." | Log, alert, offer retry |
| Parsing failure | 500 | "Could not process subtitle file." | Log, flag subtitle quality |
| Already generated | 409 | "This episode already has content." | Return existing episode |

### Retry Configuration

```java
@Configuration
public class RetryConfig {

    @Bean
    public RetryTemplate openSubtitlesRetry() {
        return RetryTemplate.builder()
            .maxAttempts(3)
            .exponentialBackoff(1000, 2.0, 10000)
            .retryOn(RateLimitExceededException.class)
            .retryOn(ServiceUnavailableException.class)
            .build();
    }

    @Bean
    public RetryTemplate openAiRetry() {
        return RetryTemplate.builder()
            .maxAttempts(5)
            .exponentialBackoff(2000, 2.0, 30000)
            .retryOn(RateLimitExceededException.class)
            .build();
    }
}
```

---

## Security Considerations

### API Key Management

```
Environment Variables (NEVER in code):
- TMDB_API_KEY
- OPENSUBTITLES_API_KEY
- OPENSUBTITLES_USERNAME
- OPENSUBTITLES_PASSWORD
- OPENAI_API_KEY

Spring Configuration:
spring:
  config:
    import: optional:file:.env[.properties]

external-apis:
  tmdb:
    api-key: ${TMDB_API_KEY}
    base-url: https://api.themoviedb.org/3
  opensubtitles:
    api-key: ${OPENSUBTITLES_API_KEY}
    base-url: https://api.opensubtitles.com/api/v1
  openai:
    api-key: ${OPENAI_API_KEY}
    model: gpt-4o-mini
```

### Input Validation

- Sanitize all user inputs before passing to external APIs
- Validate TMDB/IMDB IDs against expected format
- Limit script size to prevent DoS (max 500KB)
- Rate limit generation requests per user (5/hour)

### Output Sanitization

- Sanitize AI-generated content before storing
- Remove any potentially harmful content from subtitles
- Escape HTML entities in dialogue text

---

## Trade-offs and Alternatives Considered

### Option 1: OpenSubtitles Only (Chosen)

**Pros:**
- Largest subtitle database
- Official REST API
- Supports IMDB ID search

**Cons:**
- Download limits on free tier
- Quality varies

### Option 2: Multiple Subtitle Sources (Considered)

Using Addic7ed, Podnapisi, Subscene as fallbacks.

**Pros:**
- Higher availability
- Fallback options

**Cons:**
- Multiple integrations to maintain
- Inconsistent APIs (some require scraping)
- Legal gray area for some sources

**Decision:** Start with OpenSubtitles only. Add fallbacks later if needed.

### Option 3: Speech-to-Text from Video (Deferred)

Using Whisper API on actual video files.

**Pros:**
- Works for any content
- No dependency on subtitle databases

**Cons:**
- Requires video file access
- Much higher cost
- Slower processing

**Decision:** Consider for premium feature when no subtitles available.

### AI Provider Alternatives

| Provider | Pros | Cons |
|----------|------|------|
| OpenAI GPT-4o | Best quality, structured output | Higher cost |
| OpenAI GPT-4o-mini | Good balance of cost/quality | Slightly lower quality |
| Claude | Excellent at language tasks | Different API structure |
| Local LLM (Ollama) | No API costs | Hardware requirements, lower quality |

**Decision:** Start with GPT-4o-mini for cost efficiency. Allow configuration to switch models.

---

## Implementation Plan

### Phase 1: Foundation (Week 1)
1. [ ] Create `generation` module structure
2. [ ] Implement TMDB adapter with show search
3. [ ] Add episode listing endpoint
4. [ ] Set up API key configuration
5. [ ] Add basic error handling

### Phase 2: Script Fetching (Week 2)
1. [ ] Implement OpenSubtitles authentication
2. [ ] Add subtitle search by IMDB ID
3. [ ] Implement subtitle download
4. [ ] Create SRT/VTT parser
5. [ ] Add script caching table and logic

### Phase 3: AI Integration (Week 3)
1. [ ] Set up OpenAI client
2. [ ] Design and test vocabulary extraction prompt
3. [ ] Design and test grammar extraction prompt
4. [ ] Design and test expression extraction prompt
5. [ ] Implement exercise generation

### Phase 4: Orchestration (Week 4)
1. [ ] Create async job processing (Spring @Async or message queue)
2. [ ] Implement generation status tracking
3. [ ] Add progress updates (polling or WebSocket)
4. [ ] Connect to existing Episode/Vocabulary/etc. repositories
5. [ ] End-to-end testing

### Phase 5: Frontend Integration (Week 5)
1. [ ] Show search UI component
2. [ ] Episode selection flow
3. [ ] Generation progress indicator
4. [ ] Error handling and retry UI
5. [ ] Success flow to lesson view

### Phase 6: Hardening (Week 6)
1. [ ] Comprehensive error handling
2. [ ] Rate limiting per user
3. [ ] Monitoring and alerts
4. [ ] Documentation
5. [ ] Load testing

---

## Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| OpenSubtitles API changes/deprecation | High | Low | Abstract behind port interface, monitor announcements |
| OpenSubtitles download limits | Medium | High | Implement caching, consider VIP tier ($5/month) |
| OpenAI costs exceed budget | Medium | Medium | Use GPT-4o-mini, cache results, set spending alerts |
| AI generates inappropriate content | High | Low | Content moderation, human review option |
| Subtitle quality issues | Medium | Medium | Quality scoring, fallback to manual |
| Rate limits during high traffic | Medium | Medium | Request queuing, user quotas |
| Legal concerns with subtitle sources | High | Low | Use only official APIs, terms compliance |

---

## Cost Estimates (Monthly)

| Service | Free Tier | Estimated Usage | Monthly Cost |
|---------|-----------|-----------------|--------------|
| TMDB | Unlimited (non-commercial) | 10K requests | $0 |
| OpenSubtitles | 5 downloads/day | VIP tier needed | $5 |
| OpenAI GPT-4o-mini | Pay per use | 100 episodes, ~$0.05/episode | $5 |
| **Total** | - | - | **~$10/month** |

---

## Success Metrics

- **Generation Success Rate**: >95% of requests complete successfully
- **Generation Time**: <90 seconds from request to completion
- **Content Quality**: >4.0/5.0 user rating on generated content
- **API Availability**: >99% uptime for generation feature
- **Cache Hit Rate**: >80% for script downloads

---

## References

- [OpenSubtitles REST API Documentation](https://opensubtitles.stoplight.io/docs/opensubtitles-api/e3750fd63a100-getting-started)
- [TMDB API Documentation](https://developer.themoviedb.org/docs/getting-started)
- [OpenAI API Reference](https://platform.openai.com/docs/api-reference)
- [OpenSubtitles Best Practices](https://opensubtitles.stoplight.io/docs/opensubtitles-api/6ef2e232095c7-best-practices)
- [TMDB Rate Limiting](https://developer.themoviedb.org/docs/rate-limiting)

---

## Appendix A: SRT Parsing Example

```java
public class SrtParser {

    private static final Pattern TIMESTAMP_PATTERN =
        Pattern.compile("(\\d{2}:\\d{2}:\\d{2},\\d{3}) --> (\\d{2}:\\d{2}:\\d{2},\\d{3})");

    public String parseToPlainText(String srtContent) {
        StringBuilder dialogue = new StringBuilder();
        String[] blocks = srtContent.split("\\n\\n");

        for (String block : blocks) {
            String[] lines = block.split("\\n");
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].trim();
                // Skip sequence numbers and timestamps
                if (line.matches("\\d+") || TIMESTAMP_PATTERN.matcher(line).matches()) {
                    continue;
                }
                // Remove HTML tags from subtitles
                line = line.replaceAll("<[^>]+>", "");
                if (!line.isEmpty()) {
                    dialogue.append(line).append(" ");
                }
            }
        }
        return dialogue.toString().trim();
    }
}
```

## Appendix B: Example Extracted Content

```json
{
  "vocabulary": [
    {
      "term": "triage",
      "definition": "The process of determining the priority of patients' treatments based on the severity of their condition",
      "phonetic": "/triːˈɑːʒ/",
      "category": "MEDICAL",
      "exampleSentence": "We need to triage these patients immediately.",
      "contextTimestamp": "early"
    }
  ],
  "grammarPoints": [
    {
      "title": "Present Perfect for Recent Events",
      "explanation": "Used to describe actions that happened recently and have relevance to the present moment",
      "structure": "have/has + past participle",
      "example": "The patient has just arrived from the accident scene.",
      "contextQuote": "He's just come in from the MVA on Route 9."
    }
  ],
  "expressions": [
    {
      "phrase": "stat",
      "meaning": "Immediately, without delay (medical abbreviation from Latin 'statim')",
      "contextQuote": "I need a chest X-ray, stat!",
      "usageNote": "Used in urgent medical situations to indicate immediate priority"
    }
  ]
}
```
