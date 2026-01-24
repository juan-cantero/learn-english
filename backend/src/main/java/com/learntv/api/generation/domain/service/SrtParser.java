package com.learntv.api.generation.domain.service;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Parser for SRT (SubRip) subtitle files.
 * Converts SRT content to clean dialogue text suitable for NLP processing.
 */
@Component
public class SrtParser {

    // Pattern for SRT sequence numbers (line with just digits)
    private static final Pattern SEQUENCE_NUMBER = Pattern.compile("^\\d+$");

    // Pattern for SRT timestamps: 00:01:23,456 --> 00:01:25,789
    private static final Pattern TIMESTAMP = Pattern.compile(
            "^\\d{2}:\\d{2}:\\d{2}[,.]\\d{3}\\s*-->\\s*\\d{2}:\\d{2}:\\d{2}[,.]\\d{3}.*$"
    );

    // Pattern for HTML/formatting tags: <i>, </i>, <b>, </b>, <font...>, etc.
    private static final Pattern HTML_TAGS = Pattern.compile("<[^>]+>");

    // Pattern for ASS/SSA style codes: {\an8}, {\pos(x,y)}, etc.
    private static final Pattern ASS_CODES = Pattern.compile("\\{\\\\[^}]+\\}");

    // Pattern for hearing impaired descriptions: [music playing], (door slams), etc.
    private static final Pattern HEARING_IMPAIRED = Pattern.compile("^[\\[\\(][^\\]\\)]+[\\]\\)]$");

    // Pattern for speaker labels: WALTER:, MR. WHITE:, etc.
    private static final Pattern SPEAKER_LABEL = Pattern.compile("^[A-Z][A-Z\\s\\.]+:\\s*");

    // Pattern for music notes: ♪ lyrics ♪
    private static final Pattern MUSIC_NOTES = Pattern.compile("[♪♫]");

    /**
     * Parse SRT content to clean dialogue text.
     *
     * @param srtContent the raw SRT file content
     * @return clean dialogue text with one line per subtitle entry
     */
    public String parseToPlainText(String srtContent) {
        if (srtContent == null || srtContent.isBlank()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        String[] lines = srtContent.split("\\r?\\n");

        for (String line : lines) {
            String cleaned = cleanLine(line);
            if (!cleaned.isEmpty()) {
                if (result.length() > 0) {
                    result.append("\n");
                }
                result.append(cleaned);
            }
        }

        return result.toString().trim();
    }

    /**
     * Clean a single line of SRT content.
     *
     * @param line the raw line
     * @return cleaned line, or empty string if line should be skipped
     */
    private String cleanLine(String line) {
        if (line == null) {
            return "";
        }

        String trimmed = line.trim();

        // Skip empty lines
        if (trimmed.isEmpty()) {
            return "";
        }

        // Skip sequence numbers
        if (SEQUENCE_NUMBER.matcher(trimmed).matches()) {
            return "";
        }

        // Skip timestamps
        if (TIMESTAMP.matcher(trimmed).matches()) {
            return "";
        }

        // Skip pure hearing impaired lines
        if (HEARING_IMPAIRED.matcher(trimmed).matches()) {
            return "";
        }

        // Remove HTML tags
        String cleaned = HTML_TAGS.matcher(trimmed).replaceAll("");

        // Remove ASS/SSA style codes
        cleaned = ASS_CODES.matcher(cleaned).replaceAll("");

        // Remove music notes (often indicate song lyrics, which we skip)
        cleaned = MUSIC_NOTES.matcher(cleaned).replaceAll("");

        // Optionally remove speaker labels (keep the dialogue)
        cleaned = SPEAKER_LABEL.matcher(cleaned).replaceAll("");

        // Remove inline hearing impaired text: "Hello [door closes] there"
        cleaned = cleaned.replaceAll("\\[[^\\]]+\\]", "");
        cleaned = cleaned.replaceAll("\\([^)]+\\)", "");

        // Clean up multiple spaces
        cleaned = cleaned.replaceAll("\\s+", " ").trim();

        // Skip if line is now empty or too short
        if (cleaned.length() < 2) {
            return "";
        }

        // Skip lines that are just punctuation
        if (cleaned.matches("^[\\-\\s\\.\\?!,]+$")) {
            return "";
        }

        return cleaned;
    }

    /**
     * Parse SRT and return dialogue with preserved line breaks for multi-line subtitles.
     * This version keeps related dialogue together.
     *
     * @param srtContent the raw SRT file content
     * @return clean dialogue preserving subtitle groupings
     */
    public String parsePreservingGroups(String srtContent) {
        if (srtContent == null || srtContent.isBlank()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        StringBuilder currentGroup = new StringBuilder();
        String[] lines = srtContent.split("\\r?\\n");

        for (String line : lines) {
            String trimmed = line.trim();

            // Empty line marks end of subtitle group
            if (trimmed.isEmpty()) {
                if (currentGroup.length() > 0) {
                    if (result.length() > 0) {
                        result.append("\n\n");
                    }
                    result.append(currentGroup.toString().trim());
                    currentGroup.setLength(0);
                }
                continue;
            }

            // Skip sequence numbers and timestamps
            if (SEQUENCE_NUMBER.matcher(trimmed).matches() ||
                    TIMESTAMP.matcher(trimmed).matches()) {
                continue;
            }

            String cleaned = cleanLine(trimmed);
            if (!cleaned.isEmpty()) {
                if (currentGroup.length() > 0) {
                    currentGroup.append(" ");
                }
                currentGroup.append(cleaned);
            }
        }

        // Don't forget the last group
        if (currentGroup.length() > 0) {
            if (result.length() > 0) {
                result.append("\n\n");
            }
            result.append(currentGroup.toString().trim());
        }

        return result.toString().trim();
    }
}
