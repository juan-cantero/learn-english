package com.learntv.api.learning.adapter.in.web;

import jakarta.validation.constraints.NotBlank;

public record CheckAnswerRequest(
        @NotBlank String answer
) {}
