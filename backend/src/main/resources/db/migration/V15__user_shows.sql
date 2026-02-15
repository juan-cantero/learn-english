-- V15__user_shows.sql
-- User shows tracking (which shows a user has added to their list)

CREATE TABLE user_shows (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    show_id UUID NOT NULL REFERENCES shows(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_show UNIQUE(user_id, show_id)
);

CREATE INDEX idx_user_shows_user_id ON user_shows(user_id);
