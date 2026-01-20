-- V2__catalog_context.sql
-- Catalog bounded context: Shows

CREATE TABLE shows (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(2000),
    genre VARCHAR(50) NOT NULL,
    accent VARCHAR(50) NOT NULL,
    difficulty VARCHAR(50) NOT NULL,
    image_url VARCHAR(500),
    total_seasons INTEGER DEFAULT 0,
    total_episodes INTEGER DEFAULT 0
);

-- Index for slug lookups (common query pattern)
CREATE INDEX idx_shows_slug ON shows(slug);

-- Index for filtering by genre
CREATE INDEX idx_shows_genre ON shows(genre);

-- Index for filtering by difficulty
CREATE INDEX idx_shows_difficulty ON shows(difficulty);
