package com.learntv.api.generation.application.port.in;

public record GenerationCommand(
    String tmdbId,
    int seasonNumber,
    int episodeNumber,
    String genre
) {}
