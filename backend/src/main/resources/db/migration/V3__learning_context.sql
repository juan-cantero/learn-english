-- V3__learning_context.sql
-- Learning bounded context: Episodes, Vocabulary, Grammar, Expressions, Exercises

-- Episodes table
CREATE TABLE episodes (
    id UUID PRIMARY KEY,
    show_id UUID NOT NULL,
    show_slug VARCHAR(255) NOT NULL,
    season_number INTEGER NOT NULL,
    episode_number INTEGER NOT NULL,
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(100) NOT NULL,
    synopsis VARCHAR(2000),
    duration_minutes INTEGER DEFAULT 0,
    CONSTRAINT fk_episodes_show FOREIGN KEY (show_id) REFERENCES shows(id) ON DELETE CASCADE
);

-- Index for show lookups
CREATE INDEX idx_episodes_show_id ON episodes(show_id);
CREATE INDEX idx_episodes_show_slug ON episodes(show_slug);

-- Unique constraint for episode within a show
CREATE UNIQUE INDEX idx_episodes_show_slug_episode ON episodes(show_slug, slug);

-- Vocabulary table
CREATE TABLE vocabulary (
    id UUID PRIMARY KEY,
    episode_id UUID NOT NULL,
    term VARCHAR(255) NOT NULL,
    definition VARCHAR(1000) NOT NULL,
    phonetic VARCHAR(100),
    category VARCHAR(50) NOT NULL,
    example_sentence VARCHAR(1000),
    context_timestamp VARCHAR(20),
    CONSTRAINT fk_vocabulary_episode FOREIGN KEY (episode_id) REFERENCES episodes(id) ON DELETE CASCADE
);

-- Index for episode lookups
CREATE INDEX idx_vocabulary_episode_id ON vocabulary(episode_id);

-- Grammar points table
CREATE TABLE grammar_points (
    id UUID PRIMARY KEY,
    episode_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    explanation VARCHAR(2000) NOT NULL,
    structure VARCHAR(500),
    example VARCHAR(1000),
    context_quote VARCHAR(1000),
    CONSTRAINT fk_grammar_points_episode FOREIGN KEY (episode_id) REFERENCES episodes(id) ON DELETE CASCADE
);

-- Index for episode lookups
CREATE INDEX idx_grammar_points_episode_id ON grammar_points(episode_id);

-- Expressions table
CREATE TABLE expressions (
    id UUID PRIMARY KEY,
    episode_id UUID NOT NULL,
    phrase VARCHAR(255) NOT NULL,
    meaning VARCHAR(1000) NOT NULL,
    context_quote VARCHAR(1000),
    usage_note VARCHAR(1000),
    CONSTRAINT fk_expressions_episode FOREIGN KEY (episode_id) REFERENCES episodes(id) ON DELETE CASCADE
);

-- Index for episode lookups
CREATE INDEX idx_expressions_episode_id ON expressions(episode_id);

-- Exercises table
CREATE TABLE exercises (
    id UUID PRIMARY KEY,
    episode_id UUID NOT NULL,
    type VARCHAR(50) NOT NULL,
    question VARCHAR(1000) NOT NULL,
    correct_answer VARCHAR(500),
    options VARCHAR(2000),
    matching_pairs VARCHAR(2000),
    points INTEGER DEFAULT 10,
    CONSTRAINT fk_exercises_episode FOREIGN KEY (episode_id) REFERENCES episodes(id) ON DELETE CASCADE
);

-- Index for episode lookups
CREATE INDEX idx_exercises_episode_id ON exercises(episode_id);
