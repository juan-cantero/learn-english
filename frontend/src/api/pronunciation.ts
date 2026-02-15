import { apiPostMultipart } from './client';

export interface SuggestionDetail {
  tip: string;
}

export interface TranscriptionResult {
  transcription: string;
  expectedText: string;
  similarity: number;
  passed: boolean;
  expectedIpa: string | null;
  suggestions: SuggestionDetail[];
}

interface RawTranscriptionResponse {
  transcription: string;
  expectedText: string;
  similarity: number;
  passed: boolean;
  expectedIpa?: string | null;
  suggestions: (string | SuggestionDetail)[];
}

function normalizeResponse(raw: RawTranscriptionResponse): TranscriptionResult {
  return {
    transcription: raw.transcription,
    expectedText: raw.expectedText,
    similarity: raw.similarity,
    passed: raw.passed,
    expectedIpa: raw.expectedIpa ?? null,
    suggestions: raw.suggestions.map((s) =>
      typeof s === 'string' ? { tip: s } : s,
    ),
  };
}

export async function transcribePronunciation(
  expectedText: string,
  audioBlob: Blob,
): Promise<TranscriptionResult> {
  const formData = new FormData();
  formData.append('audio', audioBlob, 'recording.webm');
  formData.append('expectedText', expectedText);

  const raw = await apiPostMultipart<RawTranscriptionResponse>(
    '/pronunciation/transcribe',
    formData,
  );

  return normalizeResponse(raw);
}
