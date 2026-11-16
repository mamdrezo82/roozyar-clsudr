# RoozYar (روزیار) — Native Android

Persian, RTL-first, local-first personal life management app. Kotlin + Jetpack Compose (Material 3),
MVVM + Clean Architecture, Room, DataStore, Hilt. The Stitch HTML files in `design-reference/` are
design references only; the app is fully native and uses no WebView.

## Status (Phase 2 complete, CI green; not yet tested on a device by the author)

Every module below compiled and passed its unit tests on GitHub Actions. Behaviour and look on a real
phone are verified only by the product owner installing the APK.

| Feature | State |
|---|---|
| Foundation: theme from DESIGN.md tokens, Vazirmatn, RTL, 5-tab navigation, Room v3 (auto-migrations), DataStore, Hilt | done |
| Jalali calendar core + Persian digits, Jalali date picker | done |
| Home: greeting, today's progress, today's tasks, habits progress, active goals, quick actions | done |
| Tasks: list with summary and grouped sections, search, filters, add/edit/delete, priority, date + optional time, link to a goal | done |
| Habits: schedule by weekday, daily target (counter or check), streak, 7-day strip, 30-day adherence | done |
| Goals: categories, priority, deadline, milestones, progress, complete/reopen, linked tasks | done |
| Journal: entries with mood, tags, star, drafts, search/filter, streak and weekly words | done |
| Calendar: Jalali month grid with markers, day agenda (events, tasks, goal deadlines, habits), events create/edit/delete | done |
| Profile: real stats, edit profile (name, bio, optional email/phone) | done |
| Task categories, repeat rules, reminders/notifications | not started |
| Local backup export/import | not started |
| Smart planner, daily intelligence reports, focus mode, XP/achievements, life timeline, Android widgets | not started (phase 3) |
| Local AI assistant, voice | not started (phases 4-5) |
| Dashboard card customization (order / modes) | not started |

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
