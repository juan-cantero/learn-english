-- Add audio_url column to exercises table for listening exercise audio
ALTER TABLE exercises ADD COLUMN audio_url VARCHAR(500);

COMMENT ON COLUMN exercises.audio_url IS 'URL to the pre-generated TTS audio file for listening exercises';
