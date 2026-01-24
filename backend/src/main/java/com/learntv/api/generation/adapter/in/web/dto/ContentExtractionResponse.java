package com.learntv.api.generation.adapter.in.web.dto;

import com.learntv.api.generation.domain.model.ExtractedExpression;
import com.learntv.api.generation.domain.model.ExtractedGrammar;
import com.learntv.api.generation.domain.model.ExtractedVocabulary;

import java.util.List;

/**
 * Response DTO for content extraction endpoints.
 */
public record ContentExtractionResponse(
        List<ExtractedVocabulary> vocabulary,
        List<ExtractedGrammar> grammar,
        List<ExtractedExpression> expressions,
        int totalItems
) {
    public static ContentExtractionResponse ofVocabulary(List<ExtractedVocabulary> vocabulary) {
        return new ContentExtractionResponse(vocabulary, null, null, vocabulary.size());
    }

    public static ContentExtractionResponse ofGrammar(List<ExtractedGrammar> grammar) {
        return new ContentExtractionResponse(null, grammar, null, grammar.size());
    }

    public static ContentExtractionResponse ofExpressions(List<ExtractedExpression> expressions) {
        return new ContentExtractionResponse(null, null, expressions, expressions.size());
    }
}
