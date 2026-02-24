package com.learntv.api.shared.config;

import java.util.Set;

/**
 * Sanitizes user-controlled and external inputs before injection into LLM prompts.
 * Prevents prompt injection attacks by validating, truncating, and wrapping untrusted content.
 */
public final class PromptSanitizer {

    private PromptSanitizer() {}

    private static final Set<String> ALLOWED_GENRES = Set.of(
            "drama", "comedy", "thriller", "scifi", "crime",
            "horror", "romance", "action", "animation", "documentary",
            "fantasy", "mystery", "adventure", "western"
    );

    private static final String DEFAULT_GENRE = "drama";

    /**
     * Validates genre against a whitelist. Returns default if not recognized.
     */
    public static String sanitizeGenre(String genre) {
        if (genre == null || genre.isBlank()) {
            return DEFAULT_GENRE;
        }
        String normalized = genre.strip().toLowerCase().replaceAll("[^a-z]", "");
        return ALLOWED_GENRES.contains(normalized) ? normalized : DEFAULT_GENRE;
    }

    /**
     * Sanitizes short untrusted strings (vocab terms, transcription text, etc.).
     * Strips newlines and control characters, truncates to maxLength.
     */
    public static String sanitizeShortInput(String input, int maxLength) {
        if (input == null || input.isBlank()) {
            return "";
        }
        // Strip control characters (newlines, tabs, etc.)
        String cleaned = input.replaceAll("[\\p{Cntrl}]", " ")
                .replaceAll("\\s+", " ")
                .strip();
        if (cleaned.length() > maxLength) {
            cleaned = cleaned.substring(0, maxLength);
        }
        return cleaned;
    }

    /**
     * Sanitizes large untrusted content (SRT scripts, dialogue text).
     * Truncates and wraps in XML-style delimiters so the LLM treats it as data.
     */
    public static String sanitizeScriptContent(String script, int maxChars) {
        if (script == null || script.isBlank()) {
            return "<script-content>\n</script-content>";
        }
        String content = script;
        if (content.length() > maxChars) {
            content = content.substring(0, maxChars);
        }
        return "<script-content>\n" + content + "\n</script-content>";
    }
}
