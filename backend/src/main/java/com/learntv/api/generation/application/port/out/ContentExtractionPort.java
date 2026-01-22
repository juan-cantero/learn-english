package com.learntv.api.generation.application.port.out;

import com.learntv.api.generation.domain.model.ExtractedExpression;
import com.learntv.api.generation.domain.model.ExtractedGrammar;
import com.learntv.api.generation.domain.model.ExtractedVocabulary;

import java.util.List;

public interface ContentExtractionPort {
    List<ExtractedVocabulary> extractVocabulary(String script, String genre);
    List<ExtractedGrammar> extractGrammar(String script);
    List<ExtractedExpression> extractExpressions(String script);
}
