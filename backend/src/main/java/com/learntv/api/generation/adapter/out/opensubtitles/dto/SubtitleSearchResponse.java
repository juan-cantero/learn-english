package com.learntv.api.generation.adapter.out.opensubtitles.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SubtitleSearchResponse(
        @JsonProperty("total_pages") int totalPages,
        @JsonProperty("total_count") int totalCount,
        int page,
        List<SubtitleData> data
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SubtitleData(
            String id,
            String type,
            SubtitleAttributes attributes
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SubtitleAttributes(
            String language,
            @JsonProperty("download_count") int downloadCount,
            @JsonProperty("new_download_count") int newDownloadCount,
            @JsonProperty("hearing_impaired") boolean hearingImpaired,
            boolean hd,
            double fps,
            int votes,
            double ratings,
            @JsonProperty("from_trusted") boolean fromTrusted,
            @JsonProperty("foreign_parts_only") boolean foreignPartsOnly,
            @JsonProperty("upload_date") String uploadDate,
            @JsonProperty("ai_translated") boolean aiTranslated,
            @JsonProperty("machine_translated") boolean machineTranslated,
            String release,
            List<FileInfo> files
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FileInfo(
            @JsonProperty("file_id") int fileId,
            @JsonProperty("cd_number") int cdNumber,
            @JsonProperty("file_name") String fileName
    ) {}
}
