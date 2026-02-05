-- V12__migrate_user_progress.sql
-- New user_episode_progress table with proper FK to users table

-- Create new table with proper FK to users
CREATE TABLE user_episode_progress (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    episode_id UUID NOT NULL REFERENCES episodes(id) ON DELETE CASCADE,
    vocabulary_score INTEGER DEFAULT 0,
    grammar_score INTEGER DEFAULT 0,
    expressions_score INTEGER DEFAULT 0,
    exercises_score INTEGER DEFAULT 0,
    total_points INTEGER DEFAULT 0,
    completion_percentage INTEGER DEFAULT 0,
    completed BOOLEAN DEFAULT FALSE,
    last_accessed TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_episode_progress UNIQUE (user_id, episode_id)
);

CREATE INDEX idx_user_episode_progress_user ON user_episode_progress(user_id);
CREATE INDEX idx_user_episode_progress_episode ON user_episode_progress(episode_id);

-- Note: Old user_progress table is kept for now but will not be used
-- It contains string-based user_ids (like "anonymous") which don't match our new UUID-based users
