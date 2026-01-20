# LearnTV - Architecture Plan

## Vision

A Netflix/Rotten Tomatoes-style platform for learning English through TV shows. Users browse a catalog of shows, select episodes, and get interactive lessons with vocabulary, grammar, and exercises — all dynamically loaded from a database.

---

## Current State vs. Target

| Aspect | Current | Target |
|--------|---------|--------|
| Shows | 1 (The Pitt) | Unlimited catalog |
| Episodes | 1 (hardcoded) | Dynamic per show |
| Content | Hardcoded HTML/JS | Database-driven |
| Progress | localStorage | User accounts + DB |
| Admin | Manual code edits | Content management UI |

---

## Recommended Tech Stack

### Option A: Next.js + Supabase (Recommended)

**Why this stack:**
- **Next.js 14+**: Full-stack React framework, great DX, easy deployment
- **Supabase**: Free PostgreSQL + Auth + Real-time + Storage (generous free tier)
- **Tailwind CSS**: Keep our beautiful design, utility-first approach
- **TypeScript**: Type safety for complex data models

```
┌─────────────────────────────────────────────────┐
│                   FRONTEND                       │
│           Next.js 14 (App Router)               │
│    ┌─────────────────────────────────────┐      │
│    │  Pages:                             │      │
│    │  • / (Home - Show Catalog)          │      │
│    │  • /shows/[slug] (Show Details)     │      │
│    │  • /shows/[slug]/[episode] (Lesson) │      │
│    │  • /profile (User Progress)         │      │
│    └─────────────────────────────────────┘      │
│    ┌─────────────────────────────────────┐      │
│    │  Components:                        │      │
│    │  • ShowCard, EpisodeCard            │      │
│    │  • VocabularySection                │      │
│    │  • GrammarSection                   │      │
│    │  • ExerciseEngine                   │      │
│    │  • ProgressTracker                  │      │
│    └─────────────────────────────────────┘      │
└────────────────────┬────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────┐
│                   SUPABASE                       │
│  ┌──────────────┐  ┌──────────────────────────┐ │
│  │  PostgreSQL  │  │  Auth (Email/OAuth)      │ │
│  │  Database    │  │  Google, GitHub login    │ │
│  └──────────────┘  └──────────────────────────┘ │
│  ┌──────────────┐  ┌──────────────────────────┐ │
│  │  Storage     │  │  Edge Functions          │ │
│  │  (Images)    │  │  (AI processing?)        │ │
│  └──────────────┘  └──────────────────────────┘ │
└─────────────────────────────────────────────────┘
```

**Pros:**
- One codebase (frontend + API)
- Generous free tiers (Vercel + Supabase)
- TypeScript end-to-end
- Real-time features possible
- Easy deployment

**Cons:**
- React learning curve if new to it
- Supabase vendor lock-in (but PostgreSQL is portable)

---

### Option B: SvelteKit + Supabase (Simpler)

**If you prefer less complexity:**
- **SvelteKit**: Simpler than React, less boilerplate
- **Supabase**: Same database benefits
- **Tailwind CSS**: Works great with Svelte

**Pros:**
- Svelte is easier to learn than React
- Smaller bundle sizes
- Great performance

**Cons:**
- Smaller ecosystem than React
- Fewer job opportunities (if that matters)

---

### Option C: Go Backend + HTMX + SQLite (For Go Practice)

**Since you're learning Go:**
- **Go + Gin/Echo**: Fast, simple backend
- **HTMX**: Interactivity without heavy JS frameworks
- **Templ**: Type-safe HTML templates
- **SQLite**: Simple, file-based database

```
┌─────────────────────────────────────────────────┐
│              Go Backend (Gin/Echo)              │
│    ┌─────────────────────────────────────┐      │
│    │  Handlers:                          │      │
│    │  • GET /shows                       │      │
│    │  • GET /shows/:slug                 │      │
│    │  • GET /shows/:slug/:episode        │      │
│    │  • POST /progress (HTMX partial)    │      │
│    └─────────────────────────────────────┘      │
│    ┌─────────────────────────────────────┐      │
│    │  Templates (Templ):                 │      │
│    │  • layout.templ                     │      │
│    │  • show_catalog.templ               │      │
│    │  • episode_lesson.templ             │      │
│    │  • exercise_partials.templ (HTMX)   │      │
│    └─────────────────────────────────────┘      │
└────────────────────┬────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────┐
│                SQLite Database                   │
│    Single file: learntv.db                      │
│    Easy backups, easy development               │
└─────────────────────────────────────────────────┘
```

**Pros:**
- Practice Go
- Very fast performance
- Simple deployment (single binary + DB file)
- HTMX is fun and lightweight

**Cons:**
- Less interactive than SPA
- Manual auth implementation
- No real-time features built-in

---

## Database Schema

```sql
-- TV Shows
CREATE TABLE shows (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    slug VARCHAR(100) UNIQUE NOT NULL,      -- "the-pitt"
    title VARCHAR(200) NOT NULL,             -- "The Pitt"
    description TEXT,
    poster_url VARCHAR(500),
    banner_url VARCHAR(500),
    genre VARCHAR(50),                       -- "Medical Drama"
    accent_type VARCHAR(50),                 -- "American", "British"
    difficulty VARCHAR(20),                  -- "beginner", "intermediate", "advanced"
    release_year INTEGER,
    total_seasons INTEGER,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Episodes
CREATE TABLE episodes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    show_id UUID REFERENCES shows(id) ON DELETE CASCADE,
    slug VARCHAR(100) NOT NULL,              -- "s01e01-7am"
    season INTEGER NOT NULL,
    episode_number INTEGER NOT NULL,
    title VARCHAR(200) NOT NULL,             -- "7:00 A.M."
    air_date DATE,
    duration_minutes INTEGER,
    summary TEXT,
    context TEXT,                            -- Episode context for learners
    transcript TEXT,                         -- Full transcript (optional)
    created_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(show_id, season, episode_number)
);

-- Vocabulary
CREATE TABLE vocabulary (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    episode_id UUID REFERENCES episodes(id) ON DELETE CASCADE,
    word VARCHAR(100) NOT NULL,
    phonetic VARCHAR(100),
    part_of_speech VARCHAR(50),              -- "noun", "verb", "phrase"
    definition TEXT NOT NULL,
    example_sentence TEXT,                   -- From the episode
    category VARCHAR(50),                    -- "medical", "workplace", "informal"
    difficulty VARCHAR(20),                  -- "easy", "medium", "hard"
    audio_url VARCHAR(500),
    sort_order INTEGER DEFAULT 0
);

-- Grammar Points
CREATE TABLE grammar_points (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    episode_id UUID REFERENCES episodes(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,             -- "Present Continuous for Complaints"
    tag VARCHAR(100),                        -- Short label
    level VARCHAR(50),                       -- "Intermediate"
    pattern VARCHAR(200),                    -- "Subject + BE + ALWAYS + verb-ING"
    explanation TEXT NOT NULL,
    dialogue_example TEXT NOT NULL,          -- The quote from show
    speaker VARCHAR(100),                    -- Who said it
    more_examples JSONB,                     -- Array of additional examples
    sort_order INTEGER DEFAULT 0
);

-- Expressions / Idioms
CREATE TABLE expressions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    episode_id UUID REFERENCES episodes(id) ON DELETE CASCADE,
    quote TEXT NOT NULL,                     -- The expression
    speaker VARCHAR(100),
    meaning TEXT NOT NULL,
    usage_notes TEXT,
    tone VARCHAR(50),                        -- "sarcastic", "formal", "casual"
    sort_order INTEGER DEFAULT 0
);

-- Exercises (flexible schema for different types)
CREATE TABLE exercises (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    episode_id UUID REFERENCES episodes(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL,               -- "fill_blank", "matching", "quiz", "reorder"
    question TEXT NOT NULL,
    options JSONB,                           -- For quiz/matching: [{text, isCorrect}]
    correct_answer TEXT,                     -- For fill_blank
    hint TEXT,
    explanation TEXT,                        -- Shown after answering
    sort_order INTEGER DEFAULT 0
);

-- User Progress (if using auth)
CREATE TABLE user_progress (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
    episode_id UUID REFERENCES episodes(id) ON DELETE CASCADE,
    vocabulary_viewed INTEGER DEFAULT 0,
    grammar_viewed INTEGER DEFAULT 0,
    exercises_completed JSONB DEFAULT '[]',  -- Array of exercise IDs
    quiz_score INTEGER,
    quiz_attempts INTEGER DEFAULT 0,
    completed_at TIMESTAMP,
    last_accessed TIMESTAMP DEFAULT NOW(),
    UNIQUE(user_id, episode_id)
);

-- Indexes for performance
CREATE INDEX idx_episodes_show ON episodes(show_id);
CREATE INDEX idx_vocabulary_episode ON vocabulary(episode_id);
CREATE INDEX idx_grammar_episode ON grammar_points(episode_id);
CREATE INDEX idx_expressions_episode ON expressions(episode_id);
CREATE INDEX idx_exercises_episode ON exercises(episode_id);
CREATE INDEX idx_progress_user ON user_progress(user_id);
```

---

## Project Structure (Next.js)

```
learntv/
├── app/                          # Next.js App Router
│   ├── layout.tsx                # Root layout
│   ├── page.tsx                  # Home (show catalog)
│   ├── shows/
│   │   ├── page.tsx              # All shows grid
│   │   └── [slug]/
│   │       ├── page.tsx          # Show detail (episodes list)
│   │       └── [episode]/
│   │           └── page.tsx      # Episode lesson
│   ├── profile/
│   │   └── page.tsx              # User progress
│   └── api/                      # API routes (if needed)
│       └── progress/
│           └── route.ts
│
├── components/
│   ├── ui/                       # Reusable UI components
│   │   ├── Button.tsx
│   │   ├── Card.tsx
│   │   ├── Progress.tsx
│   │   └── ...
│   ├── shows/
│   │   ├── ShowCard.tsx
│   │   ├── ShowGrid.tsx
│   │   └── EpisodeList.tsx
│   ├── lesson/
│   │   ├── VocabularySection.tsx
│   │   ├── VocabularyCard.tsx
│   │   ├── GrammarSection.tsx
│   │   ├── GrammarCard.tsx
│   │   ├── ExpressionsSection.tsx
│   │   └── ExpressionCard.tsx
│   └── exercises/
│       ├── ExerciseEngine.tsx    # Main exercise controller
│       ├── FillBlank.tsx
│       ├── Matching.tsx
│       ├── Quiz.tsx
│       └── ProgressTracker.tsx
│
├── lib/
│   ├── supabase/
│   │   ├── client.ts             # Supabase client
│   │   ├── server.ts             # Server-side client
│   │   └── types.ts              # Database types
│   ├── hooks/
│   │   ├── useProgress.ts
│   │   └── useExercises.ts
│   └── utils/
│       └── helpers.ts
│
├── types/
│   └── database.ts               # TypeScript types from Supabase
│
├── styles/
│   └── globals.css               # Tailwind + custom styles
│
├── public/
│   └── images/
│       └── shows/                # Show posters
│
├── supabase/
│   ├── migrations/               # Database migrations
│   └── seed.sql                  # Initial data
│
├── tailwind.config.ts
├── next.config.js
└── package.json
```

---

## Development Phases

### Phase 1: Foundation (Week 1)
- [ ] Initialize Next.js project with TypeScript
- [ ] Set up Supabase project
- [ ] Create database schema (migrations)
- [ ] Configure Tailwind with our theme
- [ ] Set up Supabase client
- [ ] Create TypeScript types from database

### Phase 2: Show Catalog (Week 2)
- [ ] Home page with show grid
- [ ] ShowCard component
- [ ] Show detail page with episode list
- [ ] EpisodeCard component
- [ ] Responsive design
- [ ] Search/filter functionality

### Phase 3: Episode Lessons (Week 3)
- [ ] Migrate current lesson design to components
- [ ] VocabularySection (fetch from DB)
- [ ] GrammarSection (fetch from DB)
- [ ] ExpressionsSection (fetch from DB)
- [ ] Dynamic routing for episodes

### Phase 4: Exercises Engine (Week 4)
- [ ] ExerciseEngine component
- [ ] FillBlank exercise type
- [ ] Matching exercise type
- [ ] Quiz exercise type
- [ ] Scoring system
- [ ] Feedback animations

### Phase 5: User System (Week 5)
- [ ] Supabase Auth integration
- [ ] Login/Register pages
- [ ] Profile page with progress
- [ ] Save progress to database
- [ ] Resume where you left off

### Phase 6: Content Management (Week 6)
- [ ] Admin route (protected)
- [ ] Add/edit shows
- [ ] Add/edit episodes
- [ ] Bulk import from JSON
- [ ] Or: Use Supabase Studio directly

### Phase 7: Polish (Week 7+)
- [ ] Loading states & skeletons
- [ ] Error handling
- [ ] SEO optimization
- [ ] Performance optimization
- [ ] Mobile app consideration (PWA)
- [ ] AI-assisted content generation?

---

## Content Pipeline

### How to Add a New Episode

**Option 1: Manual via Supabase Studio**
1. Go to Supabase dashboard
2. Add show (if new)
3. Add episode
4. Add vocabulary items
5. Add grammar points
6. Add expressions
7. Add exercises

**Option 2: JSON Import**
Create a JSON file with episode data and import via script:

```json
{
  "show": "the-pitt",
  "episode": {
    "season": 1,
    "episode_number": 2,
    "title": "8:00 A.M.",
    "summary": "..."
  },
  "vocabulary": [
    {
      "word": "intubate",
      "phonetic": "/ˈɪntjubeɪt/",
      "definition": "Insert a tube into the trachea for breathing",
      "example": "We need to intubate immediately!",
      "category": "medical",
      "difficulty": "hard"
    }
  ],
  "grammar_points": [...],
  "expressions": [...],
  "exercises": [...]
}
```

**Option 3: AI-Assisted (Future)**
1. Paste transcript
2. AI extracts vocabulary, grammar, expressions
3. AI generates exercises
4. Human reviews and approves

---

## Cost Estimate

| Service | Free Tier | Paid (if needed) |
|---------|-----------|------------------|
| Vercel (hosting) | 100GB bandwidth | $20/mo |
| Supabase | 500MB DB, 1GB storage | $25/mo |
| Domain | - | $12/year |
| **Total (free tier)** | **$0/month** | |
| **Total (scaled)** | | **~$45/month** |

---

## Questions to Decide

1. **Auth required?** Should users need to log in, or allow anonymous progress?
2. **Mobile app?** Start as PWA, or plan for native later?
3. **Content source?** Will you manually create content, or want AI assistance?
4. **Multiple languages?** Support Spanish learners + other languages?
5. **Social features?** Leaderboards, sharing progress?

---

## Next Steps

1. **Choose tech stack** (I recommend Next.js + Supabase)
2. **Set up project** (I can help initialize)
3. **Create database** (Run migrations)
4. **Migrate The Pitt Episode 1** data to DB
5. **Build show catalog** UI
6. **Iterate from there**

Ready to start building?
