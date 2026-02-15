package com.learntv.api.generation.application.port.in;

import java.util.UUID;

public record GenerationCommand(
    String tmdbId,
    int seasonNumber,
    int episodeNumber,
    String genre,
    UUID userId
) {}
