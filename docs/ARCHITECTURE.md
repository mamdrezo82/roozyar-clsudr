# Architecture decisions (Phase 0/1)

- **Single `:app` module** with strict package layers. Reason: 12 GB dev RAM, Compose + Hilt + Room + KSP
  builds are heavy, and fewer modules mean fewer failure points while builds can only run in CI. Split later if needed.
- **Design tokens** come from the frontmatter of `roozyar_life_os_1/DESIGN.md`, the same values the Stitch screens use
  (surface `#0b1326`, primary `#4fdbc8`, primary-container `#14b8a6`, amber secondary `#ffb95f`). The two DESIGN.md files
  and the screens disagree in places; this is the reconciliation.
- **Typography:** Vazirmatn only (Latin glyphs included). Sizes/line heights from DESIGN.md, letter spacing 0.
- **Navigation:** Home, Calendar, Hub (برنامه‌ها), Goals, Profile. Hub is a new screen (not in Stitch) listing
  Tasks, Habits, Journal, Timeline, Achievements, Focus, Widgets, AI.
- **Dates:** pure-Kotlin Jalali converter in `core/date`, verified day-by-day 1990–2100 in unit tests.
- **KSP** instead of kapt for Room and Hilt.
- **Versions** are the set already in the repo (AGP 8.9.2, Kotlin 2.1.20, Compose BOM 2025.03.00, Room 2.7.0, Hilt 2.55,
  KSP 2.1.20-2.0.1). They were not resolved against Maven here; CI will confirm. Upgrade deliberately later.
- **Not claimed in the UI:** encryption, biometrics, NNAPI/NPU. Stitch text claiming them must be removed when screens are rebuilt.

## Database v1
`tasks(id, title, description, dueAtEpochMillis, priority 1..3, status, categoryId, createdAt, updatedAt)`,
`user_profile(id=1, displayName, email?, phone?, showEmailInProfile, showPhoneInProfile)`.
Planned next (phase 2): categories, habits + habit_logs, goals + milestones, journal_entries, focus_sessions, xp_events.
Schema JSON is exported to `app/schemas/` and must be committed.
