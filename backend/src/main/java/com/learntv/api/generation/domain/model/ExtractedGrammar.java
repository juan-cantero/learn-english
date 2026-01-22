package com.learntv.api.generation.domain.model;

import java.util.List;

public record ExtractedGrammar(
    String title,
    String explanation,
    String structure,
    List<String> examples
) {}
