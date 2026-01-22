package com.learntv.api.generation.domain.model;

import java.util.List;

public record GeneratedLesson(
    List<ExtractedVocabulary> vocabulary,
    List<ExtractedGrammar> grammarPoints,
    List<ExtractedExpression> expressions,
    List<GeneratedExercise> exercises
) {}
