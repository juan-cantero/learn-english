-- V4__progress_context.sql
-- Progress bounded context: User Progress

CREATE TABLE user_progress (
    id UUID PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    episode_id UUID NOT NULL,
    vocabulary_score INTEGER DEFAULT 0,
    grammar_score INTEGER DEFAULT 0,
    expressions_score INTEGER DEFAULT 0,
    exercises_score INTEGER DEFAULT 0,
    total_points INTEGER DEFAULT 0,
    completed BOOLEAN DEFAULT FALSE,
    last_accessed TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_progress_episode FOREIGN KEY (episode_id) REFERENCES episodes(id) ON DELETE CASCADE,
    CONSTRAINT uk_user_progress_user_episode UNIQUE (user_id, episode_id)
);

-- Index for user lookups (get all progress for a user)
CREATE INDEX idx_user_progress_user_id ON user_progress(user_id);

-- Index for episode lookups
CREATE INDEX idx_user_progress_episode_id ON user_progress(episode_id);
