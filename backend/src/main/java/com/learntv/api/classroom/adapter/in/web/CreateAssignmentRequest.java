package com.learntv.api.classroom.adapter.in.web;

import java.time.Instant;
import java.util.UUID;

public record CreateAssignmentRequest(
        UUID episodeId,
        String title,
        String instructions,
        Instant dueDate
) {}
