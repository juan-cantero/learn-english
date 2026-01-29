# R2 Storage Adapter - Quick Start Guide

## 1. Configure Environment Variables

Add to your `.env` file:

```env
CLOUDFLARE_ACCOUNT_ID=your-cloudflare-account-id
R2_ACCESS_KEY_ID=your-r2-access-key-id
R2_SECRET_ACCESS_KEY=your-r2-secret-access-key
R2_BUCKET_NAME=learntv-audio
R2_PUBLIC_URL=https://audio.learntv.example.com
```

## 2. Use in Your Service

```java
@Service
public class YourService {
    private final AudioStoragePort audioStorage;

    public YourService(AudioStoragePort audioStorage) {
        this.audioStorage = audioStorage;
    }

    public void uploadAudio() {
        byte[] audioData = loadAudioFile();
        String key = "episode/vocab-123.mp3";
        String url = audioStorage.upload(key, audioData, "audio/mpeg");
        System.out.println("Audio available at: " + url);
    }
}
```

## 3. Key Methods

| Method | Description | Example |
|--------|-------------|---------|
| `upload(key, data, contentType)` | Upload file, returns public URL | `upload("ep1/vocab1.mp3", bytes, "audio/mpeg")` |
| `delete(key)` | Remove file | `delete("ep1/vocab1.mp3")` |
| `getPublicUrl(key)` | Get URL without uploading | `getPublicUrl("ep1/vocab1.mp3")` |

## 4. Key Naming Convention

```
{show-slug}/{season-episode}/{type}-{id}.mp3
```

Examples:
- `the-pitt/s01e01/vocabulary-42.mp3`
- `the-pitt/s01e01/grammar-15.mp3`
- `the-pitt/s01e01/expression-8.mp3`

## 5. Error Handling

All methods throw `AudioStorageException` on failure:

```java
try {
    String url = audioStorage.upload(key, data, contentType);
} catch (AudioStorageException e) {
    log.error("Upload failed", e);
    // Handle error
}
```

## 6. Testing

Run tests:
```bash
./gradlew test --tests R2StorageAdapterTest
```

## 7. Get R2 Credentials

1. Login to Cloudflare Dashboard
2. Navigate to R2 Object Storage
3. Create bucket: `learntv-audio`
4. Generate API token with read/write permissions
5. Copy Account ID, Access Key ID, and Secret Access Key

## 8. Setup Public Access

Option A - Custom Domain (Recommended):
1. In R2 bucket settings, connect custom domain
2. Set `R2_PUBLIC_URL=https://audio.yoursite.com`

Option B - R2.dev Subdomain:
1. Enable public access via r2.dev
2. Set `R2_PUBLIC_URL=https://pub-xxxxx.r2.dev`

## Need More Info?

- Full documentation: [README.md](./README.md)
- Integration examples: [INTEGRATION_EXAMPLE.md](./INTEGRATION_EXAMPLE.md)
- Test file: [R2StorageAdapterTest.java](../../../../../../../test/java/com/learntv/api/generation/adapter/out/cloudflare/R2StorageAdapterTest.java)
