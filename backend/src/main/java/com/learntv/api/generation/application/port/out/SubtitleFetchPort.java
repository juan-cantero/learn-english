package com.learntv.api.generation.application.port.out;

import java.util.Optional;

public interface SubtitleFetchPort {
    Optional<String> fetchSubtitle(String imdbId, int season, int episode, String language);
}
