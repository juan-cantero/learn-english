package com.learntv.api.generation.domain.model;

import java.util.List;

public record ExtractedScene(
        String title,
        List<DialogueLine> lines,
        List<String> characters
) {}
