-- V8__create_generation_jobs_table.sql
-- Generation jobs table for async lesson generation tracking

CREATE TABLE generation_jobs (
    id UUID PRIMARY KEY,
    imdb_id VARCHAR(20) NOT NULL,
    season_number INTEGER NOT NULL,
    episode_number INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL,
    current_step VARCHAR(100),
    progress INTEGER DEFAULT 0,
    error_message TEXT,
    result_episode_id UUID,
    created_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    CONSTRAINT fk_generation_jobs_episode FOREIGN KEY (result_episode_id) REFERENCES episodes(id) ON DELETE SET NULL
);

-- Index for job lookups
CREATE INDEX idx_generation_jobs_status ON generation_jobs(status);
CREATE INDEX idx_generation_jobs_created_at ON generation_jobs(created_at);
