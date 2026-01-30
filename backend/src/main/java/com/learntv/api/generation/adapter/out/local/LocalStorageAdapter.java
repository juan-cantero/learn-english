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
 * Local file system adapter for audio storage.
 * Used in development to avoid requiring Cloudflare R2.
 *
 * Files are stored in a local directory and served via a simple URL pattern.
 */
@Component
@Profile("dev")
public class LocalStorageAdapter implements AudioStoragePort {

    private static final Logger log = LoggerFactory.getLogger(LocalStorageAdapter.class);

    private final Path storagePath;
    private final String baseUrl;

    public LocalStorageAdapter(
            @Value("${audio.local.storage-path:${java.io.tmpdir}/learntv-audio}") String storagePath,
            @Value("${audio.local.base-url:http://localhost:8080/api/v1/audio}") String baseUrl) {
        this.storagePath = Paths.get(storagePath);
        this.baseUrl = baseUrl;
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(storagePath);
            log.info("LocalStorageAdapter initialized. Storage path: {}", storagePath.toAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to create local storage directory: {}", storagePath, e);
            throw new AudioStorageException("Failed to initialize local storage", e);
        }
    }

    @Override
    public String upload(String key, byte[] data, String contentType) {
        log.info("Uploading audio file locally: key={}, size={} bytes, contentType={}",
                key, data.length, contentType);

        try {
            Path filePath = storagePath.resolve(key);

            // Create parent directories if needed
            Files.createDirectories(filePath.getParent());

            // Write file
            Files.write(filePath, data);

            String publicUrl = getPublicUrl(key);
            log.info("Successfully saved audio file locally: {}", publicUrl);
            log.debug("Physical path: {}", filePath.toAbsolutePath());

            return publicUrl;

        } catch (IOException e) {
            log.error("Failed to save audio file locally: key={}", key, e);
            throw new AudioStorageException("Failed to save audio file locally: " + key, e);
        }
    }

    @Override
    public void delete(String key) {
        log.info("Deleting audio file locally: key={}", key);

        try {
            Path filePath = storagePath.resolve(key);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("Successfully deleted audio file locally: key={}", key);
            } else {
                log.warn("Audio file not found for deletion: key={}", key);
            }

        } catch (IOException e) {
            log.error("Failed to delete audio file locally: key={}", key, e);
            throw new AudioStorageException("Failed to delete audio file locally: " + key, e);
        }
    }

    @Override
    public String getPublicUrl(String key) {
        String url = baseUrl;
        if (!url.endsWith("/")) {
            url += "/";
        }
        return url + key;
    }

    /**
     * Get the physical file path for a key.
     * Useful for serving files via a controller.
     */
    public Path getFilePath(String key) {
        return storagePath.resolve(key);
    }

    /**
     * Get the storage root path.
     */
    public Path getStoragePath() {
        return storagePath;
    }
}
