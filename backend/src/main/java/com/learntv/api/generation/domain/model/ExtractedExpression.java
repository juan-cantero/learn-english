package com.learntv.api.generation.domain.model;

public record ExtractedExpression(
    String phrase,
    String meaning,
    String context,
    String usageNote
) {}
