# Plan: Creación de Ejercicios con LLM

**Fecha**: 2026-01-21
**Basado en**: ADR-001 Episode Script Fetching and Content Generation System

---

## Resumen

Este documento describe el flujo completo para generar lecciones de inglés automáticamente a partir de episodios de TV, incluyendo la generación de audio para pronunciación.

---

## Flujo Completo

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         FLUJO "GENERATE EPISODE"                            │
└─────────────────────────────────────────────────────────────────────────────┘

Usuario hace clic en "Generate Episode"
           │
           ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│  FASE 1: BÚSQUEDA Y SELECCIÓN                                                │
├──────────────────────────────────────────────────────────────────────────────┤
│  1.1 Usuario busca show         → TMDB API: /search/tv?query=The+Pitt       │
│  1.2 Usuario selecciona show    → TMDB API: /tv/{id}/season/{n}             │
│  1.3 Usuario selecciona episodio                                             │
└──────────────────────────────────────────────────────────────────────────────┘
           │
           ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│  FASE 2: OBTENER SCRIPT                                                      │
├──────────────────────────────────────────────────────────────────────────────┤
│  2.1 Buscar subtítulos          → OpenSubtitles: /subtitles?imdb_id=...     │
│  2.2 Descargar archivo SRT      → OpenSubtitles: /download                   │
│  2.3 Parsear SRT a texto plano  → Interno: SrtParser.parseToPlainText()     │
│  2.4 Cachear script en DB       → cached_scripts table (30 días)            │
└──────────────────────────────────────────────────────────────────────────────┘
           │
           ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│  FASE 3: EXTRACCIÓN CON IA                                                   │
├──────────────────────────────────────────────────────────────────────────────┤
│  3.1 Extraer vocabulario        → OpenAI: prompt vocabulario (15-25 items)  │
│  3.2 Extraer gramática          → OpenAI: prompt gramática (4-6 points)     │
│  3.3 Extraer expresiones        → OpenAI: prompt expresiones (6-10 items)   │
│                                                                              │
│  Output: JSON estructurado con vocabulary[], grammarPoints[], expressions[] │
└──────────────────────────────────────────────────────────────────────────────┘
           │
           ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│  FASE 4: GENERACIÓN DE EJERCICIOS                                            │
├──────────────────────────────────────────────────────────────────────────────┤
│  4.1 Generar ejercicios         → OpenAI: prompt ejercicios                 │
│      - Fill-in-blank (5-6)                                                   │
│      - Multiple choice (4-5)                                                 │
│      - Matching (2-3)                                                        │
│      - Listening (2-3) ← NUEVO: usa el audio generado                       │
│                                                                              │
│  Output: exercises[] con type, question, correctAnswer, options, etc.       │
└──────────────────────────────────────────────────────────────────────────────┘
           │
           ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│  FASE 5: GENERACIÓN DE AUDIO                                       ★ NUEVO ★│
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  Para cada vocabulary item:                                                  │
│                                                                              │
│  5.1 Generar WAV con Piper      → piper-tts --model en_US-lessac-medium    │
│  5.2 Convertir a MP3            → ffmpeg (reducir tamaño ~5x)               │
│  5.3 Subir a Cloudflare R2      → PUT /audio/vocabulary/{term}.mp3          │
│  5.4 Guardar URL en vocabulary  → vocabulary.audioUrl = "https://..."       │
│                                                                              │
│  Procesamiento: Batch paralelo (10 concurrent) para velocidad               │
│  Fallback: Si falla, dejar audioUrl = null (frontend usa Web Speech API)    │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘
           │
           ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│  FASE 6: PERSISTENCIA                                                        │
├──────────────────────────────────────────────────────────────────────────────┤
│  6.1 Crear/actualizar Episode   → episodes table                            │
│  6.2 Insertar vocabulario       → vocabulary table (con audioUrl)           │
│  6.3 Insertar gramática         → grammar_points table                       │
│  6.4 Insertar expresiones       → expressions table                          │
│  6.5 Insertar ejercicios        → exercises table                            │
│  6.6 Actualizar job status      → generation_jobs.status = COMPLETED        │
└──────────────────────────────────────────────────────────────────────────────┘
           │
           ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│  FASE 7: RESPUESTA                                                           │
├──────────────────────────────────────────────────────────────────────────────┤
│  7.1 Retornar episodeId         → Frontend navega a la lección              │
│  7.2 Mostrar resumen            → "25 vocab, 5 grammar, 8 expressions..."   │
└──────────────────────────────────────────────────────────────────────────────┘
```

---

## Detalle: Fase 5 - Generación de Audio

### Arquitectura

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│  Spring Boot    │     │  Piper TTS      │     │  Cloudflare R2  │
│  (AudioService) │────>│  (Local binary) │────>│  (Storage)      │
└─────────────────┘     └─────────────────┘     └─────────────────┘
        │                                               │
        │           vocabulary.audioUrl                 │
        └───────────────────────────────────────────────┘
```

### Componentes Nuevos

```
com.learntv.api/
+-- generation/
    +-- application/
    │   +-- port/
    │   │   +-- out/
    │   │       +-- AudioGenerationPort.java      # Interface para TTS
    │   │       +-- AudioStoragePort.java         # Interface para R2/S3
    │   +-- service/
    │       +-- AudioGenerationService.java       # Orquesta TTS + upload
    │
    +-- adapter/
        +-- out/
            +-- piper/
            │   +-- PiperTtsAdapter.java          # Llama a piper-tts CLI
            +-- cloudflare/
                +-- R2StorageAdapter.java         # Upload a R2 (S3 SDK)
```

### Interfaces

```java
// AudioGenerationPort.java
public interface AudioGenerationPort {
    byte[] generateAudio(String text, String voice);
    byte[] convertToMp3(byte[] wavData);
}

// AudioStoragePort.java
public interface AudioStoragePort {
    String upload(String key, byte[] data, String contentType);
    void delete(String key);
    String getPublicUrl(String key);
}
```

### Flujo de Audio

```java
// AudioGenerationService.java
public List<VocabularyWithAudio> generateAudioForVocabulary(
        List<ExtractedVocabulary> vocabulary) {

    return vocabulary.parallelStream()
        .map(vocab -> {
            try {
                // 1. Generar WAV con Piper
                byte[] wav = audioGenerationPort.generateAudio(
                    vocab.getTerm(),
                    "en_US-lessac-medium"
                );

                // 2. Convertir a MP3
                byte[] mp3 = audioGenerationPort.convertToMp3(wav);

                // 3. Subir a R2
                String key = "audio/vocabulary/" + slugify(vocab.getTerm()) + ".mp3";
                String url = audioStoragePort.upload(key, mp3, "audio/mpeg");

                // 4. Retornar con URL
                return vocab.withAudioUrl(url);

            } catch (Exception e) {
                log.warn("Failed to generate audio for: " + vocab.getTerm(), e);
                return vocab.withAudioUrl(null); // Fallback a Web Speech API
            }
        })
        .toList();
}
```

### Configuración R2

```yaml
# application.yml
cloudflare:
  r2:
    account-id: ${CLOUDFLARE_ACCOUNT_ID}
    access-key-id: ${R2_ACCESS_KEY_ID}
    secret-access-key: ${R2_SECRET_ACCESS_KEY}
    bucket-name: learntv-audio
    public-url: https://audio.learntv.com  # Custom domain o R2.dev URL
```

### Modelo de Datos

```sql
-- Agregar columna a vocabulary
ALTER TABLE vocabulary ADD COLUMN audio_url VARCHAR(500);

-- Índice para búsqueda rápida
CREATE INDEX idx_vocabulary_audio ON vocabulary(audio_url) WHERE audio_url IS NOT NULL;
```

---

## Estimación de Tiempos por Fase

| Fase | Descripción | Tiempo Estimado |
|------|-------------|-----------------|
| 1 | Búsqueda y selección (TMDB) | ~2 segundos |
| 2 | Obtener script (OpenSubtitles) | ~3 segundos |
| 3 | Extracción con IA (OpenAI) | ~15-20 segundos |
| 4 | Generación de ejercicios | ~10 segundos |
| **5** | **Generación de audio (25 words)** | **~20-30 segundos** |
| 6 | Persistencia | ~2 segundos |
| **Total** | | **~50-65 segundos** |

---

## Costos Estimados (por episodio)

| Servicio | Operación | Costo |
|----------|-----------|-------|
| TMDB | Búsqueda | $0 |
| OpenSubtitles | Download (VIP) | ~$0.005 |
| OpenAI GPT-4o-mini | ~4000 tokens | ~$0.01 |
| Piper TTS | Local | $0 |
| Cloudflare R2 | Storage (25 x 8KB = 200KB) | ~$0.000003 |
| Cloudflare R2 | Egress | $0 |
| **Total por episodio** | | **~$0.015** |

---

## Próximos Pasos (Descomposición en Tareas)

### Sprint 1: Foundation
- [ ] Crear módulo `generation` con estructura hexagonal
- [ ] Implementar TMDB adapter (búsqueda de shows)
- [ ] Implementar endpoint `/api/v1/generation/shows/search`

### Sprint 2: Script Fetching
- [ ] Implementar OpenSubtitles adapter (auth + download)
- [ ] Crear SRT parser
- [ ] Implementar caching de scripts

### Sprint 3: AI Integration
- [ ] Implementar OpenAI adapter
- [ ] Crear prompts para vocabulario, gramática, expresiones
- [ ] Crear prompt para ejercicios (incluyendo LISTENING)

### Sprint 4: Audio Generation ★
- [ ] Implementar PiperTtsAdapter
- [ ] Implementar R2StorageAdapter
- [ ] Crear AudioGenerationService
- [ ] Agregar audioUrl a modelo Vocabulary

### Sprint 5: Orchestration
- [ ] Implementar job async con progreso
- [ ] Crear EpisodeGenerationService (orquestador principal)
- [ ] Conectar todo el flujo end-to-end

### Sprint 6: Frontend
- [ ] UI de búsqueda de shows
- [ ] UI de selección de episodio
- [ ] Progress indicator durante generación
- [ ] Actualizar VocabularyCard para usar audioUrl

---

## Diagrama de Secuencia Completo

```
Usuario        Frontend        Backend         TMDB    OpenSubs    OpenAI    Piper    R2
   │               │               │             │          │          │        │       │
   │──"The Pitt"──>│               │             │          │          │        │       │
   │               │──search───────>│             │          │          │        │       │
   │               │               │──search────>│          │          │        │       │
   │               │               │<───shows────│          │          │        │       │
   │               │<──shows───────│             │          │          │        │       │
   │<──show list───│               │             │          │          │        │       │
   │               │               │             │          │          │        │       │
   │──select S01E01>               │             │          │          │        │       │
   │               │──generate─────>│             │          │          │        │       │
   │               │               │──get imdb──>│          │          │        │       │
   │               │               │<──imdb id───│          │          │        │       │
   │               │               │──search subs────────>│          │        │       │
   │               │               │<──srt file───────────│          │        │       │
   │               │               │                       │          │        │       │
   │               │               │──extract vocab────────────────>│        │       │
   │               │               │<──vocabulary[]─────────────────│        │       │
   │               │               │──extract grammar──────────────>│        │       │
   │               │               │<──grammarPoints[]──────────────│        │       │
   │               │               │──generate exercises───────────>│        │       │
   │               │               │<──exercises[]─────────────────│        │       │
   │               │               │                                │        │       │
   │               │               │  ┌─── Para cada vocabulary ───┐│        │       │
   │               │               │  │                            ││        │       │
   │               │               │──│──generate wav──────────────────────>│       │
   │               │               │<─│──wav bytes─────────────────────────│       │
   │               │               │──│──upload mp3──────────────────────────────>│
   │               │               │<─│──public url───────────────────────────────│
   │               │               │  └────────────────────────────┘│        │       │
   │               │               │                                │        │       │
   │               │               │──save to DB───>│               │        │       │
   │               │<──episodeId───│                │               │        │       │
   │<──redirect────│               │                │               │        │       │
   │               │               │                │               │        │       │
```

---

## Referencias

- [ADR-001: Episode Script API Design](./adr/episode-script-api-design.md)
- [Piper TTS](https://github.com/rhasspy/piper)
- [Cloudflare R2 Documentation](https://developers.cloudflare.com/r2/)
- [OpenAI API](https://platform.openai.com/docs/api-reference)
