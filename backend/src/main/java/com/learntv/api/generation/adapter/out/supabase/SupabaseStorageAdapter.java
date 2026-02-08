package com.learntv.api.generation.adapter.out.supabase;

import com.learntv.api.generation.application.port.out.AudioStoragePort;
import com.learntv.api.generation.domain.exception.AudioStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * Adapter for storing audio files in Supabase Storage.
 * Uses the Supabase Storage REST API via WebClient.
 */
@Component
@Profile({"production", "local-supabase"})
public class SupabaseStorageAdapter implements AudioStoragePort {

    private static final Logger log = LoggerFactory.getLogger(SupabaseStorageAdapter.class);

    private final WebClient webClient;
    private final SupabaseStorageConfig config;

    public SupabaseStorageAdapter(WebClient supabaseStorageWebClient, SupabaseStorageConfig config) {
        this.webClient = supabaseStorageWebClient;
        this.config = config;
    }

    @Override
    public String upload(String key, byte[] data, String contentType) {
        log.info("Uploading audio file to Supabase Storage: key={}, size={} bytes, contentType={}",
                key, data.length, contentType);

        try {
            webClient.post()
                    .uri("/object/" + config.getBucketName() + "/" + key)
                    .header("x-upsert", "true")
                    .contentType(MediaType.parseMediaType(contentType))
                    .bodyValue(data)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            String publicUrl = getPublicUrl(key);
            log.info("Successfully uploaded audio file to Supabase Storage: {}", publicUrl);
            return publicUrl;

        } catch (WebClientResponseException e) {
            log.error("Failed to upload audio file to Supabase Storage: key={}, status={}, body={}",
                    key, e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new AudioStorageException("Failed to upload audio file to Supabase Storage: " + key, e);
        } catch (Exception e) {
            log.error("Unexpected error uploading audio file to Supabase Storage: key={}", key, e);
            throw new AudioStorageException("Unexpected error uploading audio file: " + key, e);
        }
    }

    @Override
    public void delete(String key) {
        log.info("Deleting audio file from Supabase Storage: key={}", key);

        try {
            webClient.delete()
                    .uri("/object/" + config.getBucketName() + "/" + key)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            log.info("Successfully deleted audio file from Supabase Storage: key={}", key);

        } catch (WebClientResponseException e) {
            log.error("Failed to delete audio file from Supabase Storage: key={}, status={}, body={}",
                    key, e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new AudioStorageException("Failed to delete audio file from Supabase Storage: " + key, e);
        } catch (Exception e) {
            log.error("Unexpected error deleting audio file from Supabase Storage: key={}", key, e);
            throw new AudioStorageException("Unexpected error deleting audio file: " + key, e);
        }
    }

    @Override
    public String getPublicUrl(String key) {
        return config.getSupabaseUrl() + "/storage/v1/object/public/" + config.getBucketName() + "/" + key;
    }
}
