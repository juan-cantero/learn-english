package com.learntv.api.generation.domain.model;

public record ExtractedVocabulary(
    String term,
    String definition,
    String phonetic,
    String category,
    String exampleSentence,
    String audioUrl
) {}
