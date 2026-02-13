package com.learntv.api.generation.domain.model;

/**
 * Defines the progress steps for lesson generation.
 * Each step has a defined progress percentage and description.
 */
public enum GenerationProgressStep {
    FETCHING_SCRIPT(10, "Fetching script"),
    PARSING_SCRIPT(20, "Parsing script"),
    EXTRACTING_VOCABULARY(40, "Extracting vocabulary..."),
    EXTRACTING_GRAMMAR(55, "Extracting grammar..."),
    EXTRACTING_EXPRESSIONS(70, "Extracting expressions..."),
    GENERATING_EXERCISES(85, "Generating exercises..."),
    SAVING(95, "Saving..."),
    COMPLETED(100, "Completed");

    private final int progress;
    private final String description;

    GenerationProgressStep(int progress, String description) {
        this.progress = progress;
        this.description = description;
    }

    public int getProgress() {
        return progress;
    }

    public String getDescription() {
        return description;
    }
}
