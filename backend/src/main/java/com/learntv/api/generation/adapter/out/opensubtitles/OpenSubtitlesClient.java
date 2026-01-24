package com.learntv.api.generation.adapter.out.opensubtitles;

import com.learntv.api.generation.adapter.out.opensubtitles.dto.DownloadRequest;
import com.learntv.api.generation.adapter.out.opensubtitles.dto.DownloadResponse;
import com.learntv.api.generation.adapter.out.opensubtitles.dto.SubtitleSearchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Optional;

@Component
public class OpenSubtitlesClient {

    private static final Logger log = LoggerFactory.getLogger(OpenSubtitlesClient.class);

    private final WebClient openSubtitlesWebClient;

    public OpenSubtitlesClient(WebClient openSubtitlesWebClient) {
        this.openSubtitlesWebClient = openSubtitlesWebClient;
    }

    /**
     * Search for subtitles by IMDB ID, season, and episode.
     *
     * @param imdbId  the IMDB ID (with or without 'tt' prefix)
     * @param season  the season number
     * @param episode the episode number
     * @param language the language code (e.g., "en" for English)
     * @return search response with subtitle data, or empty if error
     */
    public Optional<SubtitleSearchResponse> searchSubtitles(String imdbId, int season, int episode, String language) {
        log.debug("Searching subtitles for IMDB: {}, S{}E{}, language: {}", imdbId, season, episode, language);

        try {
            SubtitleSearchResponse response = openSubtitlesWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/subtitles")
                            .queryParam("imdb_id", normalizeImdbId(imdbId))
                            .queryParam("season_number", season)
                            .queryParam("episode_number", episode)
                            .queryParam("languages", language)
                            .queryParam("order_by", "download_count")
                            .queryParam("order_direction", "desc")
                            .build())
                    .retrieve()
                    .bodyToMono(SubtitleSearchResponse.class)
                    .block();

            log.debug("Found {} subtitles", response != null ? response.totalCount() : 0);
            return Optional.ofNullable(response);

        } catch (WebClientResponseException e) {
            log.error("OpenSubtitles API error during search: {} - {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error searching subtitles: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Request a download link for a subtitle file.
     * Note: Downloads are limited per day based on your account type.
     *
     * @param fileId the file ID from the search results
     * @return download response with temporary link, or empty if error
     */
    public Optional<DownloadResponse> requestDownload(int fileId) {
        log.debug("Requesting download for file ID: {}", fileId);

        try {
            DownloadResponse response = openSubtitlesWebClient.post()
                    .uri("/download")
                    .bodyValue(new DownloadRequest(fileId))
                    .retrieve()
                    .bodyToMono(DownloadResponse.class)
                    .block();

            if (response != null) {
                log.debug("Download link obtained. Remaining downloads: {}", response.remaining());
            }
            return Optional.ofNullable(response);

        } catch (WebClientResponseException e) {
            log.error("OpenSubtitles API error during download request: {} - {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error requesting download: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Download the actual subtitle content from the temporary link.
     *
     * @param downloadUrl the temporary download URL
     * @return the subtitle content as string, or empty if error
     */
    public Optional<String> downloadSubtitleContent(String downloadUrl) {
        log.debug("Downloading subtitle content from: {}", downloadUrl);

        try {
            // Use a separate WebClient for the download URL (different host)
            String content = WebClient.create()
                    .get()
                    .uri(downloadUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.debug("Downloaded subtitle content, length: {} chars",
                    content != null ? content.length() : 0);
            return Optional.ofNullable(content);

        } catch (Exception e) {
            log.error("Error downloading subtitle content: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Normalize IMDB ID to ensure it has the 'tt' prefix.
     */
    private String normalizeImdbId(String imdbId) {
        if (imdbId == null) {
            return null;
        }
        return imdbId.startsWith("tt") ? imdbId : "tt" + imdbId;
    }
}
