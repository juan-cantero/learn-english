package com.learntv.api.generation.adapter.out.openai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class OpenAiConfig {

    @Value("${external-apis.openai.api-key}")
    private String apiKey;

    @Value("${external-apis.openai.base-url}")
    private String baseUrl;

    @Value("${external-apis.openai.model}")
    private String model;

    @Value("${external-apis.openai.max-tokens}")
    private int maxTokens;

    @Bean
    public WebClient openAiWebClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getModel() {
        return model;
    }

    public int getMaxTokens() {
        return maxTokens;
    }
}
