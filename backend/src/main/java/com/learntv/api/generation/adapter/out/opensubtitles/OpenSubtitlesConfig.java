package com.learntv.api.generation.adapter.out.opensubtitles;

import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
public class OpenSubtitlesConfig {

    @Value("${external-apis.opensubtitles.api-key}")
    private String apiKey;

    @Value("${external-apis.opensubtitles.base-url}")
    private String baseUrl;

    @Value("${external-apis.opensubtitles.user-agent}")
    private String userAgent;

    @Bean
    public WebClient openSubtitlesWebClient() {
        // Configure HttpClient to follow redirects
        HttpClient httpClient = HttpClient.create()
                .followRedirect(true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);

        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader("Accept", "application/json")
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Api-Key", apiKey)
                .defaultHeader("User-Agent", userAgent)
                .build();
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getUserAgent() {
        return userAgent;
    }
}
