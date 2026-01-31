# LearnTV - Setup Guide

Guía completa para levantar el entorno de desarrollo.

## Requisitos

| Software | Versión | Instalación (Arch) |
|----------|---------|-------------------|
| Java | 21+ | `sudo pacman -S jdk21-openjdk` |
| Node.js | 20+ | `sudo pacman -S nodejs npm` |
| Docker | Latest | `sudo pacman -S docker docker-compose` |
| Piper TTS | Latest | `paru -S piper-tts-bin` |
| ffmpeg | Latest | `sudo pacman -S ffmpeg` |

## Estructura del Proyecto

```
learn-english/
├── backend/          # Spring Boot API (Java 21)
├── frontend/         # React + Vite + TanStack
├── docs/             # Documentación
└── SETUP.md          # Este archivo
```

---

## 1. Base de Datos (PostgreSQL)

```bash
cd backend

# Iniciar PostgreSQL en Docker
docker-compose up -d

# Verificar que está corriendo
docker ps
# → learntv-db en puerto 5433

# Ver logs
docker-compose logs -f postgres
```

**Conexión:**
- Host: `localhost`
- Puerto: `5433`
- Base de datos: `learntv`
- Usuario: `learntv`
- Password: `learntv_dev`

---

## 2. Backend (Spring Boot)

### 2.1 Configurar Variables de Entorno

```bash
cd backend

# Copiar template
cp .env.example .env

# Editar con tus API keys
nano .env
```

**Variables requeridas:**

```env
# TMDB - para buscar shows (https://www.themoviedb.org/settings/api)
TMDB_API_KEY=tu_api_key

# OpenSubtitles - para subtítulos (https://www.opensubtitles.com/consumers)
OPENSUBTITLES_API_KEY=tu_api_key

# OpenAI - para extracción de contenido (https://platform.openai.com/api-keys)
OPENAI_API_KEY=sk-tu_api_key
```

**Variables opcionales (para producción):**

```env
# Cloudflare R2 - storage de audio
CLOUDFLARE_ACCOUNT_ID=tu_account_id
R2_ACCESS_KEY_ID=tu_access_key
R2_SECRET_ACCESS_KEY=tu_secret_key
R2_BUCKET_NAME=learntv-audio
R2_PUBLIC_URL=https://tu-bucket.r2.dev
```

### 2.2 Instalar Piper TTS (Audio)

```bash
# En Arch Linux
paru -S piper-tts-bin

# Descargar modelo de voz
mkdir -p ~/.local/share/piper
cd ~/.local/share/piper

# Descargar modelo en inglés (60MB)
curl -LO https://huggingface.co/rhasspy/piper-voices/resolve/main/en/en_US/lessac/medium/en_US-lessac-medium.onnx
curl -LO https://huggingface.co/rhasspy/piper-voices/resolve/main/en/en_US/lessac/medium/en_US-lessac-medium.onnx.json

# Verificar
echo "Hello world" | piper-tts --model en_US-lessac-medium.onnx --output_file /tmp/test.wav
```

### 2.3 Iniciar Backend

```bash
cd backend

# Cargar variables de entorno
source .env
# O exportar manualmente:
export TMDB_API_KEY=...
export OPENSUBTITLES_API_KEY=...
export OPENAI_API_KEY=...

# Ejecutar
./gradlew bootRun
```

**El backend estará en:**
- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- Health: http://localhost:8080/actuator/health

---

## 3. Frontend (React + Vite)

```bash
cd frontend

# Instalar dependencias
npm install

# Iniciar en modo desarrollo
npm run dev
```

**El frontend estará en:** http://localhost:5173

---

## 4. Verificar Todo

### 4.1 Health Checks

```bash
# Base de datos
docker exec learntv-db pg_isready -U learntv
# → accepting connections

# Backend
curl http://localhost:8080/actuator/health
# → {"status":"UP"}

# Frontend
curl -I http://localhost:5173
# → HTTP/1.1 200 OK
```

### 4.2 Probar APIs

```bash
# Buscar un show
curl "http://localhost:8080/api/v1/generation/shows/search?q=breaking%20bad"

# Verificar TTS
curl "http://localhost:8080/api/v1/tts/speak?text=hello" --output /tmp/hello.wav
mpv /tmp/hello.wav

# Verificar storage local
curl http://localhost:8080/api/v1/audio
# → "Local audio storage is ready at: /tmp/learntv-audio"
```

---

## 5. Comandos Útiles

### Backend

```bash
# Compilar
./gradlew compileJava

# Tests
./gradlew test

# Build JAR
./gradlew bootJar

# Limpiar
./gradlew clean
```

### Docker

```bash
# Iniciar DB
docker-compose up -d

# Parar DB
docker-compose down

# Parar y borrar datos
docker-compose down -v

# Ver logs
docker-compose logs -f
```

### Frontend

```bash
# Dev server
npm run dev

# Build producción
npm run build

# Preview build
npm run preview

# Lint
npm run lint
```

---

## 6. Perfiles de Spring

| Perfil | Base de Datos | Audio Storage | Uso |
|--------|---------------|---------------|-----|
| `dev` (default) | PostgreSQL Docker (5433) | Local (`/tmp/learntv-audio`) | Desarrollo |
| `test` | H2 in-memory | - | Tests |
| `production` | PostgreSQL externo | Cloudflare R2 | Producción |

Cambiar perfil:
```bash
./gradlew bootRun --args='--spring.profiles.active=test'
```

---

## 7. Troubleshooting

### "Connection refused" al conectar DB

```bash
# Verificar que Docker está corriendo
docker ps | grep learntv-db

# Si no está, iniciarlo
docker-compose up -d
```

### "Piper TTS not found"

```bash
# Verificar instalación
which piper-tts

# Verificar modelo
ls ~/.local/share/piper/
# Debe tener: en_US-lessac-medium.onnx
```

### "TMDB_API_KEY not set"

```bash
# Verificar que .env existe
cat backend/.env

# Cargar variables
cd backend && source .env

# O exportar
export TMDB_API_KEY=tu_key
```

### Puerto 5433 ocupado

```bash
# Ver qué usa el puerto
sudo lsof -i :5433

# Cambiar puerto en docker-compose.yml si es necesario
```

---

## 8. URLs Importantes

| Servicio | URL |
|----------|-----|
| Frontend | http://localhost:5173 |
| Backend API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| API Docs (JSON) | http://localhost:8080/api-docs |
| Local Audio | http://localhost:8080/api/v1/audio |

---

## Quick Start (TL;DR)

```bash
# Terminal 1 - Base de datos
cd backend && docker-compose up -d

# Terminal 2 - Backend
cd backend
cp .env.example .env
# Editar .env con tus API keys
source .env && ./gradlew bootRun

# Terminal 3 - Frontend
cd frontend && npm install && npm run dev
```

Abrir http://localhost:5173 🎉
