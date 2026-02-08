# Deployment Guide

## Architecture

```
[Netlify]  -->  [Render]  -->  [Supabase PostgreSQL]
 Frontend       Backend        Database + Auth + Storage
   SPA          Docker
```

## 1. Supabase (Database + Auth)

1. Go to https://supabase.com and create a new project
2. Note down from **Settings > API**:
   - Project URL (`https://xxxxx.supabase.co`)
   - `anon` public key (safe for frontend)
   - JWT Secret (from Settings > API > JWT Settings)
3. Note down from **Settings > Database**:
   - Connection string (URI format)

The Flyway migrations will create all tables on first backend startup.

## 2. Supabase Storage (Audio)

1. In your Supabase project, go to **Storage** > **New bucket**
2. Name: `learntv-audio`, toggle **Public** on
3. Go to **Settings > API** and copy the `service_role` key
4. Set `SUPABASE_SERVICE_ROLE_KEY` env var with this key

## 3. Render (Backend)

1. Go to https://render.com and connect your GitHub repo
2. Create a new **Web Service**:
   - **Root Directory**: `backend`
   - **Runtime**: Docker
   - **Instance Type**: Free
   - **Region**: Choose closest to your users
3. Add these **Environment Variables** in Render dashboard:

| Variable | Value |
|----------|-------|
| `DATABASE_URL` | `jdbc:postgresql://db.xxx.supabase.co:5432/postgres?user=postgres&password=xxx` |
| `SUPABASE_JWT_SECRET` | From Supabase JWT Settings |
| `SUPABASE_URL` | `https://xxxxx.supabase.co` |
| `CORS_ALLOWED_ORIGINS` | `https://your-app.netlify.app` |
| `TMDB_API_KEY` | Your TMDB key |
| `OPENAI_API_KEY` | Your OpenAI key |
| `OPENSUBTITLES_API_KEY` | Your OpenSubtitles key |
| `SUPABASE_SERVICE_ROLE_KEY` | From Supabase Settings > API |
| `SUPABASE_STORAGE_BUCKET` | `learntv-audio` |

4. Deploy. First build takes ~5 min. Subsequent deploys are faster.

**Note:** Free tier spins down after 15 min of inactivity. First request after sleep takes ~30s (Java cold start).

## 4. Netlify (Frontend)

1. Go to https://netlify.com and connect your GitHub repo
2. Create a new site:
   - **Base directory**: `frontend`
   - **Build command**: `npm run build`
   - **Publish directory**: `frontend/dist`
3. Add these **Environment Variables** in Netlify dashboard (Site settings > Environment variables):

| Variable | Value |
|----------|-------|
| `VITE_API_BASE_URL` | `https://your-backend.onrender.com/api/v1` |
| `VITE_SUPABASE_URL` | `https://xxxxx.supabase.co` |
| `VITE_SUPABASE_ANON_KEY` | From Supabase (anon key - safe to expose) |

4. Deploy. Takes ~30 seconds.

## 5. Post-Deploy Checklist

- [ ] Open the Netlify URL - homepage loads
- [ ] Register a new account - check Supabase Auth dashboard
- [ ] Browse shows - backend is responding
- [ ] Generate a lesson - OpenAI + TMDB + OpenSubtitles working
- [ ] Check audio plays - Supabase Storage working
- [ ] Create a classroom and join from another account

## Migration to VPS Later

When ready to move to a VPS:

1. **Backend**: Run the same Docker image on your VPS (`docker compose up`)
2. **Frontend**: `npm run build` and serve `dist/` with nginx
3. **Database**: Keep Supabase, or self-host PostgreSQL and import the dump
4. **Auth**: Keep Supabase Auth, or migrate to self-hosted Supabase
5. **Storage**: Keep Supabase Storage, or self-host with S3-compatible storage
6. Update DNS to point to your VPS
