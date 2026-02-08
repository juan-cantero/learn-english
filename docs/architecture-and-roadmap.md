# LearnTV Architecture & Roadmap

## Diagrams to Create (Miro)

### 1. System Architecture
- **Frontend**: React + TanStack Router/Query
- **Backend**: Spring Boot (Hexagonal Architecture)
- **Auth**: Supabase Auth (JWT)
- **Database**: PostgreSQL
- **Storage**: Cloudflare R2 (audio files)
- **TTS**: Piper (local audio generation)
- **External APIs**: TMDB, OpenSubtitles, OpenAI

### 2. Hexagonal Architecture (Backend)
```
Bounded Contexts:
├── catalog/       # Shows, episodes browsing
├── learning/      # Lessons, vocabulary, grammar, exercises
├── progress/      # User progress tracking
├── generation/    # AI content generation
├── user/          # User profiles, stats
└── classroom/     # Classrooms, assignments
```

Each context follows:
- **Domain**: Entities, value objects, business logic
- **Application**: Use cases, ports (interfaces)
- **Adapter**: Controllers (in), Repositories (out), External APIs (out)

### 3. User Flows

#### Student Flow
```
Browse Shows → Select Episode → Study Lesson → Complete Exercises → Track Progress
                                    ↓
                           (Optional) Join Classroom → Complete Assignments
```

#### Teacher Flow
```
Upgrade to Teacher → Create Classroom → Share Join Code → Assign Episodes → Monitor Submissions
```

#### Authentication Flow
```
Frontend → Supabase Auth → JWT Token → Backend API → Validate JWT → Create/Get User
```

### 4. Data Model (Entity Relationships)
```
Shows ──< Episodes ──< Lessons
                         ├── Vocabulary
                         ├── Grammar Points
                         ├── Expressions
                         └── Exercises

Users ──< UserProgress (per episode)
  │
  ├──< Classrooms (as teacher)
  │       └──< ClassroomStudents
  │       └──< Assignments ──< Submissions
  │
  └──< ClassroomStudents (as student)
```

---

## Completed Features

- [x] TV show browsing (TMDB integration)
- [x] Episode lesson generation (OpenAI + subtitles)
- [x] Vocabulary, grammar, expressions display
- [x] Interactive exercises (fill-in-blanks, matching, quiz, listening)
- [x] Practice Pronunciation mode
- [x] Audio generation (Piper TTS)
- [x] User authentication (Supabase + JWT)
- [x] Progress tracking per episode
- [x] Classroom system (backend)
- [x] Assignment system (backend)

---

## Potential Next Steps

### High Priority

1. **Classroom UI (Frontend)**
   - Teacher dashboard: create classroom, view students, manage assignments
   - Student view: join classroom, see assigned work
   - Join classroom modal with code input

2. **Assignment UI (Frontend)**
   - Teacher: create assignment (select episode, set due date)
   - Teacher: view submissions with scores
   - Student: assignment list, start/complete flow

3. **Profile Page**
   - Edit display name, avatar
   - View stats (episodes completed, streak)
   - Upgrade to teacher button

### Medium Priority

4. **Protected Routes**
   - Redirect to login for authenticated pages
   - Role-based access (teacher-only pages)

5. **Pull-to-Refresh (Mobile)**
   - Pending task: `10.3 Implementar pull-to-refresh`

6. **Notifications**
   - Assignment due date reminders
   - New assignment notifications for students

### Future Ideas

7. **Gamification**
   - Streaks, badges, leaderboards
   - XP system

8. **Social Features**
   - Comments on lessons
   - Share progress

9. **Spaced Repetition**
   - Review vocabulary at optimal intervals
   - Flashcard mode

10. **Offline Mode**
    - Download lessons for offline study
    - Sync progress when online

---

## Tech Debt / Improvements

- [ ] Add comprehensive tests (unit, integration)
- [ ] Split frontend bundle (code splitting)
- [ ] Add error boundaries
- [ ] Improve loading states
- [ ] Add rate limiting to API
- [ ] Production Supabase setup
