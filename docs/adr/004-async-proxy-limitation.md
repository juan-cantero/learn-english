# ADR-004: Spring @Async Proxy Limitation Fix

## Date
2026-01-31

## Status
Resolved

## Context

The lesson generation process involves multiple slow operations (OpenAI API calls, script fetching, audio generation) that can take several minutes. To provide a good user experience, we wanted to:

1. Return the job ID immediately to the frontend
2. Process the generation in the background
3. Allow the frontend to poll for progress updates

## Problem

The `@Async` annotation on `processGenerationAsync()` method was **not working**. The entire generation ran synchronously, blocking the HTTP request for 60+ seconds until it timed out.

### Root Cause

Spring's `@Async` uses **proxy-based AOP**. When you call an `@Async` method from within the same class, the call bypasses the proxy and executes synchronously.

```java
@Service
public class GenerateEpisodeLessonService {

    public GenerationJob startGeneration(GenerationCommand command) {
        // ...
        // This call BYPASSES the proxy - @Async is ignored!
        this.processGenerationAsync(jobId, imdbId, command);
        return job;
    }

    @Async  // This annotation has NO EFFECT when called from same class
    public void processGenerationAsync(UUID jobId, String imdbId, GenerationCommand command) {
        // Runs synchronously, blocking the HTTP thread
    }
}
```

### Symptoms

- HTTP request blocked for 60+ seconds
- Frontend showed "Generating Lesson..." on the button but no spinner in header
- "Generation started!" banner appeared AFTER generation completed
- Timeouts from OpenAI API caused the entire request to fail

## Solution

Extract the async processing into a **separate Spring bean**. This ensures Spring's proxy intercepts the `@Async` call.

### Before (broken)

```
GenerateEpisodeLessonService
├── startGeneration()
│   └── this.processGenerationAsync()  ← Same class call, proxy bypassed
└── processGenerationAsync() @Async    ← Annotation ignored
```

### After (working)

```
GenerateEpisodeLessonService
├── startGeneration()
│   └── asyncProcessor.processGeneration()  ← Different bean, proxy works
│
AsyncGenerationProcessor
└── processGeneration() @Async              ← Annotation works correctly
```

### Files Changed

1. **Created**: `AsyncGenerationProcessor.java`
   - New service containing the `@Async` method
   - Handles all generation steps (fetch script, extract content, generate exercises, etc.)

2. **Simplified**: `GenerateEpisodeLessonService.java`
   - Only creates the job and delegates to `AsyncGenerationProcessor`
   - Returns immediately with the job ID

## Verification

After the fix:
1. HTTP request returns in <1 second with job ID
2. Frontend shows spinner in header immediately
3. Polling works correctly, showing real-time progress
4. Generation continues in background even if user navigates away

## Lessons Learned

1. **Spring proxy limitation**: `@Async`, `@Transactional`, and other proxy-based annotations don't work for self-invocation (calling methods on `this`)

2. **Solutions for proxy bypass**:
   - Extract to a separate bean (recommended)
   - Inject the bean into itself and call via the injected reference
   - Use `@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)`
   - Use AspectJ compile-time weaving instead of Spring AOP

3. **Testing tip**: If async behavior isn't working, check the thread name in logs. If it shows `nio-8080-exec-X` instead of a task executor thread, the `@Async` isn't being applied.

## References

- [Spring @Async Documentation](https://docs.spring.io/spring-framework/reference/integration/scheduling.html#scheduling-annotation-support-async)
- [Baeldung: Spring Async](https://www.baeldung.com/spring-async)
