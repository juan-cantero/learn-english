CREATE TABLE shadowing_scenes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    episode_id UUID NOT NULL REFERENCES episodes(id) ON DELETE CASCADE,
    scene_index INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    lines JSONB NOT NULL,
    characters TEXT[] NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_shadowing_scene UNIQUE(episode_id, scene_index)
);

CREATE INDEX idx_shadowing_scenes_episode ON shadowing_scenes(episode_id);
