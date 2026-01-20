# LearnTV - Architecture v2

## Stack Overview

```
┌─────────────────────────────────────────────────────────────┐
│                        FRONTEND                              │
│                                                              │
│   React 18 + Vite                                           │
│   ├── TanStack Router (type-safe routing)                   │
│   ├── TanStack Query (data fetching & caching)              │
│   ├── Tailwind CSS (styling)                                │
│   └── TypeScript                                            │
│                                                              │
└──────────────────────────┬──────────────────────────────────┘
                           │ HTTP/REST
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                        BACKEND                               │
│                                                              │
│   Node.js + Hono (lightweight, fast, TypeScript)            │
│   ├── Drizzle ORM (type-safe, multi-DB support)             │
│   ├── Zod (validation)                                      │
│   └── JWT (auth when needed)                                │
│                                                              │
└──────────────────────────┬──────────────────────────────────┘
                           │
           ┌───────────────┴───────────────┐
           ▼                               ▼
┌─────────────────────┐         ┌─────────────────────┐
│   LOCAL (Dev)       │         │   PRODUCTION        │
│                     │         │                     │
│   SQLite            │   →→→   │   Supabase          │
│   (single file)     │         │   (PostgreSQL)      │
│                     │         │                     │
└─────────────────────┘         └─────────────────────┘
```

---

## Why This Stack?

### Frontend: React + TanStack + Vite

| Tool | Purpose | Why |
|------|---------|-----|
| **Vite** | Build tool | Lightning fast HMR, modern |
| **React 18** | UI library | You know it, huge ecosystem |
| **TanStack Router** | Routing | Type-safe, file-based optional |
| **TanStack Query** | Data fetching | Caching, refetching, mutations |
| **Tailwind CSS** | Styling | Already using, utility-first |
| **TypeScript** | Type safety | End-to-end types with backend |

### Backend: Hono + Drizzle

| Tool | Purpose | Why |
|------|---------|-----|
| **Hono** | HTTP framework | Fast, tiny, TypeScript-first, runs anywhere |
| **Drizzle ORM** | Database | Type-safe, supports SQLite + PostgreSQL |
| **Zod** | Validation | Schema validation, works with TypeScript |
| **better-sqlite3** | Local DB | Fast SQLite for development |

### Why Hono over Express?

```typescript
// Hono is cleaner and more modern
const app = new Hono()

app.get('/api/shows', async (c) => {
  const shows = await db.select().from(showsTable)
  return c.json(shows)
})

// Type-safe route params
app.get('/api/shows/:slug', async (c) => {
  const { slug } = c.req.param()  // TypeScript knows this
  // ...
})
```

- 12KB vs Express's 200KB+
- Built-in TypeScript
- Can run on Supabase Edge Functions later
- Modern middleware system

### Why Drizzle over Prisma?

- **Drizzle**: SQL-like, lightweight, supports SQLite ↔ PostgreSQL easily
- **Prisma**: Great but heavier, schema migrations more complex

```typescript
// Drizzle - feels like SQL, fully typed
const shows = await db
  .select()
  .from(showsTable)
  .where(eq(showsTable.slug, 'the-pitt'))
  .leftJoin(episodesTable, eq(episodesTable.showId, showsTable.id))
```

---

## Local Development Flow

```
1. Clone repo
2. pnpm install
3. pnpm db:push        # Creates SQLite database
4. pnpm db:seed        # Seeds with The Pitt data
5. pnpm dev            # Starts frontend + backend

No Docker needed! No cloud accounts needed!
```

### Database Strategy

**Local (SQLite)**
```
learntv.db  ← Single file, zero config
```

**Production (Supabase)**
```
Just change DATABASE_URL in .env
Drizzle handles the rest
```

The schema is identical — Drizzle abstracts the differences.

---

## Project Structure

```
learntv/
├── apps/
│   ├── web/                      # React frontend
│   │   ├── src/
│   │   │   ├── main.tsx
│   │   │   ├── App.tsx
│   │   │   ├── routes/           # TanStack Router
│   │   │   │   ├── __root.tsx
│   │   │   │   ├── index.tsx              # Home
│   │   │   │   ├── shows/
│   │   │   │   │   ├── index.tsx          # All shows
│   │   │   │   │   ├── $slug.tsx          # Show detail
│   │   │   │   │   └── $slug.$episode.tsx # Episode lesson
│   │   │   │   └── profile.tsx
│   │   │   ├── components/
│   │   │   │   ├── ui/           # Reusable UI
│   │   │   │   ├── shows/        # Show-related
│   │   │   │   ├── lesson/       # Lesson components
│   │   │   │   └── exercises/    # Exercise components
│   │   │   ├── hooks/
│   │   │   │   ├── useShows.ts
│   │   │   │   ├── useEpisode.ts
│   │   │   │   └── useProgress.ts
│   │   │   ├── lib/
│   │   │   │   ├── api.ts        # API client
│   │   │   │   └── utils.ts
│   │   │   └── styles/
│   │   │       └── globals.css
│   │   ├── index.html
│   │   ├── vite.config.ts
│   │   ├── tailwind.config.ts
│   │   └── package.json
│   │
│   └── api/                      # Hono backend
│       ├── src/
│       │   ├── index.ts          # Entry point
│       │   ├── routes/
│       │   │   ├── shows.ts
│       │   │   ├── episodes.ts
│       │   │   ├── vocabulary.ts
│       │   │   ├── exercises.ts
│       │   │   └── progress.ts
│       │   ├── db/
│       │   │   ├── index.ts      # DB connection
│       │   │   ├── schema.ts     # Drizzle schema
│       │   │   └── seed.ts       # Seed data
│       │   ├── middleware/
│       │   │   └── auth.ts       # JWT auth (later)
│       │   └── lib/
│       │       └── utils.ts
│       ├── drizzle.config.ts
│       └── package.json
│
├── packages/
│   └── shared/                   # Shared types
│       ├── src/
│       │   └── types.ts          # API types, shared between FE/BE
│       └── package.json
│
├── package.json                  # Workspace root
├── pnpm-workspace.yaml
├── .env.example
└── README.md
```

---

## Database Schema (Drizzle)

```typescript
// apps/api/src/db/schema.ts
import { sqliteTable, text, integer, real } from 'drizzle-orm/sqlite-core'
// For production, switch to: import { pgTable, ... } from 'drizzle-orm/pg-core'

// Shows
export const shows = sqliteTable('shows', {
  id: text('id').primaryKey().$defaultFn(() => crypto.randomUUID()),
  slug: text('slug').unique().notNull(),
  title: text('title').notNull(),
  description: text('description'),
  posterUrl: text('poster_url'),
  bannerUrl: text('banner_url'),
  genre: text('genre'),
  accentType: text('accent_type'),  // 'american', 'british'
  difficulty: text('difficulty'),    // 'beginner', 'intermediate', 'advanced'
  releaseYear: integer('release_year'),
  totalSeasons: integer('total_seasons'),
  createdAt: integer('created_at', { mode: 'timestamp' }).$defaultFn(() => new Date()),
})

// Episodes
export const episodes = sqliteTable('episodes', {
  id: text('id').primaryKey().$defaultFn(() => crypto.randomUUID()),
  showId: text('show_id').references(() => shows.id, { onDelete: 'cascade' }).notNull(),
  slug: text('slug').notNull(),
  season: integer('season').notNull(),
  episodeNumber: integer('episode_number').notNull(),
  title: text('title').notNull(),
  airDate: text('air_date'),
  durationMinutes: integer('duration_minutes'),
  summary: text('summary'),
  context: text('context'),
  createdAt: integer('created_at', { mode: 'timestamp' }).$defaultFn(() => new Date()),
})

// Vocabulary
export const vocabulary = sqliteTable('vocabulary', {
  id: text('id').primaryKey().$defaultFn(() => crypto.randomUUID()),
  episodeId: text('episode_id').references(() => episodes.id, { onDelete: 'cascade' }).notNull(),
  word: text('word').notNull(),
  phonetic: text('phonetic'),
  partOfSpeech: text('part_of_speech'),
  definition: text('definition').notNull(),
  exampleSentence: text('example_sentence'),
  category: text('category'),        // 'medical', 'workplace', 'informal'
  difficulty: text('difficulty'),    // 'easy', 'medium', 'hard'
  sortOrder: integer('sort_order').default(0),
})

// Grammar Points
export const grammarPoints = sqliteTable('grammar_points', {
  id: text('id').primaryKey().$defaultFn(() => crypto.randomUUID()),
  episodeId: text('episode_id').references(() => episodes.id, { onDelete: 'cascade' }).notNull(),
  title: text('title').notNull(),
  tag: text('tag'),
  level: text('level'),
  pattern: text('pattern'),
  explanation: text('explanation').notNull(),
  dialogueExample: text('dialogue_example').notNull(),
  speaker: text('speaker'),
  moreExamples: text('more_examples', { mode: 'json' }).$type<string[]>(),
  sortOrder: integer('sort_order').default(0),
})

// Expressions
export const expressions = sqliteTable('expressions', {
  id: text('id').primaryKey().$defaultFn(() => crypto.randomUUID()),
  episodeId: text('episode_id').references(() => episodes.id, { onDelete: 'cascade' }).notNull(),
  quote: text('quote').notNull(),
  speaker: text('speaker'),
  meaning: text('meaning').notNull(),
  usageNotes: text('usage_notes'),
  tone: text('tone'),
  sortOrder: integer('sort_order').default(0),
})

// Exercises
export const exercises = sqliteTable('exercises', {
  id: text('id').primaryKey().$defaultFn(() => crypto.randomUUID()),
  episodeId: text('episode_id').references(() => episodes.id, { onDelete: 'cascade' }).notNull(),
  type: text('type').notNull(),      // 'fill_blank', 'matching', 'quiz'
  question: text('question').notNull(),
  options: text('options', { mode: 'json' }).$type<Array<{text: string, isCorrect?: boolean}>>(),
  correctAnswer: text('correct_answer'),
  hint: text('hint'),
  explanation: text('explanation'),
  sortOrder: integer('sort_order').default(0),
})

// User Progress (for later)
export const userProgress = sqliteTable('user_progress', {
  id: text('id').primaryKey().$defaultFn(() => crypto.randomUUID()),
  odId: text('user_id').notNull(),  // Will be Supabase auth.users.id later
  episodeId: text('episode_id').references(() => episodes.id, { onDelete: 'cascade' }).notNull(),
  vocabularyViewed: integer('vocabulary_viewed').default(0),
  grammarViewed: integer('grammar_viewed').default(0),
  exercisesCompleted: text('exercises_completed', { mode: 'json' }).$type<string[]>().default([]),
  quizScore: integer('quiz_score'),
  quizAttempts: integer('quiz_attempts').default(0),
  completedAt: integer('completed_at', { mode: 'timestamp' }),
  lastAccessed: integer('last_accessed', { mode: 'timestamp' }).$defaultFn(() => new Date()),
})
```

---

## API Routes (Hono)

```typescript
// apps/api/src/routes/shows.ts
import { Hono } from 'hono'
import { db } from '../db'
import { shows, episodes } from '../db/schema'
import { eq } from 'drizzle-orm'

const showsRouter = new Hono()

// GET /api/shows - All shows
showsRouter.get('/', async (c) => {
  const allShows = await db.select().from(shows)
  return c.json(allShows)
})

// GET /api/shows/:slug - Single show with episodes
showsRouter.get('/:slug', async (c) => {
  const { slug } = c.req.param()

  const show = await db.select().from(shows).where(eq(shows.slug, slug)).get()
  if (!show) return c.json({ error: 'Show not found' }, 404)

  const showEpisodes = await db
    .select()
    .from(episodes)
    .where(eq(episodes.showId, show.id))
    .orderBy(episodes.season, episodes.episodeNumber)

  return c.json({ ...show, episodes: showEpisodes })
})

export { showsRouter }
```

```typescript
// apps/api/src/routes/episodes.ts
import { Hono } from 'hono'
import { db } from '../db'
import { episodes, vocabulary, grammarPoints, expressions, exercises } from '../db/schema'
import { eq, and } from 'drizzle-orm'

const episodesRouter = new Hono()

// GET /api/shows/:showSlug/episodes/:episodeSlug - Full episode lesson
episodesRouter.get('/:showSlug/:episodeSlug', async (c) => {
  const { showSlug, episodeSlug } = c.req.param()

  // Get episode with all related content
  const episode = await db
    .select()
    .from(episodes)
    .innerJoin(shows, eq(episodes.showId, shows.id))
    .where(and(
      eq(shows.slug, showSlug),
      eq(episodes.slug, episodeSlug)
    ))
    .get()

  if (!episode) return c.json({ error: 'Episode not found' }, 404)

  const [vocabItems, grammarItems, expressionItems, exerciseItems] = await Promise.all([
    db.select().from(vocabulary).where(eq(vocabulary.episodeId, episode.episodes.id)),
    db.select().from(grammarPoints).where(eq(grammarPoints.episodeId, episode.episodes.id)),
    db.select().from(expressions).where(eq(expressions.episodeId, episode.episodes.id)),
    db.select().from(exercises).where(eq(exercises.episodeId, episode.episodes.id)),
  ])

  return c.json({
    ...episode.episodes,
    show: episode.shows,
    vocabulary: vocabItems,
    grammarPoints: grammarItems,
    expressions: expressionItems,
    exercises: exerciseItems,
  })
})

export { episodesRouter }
```

---

## Frontend Hooks (TanStack Query)

```typescript
// apps/web/src/hooks/useShows.ts
import { useQuery } from '@tanstack/react-query'
import { api } from '../lib/api'

export function useShows() {
  return useQuery({
    queryKey: ['shows'],
    queryFn: () => api.get('/shows').then(r => r.json()),
  })
}

export function useShow(slug: string) {
  return useQuery({
    queryKey: ['shows', slug],
    queryFn: () => api.get(`/shows/${slug}`).then(r => r.json()),
    enabled: !!slug,
  })
}
```

```typescript
// apps/web/src/hooks/useEpisode.ts
import { useQuery } from '@tanstack/react-query'
import { api } from '../lib/api'

export function useEpisode(showSlug: string, episodeSlug: string) {
  return useQuery({
    queryKey: ['episodes', showSlug, episodeSlug],
    queryFn: () => api.get(`/episodes/${showSlug}/${episodeSlug}`).then(r => r.json()),
    enabled: !!showSlug && !!episodeSlug,
    staleTime: 5 * 60 * 1000, // Cache for 5 minutes
  })
}
```

---

## Local → Production Migration

### Step 1: Local Development (SQLite)
```env
# .env.local
DATABASE_URL=file:./learntv.db
```

### Step 2: Production (Supabase)
```env
# .env.production
DATABASE_URL=postgresql://postgres:[password]@db.[project].supabase.co:5432/postgres
```

### Step 3: Schema Change (one-time)
```typescript
// Just swap imports in schema.ts
// FROM:
import { sqliteTable, text, integer } from 'drizzle-orm/sqlite-core'

// TO:
import { pgTable, text, integer, uuid, timestamp } from 'drizzle-orm/pg-core'
```

Drizzle makes this nearly seamless.

---

## Commands

```bash
# Development
pnpm dev              # Start frontend + backend
pnpm dev:web          # Frontend only
pnpm dev:api          # Backend only

# Database
pnpm db:generate      # Generate migrations from schema
pnpm db:push          # Push schema to database
pnpm db:seed          # Seed with initial data
pnpm db:studio        # Open Drizzle Studio (GUI)

# Build
pnpm build            # Build all
pnpm build:web        # Build frontend
pnpm build:api        # Build backend

# Type checking
pnpm typecheck        # Check all TypeScript
```

---

## Development Phases (Updated)

### Phase 1: Project Setup (This session)
- [ ] Initialize monorepo with pnpm workspaces
- [ ] Set up Vite + React + TanStack Router
- [ ] Set up Hono + Drizzle + SQLite
- [ ] Create database schema
- [ ] Seed with The Pitt Episode 1 data
- [ ] Basic API routes

### Phase 2: Show Catalog UI
- [ ] Home page (show grid)
- [ ] ShowCard component
- [ ] Show detail page (episodes list)
- [ ] TanStack Query integration

### Phase 3: Episode Lessons
- [ ] Migrate current beautiful design to React
- [ ] VocabularySection component
- [ ] GrammarSection component
- [ ] ExpressionsSection component
- [ ] Dynamic data loading

### Phase 4: Exercise Engine
- [ ] ExerciseEngine component
- [ ] FillBlank, Matching, Quiz components
- [ ] Progress tracking (local storage first)
- [ ] TanStack Query mutations

### Phase 5: Polish & More Content
- [ ] Add more episodes
- [ ] Search/filter
- [ ] Responsive design
- [ ] Loading states

### Phase 6: Auth & Cloud (Later)
- [ ] Supabase integration
- [ ] User authentication
- [ ] Cloud progress sync
- [ ] Deployment

---

## Questions Answered

| Question | Answer |
|----------|--------|
| No Next.js? | ✅ Using Vite + React + TanStack Router |
| Local-first? | ✅ SQLite, no cloud needed |
| Supabase compatible? | ✅ Same PostgreSQL schema, easy switch |
| TanStack? | ✅ Router + Query |
| Users later? | ✅ Schema ready, implement when needed |

---

Ready to initialize the project?
