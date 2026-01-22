# Review and improve LearnTV UI/UX

**Status:** Completed
**Task ID:** 56541f77-6e15-4220-b6d8-fa4fe321621d
**Created:** 2026-01-21T19:13:04.596Z
**Completed:** 2026-01-21

---

**Agent**: ui-ux-designer

## Tasks
- Navigate to http://localhost:5173
- Review all pages: Home, Show Detail, Lesson, Progress
- Check visual hierarchy, spacing, colors, accessibility
- Provide prioritized list of improvements
- Suggest specific CSS/component changes

---

## Review Results (2026-01-21)

### Critical Issues
1. **Progress page crash** - `snapshot.recentProgress.length` undefined (`progress.tsx:72`)
2. **Touch targets too small** - Speaker buttons ~32px, need 44px (WCAG)
3. **Color contrast** - Secondary text `#a3a3a3` → suggest `#b3b3b3`

### Improvements
4. Inconsistent spacing in show card badges
5. Breadcrumb shows slug "the-pitt" instead of "The Pitt"
6. Missing keyboard focus indicators
7. Progress bar visually heavy
8. Empty states lack visual interest
9. Card hover states need micro-interactions

### Polish
10. Loading skeleton shimmer animations
11. Category badge icons
12. Episode synopsis typography
13. Exercise feedback animations
14. Monospace font weight increase
15. Header logo gradient

### Key Files
- `frontend/src/routes/progress.tsx:72`
- `frontend/src/index.css:13`
- `frontend/src/components/lesson/VocabularyCard.tsx`
- `frontend/src/components/show/ShowCard.tsx`
