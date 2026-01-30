-- V7__add_vocabulary_audio_url.sql
-- Add audio_url column to vocabulary table for storing pronunciation audio URLs

ALTER TABLE vocabulary ADD COLUMN audio_url VARCHAR(500);
