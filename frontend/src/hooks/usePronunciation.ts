import { useMutation } from '@tanstack/react-query';
import { transcribePronunciation, type TranscriptionResult } from '../api/pronunciation';

interface TranscribeParams {
  expectedText: string;
  audioBlob: Blob;
}

export function useTranscribePronunciation() {
  return useMutation<TranscriptionResult, Error, TranscribeParams>({
    mutationFn: ({ expectedText, audioBlob }) =>
      transcribePronunciation(expectedText, audioBlob),
  });
}
