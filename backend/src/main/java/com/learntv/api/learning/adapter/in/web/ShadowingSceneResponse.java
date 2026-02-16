package com.learntv.api.learning.adapter.in.web;

import com.learntv.api.generation.domain.model.DialogueLine;
import com.learntv.api.learning.application.port.ShadowingSceneRepository.ShadowingScene;

import java.util.List;
import java.util.UUID;

public record ShadowingSceneResponse(
        UUID id,
        String title,
        List<DialogueLineResponse> lines,
        List<String> characters
) {

    public static ShadowingSceneResponse fromDomain(ShadowingScene scene) {
        List<DialogueLineResponse> lineResponses = scene.lines().stream()
                .map(line -> new DialogueLineResponse(line.character(), line.text(), line.startTime()))
                .toList();
        return new ShadowingSceneResponse(scene.id(), scene.title(), lineResponses, scene.characters());
    }

    public record DialogueLineResponse(
            String character,
            String text,
            String startTime
    ) {}
}
