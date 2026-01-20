package com.learntv.api.progress.adapter.in.web;

public record SaveProgressRequest(
        String category,
        int points,
        Boolean completed
) {}
