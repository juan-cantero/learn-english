# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

LearnTV is an English learning platform that uses TV shows as source material. Users study vocabulary, grammar, expressions, and complete interactive exercises based on episode content. Currently features a static prototype for "The Pitt" S01E01.

## Current Architecture

**Static HTML/CSS/JS prototype** — no build tools or frameworks required.

```
learn-english/
├── index.html          # Main lesson page
├── css/styles.css      # All styles (CSS variables, components)
├── js/app.js           # Interactive functionality
└── docs/               # Planning documents
```

### Running Locally

Open `index.html` directly in a browser. No server required for the current prototype.

## Code Structure

### CSS (`css/styles.css`)
- CSS custom properties in `:root` for theming (colors, typography, spacing)
- Dark medical theme with crimson accents (`--accent-primary: #dc2626`)
- Fonts: Space Grotesk (display), JetBrains Mono (code/numbers)
- Mobile responsive at 900px and 600px breakpoints

### JavaScript (`js/app.js`)
- Global `state` object manages progress, quiz answers, and matching exercise state
- `localStorage` key: `thepitt-s01e01-progress` for persisting user progress
- Main modules:
  - `initVocabularyFilter()` — category tab filtering
  - `initFillInBlanks()` — text input exercises with answer checking
  - `initMatching()` — term-definition matching game
  - `initQuiz()` — multiple choice quiz with scoring
  - `initProgressTracking()` — IntersectionObserver-based section tracking
- Keyboard shortcuts: 1-4 keys navigate to sections
- Web Speech API for pronunciation (fallback when no audio files)

### HTML Structure
Four main content sections with `data-*` attributes for interactivity:
- `#vocabulary` — vocab cards with `data-category` filtering
- `#grammar` — grammar point cards
- `#expressions` — quote-based expression explanations
- `#exercises` — tabbed exercises (`data-exercise` attribute)

## Planned Migration

Architecture documents outline future migration to a full-stack application:
- **Backend options**: Spring Boot (Java) with hexagonal architecture OR Node.js/Hono
- **Database**: SQLite locally, Supabase/PostgreSQL in production
- **Frontend**: React + TanStack Router + TanStack Query

Key database entities: Shows, Episodes, Vocabulary, GrammarPoints, Expressions, Exercises, UserProgress

## Coding Conventions

- Use CSS custom properties for any new colors or spacing values
- Maintain the dark theme aesthetic with crimson accent colors
- Exercise components use `data-*` attributes for answer validation
- Progress updates should call `updateProgress(category, points)` and persist to localStorage
