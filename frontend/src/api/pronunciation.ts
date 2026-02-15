import { apiPostMultipart } from './client';

export interface TranscriptionResult {
  transcription: string;
  expectedText: string;
  similarity: number;
  passed: boolean;
  suggestions: string[];
}

export async function transcribePronunciation(
  expectedText: string,
  audioBlob: Blob,
): Promise<TranscriptionResult> {
  const formData = new FormData();
  formData.append('audio', audioBlob, 'recording.webm');
  formData.append('expectedText', expectedText);

  return apiPostMultipart<TranscriptionResult>(
    '/pronunciation/transcribe',
    formData,
  );
}
