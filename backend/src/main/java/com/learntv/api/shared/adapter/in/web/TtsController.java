package com.learntv.api.shared.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/tts")
@Tag(name = "TTS", description = "Text-to-Speech using Piper")
public class TtsController {

    private static final String PIPER_MODEL = System.getProperty("user.home") +
            "/.local/share/piper/en_US-lessac-medium.onnx";

    @GetMapping("/speak")
    @Operation(summary = "Convert text to speech", description = "Returns WAV audio of the spoken text")
    public ResponseEntity<byte[]> speak(@RequestParam String text) {
        try {
            // Sanitize input - only allow alphanumeric, spaces, and basic punctuation
            String sanitizedText = text.replaceAll("[^a-zA-Z0-9\\s.,!?'-]", "").trim();
            if (sanitizedText.isEmpty() || sanitizedText.length() > 200) {
                return ResponseEntity.badRequest().build();
            }

            // Create temp file for output
            Path tempFile = Files.createTempFile("tts_", ".wav");

            try {
                // Run Piper
                ProcessBuilder pb = new ProcessBuilder(
                    "bash", "-c",
                    String.format("echo '%s' | piper-tts --model '%s' --output_file '%s'",
                        sanitizedText.replace("'", "'\\''"),
                        PIPER_MODEL,
                        tempFile.toString())
                );
                pb.redirectErrorStream(true);

                Process process = pb.start();
                boolean finished = process.waitFor(10, TimeUnit.SECONDS);

                if (!finished) {
                    process.destroyForcibly();
                    return ResponseEntity.internalServerError().build();
                }

                if (process.exitValue() != 0) {
                    // Read error output for debugging
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(process.getInputStream()))) {
                        String error = reader.lines().reduce("", (a, b) -> a + "\n" + b);
                        System.err.println("Piper error: " + error);
                    }
                    return ResponseEntity.internalServerError().build();
                }

                // Read the generated audio file
                byte[] audioData = Files.readAllBytes(tempFile);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType("audio/wav"));
                headers.setContentLength(audioData.length);
                headers.setCacheControl("public, max-age=86400"); // Cache for 1 day

                return ResponseEntity.ok()
                        .headers(headers)
                        .body(audioData);

            } finally {
                // Clean up temp file
                Files.deleteIfExists(tempFile);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
