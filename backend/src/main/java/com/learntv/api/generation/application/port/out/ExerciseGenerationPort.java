package com.learntv.api.generation.application.port.out;

import com.learntv.api.generation.domain.model.ExtractedExpression;
import com.learntv.api.generation.domain.model.ExtractedGrammar;
import com.learntv.api.generation.domain.model.ExtractedVocabulary;
import com.learntv.api.generation.domain.model.GeneratedExercise;

import java.util.List;

public interface ExerciseGenerationPort {
    List<GeneratedExercise> generateExercises(
        List<ExtractedVocabulary> vocabulary,
        List<ExtractedGrammar> grammar,
        List<ExtractedExpression> expressions
    );
}
