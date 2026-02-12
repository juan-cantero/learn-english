package com.learntv.api.generation.adapter.in.web;

import com.learntv.api.generation.adapter.out.local.LocalStorageAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Controller for serving locally stored audio files in development.
 * Only active when the "dev" profile is enabled.
 */
@RestController
@RequestMapping("/api/v1/audio")
@Profile({"dev", "test"})
public class LocalAudioController {

    private static final Logger log = LoggerFactory.getLogger(LocalAudioController.class);

    private final LocalStorageAdapter localStorageAdapter;

    public LocalAudioController(LocalStorageAdapter localStorageAdapter) {
        this.localStorageAdapter = localStorageAdapter;
    }

    /**
     * Serve audio files from local storage.
     * Supports paths like: /api/v1/audio/vocab/hello.mp3
     */
    @GetMapping("/**")
    public ResponseEntity<Resource> serveAudio(@RequestAttribute(name = "org.springframework.web.servlet.HandlerMapping.pathWithinHandlerMapping", required = false) String path,
                                                jakarta.servlet.http.HttpServletRequest request) {
        // Extract the file key from the request URI
        String requestUri = request.getRequestURI();
        String basePath = "/api/v1/audio/";

        if (!requestUri.startsWith(basePath)) {
            return ResponseEntity.notFound().build();
        }

        String key = requestUri.substring(basePath.length());

        if (key.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        log.debug("Serving local audio file: key={}", key);

        try {
            Path filePath = localStorageAdapter.getFilePath(key);

            if (!Files.exists(filePath)) {
                log.warn("Audio file not found: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(filePath);

            // Determine content type based on extension
            String contentType = "audio/mpeg"; // Default for mp3
            if (key.endsWith(".wav")) {
                contentType = "audio/wav";
            } else if (key.endsWith(".ogg")) {
                contentType = "audio/ogg";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CACHE_CONTROL, "max-age=86400") // Cache for 24 hours
                    .body(resource);

        } catch (Exception e) {
            log.error("Error serving audio file: key={}", key, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Health check endpoint for local audio storage.
     */
    @GetMapping
    public ResponseEntity<String> healthCheck() {
        Path storagePath = localStorageAdapter.getStoragePath();
        if (Files.exists(storagePath) && Files.isDirectory(storagePath)) {
            return ResponseEntity.ok("Local audio storage is ready at: " + storagePath.toAbsolutePath());
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Local audio storage not available");
    }
}
