package com.learntv.api.generation.application.service;

import com.learntv.api.generation.application.port.out.AudioGenerationPort;
import com.learntv.api.generation.application.port.out.AudioStoragePort;
import com.learntv.api.generation.domain.exception.AudioGenerationException;
import com.learntv.api.generation.domain.exception.AudioStorageException;
import com.learntv.api.generation.domain.model.ExtractedVocabulary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AudioGenerationServiceTest {

    private AudioGenerationPort audioGeneration;
    private AudioStoragePort audioStorage;
    private AudioGenerationService service;

    @BeforeEach
    void setUp() {
        audioGeneration = mock(AudioGenerationPort.class);
        audioStorage = mock(AudioStoragePort.class);
        service = new AudioGenerationService(audioGeneration, audioStorage);
    }

    @Test
    void generateAudioForVocabulary_shouldGenerateAudioForAllItems() {
        // Given
        List<ExtractedVocabulary> vocabulary = List.of(
                createVocabulary("triage", null),
                createVocabulary("critical", null),
                createVocabulary("stabilize", null)
        );

        byte[] mockWav = "wav-data".getBytes();
        byte[] mockMp3 = "mp3-data".getBytes();

        when(audioGeneration.generateWav(any())).thenReturn(mockWav);
        when(audioGeneration.convertToMp3(any())).thenReturn(mockMp3);
        when(audioStorage.upload(any(), any(), eq("audio/mpeg")))
                .thenAnswer(invocation -> {
                    String key = invocation.getArgument(0);
                    return "https://audio.test.com/" + key;
                });

        // When
        List<ExtractedVocabulary> result = service.generateAudioForVocabulary(vocabulary);

        // Then
        assertEquals(3, result.size());

        // Since parallel execution doesn't guarantee order, verify all terms are present with audio URLs
        List<String> terms = result.stream().map(ExtractedVocabulary::term).toList();
        assertTrue(terms.contains("triage"));
        assertTrue(terms.contains("critical"));
        assertTrue(terms.contains("stabilize"));

        // Verify all have audio URLs
        result.forEach(vocab -> {
            assertNotNull(vocab.audioUrl());
            assertTrue(vocab.audioUrl().contains(vocab.term()));
        });

        verify(audioGeneration, times(3)).generateWav(any());
        verify(audioGeneration, times(3)).convertToMp3(any());
        verify(audioStorage, times(3)).upload(any(), any(), eq("audio/mpeg"));
    }

    @Test
    void generateAudioForVocabulary_shouldHandleEmptyList() {
        // Given
        List<ExtractedVocabulary> vocabulary = Collections.emptyList();

        // When
        List<ExtractedVocabulary> result = service.generateAudioForVocabulary(vocabulary);

        // Then
        assertTrue(result.isEmpty());
        verifyNoInteractions(audioGeneration);
        verifyNoInteractions(audioStorage);
    }

    @Test
    void generateAudioForVocabulary_shouldHandleNullList() {
        // When
        List<ExtractedVocabulary> result = service.generateAudioForVocabulary(null);

        // Then
        assertTrue(result.isEmpty());
        verifyNoInteractions(audioGeneration);
        verifyNoInteractions(audioStorage);
    }

    @Test
    void generateAudioForVocabulary_shouldSetAudioUrlToNullWhenGenerationFails() {
        // Given
        List<ExtractedVocabulary> vocabulary = List.of(
                createVocabulary("triage", null),
                createVocabulary("critical", null)
        );

        byte[] mockWav = "wav-data".getBytes();
        byte[] mockMp3 = "mp3-data".getBytes();

        // First item succeeds
        when(audioGeneration.generateWav("triage")).thenReturn(mockWav);
        when(audioGeneration.convertToMp3(mockWav)).thenReturn(mockMp3);
        when(audioStorage.upload(eq("audio/vocab/triage.mp3"), any(), eq("audio/mpeg")))
                .thenReturn("https://audio.test.com/audio/vocab/triage.mp3");

        // Second item fails during WAV generation
        when(audioGeneration.generateWav("critical"))
                .thenThrow(new AudioGenerationException("TTS failed"));

        // When
        List<ExtractedVocabulary> result = service.generateAudioForVocabulary(vocabulary);

        // Then
        assertEquals(2, result.size());

        assertEquals("triage", result.get(0).term());
        assertNotNull(result.get(0).audioUrl());

        assertEquals("critical", result.get(1).term());
        assertNull(result.get(1).audioUrl());
    }

    @Test
    void generateAudioForVocabulary_shouldSetAudioUrlToNullWhenConversionFails() {
        // Given
        List<ExtractedVocabulary> vocabulary = List.of(
                createVocabulary("triage", null)
        );

        byte[] mockWav = "wav-data".getBytes();

        when(audioGeneration.generateWav("triage")).thenReturn(mockWav);
        when(audioGeneration.convertToMp3(mockWav))
                .thenThrow(new AudioGenerationException("MP3 conversion failed"));

        // When
        List<ExtractedVocabulary> result = service.generateAudioForVocabulary(vocabulary);

        // Then
        assertEquals(1, result.size());
        assertEquals("triage", result.get(0).term());
        assertNull(result.get(0).audioUrl());
    }

    @Test
    void generateAudioForVocabulary_shouldSetAudioUrlToNullWhenStorageFails() {
        // Given
        List<ExtractedVocabulary> vocabulary = List.of(
                createVocabulary("triage", null)
        );

        byte[] mockWav = "wav-data".getBytes();
        byte[] mockMp3 = "mp3-data".getBytes();

        when(audioGeneration.generateWav("triage")).thenReturn(mockWav);
        when(audioGeneration.convertToMp3(mockWav)).thenReturn(mockMp3);
        when(audioStorage.upload(any(), any(), eq("audio/mpeg")))
                .thenThrow(new AudioStorageException("R2 upload failed"));

        // When
        List<ExtractedVocabulary> result = service.generateAudioForVocabulary(vocabulary);

        // Then
        assertEquals(1, result.size());
        assertEquals("triage", result.get(0).term());
        assertNull(result.get(0).audioUrl());
    }

    @Test
    void generateAudioForVocabulary_shouldSlugifyTermsCorrectly() {
        // Given
        List<ExtractedVocabulary> vocabulary = List.of(
                createVocabulary("life-threatening", null),
                createVocabulary("ICU (Intensive Care Unit)", null),
                createVocabulary("burn out", null)
        );

        byte[] mockWav = "wav-data".getBytes();
        byte[] mockMp3 = "mp3-data".getBytes();

        when(audioGeneration.generateWav(any())).thenReturn(mockWav);
        when(audioGeneration.convertToMp3(any())).thenReturn(mockMp3);
        when(audioStorage.upload(any(), any(), eq("audio/mpeg")))
                .thenAnswer(invocation -> {
                    String key = invocation.getArgument(0);
                    return "https://audio.test.com/" + key;
                });

        // When
        List<ExtractedVocabulary> result = service.generateAudioForVocabulary(vocabulary);

        // Then
        verify(audioStorage).upload(eq("audio/vocab/life-threatening.mp3"), any(), eq("audio/mpeg"));
        verify(audioStorage).upload(eq("audio/vocab/icu-intensive-care-unit.mp3"), any(), eq("audio/mpeg"));
        verify(audioStorage).upload(eq("audio/vocab/burn-out.mp3"), any(), eq("audio/mpeg"));
    }

    @Test
    void generateAudioForVocabulary_shouldPreserveOtherVocabularyFields() {
        // Given
        ExtractedVocabulary original = new ExtractedVocabulary(
                "triage",
                "The process of sorting patients",
                "/ˈtriːɑːʒ/",
                "Medical",
                "The ER performs triage on all incoming patients.",
                null
        );

        List<ExtractedVocabulary> vocabulary = List.of(original);

        byte[] mockWav = "wav-data".getBytes();
        byte[] mockMp3 = "mp3-data".getBytes();

        when(audioGeneration.generateWav("triage")).thenReturn(mockWav);
        when(audioGeneration.convertToMp3(mockWav)).thenReturn(mockMp3);
        when(audioStorage.upload(any(), any(), eq("audio/mpeg")))
                .thenReturn("https://audio.test.com/audio/vocab/triage.mp3");

        // When
        List<ExtractedVocabulary> result = service.generateAudioForVocabulary(vocabulary);

        // Then
        assertEquals(1, result.size());
        ExtractedVocabulary vocab = result.get(0);

        assertEquals(original.term(), vocab.term());
        assertEquals(original.definition(), vocab.definition());
        assertEquals(original.phonetic(), vocab.phonetic());
        assertEquals(original.category(), vocab.category());
        assertEquals(original.exampleSentence(), vocab.exampleSentence());
        assertNotNull(vocab.audioUrl());
    }

    @Test
    void generateAudioForVocabulary_shouldHandleMultipleFailuresGracefully() {
        // Given
        List<ExtractedVocabulary> vocabulary = List.of(
                createVocabulary("success1", null),
                createVocabulary("fail1", null),
                createVocabulary("success2", null),
                createVocabulary("fail2", null),
                createVocabulary("success3", null)
        );

        byte[] mockWav = "wav-data".getBytes();
        byte[] mockMp3 = "mp3-data".getBytes();

        // Success cases
        when(audioGeneration.generateWav("success1")).thenReturn(mockWav);
        when(audioGeneration.generateWav("success2")).thenReturn(mockWav);
        when(audioGeneration.generateWav("success3")).thenReturn(mockWav);
        when(audioGeneration.convertToMp3(mockWav)).thenReturn(mockMp3);

        when(audioStorage.upload(contains("success1"), any(), eq("audio/mpeg")))
                .thenReturn("https://audio.test.com/audio/vocab/success1.mp3");
        when(audioStorage.upload(contains("success2"), any(), eq("audio/mpeg")))
                .thenReturn("https://audio.test.com/audio/vocab/success2.mp3");
        when(audioStorage.upload(contains("success3"), any(), eq("audio/mpeg")))
                .thenReturn("https://audio.test.com/audio/vocab/success3.mp3");

        // Failure cases
        when(audioGeneration.generateWav("fail1"))
                .thenThrow(new AudioGenerationException("Generation failed"));
        when(audioGeneration.generateWav("fail2"))
                .thenThrow(new AudioGenerationException("Generation failed"));

        // When
        List<ExtractedVocabulary> result = service.generateAudioForVocabulary(vocabulary);

        // Then
        assertEquals(5, result.size());

        assertNotNull(result.get(0).audioUrl());
        assertNull(result.get(1).audioUrl());
        assertNotNull(result.get(2).audioUrl());
        assertNull(result.get(3).audioUrl());
        assertNotNull(result.get(4).audioUrl());
    }

    private ExtractedVocabulary createVocabulary(String term, String audioUrl) {
        return new ExtractedVocabulary(
                term,
                "Definition of " + term,
                "/phonetic/",
                "Medical",
                "Example sentence with " + term,
                audioUrl
        );
    }
}
