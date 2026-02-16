package com.learntv.api.generation.application.port.out;

import com.learntv.api.generation.domain.model.ExtractedScene;

import java.util.List;

public interface ShadowingExtractionPort {

    List<ExtractedScene> extractShadowingScenes(
            String rawSrt,
            List<String> vocabularyTerms,
            List<String> expressions
    );
}
