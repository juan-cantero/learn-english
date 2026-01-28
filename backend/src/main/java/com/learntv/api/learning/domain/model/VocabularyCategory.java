package com.learntv.api.learning.domain.model;

public enum VocabularyCategory {
    MEDICAL,
    TECHNICAL,
    SLANG,
    IDIOM,
    PROFESSIONAL,
    EVERYDAY,
    EMOTIONAL,
    COLLOQUIAL,
    ACTION;

    public static VocabularyCategory fromString(String category) {
        if (category == null) return EVERYDAY;
        return switch (category.toLowerCase()) {
            case "medical" -> MEDICAL;
            case "technical" -> TECHNICAL;
            case "slang" -> SLANG;
            case "idiom" -> IDIOM;
            case "professional" -> PROFESSIONAL;
            case "emotional" -> EMOTIONAL;
            case "colloquial" -> COLLOQUIAL;
            case "action" -> ACTION;
            default -> EVERYDAY;
        };
    }
}
