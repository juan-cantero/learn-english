# LearnTV - Next Tasks for Vibe Kanban

## Context

We've set up 4 custom agents globally and configured Vibe Kanban MCP server. After restarting Claude Code, we need to create tasks in Vibe Kanban for each agent.

## Agents Created

Located in `~/.claude/agents/`:

1. **ui-ux-designer** - UI/UX review specialist with Playwright for visual inspection
2. **frontend-developer** - React/TypeScript frontend implementation
3. **software-architect** - System design and API integration planning (uses Opus)
4. **qa-tester** - Manual testing with Playwright MCP

## Tasks to Create in Vibe Kanban

### Task 1: UI/UX Review
- **Assignee**: `ui-ux-designer` agent
- **Title**: Review and improve LearnTV UI/UX
- **Description**:
  - Navigate to http://localhost:5173
  - Review all pages: Home, Show Detail, Lesson, Progress
  - Check visual hierarchy, spacing, colors, accessibility
  - Provide prioritized list of improvements
  - Suggest specific CSS/component changes

### Task 2: Listening Exercise Feature
- **Assignee**: `frontend-developer` agent
- **Title**: Implement listening exercise with audio playback
- **Description**:
  - Create `ListeningExercise.tsx` component in `frontend/src/components/exercises/`
  - Add audio playback (Web Audio API or HTML5 audio)
  - User hears audio, types what they heard
  - Check answer via API
  - Handle loading/error states for audio
  - Update `ExerciseSection.tsx` to include LISTENING type

### Task 3: Episode Script API Integration Design
- **Assignee**: `software-architect` agent
- **Title**: Design episode script fetching and content generation system
- **Description**:
  - Research APIs for TV show scripts/transcripts (OpenSubtitles, etc.)
  - Design the integration architecture:
    - How user selects a show/episode
    - API call to fetch script
    - Processing pipeline to extract vocabulary, grammar, expressions
    - "Generate Episode" button flow
  - Consider: rate limits, caching, error handling
  - Output: Architecture decision record with implementation plan

### Task 4: End-to-End Testing
- **Assignee**: `qa-tester` agent
- **Title**: E2E testing of LearnTV application
- **Description**:
  - Test environment: Frontend http://localhost:5173, Backend http://localhost:8080
  - Test flows:
    1. Browse shows catalog
    2. Navigate to show detail
    3. Open lesson page
    4. Complete vocabulary/grammar/expressions sections
    5. Do each exercise type (fill-in-blank, multiple choice, matching)
    6. Check progress updates
    7. Visit progress dashboard
  - Report bugs with screenshots and steps to reproduce

## Project Paths

- **Frontend**: `/home/juanqui/learn-english/frontend`
- **Backend**: `/home/juanqui/learn-english/backend`
- **Root**: `/home/juanqui/learn-english`

## Prerequisites

Before starting tasks:
1. Backend running: `cd backend && ./gradlew bootRun`
2. Frontend running: `cd frontend && npm run dev`
3. PostgreSQL Docker: `docker ps` (should show learntv-db)

## Vibe Kanban Commands

After restart, use the Vibe Kanban MCP tools to:
1. List/create project for LearnTV
2. Create the 4 tasks above
3. Assign each task to start workspace sessions with the appropriate agent
