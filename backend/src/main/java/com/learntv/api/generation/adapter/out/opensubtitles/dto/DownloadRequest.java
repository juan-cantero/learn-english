package com.learntv.api.generation.adapter.out.opensubtitles.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DownloadRequest(
        @JsonProperty("file_id") int fileId
) {}
