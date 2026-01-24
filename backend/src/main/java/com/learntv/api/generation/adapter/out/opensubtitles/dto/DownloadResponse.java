package com.learntv.api.generation.adapter.out.opensubtitles.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DownloadResponse(
        String link,
        @JsonProperty("file_name") String fileName,
        int requests,
        int remaining,
        String message,
        @JsonProperty("reset_time") String resetTime,
        @JsonProperty("reset_time_utc") String resetTimeUtc
) {}
