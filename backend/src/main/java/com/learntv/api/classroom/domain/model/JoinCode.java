package com.learntv.api.classroom.domain.model;

import java.security.SecureRandom;
import java.util.Objects;

/**
 * Value object representing a classroom join code.
 * Format: 4-6 alphanumeric characters (no ambiguous chars like 0/O, 1/I/l)
 */
public record JoinCode(String value) {

    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    public JoinCode {
        Objects.requireNonNull(value, "JoinCode value cannot be null");
        if (value.length() < 4 || value.length() > 10) {
            throw new IllegalArgumentException("Join code must be 4-10 characters");
        }
    }

    public static JoinCode of(String value) {
        return new JoinCode(value.toUpperCase());
    }

    public static JoinCode generate() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return new JoinCode(code.toString());
    }

    @Override
    public String toString() {
        return value;
    }
}
