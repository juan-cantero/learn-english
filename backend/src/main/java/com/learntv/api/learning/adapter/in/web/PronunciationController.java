package com.learntv.api.learning.adapter.in.web;

import com.learntv.api.learning.application.service.PronunciationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * REST controller for pronunciation practice using Whisper AI.
 */
@RestController
@RequestMapping("/api/v1/pronunciation")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Pronunciation", description = "Pronunciation practice with Whisper AI")
public class PronunciationController {

    private final PronunciationService pronunciationService;

    @PostMapping("/transcribe")
    @Operation(
            summary = "Transcribe and evaluate pronunciation",
            description = "Accepts audio file, transcribes it using Whisper API, and compares to expected text"
    )
    public ResponseEntity<TranscriptionResponse> transcribe(
            @RequestPart("audio") MultipartFile audio,
            @RequestParam String expectedText) throws IOException {

        log.info("Received pronunciation request - file: {}, size: {} bytes, expected: '{}'",
                audio.getOriginalFilename(), audio.getSize(), expectedText);

        byte[] audioBytes = audio.getBytes();

        PronunciationService.PronunciationResult result =
                pronunciationService.evaluate(audioBytes, audio.getOriginalFilename(), expectedText);

        return ResponseEntity.ok(TranscriptionResponse.fromDomain(result));
    }

    /**
     * Response DTO for transcription endpoint.
     */
    public record TranscriptionResponse(
            String transcription,
            String expectedText,
            double similarity,
            boolean passed,
            List<String> suggestions
    ) {
        public static TranscriptionResponse fromDomain(PronunciationService.PronunciationResult result) {
            return new TranscriptionResponse(
                    result.transcription(),
                    result.expectedText(),
                    result.similarity(),
                    result.passed(),
                    result.suggestions()
            );
        }
    }
}
