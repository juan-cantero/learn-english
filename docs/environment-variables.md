# Environment Variables Guide

## How It Works

Environment variables keep secrets (API keys, database passwords) out of the codebase. The app never hardcodes sensitive values — they're injected at runtime.

```
.env file (local dev)
        |
        v
application.yml  ──>  @Value / @ConfigurationProperties  ──>  Java classes
        ^
        |
Render env vars (production)
```

## Security Model

| Rule | How |
|------|-----|
| Secrets never in git | `.gitignore` excludes `.env`, `*.env`, `.env.local` |
| Templates are committed | `.env.example` shows required vars with placeholder values |
| Frontend can't leak secrets | Vite only exposes `VITE_*` prefixed vars — everything else is invisible |
| Supabase `anon` key in frontend | Safe — it's a public key with Row Level Security |
| Supabase `service_role` key in backend only | Admin key — never exposed to browser |

## Backend (Spring Boot)

### Loading mechanism

The `spring-dotenv` library (`build.gradle.kts`) automatically reads `backend/.env` at startup. In production, Render injects env vars directly into the Docker container — no `.env` file needed.

### Flow

```
backend/.env (local)  ─┐
                        ├──>  Spring Environment  ──>  application.yml  ──>  Java config
Render env vars (prod) ─┘
```

### application.yml syntax

```yaml
supabase:
  url: ${SUPABASE_URL:https://localhost}
#         ^env var name   ^default if missing
```

### Java binding patterns

**@Value** — simple injection:
```java
@Value("${external-apis.openai.api-key}")
private String apiKey;
```

**@ConfigurationProperties** — type-safe records:
```java
@ConfigurationProperties(prefix = "supabase.jwt")
public record SupabaseJwtProperties(String secret, String issuer, ...)
```

### Backend variables

| Variable | Where used | Secret? |
|----------|-----------|---------|
| `DATABASE_URL` | PostgreSQL connection | Yes |
| `SUPABASE_URL` | Storage API base URL, JWT issuer | No |
| `SUPABASE_JWT_SECRET` | HS256 token verification | Yes |
| `SUPABASE_JWT_EC_KEY_X` | ES256 token verification (public key) | No |
| `SUPABASE_JWT_EC_KEY_Y` | ES256 token verification (public key) | No |
| `SUPABASE_SERVICE_ROLE_KEY` | Storage uploads/deletes (admin) | Yes |
| `SUPABASE_STORAGE_BUCKET` | Bucket name | No |
| `CORS_ALLOWED_ORIGINS` | Allowed frontend origins | No |
| `TMDB_API_KEY` | TMDB show metadata | Yes |
| `OPENAI_API_KEY` | Lesson content generation | Yes |
| `OPENSUBTITLES_API_KEY` | Subtitle downloads | Yes |

### Spring profiles

| Profile | Activated by | What changes |
|---------|-------------|-------------|
| `dev` | Default | Local PostgreSQL, mock auth, local file storage |
| `test` | Tests | H2 in-memory DB, local file storage |
| `local-supabase` | Manual | Local PostgreSQL, real Supabase auth + storage |
| `production` | Dockerfile | Supabase PostgreSQL, real auth, Supabase Storage |

The profile controls which `AudioStoragePort` implementation is used:
- `dev` / `test` → `LocalStorageAdapter` (saves to filesystem)
- `local-supabase` / `production` → `SupabaseStorageAdapter` (REST API)

## Frontend (Vite)

### Why does the frontend need keys?

The frontend talks to **two** services directly:

```
Browser  ──>  Supabase Auth (login, signup, session)
Browser  ──>  Render Backend (API calls for shows, lessons, etc.)
```

For login and signup, the browser calls Supabase directly — not through the backend. That's why the frontend needs `VITE_SUPABASE_URL` and `VITE_SUPABASE_ANON_KEY`.

The `anon` key is **designed to be public**. It can't do anything dangerous because Supabase enforces Row Level Security (RLS) on every request. The key just identifies the project, like a project ID. Anyone can see it in the browser's JavaScript — that's fine.

The `service_role` key is different — it **bypasses RLS** and has admin access. That's why it only lives in the backend, never in the frontend.

| Key | Where | Why | Safe in browser? |
|-----|-------|-----|-----------------|
| `anon` key | Frontend | Auth (login/signup) | Yes — RLS protects data |
| `service_role` key | Backend only | Storage admin ops | No — bypasses RLS |
| API keys (TMDB, OpenAI) | Backend only | Server-to-server calls | No — would leak billing |

### How does Vite replace variables?

When you run `npm run build`, Vite scans the source code and does a **literal text replacement**:

**Source code (what you write):**
```typescript
const url = import.meta.env.VITE_SUPABASE_URL
```

**After build (what ends up in dist/):**
```typescript
const url = "https://ipxonmprtfidtohwzzpx.supabase.co"
```

The value is hardcoded into the JavaScript file. There's no runtime lookup — it's a build-time-only operation. This means:
- You must rebuild (redeploy on Netlify) after changing env vars
- Only `VITE_*` prefixed vars get replaced — Vite ignores the rest as a safety measure
- Never create a `VITE_SERVICE_ROLE_KEY` — it would be visible in the browser's JS source

### Loading mechanism

Vite reads `.env` files at **build time** and statically replaces `import.meta.env.VITE_*` in the JavaScript bundle. Only variables prefixed with `VITE_` are included.

### Flow

```
.env.local (local dev)  ─┐
                          ├──>  Vite build  ──>  dist/  (values baked into JS)
Netlify env vars (prod)  ─┘
```

### Usage in code

```typescript
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api/v1';
const supabaseUrl = import.meta.env.VITE_SUPABASE_URL || 'http://127.0.0.1:54321';
```

### Frontend variables

| Variable | Purpose | Secret? |
|----------|---------|---------|
| `VITE_API_BASE_URL` | Backend URL | No |
| `VITE_SUPABASE_URL` | Supabase project URL | No |
| `VITE_SUPABASE_ANON_KEY` | Public Supabase key (RLS enforced) | No |

In dev mode, the Vite proxy (`vite.config.ts`) forwards `/api` requests to `localhost:8080`, so `VITE_API_BASE_URL` isn't needed locally.

## Production (Render + Netlify + Supabase)

```
Netlify                    Render                         Supabase
┌─────────────────┐       ┌───────────────────┐          ┌──────────────┐
│ VITE_API_BASE_  │──────>│ DATABASE_URL      │────────> │ PostgreSQL   │
│ VITE_SUPABASE_  │       │ SUPABASE_*        │────────> │ Auth         │
│                 │       │ *_API_KEY          │          │ Storage      │
│ (build time)    │       │ CORS_ALLOWED_*    │          │              │
└─────────────────┘       └───────────────────┘          └──────────────┘
  env vars in               env vars in                    provides keys
  Netlify dashboard         Render dashboard               in dashboard
```

- **Render**: env vars injected into Docker container at runtime
- **Netlify**: env vars used during `npm run build`, baked into static files
- **Supabase**: source of truth for database URL, JWT keys, and API keys

## Local Development Setup

1. Copy the templates:
   ```bash
   cp backend/.env.example backend/.env
   cp frontend/.env.example frontend/.env.local
   ```

2. Fill in your values in `backend/.env`

3. Frontend needs no `.env.local` for basic dev — the Vite proxy and local Supabase defaults handle it

## Adding a New Environment Variable

1. Add to `application.yml` with `${VAR_NAME:default}` syntax
2. Add to `backend/.env.example` with a placeholder value
3. Add to your local `backend/.env` with the real value
4. Add to Render dashboard for production
5. If it's a frontend var, prefix with `VITE_` and add to Netlify
