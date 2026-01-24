package com.learntv.api.generation.adapter.out.opensubtitles;

import com.learntv.api.generation.adapter.out.opensubtitles.dto.DownloadResponse;
import com.learntv.api.generation.adapter.out.opensubtitles.dto.SubtitleSearchResponse;
import com.learntv.api.generation.application.port.out.SubtitleFetchPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adapter for fetching subtitles from OpenSubtitles API.
 * Implements the SubtitleFetchPort for the generation module.
 */
@Component
public class OpenSubtitlesAdapter implements SubtitleFetchPort {

    private static final Logger log = LoggerFactory.getLogger(OpenSubtitlesAdapter.class);

    private final OpenSubtitlesClient client;

    public OpenSubtitlesAdapter(OpenSubtitlesClient client) {
        this.client = client;
    }

    @Override
    public Optional<String> fetchSubtitle(String imdbId, int season, int episode, String language) {
        log.info("Fetching subtitle for IMDB: {}, S{}E{}, language: {}", imdbId, season, episode, language);

        // Step 1: Search for subtitles
        Optional<SubtitleSearchResponse> searchResult = client.searchSubtitles(imdbId, season, episode, language);

        if (searchResult.isEmpty() || searchResult.get().data() == null || searchResult.get().data().isEmpty()) {
            log.warn("No subtitles found for IMDB: {}, S{}E{}", imdbId, season, episode);
            return Optional.empty();
        }

        // Step 2: Get the best subtitle (first result, sorted by download count)
        SubtitleSearchResponse.SubtitleData bestSubtitle = selectBestSubtitle(searchResult.get());
        if (bestSubtitle == null || bestSubtitle.attributes() == null ||
                bestSubtitle.attributes().files() == null || bestSubtitle.attributes().files().isEmpty()) {
            log.warn("No valid subtitle files found in search results");
            return Optional.empty();
        }

        int fileId = bestSubtitle.attributes().files().get(0).fileId();
        log.debug("Selected subtitle file ID: {}, release: {}",
                fileId, bestSubtitle.attributes().release());

        // Step 3: Request download link
        Optional<DownloadResponse> downloadResponse = client.requestDownload(fileId);
        if (downloadResponse.isEmpty() || downloadResponse.get().link() == null) {
            log.error("Failed to get download link for file ID: {}", fileId);
            return Optional.empty();
        }

        // Step 4: Download the actual content
        String downloadUrl = downloadResponse.get().link();
        Optional<String> content = client.downloadSubtitleContent(downloadUrl);

        if (content.isPresent()) {
            log.info("Successfully fetched subtitle for IMDB: {}, S{}E{}, size: {} chars",
                    imdbId, season, episode, content.get().length());
        }

        return content;
    }

    /**
     * Select the best subtitle from search results.
     * Prefers non-AI-translated, non-machine-translated, trusted sources.
     */
    private SubtitleSearchResponse.SubtitleData selectBestSubtitle(SubtitleSearchResponse response) {
        return response.data().stream()
                .filter(sub -> sub.attributes() != null)
                // Prefer non-AI and non-machine translated
                .filter(sub -> !sub.attributes().aiTranslated())
                .filter(sub -> !sub.attributes().machineTranslated())
                // Prefer hearing impaired = false (cleaner subtitles)
                .filter(sub -> !sub.attributes().hearingImpaired())
                .findFirst()
                // Fallback to first result if no perfect match
                .orElse(response.data().isEmpty() ? null : response.data().get(0));
    }
}
