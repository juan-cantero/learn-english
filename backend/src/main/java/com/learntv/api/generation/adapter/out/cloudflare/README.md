# Cloudflare R2 Storage Adapter

This adapter provides integration with Cloudflare R2 for storing audio files. R2 is Cloudflare's S3-compatible object storage service.

## Configuration

Add the following environment variables to your `.env` file or configure them in your deployment environment:

```env
CLOUDFLARE_ACCOUNT_ID=your-cloudflare-account-id
R2_ACCESS_KEY_ID=your-r2-access-key-id
R2_SECRET_ACCESS_KEY=your-r2-secret-access-key
R2_BUCKET_NAME=learntv-audio
R2_PUBLIC_URL=https://audio.learntv.example.com
```

### Getting R2 Credentials

1. Log in to your Cloudflare dashboard
2. Navigate to R2 Object Storage
3. Create a bucket (e.g., `learntv-audio`)
4. Generate API tokens:
   - Go to "Manage R2 API Tokens"
   - Create a new token with read and write permissions
   - Save the Access Key ID and Secret Access Key

### Setting up Public Access

To serve audio files publicly:

1. **Option 1: Custom Domain (Recommended)**
   - In R2 bucket settings, connect a custom domain
   - Use this domain as your `R2_PUBLIC_URL`
   - Example: `https://audio.learntv.example.com`

2. **Option 2: R2.dev Subdomain**
   - Enable public access via r2.dev subdomain
   - Use the provided subdomain as your `R2_PUBLIC_URL`
   - Example: `https://pub-xxxxx.r2.dev`

## Usage

The adapter is automatically available for dependency injection:

```java
@Service
public class AudioService {
    private final AudioStoragePort audioStorage;

    public AudioService(AudioStoragePort audioStorage) {
        this.audioStorage = audioStorage;
    }

    public void processAudio() {
        byte[] audioData = generateAudio();
        String key = "episode-1/vocab-123.mp3";
        String contentType = "audio/mpeg";

        // Upload and get public URL
        String publicUrl = audioStorage.upload(key, audioData, contentType);
        System.out.println("Audio available at: " + publicUrl);
    }
}
```

## Key Naming Conventions

Recommended key structure for organizing audio files:

```
show-slug/season-episode/vocabulary-{id}.mp3
show-slug/season-episode/grammar-{id}.mp3
show-slug/season-episode/expression-{id}.mp3
```

Example:
```
the-pitt/s01e01/vocabulary-42.mp3
the-pitt/s01e01/grammar-15.mp3
```

## API Methods

### `upload(String key, byte[] data, String contentType): String`
Uploads an audio file to R2 and returns the public URL.

- **Parameters:**
  - `key`: The object key (path) in the bucket
  - `data`: Audio file data as byte array
  - `contentType`: MIME type (e.g., "audio/mpeg")
- **Returns:** Public URL to access the file
- **Throws:** `AudioStorageException` if upload fails

### `delete(String key): void`
Deletes an audio file from R2.

- **Parameters:**
  - `key`: The object key to delete
- **Throws:** `AudioStorageException` if deletion fails

### `getPublicUrl(String key): String`
Constructs the public URL for a given key.

- **Parameters:**
  - `key`: The object key
- **Returns:** Public URL to access the file

## Error Handling

All methods throw `AudioStorageException` on failure, which includes:
- Network connectivity issues
- Authentication failures
- Insufficient permissions
- Bucket not found
- Invalid keys

## Testing

Run the unit tests:

```bash
./gradlew test --tests R2StorageAdapterTest
```

## Technical Details

- Uses AWS SDK for Java v2 (R2 is S3-compatible)
- Synchronous operations (blocking I/O)
- Automatic retry not configured (can be added via SDK interceptors)
- Content-Type is preserved for proper browser handling
- Region is set to `us-east-1` (required by SDK but not used by R2)

## Cost Optimization

R2 offers free egress and competitive storage pricing:
- Storage: ~$0.015 per GB/month
- Class A operations (write): $4.50 per million
- Class B operations (read): $0.36 per million
- **Zero egress fees** (unlike S3)

## Troubleshooting

### "Access Denied" errors
- Verify your R2 API token has write permissions
- Check that the bucket name matches your configuration
- Ensure the token hasn't expired

### "Unable to load credentials" errors
- Verify environment variables are set correctly
- Check that values don't contain extra whitespace
- Restart the application after updating `.env`

### Files upload but can't be accessed
- Verify your bucket has public access enabled
- Check that the `R2_PUBLIC_URL` matches your domain configuration
- Test the public URL directly in a browser
