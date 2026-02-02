package com.learntv.api.generation.domain.model;

public record ExtractedExpression(
    String phrase,
    String meaning,
    String context,
    String usageNote,
    String audioUrl
) {
    /**
     * Create a new ExtractedExpression with an audio URL.
     */
    public ExtractedExpression withAudioUrl(String audioUrl) {
        return new ExtractedExpression(phrase, meaning, context, usageNote, audioUrl);
    }
}
