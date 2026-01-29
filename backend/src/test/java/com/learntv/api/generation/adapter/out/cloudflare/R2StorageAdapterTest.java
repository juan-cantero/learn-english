package com.learntv.api.generation.adapter.out.cloudflare;

import com.learntv.api.generation.domain.exception.AudioStorageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class R2StorageAdapterTest {

    private S3Client s3Client;
    private R2StorageConfig config;
    private R2StorageAdapter adapter;

    @BeforeEach
    void setUp() {
        s3Client = mock(S3Client.class);
        config = mock(R2StorageConfig.class);

        when(config.getBucketName()).thenReturn("test-bucket");
        when(config.getPublicUrl()).thenReturn("https://audio.test.com");

        adapter = new R2StorageAdapter(s3Client, config);
    }

    @Test
    void upload_shouldUploadFileAndReturnPublicUrl() {
        // Given
        String key = "test/audio.mp3";
        byte[] data = "test audio data".getBytes();
        String contentType = "audio/mpeg";

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        // When
        String publicUrl = adapter.upload(key, data, contentType);

        // Then
        assertEquals("https://audio.test.com/test/audio.mp3", publicUrl);

        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        ArgumentCaptor<RequestBody> bodyCaptor = ArgumentCaptor.forClass(RequestBody.class);
        verify(s3Client).putObject(requestCaptor.capture(), bodyCaptor.capture());

        PutObjectRequest capturedRequest = requestCaptor.getValue();
        assertEquals("test-bucket", capturedRequest.bucket());
        assertEquals(key, capturedRequest.key());
        assertEquals(contentType, capturedRequest.contentType());
        assertEquals(data.length, capturedRequest.contentLength());
    }

    @Test
    void upload_shouldThrowAudioStorageException_whenS3ExceptionOccurs() {
        // Given
        String key = "test/audio.mp3";
        byte[] data = "test audio data".getBytes();
        String contentType = "audio/mpeg";

        AwsErrorDetails errorDetails = AwsErrorDetails.builder()
                .errorMessage("Access denied")
                .errorCode("AccessDenied")
                .build();

        S3Exception s3Exception = (S3Exception) S3Exception.builder()
                .awsErrorDetails(errorDetails)
                .build();

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(s3Exception);

        // When & Then
        AudioStorageException exception = assertThrows(AudioStorageException.class, () ->
                adapter.upload(key, data, contentType)
        );

        assertTrue(exception.getMessage().contains("Failed to upload audio file to R2"));
        assertNotNull(exception.getCause());
    }

    @Test
    void delete_shouldDeleteFileFromR2() {
        // Given
        String key = "test/audio.mp3";

        // When
        adapter.delete(key);

        // Then
        ArgumentCaptor<DeleteObjectRequest> requestCaptor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3Client).deleteObject(requestCaptor.capture());

        DeleteObjectRequest capturedRequest = requestCaptor.getValue();
        assertEquals("test-bucket", capturedRequest.bucket());
        assertEquals(key, capturedRequest.key());
    }

    @Test
    void delete_shouldThrowAudioStorageException_whenS3ExceptionOccurs() {
        // Given
        String key = "test/audio.mp3";

        AwsErrorDetails errorDetails = AwsErrorDetails.builder()
                .errorMessage("Object not found")
                .errorCode("NoSuchKey")
                .build();

        S3Exception s3Exception = (S3Exception) S3Exception.builder()
                .awsErrorDetails(errorDetails)
                .build();

        when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                .thenThrow(s3Exception);

        // When & Then
        AudioStorageException exception = assertThrows(AudioStorageException.class, () ->
                adapter.delete(key)
        );

        assertTrue(exception.getMessage().contains("Failed to delete audio file from R2"));
        assertNotNull(exception.getCause());
    }

    @Test
    void getPublicUrl_shouldReturnCorrectUrl() {
        // Given
        String key = "episode-1/vocab-123.mp3";

        // When
        String publicUrl = adapter.getPublicUrl(key);

        // Then
        assertEquals("https://audio.test.com/episode-1/vocab-123.mp3", publicUrl);
    }

    @Test
    void getPublicUrl_shouldHandlePublicUrlWithTrailingSlash() {
        // Given
        when(config.getPublicUrl()).thenReturn("https://audio.test.com/");
        adapter = new R2StorageAdapter(s3Client, config);
        String key = "test.mp3";

        // When
        String publicUrl = adapter.getPublicUrl(key);

        // Then
        assertEquals("https://audio.test.com/test.mp3", publicUrl);
    }
}
