package com.learntv.api.generation.adapter.out.piper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PiperTtsConfig {

    @Value("${external-apis.piper.model-path:${user.home}/.local/share/piper/en_US-lessac-medium.onnx}")
    private String modelPath;

    @Value("${external-apis.piper.timeout-seconds:10}")
    private int timeoutSeconds;

    @Value("${external-apis.piper.ffmpeg-quality:6}")
    private int ffmpegQuality;

    public String getModelPath() {
        return modelPath;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public int getFfmpegQuality() {
        return ffmpegQuality;
    }
}
