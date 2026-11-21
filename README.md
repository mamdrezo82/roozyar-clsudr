# RoozYar (روزیار) — Native Android

Persian, RTL-first, local-first personal life management app. Kotlin + Jetpack Compose (Material 3),
MVVM + Clean Architecture, Room, DataStore, Hilt. The Stitch HTML files in `design-reference/` are
design references only; the app is fully native and uses no WebView.

## Status (phases 1-3 done; CI green; not yet tested on a device by the author)

Every module below compiled and passed its unit tests on GitHub Actions. Behaviour and look on a real phone are
verified only by the product owner installing the APK.

| Feature | State |
|---|---|
| Foundation: theme from DESIGN.md tokens, Vazirmatn, RTL, 5-tab navigation, Room v6 (auto-migrations), DataStore, Hilt | done |
| Jalali calendar core, Persian digits, Jalali date picker | done |
| Home: greeting, today's progress, today's tasks, habits progress, active goals, quick actions | done |
| Tasks: summary, grouped list, search, status/category filters, priority, categories, date + time, reminder, time estimate, link to a goal | done |
| Habits: weekday schedule, daily target, streak, 7-day strip, 30-day adherence, daily reminder | done |
| Goals: categories, priority, deadline, milestones, progress, linked tasks | done |
| Journal: mood, tags, star, drafts, search/filter, streak | done |
| Calendar: Jalali month grid, markers, day agenda, events | done |
| Reminders: tasks and habits via WorkManager + notifications (may arrive a few minutes late when the phone is idle or battery-restricted) | done |
| Local backup: export/restore everything as JSON, validated and atomic | done |
| Focus mode: persisted timer, breaks, sessions linked to task/goal, daily and weekly report | done |
| XP, levels, achievements (derived from stored data; unlock time = when first noticed) | done |
| Life timeline and daily/weekly/monthly reports | done |
| Smart planner: plan a day inside free time, week overview, move overdue tasks, milestone to task | done |
| Profile: real stats, level, edit profile | done |
| Home-screen widget (Glance): today's tasks, habits progress, overdue; refreshes while the app process is alive and every ~30 min | done |
| Dashboard customization: modes (general, exam, internship), card order and visibility | done |
| Repeating tasks (daily / weekly / monthly in Jalali months); completing one creates the next | done |
| Local AI assistant (llama.cpp, memory, action registry) | not started (phase 4) |
| Voice (push-to-talk speech-to-text / text-to-speech) | not started (phase 5) |

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
