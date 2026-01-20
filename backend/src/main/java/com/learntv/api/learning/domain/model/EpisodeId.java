package com.learntv.api.learning.domain.model;

import java.util.Objects;
import java.util.UUID;

public record EpisodeId(UUID value) {

    public EpisodeId {
        Objects.requireNonNull(value, "EpisodeId value cannot be null");
    }

    public static EpisodeId generate() {
        return new EpisodeId(UUID.randomUUID());
    }

    public static EpisodeId of(UUID value) {
        return new EpisodeId(value);
    }

    public static EpisodeId of(String value) {
        return new EpisodeId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
