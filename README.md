# RoozYar (روزیار) — Native Android

Persian, RTL-first, local-first personal life management app. Kotlin + Jetpack Compose (Material 3),
MVVM + Clean Architecture, Room, DataStore, Hilt. The Stitch HTML files in `design-reference/` are
design references only; the app is fully native and uses no WebView.

## Status (Phase 1 — native foundation)

Written, **not yet compiled or run by the author**: no Android SDK/Gradle was reachable in the authoring
environment. The first CI run (or Android Studio sync) is the first real verification.

| Item | State |
|---|---|
| Single-module project, Gradle wrapper 8.11.1, version catalog, KSP | written |
| Dark theme from DESIGN.md tokens, Vazirmatn (4 weights), RTL pinned in theme | written |
| 5-tab navigation (خانه، تقویم، برنامه‌ها، اهداف، پروفایل) | written; tabs other than Home are honest placeholders |
| Room (tasks, user_profile) + DataStore + Hilt wiring | written |
| Jalali calendar core (`core/date`) + Persian digits | written; algorithm cross-checked in Python, JVM unit tests included |
| Home shows real Jalali date and real open-task count from Room | written |
| Shared `ConfirmDialog` (design screen 14) | written |
| GitHub Actions: unit tests + debug APK artifact | written, never run |
| Features (tasks, habits, goals, journal, ...), AI, voice | not started |

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
