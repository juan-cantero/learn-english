package com.learntv.api.generation.application.port.out;

import com.learntv.api.generation.domain.model.ExtractedExpression;
import com.learntv.api.generation.domain.model.ExtractedGrammar;
import com.learntv.api.generation.domain.model.ExtractedVocabulary;
import com.learntv.api.generation.domain.model.GeneratedExercise;

import java.util.List;

/**
 * Port for generating exercises from extracted content.
 */
public interface ExerciseGenerationPort {

    /**
     * Generate exercises based on extracted content.
     *
     * @param vocabulary  extracted vocabulary items
     * @param grammar     extracted grammar points
     * @param expressions extracted expressions
     * @return list of 12-15 generated exercises
     */
    List<GeneratedExercise> generateExercises(
            List<ExtractedVocabulary> vocabulary,
            List<ExtractedGrammar> grammar,
            List<ExtractedExpression> expressions
    );
}
