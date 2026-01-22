package com.learntv.api.generation.domain.model;

import java.util.List;

public record GeneratedExercise(
    String type,
    String question,
    String correctAnswer,
    List<String> options,
    int points
) {}
