# Piper TTS Adapter

This adapter provides audio generation capabilities using [Piper TTS](https://github.com/rhasspy/piper), a fast, local text-to-speech system.

## Components

### PiperTtsAdapter
Main adapter implementing `AudioGenerationPort`. Uses ProcessBuilder to invoke:
- `piper-tts` CLI for WAV generation
- `ffmpeg` for WAV to MP3 conversion

### PiperTtsConfig
Spring configuration for Piper TTS settings:
- `model-path`: Path to the ONNX model file
- `timeout-seconds`: Maximum execution time for TTS/conversion processes
- `ffmpeg-quality`: MP3 quality setting (0-9, lower = better quality)

### AudioGenerationException
Domain exception for audio generation failures.

## Prerequisites

### 1. Install Piper TTS

```bash
# Download and install Piper
curl -sSL https://github.com/rhasspy/piper/releases/download/v1.2.0/piper_amd64.tar.gz | tar -xz -C ~/.local/bin/
chmod +x ~/.local/bin/piper

# Create symlink for easier access
ln -s ~/.local/bin/piper ~/.local/bin/piper-tts

# Download the English model
mkdir -p ~/.local/share/piper
cd ~/.local/share/piper
wget https://huggingface.co/rhasspy/piper-voices/resolve/main/en/en_US/lessac/medium/en_US-lessac-medium.onnx
wget https://huggingface.co/rhasspy/piper-voices/resolve/main/en/en_US/lessac/medium/en_US-lessac-medium.onnx.json
```

### 2. Install ffmpeg

```bash
# Ubuntu/Debian
sudo apt-get install ffmpeg

# macOS
brew install ffmpeg

# Arch Linux
sudo pacman -S ffmpeg
```

## Configuration

In `application.yml`:

```yaml
external-apis:
  piper:
    model-path: ${PIPER_MODEL_PATH:${user.home}/.local/share/piper/en_US-lessac-medium.onnx}
    timeout-seconds: 10
    ffmpeg-quality: 6
```

Environment variables:
- `PIPER_MODEL_PATH`: Override the default model path

## Usage

The adapter is automatically wired via Spring dependency injection:

```java
@Service
public class VocabularyAudioService {
    private final AudioGenerationPort audioGeneration;

    public VocabularyAudioService(AudioGenerationPort audioGeneration) {
        this.audioGeneration = audioGeneration;
    }

    public byte[] generatePronunciation(String word) {
        byte[] wavData = audioGeneration.generateWav(word);
        return audioGeneration.convertToMp3(wavData);
    }
}
```

## Error Handling

The adapter validates installations on first use and throws `AudioGenerationException` for:
- Piper TTS not installed or not in PATH
- Model file not found
- ffmpeg not installed
- Process timeouts
- Conversion failures

## Performance

Expected file sizes:
- WAV: ~160 KB per word (16kHz, 16-bit)
- MP3 (quality 6): ~8 KB per word

Processing time: ~0.5-2 seconds per word (depending on system)

## Limitations

- Currently only supports English (en_US-lessac-medium model)
- Requires local installation of Piper TTS and ffmpeg
- Not suitable for cloud deployments without custom Docker images
- Synchronous processing (blocks thread during generation)
