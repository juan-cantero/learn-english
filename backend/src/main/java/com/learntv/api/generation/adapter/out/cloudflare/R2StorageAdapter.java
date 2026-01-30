package com.learntv.api.generation.adapter.out.cloudflare;

import com.learntv.api.generation.application.port.out.AudioStoragePort;
import com.learntv.api.generation.domain.exception.AudioStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * Adapter for storing audio files in Cloudflare R2 storage.
 * R2 is S3-compatible, so we use the AWS S3 SDK.
 */
@Component
@Profile("production")
public class R2StorageAdapter implements AudioStoragePort {

    private static final Logger log = LoggerFactory.getLogger(R2StorageAdapter.class);

    private final S3Client s3Client;
    private final R2StorageConfig config;

    public R2StorageAdapter(S3Client s3Client, R2StorageConfig config) {
        this.s3Client = s3Client;
        this.config = config;
    }

    @Override
    public String upload(String key, byte[] data, String contentType) {
        log.info("Uploading audio file to R2: key={}, size={} bytes, contentType={}",
                key, data.length, contentType);

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(config.getBucketName())
                    .key(key)
                    .contentType(contentType)
                    .contentLength((long) data.length)
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromBytes(data));

            String publicUrl = getPublicUrl(key);
            log.info("Successfully uploaded audio file to R2: {}", publicUrl);
            return publicUrl;

        } catch (S3Exception e) {
            log.error("Failed to upload audio file to R2: key={}, error={}", key, e.awsErrorDetails().errorMessage(), e);
            throw new AudioStorageException("Failed to upload audio file to R2: " + key, e);
        } catch (Exception e) {
            log.error("Unexpected error uploading audio file to R2: key={}", key, e);
            throw new AudioStorageException("Unexpected error uploading audio file: " + key, e);
        }
    }

    @Override
    public void delete(String key) {
        log.info("Deleting audio file from R2: key={}", key);

        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(config.getBucketName())
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteRequest);
            log.info("Successfully deleted audio file from R2: key={}", key);

        } catch (S3Exception e) {
            log.error("Failed to delete audio file from R2: key={}, error={}", key, e.awsErrorDetails().errorMessage(), e);
            throw new AudioStorageException("Failed to delete audio file from R2: " + key, e);
        } catch (Exception e) {
            log.error("Unexpected error deleting audio file from R2: key={}", key, e);
            throw new AudioStorageException("Unexpected error deleting audio file: " + key, e);
        }
    }

    @Override
    public String getPublicUrl(String key) {
        // Return the public URL based on the configured public URL base
        // Format: https://audio.learntv.example.com/<key>
        String url = config.getPublicUrl();
        if (!url.endsWith("/")) {
            url += "/";
        }
        return url + key;
    }
}
