-- Add audio_url column to expressions table for TTS audio storage
ALTER TABLE expressions ADD COLUMN audio_url VARCHAR(500);

COMMENT ON COLUMN expressions.audio_url IS 'URL to the pre-generated TTS audio file for this expression phrase';
