package com.learntv.api.generation.adapter.out.supabase;

import com.learntv.api.generation.domain.exception.AudioStorageException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SupabaseStorageAdapterTest {

    private MockWebServer mockWebServer;
    private SupabaseStorageConfig config;
    private SupabaseStorageAdapter adapter;

    @BeforeEach
    void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = mockWebServer.url("").toString();
        // Remove trailing slash
        baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;

        WebClient webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer test-key")
                .build();

        config = mock(SupabaseStorageConfig.class);
        when(config.getBucketName()).thenReturn("test-bucket");
        when(config.getSupabaseUrl()).thenReturn("https://test.supabase.co");

        adapter = new SupabaseStorageAdapter(webClient, config);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test
    void upload_shouldPostFileAndReturnPublicUrl() throws Exception {
        // Given
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));

        String key = "episode-1/vocab-123.mp3";
        byte[] data = "test audio data".getBytes();
        String contentType = "audio/mpeg";

        // When
        String publicUrl = adapter.upload(key, data, contentType);

        // Then
        assertEquals("https://test.supabase.co/storage/v1/object/public/test-bucket/episode-1/vocab-123.mp3", publicUrl);

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/object/test-bucket/episode-1/vocab-123.mp3", request.getPath());
        assertEquals("true", request.getHeader("x-upsert"));
        assertEquals("audio/mpeg", request.getHeader("Content-Type"));
        assertEquals("test audio data", request.getBody().readUtf8());
    }

    @Test
    void upload_shouldThrowAudioStorageException_whenServerReturnsError() {
        // Given
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(403)
                .setBody("{\"error\":\"Forbidden\"}"));

        String key = "test/audio.mp3";
        byte[] data = "test audio data".getBytes();
        String contentType = "audio/mpeg";

        // When & Then
        AudioStorageException exception = assertThrows(AudioStorageException.class, () ->
                adapter.upload(key, data, contentType)
        );

        assertTrue(exception.getMessage().contains("Failed to upload audio file to Supabase Storage"));
    }

    @Test
    void delete_shouldSendDeleteRequest() throws Exception {
        // Given
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));

        String key = "episode-1/vocab-123.mp3";

        // When
        adapter.delete(key);

        // Then
        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("DELETE", request.getMethod());
        assertEquals("/object/test-bucket/episode-1/vocab-123.mp3", request.getPath());
    }

    @Test
    void delete_shouldThrowAudioStorageException_whenServerReturnsError() {
        // Given
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody("{\"error\":\"Not found\"}"));

        String key = "test/audio.mp3";

        // When & Then
        AudioStorageException exception = assertThrows(AudioStorageException.class, () ->
                adapter.delete(key)
        );

        assertTrue(exception.getMessage().contains("Failed to delete audio file from Supabase Storage"));
    }

    @Test
    void getPublicUrl_shouldReturnCorrectUrl() {
        String key = "episode-1/vocab-123.mp3";

        String publicUrl = adapter.getPublicUrl(key);

        assertEquals("https://test.supabase.co/storage/v1/object/public/test-bucket/episode-1/vocab-123.mp3", publicUrl);
    }
}
