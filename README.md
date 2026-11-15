# RoozYar (روزیار) — Native Android

Persian, RTL-first, local-first personal life management app. Kotlin + Jetpack Compose (Material 3),
MVVM + Clean Architecture, Room, DataStore, Hilt. The Stitch HTML files in `design-reference/` are
design references only; the app is fully native and uses no WebView.

## Status (Phase 1 built and installed; Phase 2 step 1 — Tasks — written, not yet built)

Phase 1 compiled on GitHub Actions and installed on a phone. Everything added after that is **not yet compiled**:
the authoring environment has no Android SDK/Gradle, so each new step is first verified by the next CI run.

| Item | State |
|---|---|
| Single-module project, Gradle wrapper 8.11.1, version catalog, KSP | written |
| Dark theme from DESIGN.md tokens, Vazirmatn (4 weights), RTL pinned in theme | written |
| 5-tab navigation (خانه، تقویم، برنامه‌ها، اهداف، پروفایل) | written; tabs other than Home are honest placeholders |
| Room (tasks, user_profile) + DataStore + Hilt wiring | written |
| Jalali calendar core (`core/date`) + Persian digits | written; algorithm cross-checked in Python, JVM unit tests included |
| Home: real Jalali date, today's progress, today's tasks, overdue count (from Room) | written |
| Tasks: list, search, filter, add/edit (Jalali date picker, optional time, priority), complete, delete with confirm | written (Phase 2, step 1) |
| Hub screen: Tasks live, other sections shown as «به‌زودی» | written |
| Room schema v2 (+ migration 1→2: `dueHasTime`) | written |
| Shared `ConfirmDialog` (design screen 14) | written |
| GitHub Actions: unit tests + debug APK artifact | written, never run |
| Task categories, repeat rules, reminders | not started (need DB migration + WorkManager/notification permission) |
| Calendar, habits, goals, journal, profile, planner, focus, XP, timeline, widgets, AI, voice | not started |

## Build

Android Studio (JDK 17, Android SDK 35) → open folder → Sync → Run. Or: `./gradlew testDebugUnitTest assembleDebug`.
CI: push to GitHub; the `Android CI` workflow uploads `roozyar-debug-apk` and test reports.

## Layout

```
app/src/main/java/com/roozyar/app/
  core/{design,common,date,database,datastore,navigation}
  data/repository   domain/{model,repository,usecase}   di   feature/{dashboard,placeholder}
```
UI never touches Room directly: Screen → ViewModel → UseCase → Repository (interface) → DAO.
