# Phase 1 — Native foundation

## Implemented in this delivery

1. Android application module and Gradle Kotlin DSL project configuration.
2. Compose + Material 3 theme using the second Design System's Persian-first color tokens.
3. RTL root composition.
4. Main Navigation Compose graph for Home, Planner, Calendar, Goals and Profile.
5. Reserved `tasks` route, deliberately not added to the initial bottom bar.
6. Room entities and DAOs for tasks and the local user profile.
7. DataStore preferences wrapper.
8. Hilt application bootstrap and providers for Room and DataStore.
9. Initial native Dashboard shell.

## Data decisions

`UserProfileEntity.email` and `phone` are nullable. `showEmailInProfile` and `showPhoneInProfile` are independent flags. The fields are local database data only.

`TaskEntity` is independent of UI navigation. It has due date, priority, status and timestamps, and can be consumed by Dashboard and Smart Planner before a dedicated Tasks feature is introduced.

## Known gaps / next work

- Stitch archive does not contain font binaries. Add licensed Vazirmatn, Plus Jakarta Sans and JetBrains Mono files under `app/src/main/res/font/`; until then Android system sans-serif is used as fallback.
- The two design files contain conflicting surface values and differing accent descriptions. The current initial tokens follow Design System 2 for Persian-first product colors; a full token audit should reconcile the remaining values before screen-by-screen reconstruction.
- Calendar/Jalali conversion, task repository/use cases, profile UI, database migrations and UI tests are not yet implemented.
- No APK has been built or tested in this environment because Gradle and Android SDK are absent.
