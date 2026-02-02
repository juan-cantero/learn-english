package com.learntv.api.generation.adapter.out.local;

import com.learntv.api.generation.application.port.out.AudioStoragePort;
import com.learntv.api.generation.domain.exception.AudioStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Local file system implementation of AudioStoragePort.
 * Stores audio files locally and serves them via the backend API.
 * Used for development when R2 is not configured.
 */
@Component
@Profile("!production")
public class LocalStorageAdapter implements AudioStoragePort {

    private static final Logger log = LoggerFactory.getLogger(LocalStorageAdapter.class);

    @Value("${app.audio.local-storage-path:./audio-storage}")
    private String storagePath;

    @Value("${app.audio.base-url:http://localhost:8080/api/v1/audio}")
    private String baseUrl;

    private Path storageDir;

    @PostConstruct
    public void init() {
        storageDir = Paths.get(storagePath).toAbsolutePath();
        try {
            Files.createDirectories(storageDir);
            log.info("Local audio storage initialized at: {}", storageDir);
        } catch (IOException e) {
            log.error("Failed to create local audio storage directory: {}", storageDir, e);
            throw new AudioStorageException("Failed to initialize local audio storage", e);
        }
    }

    @Override
    public String upload(String key, byte[] data, String contentType) {
        log.info("Storing audio file locally: key={}, size={} bytes", key, data.length);

        try {
            // Create subdirectories if needed (e.g., audio/vocab/term.mp3)
            Path filePath = storageDir.resolve(key);
            Files.createDirectories(filePath.getParent());

            // Write the file
            Files.write(filePath, data);

            String publicUrl = getPublicUrl(key);
            log.info("Successfully stored audio file locally: {}", publicUrl);
            return publicUrl;

        } catch (IOException e) {
            log.error("Failed to store audio file locally: key={}", key, e);
            throw new AudioStorageException("Failed to store audio file locally: " + key, e);
        }
    }

    @Override
    public void delete(String key) {
        log.info("Deleting audio file locally: key={}", key);

        try {
            Path filePath = storageDir.resolve(key);
            Files.deleteIfExists(filePath);
            log.info("Successfully deleted audio file locally: key={}", key);
        } catch (IOException e) {
            log.error("Failed to delete audio file locally: key={}", key, e);
            throw new AudioStorageException("Failed to delete audio file locally: " + key, e);
        }
    }

    @Override
    public String getPublicUrl(String key) {
        // URL format: http://localhost:8080/api/v1/audio/{key}
        String url = baseUrl;
        if (!url.endsWith("/")) {
            url += "/";
        }
        return url + key;
    }

    /**
     * Get the file path for a given key. Used by the controller to serve files.
     */
    public Path getFilePath(String key) {
        return storageDir.resolve(key);
    }

    /**
     * Get the storage directory path. Used by the controller for health checks.
     */
    public Path getStoragePath() {
        return storageDir;
    }
}
