package com.learntv.api.generation.adapter.out.piper;

import com.learntv.api.generation.application.port.out.AudioGenerationPort;
import com.learntv.api.generation.domain.exception.AudioGenerationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 * Piper TTS implementation of AudioGenerationPort.
 * Uses local Piper TTS CLI and ffmpeg to generate and convert audio files.
 */
@Component
public class PiperTtsAdapter implements AudioGenerationPort {

    private static final Logger log = LoggerFactory.getLogger(PiperTtsAdapter.class);

    private final PiperTtsConfig config;

    public PiperTtsAdapter(PiperTtsConfig config) {
        this.config = config;
    }

    @Override
    public byte[] generateWav(String text) {
        log.debug("Generating WAV audio for text: {}", truncateForLog(text));

        validatePiperInstallation();

        try {
            // Create temporary file for WAV output
            Path wavTempFile = Files.createTempFile("piper-tts-", ".wav");

            try {
                // Build Piper TTS command
                ProcessBuilder pb = new ProcessBuilder(
                    "bash", "-c",
                    String.format("echo '%s' | piper-tts --model %s --output_file %s",
                        escapeSingleQuotes(text),
                        config.getModelPath(),
                        wavTempFile.toAbsolutePath())
                );

                pb.redirectErrorStream(true);

                log.debug("Executing Piper TTS command");
                Process process = pb.start();

                // Capture output for debugging
                String output = captureProcessOutput(process.getInputStream());

                // Wait for process to complete with timeout
                boolean completed = process.waitFor(config.getTimeoutSeconds(), TimeUnit.SECONDS);

                if (!completed) {
                    process.destroyForcibly();
                    throw new AudioGenerationException(
                        "Piper TTS process timed out after " + config.getTimeoutSeconds() + " seconds"
                    );
                }

                int exitCode = process.exitValue();
                if (exitCode != 0) {
                    log.error("Piper TTS failed with exit code: {}. Output: {}", exitCode, output);
                    throw new AudioGenerationException(
                        "Piper TTS failed with exit code: " + exitCode
                    );
                }

                // Read the generated WAV file
                byte[] wavData = Files.readAllBytes(wavTempFile);
                log.info("Successfully generated WAV audio: {} bytes", wavData.length);

                return wavData;

            } finally {
                // Clean up temporary file
                Files.deleteIfExists(wavTempFile);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AudioGenerationException("Piper TTS process was interrupted", e);
        } catch (IOException e) {
            throw new AudioGenerationException("Failed to generate WAV audio", e);
        }
    }

    @Override
    public byte[] convertToMp3(byte[] wavData) {
        log.debug("Converting WAV to MP3: {} bytes", wavData.length);

        validateFfmpegInstallation();

        Path wavTempFile = null;
        Path mp3TempFile = null;

        try {
            // Create temporary files
            wavTempFile = Files.createTempFile("convert-", ".wav");
            mp3TempFile = Files.createTempFile("convert-", ".mp3");

            // Write WAV data to temp file
            Files.write(wavTempFile, wavData);

            // Build ffmpeg command
            ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-i", wavTempFile.toAbsolutePath().toString(),
                "-codec:a", "libmp3lame",
                "-qscale:a", String.valueOf(config.getFfmpegQuality()),
                "-y", // Overwrite output file
                mp3TempFile.toAbsolutePath().toString()
            );

            pb.redirectErrorStream(true);

            log.debug("Executing ffmpeg conversion");
            Process process = pb.start();

            // Capture output for debugging
            String output = captureProcessOutput(process.getInputStream());

            // Wait for process to complete with timeout
            boolean completed = process.waitFor(config.getTimeoutSeconds(), TimeUnit.SECONDS);

            if (!completed) {
                process.destroyForcibly();
                throw new AudioGenerationException(
                    "ffmpeg process timed out after " + config.getTimeoutSeconds() + " seconds"
                );
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                log.error("ffmpeg failed with exit code: {}. Output: {}", exitCode, output);
                throw new AudioGenerationException(
                    "ffmpeg conversion failed with exit code: " + exitCode
                );
            }

            // Read the generated MP3 file
            byte[] mp3Data = Files.readAllBytes(mp3TempFile);
            log.info("Successfully converted to MP3: {} bytes (from {} bytes WAV)",
                mp3Data.length, wavData.length);

            return mp3Data;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AudioGenerationException("ffmpeg process was interrupted", e);
        } catch (IOException e) {
            throw new AudioGenerationException("Failed to convert WAV to MP3", e);
        } finally {
            // Clean up temporary files
            cleanupTempFile(wavTempFile);
            cleanupTempFile(mp3TempFile);
        }
    }

    /**
     * Validates that Piper TTS is installed and accessible.
     */
    private void validatePiperInstallation() {
        try {
            ProcessBuilder pb = new ProcessBuilder("which", "piper-tts");
            Process process = pb.start();
            boolean completed = process.waitFor(2, TimeUnit.SECONDS);

            if (!completed || process.exitValue() != 0) {
                throw new AudioGenerationException(
                    "Piper TTS is not installed or not in PATH. Please install piper-tts."
                );
            }

            // Check if model file exists
            File modelFile = new File(config.getModelPath());
            if (!modelFile.exists()) {
                throw new AudioGenerationException(
                    "Piper TTS model not found at: " + config.getModelPath()
                );
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AudioGenerationException("Validation interrupted", e);
        } catch (IOException e) {
            throw new AudioGenerationException("Failed to validate Piper TTS installation", e);
        }
    }

    /**
     * Validates that ffmpeg is installed and accessible.
     */
    private void validateFfmpegInstallation() {
        try {
            ProcessBuilder pb = new ProcessBuilder("which", "ffmpeg");
            Process process = pb.start();
            boolean completed = process.waitFor(2, TimeUnit.SECONDS);

            if (!completed || process.exitValue() != 0) {
                throw new AudioGenerationException(
                    "ffmpeg is not installed or not in PATH. Please install ffmpeg."
                );
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AudioGenerationException("Validation interrupted", e);
        } catch (IOException e) {
            throw new AudioGenerationException("Failed to validate ffmpeg installation", e);
        }
    }

    /**
     * Captures output from a process input stream.
     */
    private String captureProcessOutput(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        return output.toString();
    }

    /**
     * Escapes single quotes in text for bash command.
     */
    private String escapeSingleQuotes(String text) {
        return text.replace("'", "'\\''");
    }

    /**
     * Truncates text for logging to avoid cluttering logs.
     */
    private String truncateForLog(String text) {
        if (text.length() <= 50) {
            return text;
        }
        return text.substring(0, 47) + "...";
    }

    /**
     * Safely deletes a temporary file, logging any errors.
     */
    private void cleanupTempFile(Path tempFile) {
        if (tempFile != null) {
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException e) {
                log.warn("Failed to delete temporary file: {}", tempFile, e);
            }
        }
    }
}
