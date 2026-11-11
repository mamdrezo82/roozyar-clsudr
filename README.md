# RoozYar — Native Android

RoozYar is a Persian, RTL-first, local-first personal life management app. The Android client is being rebuilt natively with Kotlin and Jetpack Compose. The Stitch HTML files are retained only as design references; they are not loaded in a WebView.

## Current implementation status

- [x] Native Android application skeleton
- [x] Kotlin + Jetpack Compose + Material 3 foundation
- [x] Dark palette tokens based on the Persian Design System
- [x] RTL root layout
- [x] Navigation Compose graph with reserved `tasks` route
- [x] Room database foundation (`TaskEntity`, `UserProfileEntity`)
- [x] DataStore preferences foundation
- [x] Hilt application and database/preferences providers
- [ ] Bundled Vazirmatn / Plus Jakarta Sans / JetBrains Mono font assets
- [ ] Full Stitch screen reconstruction
- [ ] Functional Tasks, Calendar, Habits, Goals and Journal features
- [ ] Android build and device verification

## Product decisions

- Email and phone are optional profile fields, stored only on-device. Their visibility is independently controlled by the user. No account or server dependency is introduced.
- Task data is modeled independently from screens. The first release can surface task operations from Dashboard and Smart Planner; the dedicated Tasks screen is planned for phase two. The `tasks` navigation route is reserved now.
- Local-first only: no Firebase, cloud sync, Supabase, mandatory sign-in, or external personal-data storage.

## Build environment

The project is configured for JDK 17 bytecode and Android SDK 35. The current work container has Java 21 but does not have Gradle, Android SDK, ADB, or an emulator installed, so an Android Gradle build cannot be run or certified in this environment yet. Open the project in Android Studio with a compatible Android SDK and allow Gradle sync before building.

## Design references

`design-reference/stitch_roozyar_android_app_design/` contains the original Stitch HTML, PNG screenshots and both DESIGN.md files. These are source references only. The native app does not use WebView.
