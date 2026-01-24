-- Cached scripts for episode subtitles/transcripts
-- Stores both raw SRT and parsed text to avoid re-downloading and re-parsing

CREATE TABLE cached_scripts (
    id UUID PRIMARY KEY,
    imdb_id VARCHAR(20) NOT NULL,
    season_number INT NOT NULL,
    episode_number INT NOT NULL,
    language VARCHAR(10) NOT NULL,
    raw_content TEXT NOT NULL,
    parsed_text TEXT NOT NULL,
    downloaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,

    -- Unique constraint to prevent duplicate caches
    CONSTRAINT uk_cached_scripts_episode UNIQUE (imdb_id, season_number, episode_number, language)
);

-- Index for quick lookups
CREATE INDEX idx_cached_scripts_lookup ON cached_scripts (imdb_id, season_number, episode_number, language);

-- Index for cache expiration cleanup
CREATE INDEX idx_cached_scripts_expires ON cached_scripts (expires_at);

COMMENT ON TABLE cached_scripts IS 'Cache for downloaded subtitles/scripts to reduce API calls';
COMMENT ON COLUMN cached_scripts.raw_content IS 'Original SRT content from OpenSubtitles';
COMMENT ON COLUMN cached_scripts.parsed_text IS 'Clean dialogue text after SRT parsing';
COMMENT ON COLUMN cached_scripts.expires_at IS 'Cache expiration (default 30 days from download)';
