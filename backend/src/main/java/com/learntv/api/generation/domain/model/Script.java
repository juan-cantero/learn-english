package com.learntv.api.generation.domain.model;

public record Script(
    String imdbId,
    int seasonNumber,
    int episodeNumber,
    String text,
    ScriptSource source
) {}
