-- Rename cached_scripts to episode_scripts and remove TTL
-- Scripts are now permanently stored, not cached

-- Rename the table
ALTER TABLE cached_scripts RENAME TO episode_scripts;

-- Drop the expires_at column (no more TTL)
ALTER TABLE episode_scripts DROP COLUMN expires_at;

-- Drop the expiration index (no longer needed)
DROP INDEX IF EXISTS idx_cached_scripts_expires;

-- Rename the lookup index
DROP INDEX IF EXISTS idx_cached_scripts_lookup;
CREATE INDEX idx_episode_scripts_lookup ON episode_scripts (imdb_id, season_number, episode_number, language);

-- Update constraint name
ALTER TABLE episode_scripts DROP CONSTRAINT IF EXISTS uk_cached_scripts_episode;
ALTER TABLE episode_scripts ADD CONSTRAINT uk_episode_scripts_episode
    UNIQUE (imdb_id, season_number, episode_number, language);

COMMENT ON TABLE episode_scripts IS 'Permanently stored scripts for episodes - source of truth for content generation';
