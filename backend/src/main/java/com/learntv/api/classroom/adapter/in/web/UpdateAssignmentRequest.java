package com.learntv.api.classroom.adapter.in.web;

import java.time.Instant;

public record UpdateAssignmentRequest(
        String title,
        String instructions,
        Instant dueDate
) {}
