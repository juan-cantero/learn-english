# Vibe Kanban Setup - Context for New Session

## Status
- Vibe Kanban is running at: http://localhost:34519
- MCP configured in: ~/.claude/settings.local.json
- Claude Code needs MCP tools to create tasks

## What to Do in New Session

Ask Claude: "Read VIBE_KANBAN_SETUP.md and NEXT_TASKS.md, then create the 4 tasks in Vibe Kanban"

## Tasks to Create

### 1. UI/UX Review
- **Agent**: ui-ux-designer
- **Description**: Review http://localhost:5173, check visual hierarchy, spacing, colors, accessibility. Provide prioritized improvements.

### 2. Listening Exercise Feature
- **Agent**: frontend-developer
- **Description**: Create ListeningExercise.tsx with audio playback, answer checking, integrate into ExerciseSection.

### 3. Episode Script API Design
- **Agent**: software-architect
- **Description**: Design system to fetch TV scripts and generate lesson content (vocabulary, grammar, expressions).

### 4. E2E Testing
- **Agent**: qa-tester
- **Description**: Test full flows at localhost:5173 (frontend) and localhost:8080 (backend).

## Project Path
`/home/juanqui/learn-english`

## Prerequisites Before Tasks
1. Backend: `cd backend && ./gradlew bootRun`
2. Frontend: `cd frontend && npm run dev`
3. PostgreSQL Docker running
