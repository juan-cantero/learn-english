# LearnTV - Backend Planning Document

## Tech Stack

| Layer | Technology |
|-------|------------|
| **Language** | Java 21 |
| **Framework** | Spring Boot 3.2+ |
| **Architecture** | Hexagonal (Ports & Adapters) |
| **Database (Local)** | H2 (in-memory) or PostgreSQL via Docker |
| **Database (Prod)** | Supabase (PostgreSQL) |
| **ORM** | Spring Data JPA + Hibernate |
| **Validation** | Jakarta Validation |
| **API Docs** | SpringDoc OpenAPI (Swagger) |
| **Build** | Gradle (Kotlin DSL) |

---

## 1. Domain Model

### Core Entities

```mermaid
classDiagram
    class Show {
        -UUID id
        -String slug
        -String title
        -String description
        -String posterUrl
        -String bannerUrl
        -Genre genre
        -AccentType accentType
        -DifficultyLevel difficulty
        -Integer releaseYear
        -Integer totalSeasons
        -LocalDateTime createdAt
        +addEpisode(Episode)
        +getEpisodeCount()
    }

    class Episode {
        -UUID id
        -String slug
        -Integer season
        -Integer episodeNumber
        -String title
        -LocalDate airDate
        -Integer durationMinutes
        -String summary
        -String context
        -LocalDateTime createdAt
        +addVocabulary(Vocabulary)
        +addGrammarPoint(GrammarPoint)
        +addExpression(Expression)
        +addExercise(Exercise)
        +getCompletionStats()
    }

    class Vocabulary {
        -UUID id
        -String word
        -String phonetic
        -PartOfSpeech partOfSpeech
        -String definition
        -String exampleSentence
        -VocabularyCategory category
        -DifficultyLevel difficulty
        -Integer sortOrder
    }

    class GrammarPoint {
        -UUID id
        -String title
        -String tag
        -String level
        -String pattern
        -String explanation
        -String dialogueExample
        -String speaker
        -List~String~ moreExamples
        -Integer sortOrder
    }

    class Expression {
        -UUID id
        -String quote
        -String speaker
        -String meaning
        -String usageNotes
        -String tone
        -Integer sortOrder
    }

    class Exercise {
        -UUID id
        -ExerciseType type
        -String question
        -List~ExerciseOption~ options
        -String correctAnswer
        -String hint
        -String explanation
        -Integer sortOrder
    }

    class ExerciseOption {
        -String text
        -Boolean isCorrect
    }

    class User {
        -UUID id
        -String email
        -String displayName
        -String avatarUrl
        -LocalDateTime createdAt
        -LocalDateTime lastLoginAt
    }

    class UserProgress {
        -UUID id
        -Integer vocabularyViewed
        -Integer grammarViewed
        -List~UUID~ exercisesCompleted
        -Integer quizScore
        -Integer quizAttempts
        -LocalDateTime completedAt
        -LocalDateTime lastAccessedAt
    }

    Show "1" --> "*" Episode : contains
    Episode "1" --> "*" Vocabulary : has
    Episode "1" --> "*" GrammarPoint : has
    Episode "1" --> "*" Expression : has
    Episode "1" --> "*" Exercise : has
    Exercise "1" --> "*" ExerciseOption : has
    User "1" --> "*" UserProgress : tracks
    UserProgress "*" --> "1" Episode : for
```

### Enumerations

```mermaid
classDiagram
    class Genre {
        <<enumeration>>
        MEDICAL_DRAMA
        LEGAL_DRAMA
        COMEDY
        THRILLER
        SCI_FI
        CRIME
        DOCUMENTARY
    }

    class AccentType {
        <<enumeration>>
        AMERICAN
        BRITISH
        AUSTRALIAN
        MIXED
    }

    class DifficultyLevel {
        <<enumeration>>
        BEGINNER
        INTERMEDIATE
        UPPER_INTERMEDIATE
        ADVANCED
    }

    class PartOfSpeech {
        <<enumeration>>
        NOUN
        VERB
        ADJECTIVE
        ADVERB
        PHRASE
        IDIOM
        ABBREVIATION
    }

    class VocabularyCategory {
        <<enumeration>>
        MEDICAL
        LEGAL
        WORKPLACE
        INFORMAL
        SLANG
        TECHNICAL
        EVERYDAY
    }

    class ExerciseType {
        <<enumeration>>
        FILL_BLANK
        MATCHING
        MULTIPLE_CHOICE
        REORDER
        TRUE_FALSE
    }
```

---

## 2. Database Schema (ERD)

```mermaid
erDiagram
    SHOWS {
        uuid id PK
        varchar(100) slug UK
        varchar(200) title
        text description
        varchar(500) poster_url
        varchar(500) banner_url
        varchar(50) genre
        varchar(50) accent_type
        varchar(30) difficulty
        int release_year
        int total_seasons
        timestamp created_at
    }

    EPISODES {
        uuid id PK
        uuid show_id FK
        varchar(100) slug
        int season
        int episode_number
        varchar(200) title
        date air_date
        int duration_minutes
        text summary
        text context
        timestamp created_at
    }

    VOCABULARY {
        uuid id PK
        uuid episode_id FK
        varchar(100) word
        varchar(100) phonetic
        varchar(50) part_of_speech
        text definition
        text example_sentence
        varchar(50) category
        varchar(30) difficulty
        int sort_order
    }

    GRAMMAR_POINTS {
        uuid id PK
        uuid episode_id FK
        varchar(200) title
        varchar(100) tag
        varchar(50) level
        varchar(200) pattern
        text explanation
        text dialogue_example
        varchar(100) speaker
        jsonb more_examples
        int sort_order
    }

    EXPRESSIONS {
        uuid id PK
        uuid episode_id FK
        text quote
        varchar(100) speaker
        text meaning
        text usage_notes
        varchar(50) tone
        int sort_order
    }

    EXERCISES {
        uuid id PK
        uuid episode_id FK
        varchar(50) type
        text question
        jsonb options
        varchar(500) correct_answer
        text hint
        text explanation
        int sort_order
    }

    USERS {
        uuid id PK
        varchar(255) email UK
        varchar(100) display_name
        varchar(500) avatar_url
        timestamp created_at
        timestamp last_login_at
    }

    USER_PROGRESS {
        uuid id PK
        uuid user_id FK
        uuid episode_id FK
        int vocabulary_viewed
        int grammar_viewed
        jsonb exercises_completed
        int quiz_score
        int quiz_attempts
        timestamp completed_at
        timestamp last_accessed_at
    }

    SHOWS ||--o{ EPISODES : "has many"
    EPISODES ||--o{ VOCABULARY : "has many"
    EPISODES ||--o{ GRAMMAR_POINTS : "has many"
    EPISODES ||--o{ EXPRESSIONS : "has many"
    EPISODES ||--o{ EXERCISES : "has many"
    USERS ||--o{ USER_PROGRESS : "has many"
    EPISODES ||--o{ USER_PROGRESS : "tracked by"
```

---

## 3. Hexagonal Architecture

### Layer Diagram

```mermaid
flowchart TB
    subgraph INFRASTRUCTURE["Infrastructure Layer (Adapters)"]
        subgraph INPUT["Input Adapters (Driving)"]
            REST["REST Controllers"]
            SWAGGER["OpenAPI/Swagger"]
        end

        subgraph OUTPUT["Output Adapters (Driven)"]
            JPA["JPA Repositories"]
            MAPPER["Entity Mappers"]
        end
    end

    subgraph APPLICATION["Application Layer"]
        subgraph PORTS_IN["Input Ports"]
            UC1["GetAllShowsUseCase"]
            UC2["GetShowBySlugUseCase"]
            UC3["GetEpisodeLessonUseCase"]
            UC4["SaveUserProgressUseCase"]
            UC5["CreateShowUseCase"]
        end

        subgraph PORTS_OUT["Output Ports"]
            REPO1["ShowRepository"]
            REPO2["EpisodeRepository"]
            REPO3["UserProgressRepository"]
        end
    end

    subgraph DOMAIN["Domain Layer (Core)"]
        ENTITIES["Entities"]
        VO["Value Objects"]
        DOMAIN_SVC["Domain Services"]
    end

    REST --> UC1
    REST --> UC2
    REST --> UC3
    REST --> UC4
    REST --> UC5

    UC1 --> REPO1
    UC2 --> REPO1
    UC3 --> REPO2
    UC4 --> REPO3
    UC5 --> REPO1

    REPO1 --> JPA
    REPO2 --> JPA
    REPO3 --> JPA

    UC1 --> ENTITIES
    UC2 --> ENTITIES
    UC3 --> ENTITIES
    UC4 --> ENTITIES
    UC5 --> ENTITIES

    ENTITIES --> VO
    ENTITIES --> DOMAIN_SVC
```

### Package Structure

```
com.learntv.api/
├── domain/                           # CORE (no framework dependencies)
│   ├── model/
│   │   ├── show/
│   │   │   ├── Show.java
│   │   │   ├── ShowId.java          # Value Object
│   │   │   ├── Genre.java
│   │   │   └── AccentType.java
│   │   ├── episode/
│   │   │   ├── Episode.java
│   │   │   ├── EpisodeId.java
│   │   │   └── EpisodeContent.java  # Aggregate
│   │   ├── vocabulary/
│   │   │   └── Vocabulary.java
│   │   ├── grammar/
│   │   │   └── GrammarPoint.java
│   │   ├── expression/
│   │   │   └── Expression.java
│   │   ├── exercise/
│   │   │   ├── Exercise.java
│   │   │   └── ExerciseOption.java
│   │   └── user/
│   │       ├── User.java
│   │       └── UserProgress.java
│   └── exception/
│       ├── ShowNotFoundException.java
│       └── EpisodeNotFoundException.java
│
├── application/                      # USE CASES
│   ├── port/
│   │   ├── input/                   # Input Ports (Driving)
│   │   │   ├── GetAllShowsUseCase.java
│   │   │   ├── GetShowBySlugUseCase.java
│   │   │   ├── GetEpisodeLessonUseCase.java
│   │   │   ├── CreateShowUseCase.java
│   │   │   └── SaveUserProgressUseCase.java
│   │   └── output/                  # Output Ports (Driven)
│   │       ├── ShowRepositoryPort.java
│   │       ├── EpisodeRepositoryPort.java
│   │       └── UserProgressRepositoryPort.java
│   └── service/                     # Use Case Implementations
│       ├── ShowService.java
│       ├── EpisodeService.java
│       └── UserProgressService.java
│
└── infrastructure/                   # ADAPTERS
    ├── adapter/
    │   ├── input/
    │   │   └── rest/                # REST Controllers
    │   │       ├── ShowController.java
    │   │       ├── EpisodeController.java
    │   │       ├── ProgressController.java
    │   │       └── dto/
    │   │           ├── ShowResponse.java
    │   │           ├── EpisodeResponse.java
    │   │           ├── LessonResponse.java
    │   │           └── CreateShowRequest.java
    │   └── output/
    │       └── persistence/         # JPA Implementation
    │           ├── entity/
    │           │   ├── ShowJpaEntity.java
    │           │   ├── EpisodeJpaEntity.java
    │           │   └── ...
    │           ├── repository/
    │           │   ├── ShowJpaRepository.java
    │           │   └── EpisodeJpaRepository.java
    │           ├── adapter/
    │           │   ├── ShowRepositoryAdapter.java
    │           │   └── EpisodeRepositoryAdapter.java
    │           └── mapper/
    │               ├── ShowMapper.java
    │               └── EpisodeMapper.java
    └── config/
        ├── BeanConfiguration.java   # Wire use cases
        ├── WebConfig.java
        └── OpenApiConfig.java
```

---

## 4. REST API Design

### Endpoints

```yaml
# Shows
GET    /api/v1/shows                    # List all shows
GET    /api/v1/shows/{slug}             # Get show with episodes
POST   /api/v1/shows                    # Create show (admin)
PUT    /api/v1/shows/{slug}             # Update show (admin)
DELETE /api/v1/shows/{slug}             # Delete show (admin)

# Episodes
GET    /api/v1/shows/{showSlug}/episodes                    # List episodes
GET    /api/v1/shows/{showSlug}/episodes/{episodeSlug}      # Get episode lesson (full content)
POST   /api/v1/shows/{showSlug}/episodes                    # Create episode (admin)
PUT    /api/v1/shows/{showSlug}/episodes/{episodeSlug}      # Update episode (admin)

# Vocabulary (for a specific episode)
GET    /api/v1/episodes/{episodeId}/vocabulary              # List vocabulary
POST   /api/v1/episodes/{episodeId}/vocabulary              # Add vocabulary (admin)

# Grammar Points
GET    /api/v1/episodes/{episodeId}/grammar                 # List grammar points
POST   /api/v1/episodes/{episodeId}/grammar                 # Add grammar point (admin)

# Expressions
GET    /api/v1/episodes/{episodeId}/expressions             # List expressions
POST   /api/v1/episodes/{episodeId}/expressions             # Add expression (admin)

# Exercises
GET    /api/v1/episodes/{episodeId}/exercises               # List exercises
POST   /api/v1/episodes/{episodeId}/exercises               # Add exercise (admin)
POST   /api/v1/exercises/{exerciseId}/check                 # Check answer

# User Progress
GET    /api/v1/users/me/progress                            # Get all progress
GET    /api/v1/users/me/progress/{episodeId}                # Get episode progress
POST   /api/v1/users/me/progress/{episodeId}                # Save progress
```

### Response Examples

```json
// GET /api/v1/shows
{
  "data": [
    {
      "id": "uuid",
      "slug": "the-pitt",
      "title": "The Pitt",
      "description": "Medical drama...",
      "posterUrl": "https://...",
      "genre": "MEDICAL_DRAMA",
      "accentType": "AMERICAN",
      "difficulty": "UPPER_INTERMEDIATE",
      "releaseYear": 2025,
      "totalSeasons": 1,
      "episodeCount": 15
    }
  ],
  "meta": {
    "total": 1,
    "page": 1,
    "pageSize": 20
  }
}
```

```json
// GET /api/v1/shows/the-pitt/episodes/s01e01-7am
{
  "episode": {
    "id": "uuid",
    "slug": "s01e01-7am",
    "season": 1,
    "episodeNumber": 1,
    "title": "7:00 A.M.",
    "summary": "...",
    "context": "...",
    "durationMinutes": 53
  },
  "show": {
    "slug": "the-pitt",
    "title": "The Pitt"
  },
  "vocabulary": [
    {
      "id": "uuid",
      "word": "trauma",
      "phonetic": "/ˈtrɔːmə/",
      "partOfSpeech": "NOUN",
      "definition": "A serious injury...",
      "exampleSentence": "We've got a trauma coming in.",
      "category": "MEDICAL",
      "difficulty": "INTERMEDIATE"
    }
  ],
  "grammarPoints": [...],
  "expressions": [...],
  "exercises": [...]
}
```

---

## 5. Use Cases

### Core Use Cases

```mermaid
graph LR
    subgraph Actors
        USER[User/Learner]
        ADMIN[Admin]
    end

    subgraph "Show Management"
        UC1[Browse Shows]
        UC2[View Show Details]
        UC3[Create Show]
        UC4[Update Show]
    end

    subgraph "Episode Learning"
        UC5[View Episode Lesson]
        UC6[Study Vocabulary]
        UC7[Study Grammar]
        UC8[Study Expressions]
    end

    subgraph "Exercises"
        UC9[Complete Fill-in-Blank]
        UC10[Complete Matching]
        UC11[Take Quiz]
        UC12[Check Answer]
    end

    subgraph "Progress"
        UC13[Track Progress]
        UC14[View Statistics]
        UC15[Resume Learning]
    end

    USER --> UC1
    USER --> UC2
    USER --> UC5
    USER --> UC6
    USER --> UC7
    USER --> UC8
    USER --> UC9
    USER --> UC10
    USER --> UC11
    USER --> UC12
    USER --> UC13
    USER --> UC14
    USER --> UC15

    ADMIN --> UC3
    ADMIN --> UC4
    ADMIN --> UC1
    ADMIN --> UC2
```

### Use Case Details

| Use Case | Actor | Description | Input | Output |
|----------|-------|-------------|-------|--------|
| Browse Shows | User | View catalog of TV shows | Page, filters | List of shows |
| View Show Details | User | See show info + episode list | Show slug | Show + episodes |
| View Episode Lesson | User | Get full lesson content | Show slug, episode slug | All lesson data |
| Complete Exercise | User | Submit exercise answer | Exercise ID, answer | Correct/incorrect + explanation |
| Track Progress | User | Save learning progress | Episode ID, progress data | Saved progress |
| Create Show | Admin | Add new TV show | Show data | Created show |
| Create Episode | Admin | Add episode with content | Episode + vocabulary + grammar + exercises | Created episode |

---

## 6. Sequence Diagrams

### Get Episode Lesson

```mermaid
sequenceDiagram
    participant C as Client
    participant RC as RestController
    participant UC as GetEpisodeLessonUseCase
    participant ER as EpisodeRepositoryPort
    participant DB as Database

    C->>RC: GET /shows/{show}/episodes/{episode}
    RC->>UC: execute(showSlug, episodeSlug)
    UC->>ER: findByShowSlugAndEpisodeSlug(showSlug, episodeSlug)
    ER->>DB: SELECT episode + vocabulary + grammar + expressions + exercises
    DB-->>ER: Data
    ER-->>UC: Episode with content
    UC-->>RC: EpisodeLesson (domain)
    RC-->>C: LessonResponse (DTO)
```

### Save Progress

```mermaid
sequenceDiagram
    participant C as Client
    participant RC as ProgressController
    participant UC as SaveUserProgressUseCase
    participant PR as UserProgressRepositoryPort
    participant DB as Database

    C->>RC: POST /users/me/progress/{episodeId}
    Note right of C: { vocabularyViewed: 5, quizScore: 80 }
    RC->>UC: execute(userId, episodeId, progressData)
    UC->>PR: findByUserAndEpisode(userId, episodeId)
    PR->>DB: SELECT
    DB-->>PR: Existing progress (or null)
    UC->>UC: Merge progress
    UC->>PR: save(updatedProgress)
    PR->>DB: INSERT/UPDATE
    DB-->>PR: Saved
    PR-->>UC: UserProgress
    UC-->>RC: UserProgress (domain)
    RC-->>C: ProgressResponse (DTO)
```

---

## 7. Technology Decisions

| Decision | Choice | Reason |
|----------|--------|--------|
| **Java Version** | 21 | LTS, virtual threads, pattern matching |
| **Spring Boot** | 3.2+ | Latest features, Java 21 support |
| **Database Access** | Spring Data JPA | Standard, well-documented |
| **Validation** | Jakarta Validation | Bean validation standard |
| **Mapping** | MapStruct | Compile-time, type-safe |
| **API Docs** | SpringDoc OpenAPI | Auto-generates Swagger UI |
| **Testing** | JUnit 5 + Mockito + Testcontainers | Standard stack |
| **Local DB** | H2 or PostgreSQL (Docker) | Easy setup |
| **Build Tool** | Gradle (Kotlin DSL) | Modern, flexible |

---

## Next Steps

1. [ ] Review and approve this plan
2. [ ] Set up Spring Boot project structure
3. [ ] Implement domain layer (entities, value objects)
4. [ ] Implement application layer (use cases, ports)
5. [ ] Implement infrastructure layer (JPA, REST)
6. [ ] Seed with The Pitt Episode 1 data
7. [ ] Test API with Swagger UI

---

## Questions for You

1. **H2 or PostgreSQL (Docker) for local development?**
   - H2: Zero setup, in-memory
   - PostgreSQL: Closer to production (Supabase)

2. **MapStruct for mapping or manual mappers?**

3. **Any additional entities or features to add now?**
