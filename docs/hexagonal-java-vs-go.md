# Hexagonal Architecture: Java vs Go

Quick reference comparing hexagonal/clean architecture patterns between Java (Spring Boot) and Go.

## Folder Structure

### Java (Spring Boot)

```
catalog/
├── domain/
│   └── model/
│       ├── Show.java              # Entity with builder (100+ lines)
│       ├── Genre.java             # Enum
│       └── DifficultyLevel.java   # Enum
├── application/
│   ├── port/
│   │   └── ShowRepository.java    # Interface (output port)
│   └── usecase/
│       └── BrowseCatalogUseCase.java
└── adapter/
    ├── in/web/
    │   └── ShowController.java    # Input adapter (REST)
    └── out/persistence/
        ├── ShowJpaEntity.java
        ├── ShowJpaRepository.java
        └── ShowRepositoryAdapter.java  # Implements ShowRepository
```

### Go

```
catalog/
├── domain/
│   └── show.go                    # Struct + enums (~40 lines)
├── usecase/
│   └── browse_catalog.go          # UseCase + Interface defined here
└── adapter/
    ├── http/
    │   └── show_handler.go        # Input adapter (REST)
    └── postgres/
        └── show_repo.go           # Satisfies interface implicitly
```

---

## Domain Model

### Java
```java
public class Show {
    private final ShowId id;
    private final String title;
    private final String slug;
    private final Genre genre;
    // ... more fields

    private Show(Builder builder) { /* ... */ }

    public static Builder builder() { return new Builder(); }

    // Getters (one per field)
    public ShowId getId() { return id; }
    public String getTitle() { return title; }
    // ...

    // Builder class (~60 lines)
    public static class Builder {
        public Builder id(ShowId id) { /* ... */ }
        public Builder title(String title) { /* ... */ }
        // ...
        public Show build() { return new Show(this); }
    }
}
```

### Go
```go
package domain

import "github.com/google/uuid"

type Genre string

const (
    GenreDrama  Genre = "DRAMA"
    GenreComedy Genre = "COMEDY"
)

type Show struct {
    ID          uuid.UUID
    Title       string
    Slug        string
    Genre       Genre
    Description string
    ImageURL    string
}
```

No getters, no setters, no builder. Fields are public (capitalized).

---

## Interface Definition (Key Difference)

### Java: Interface in separate `port/` package

```java
// application/port/ShowRepository.java
package com.app.catalog.application.port;

public interface ShowRepository {
    List<Show> findAll();
    Optional<Show> findBySlug(String slug);
}

// application/usecase/BrowseCatalogUseCase.java
package com.app.catalog.application.usecase;

import com.app.catalog.application.port.ShowRepository;  // imports port

public class BrowseCatalogUseCase {
    private final ShowRepository showRepository;

    public BrowseCatalogUseCase(ShowRepository showRepository) {
        this.showRepository = showRepository;
    }
}

// adapter/out/persistence/ShowRepositoryAdapter.java
import com.app.catalog.application.port.ShowRepository;  // imports port

@Repository
public class ShowRepositoryAdapter implements ShowRepository {  // explicit
    // ...
}
```

### Go: Interface with the consumer (idiomatic)

```go
// usecase/browse_catalog.go
package usecase

// Interface defined HERE - where it's used
type ShowReader interface {
    FindAll(ctx context.Context) ([]domain.Show, error)
}

type BrowseCatalogUseCase struct {
    shows ShowReader
}

func NewBrowseCatalogUseCase(shows ShowReader) *BrowseCatalogUseCase {
    return &BrowseCatalogUseCase{shows: shows}
}
```

```go
// adapter/postgres/show_repo.go
package postgres

// NO "implements" keyword
// NO import from usecase package
// Just has methods that match the interface

type ShowRepo struct {
    db *sql.DB
}

func (r *ShowRepo) FindAll(ctx context.Context) ([]domain.Show, error) {
    // SQL query...
}
```

Go automatically recognizes that `ShowRepo` satisfies `ShowReader` because the method signatures match.

---

## Dependency Flow Diagram

### Java
```
┌─────────────────────────────────────────────────────────┐
│  APPLICATION LAYER                                      │
│  ┌─────────────────┐     ┌─────────────────────────┐   │
│  │ BrowseCatalog   │────►│ ShowRepository (port)   │   │
│  │ UseCase         │     │ (interface)             │   │
│  └─────────────────┘     └─────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
                                      ▲
                                      │ imports + implements
┌─────────────────────────────────────────────────────────┐
│  ADAPTER LAYER                                          │
│  ┌─────────────────────────┐                           │
│  │ ShowRepositoryAdapter   │                           │
│  │ implements ShowRepository│                           │
│  └─────────────────────────┘                           │
└─────────────────────────────────────────────────────────┘
```

### Go
```
┌─────────────────────────────────────────────────────────┐
│  APPLICATION LAYER (usecase/)                           │
│  ┌─────────────────────────────────────────────────┐   │
│  │ browse_catalog.go                               │   │
│  │                                                 │   │
│  │  type ShowReader interface { ... }  ◄── here   │   │
│  │  type BrowseCatalogUseCase struct { ... }      │   │
│  └─────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
                          ▲
                          │ satisfies (implicit)
┌─────────────────────────────────────────────────────────┐
│  ADAPTER LAYER (adapter/postgres/)                      │
│  ┌─────────────────────────────────────────────────┐   │
│  │ show_repo.go                                    │   │
│  │                                                 │   │
│  │  type ShowRepo struct { db *sql.DB }           │   │
│  │  func (r *ShowRepo) FindAll() []Show { ... }   │   │
│  │                                                 │   │
│  │  // NO imports from usecase package!           │   │
│  └─────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
```

---

## Wiring Dependencies

### Java (Spring auto-wires with annotations)

```java
@Configuration
public class CatalogConfig {

    @Bean
    public BrowseCatalogUseCase browseCatalogUseCase(ShowRepository repo) {
        return new BrowseCatalogUseCase(repo);
    }
}

// Or just use @Autowired / constructor injection
// Spring finds ShowRepositoryAdapter because of @Repository
```

### Go (manual, explicit wiring)

```go
// main.go
func main() {
    db, _ := sql.Open("postgres", "...")

    // Build dependency graph manually
    showRepo := postgres.NewShowRepo(db)
    browseCatalog := usecase.NewBrowseCatalogUseCase(showRepo)
    handler := http.NewShowHandler(browseCatalog)

    // Routes
    mux := http.NewServeMux()
    mux.HandleFunc("/api/v1/shows", handler.BrowseShows)

    http.ListenAndServe(":8080", mux)
}
```

---

## Testing

### Java (needs mocking framework)

```java
@ExtendWith(MockitoExtension.class)
class BrowseCatalogUseCaseTest {

    @Mock
    ShowRepository showRepository;

    @InjectMocks
    BrowseCatalogUseCase useCase;

    @Test
    void shouldReturnAllShows() {
        when(showRepository.findAll()).thenReturn(List.of(
            Show.builder().title("Seinfeld").build()
        ));

        List<Show> result = useCase.execute();

        assertThat(result).hasSize(1);
    }
}
```

### Go (plain struct, no framework)

```go
// Fake implementation
type fakeShowRepo struct {
    shows []domain.Show
}

func (f *fakeShowRepo) FindAll(ctx context.Context) ([]domain.Show, error) {
    return f.shows, nil
}

// Test
func TestBrowseCatalog(t *testing.T) {
    fake := &fakeShowRepo{
        shows: []domain.Show{{Title: "Seinfeld"}},
    }

    uc := usecase.NewBrowseCatalogUseCase(fake)
    result, err := uc.Execute(context.Background(), usecase.CatalogFilter{})

    if err != nil {
        t.Fatal(err)
    }
    if len(result) != 1 {
        t.Errorf("expected 1 show, got %d", len(result))
    }
}
```

---

## Advantages of Go's Implicit Interfaces

| Advantage | Explanation |
|-----------|-------------|
| **No wrappers for external libs** | Third-party code satisfies your interfaces automatically |
| **Simple testing** | Create fakes in 5 lines, no Mockito |
| **Small interfaces** | Define only what you need, where you need it |
| **No import cycles** | Adapter doesn't import from application layer |
| **Retrofitting** | Add interfaces to existing code without changes |

---

## When to Use Each Approach

| Scenario | Recommendation |
|----------|----------------|
| Large team with Java experience | Java/Spring Boot |
| Complex enterprise integrations | Java/Spring Boot |
| Need Hibernate for complex relations | Java/Spring Boot |
| New project, small team | Go |
| Microservices / cloud-native | Go |
| Resource-constrained deployment | Go |
| Learning / side project | Either (Go is simpler) |

---

## Quick Reference

| Concept | Java | Go |
|---------|------|-----|
| Interface definition | Separate `port/` package | With consumer (idiomatic) |
| Implementation | `implements` keyword | Implicit (method matching) |
| Dependency injection | Spring (annotations) | Manual in `main.go` |
| Mocking | Mockito, EasyMock | Plain structs |
| Domain model | Builder pattern | Simple structs |
| Lines of code | More verbose | Concise |
| Docker image | ~300MB | ~15MB |
| Memory usage | ~300-500MB | ~20-50MB |
