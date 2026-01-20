package com.learntv.api.catalog.domain.model;

import java.util.Objects;
import java.util.UUID;

public record ShowId(UUID value) {

    public ShowId {
        Objects.requireNonNull(value, "ShowId value cannot be null");
    }

    public static ShowId generate() {
        return new ShowId(UUID.randomUUID());
    }

    public static ShowId of(UUID value) {
        return new ShowId(value);
    }

    public static ShowId of(String value) {
        return new ShowId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
