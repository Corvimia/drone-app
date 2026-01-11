# Repository Guidelines

## Project Structure & Module Organization
- `app/` holds the Android application module.
- `app/src/main/java/dev/girlz/drone_app/` contains Kotlin source code (Compose UI, data, and view models).
- `app/src/main/res/` contains Android resources (layouts, strings, themes, icons).
- `app/src/test/` contains local unit tests; `app/src/androidTest/` contains instrumentation tests.
- `gradle/` and `build.gradle.kts` manage build configuration and dependency versions.

## Build, Test, and Development Commands
- `./gradlew :app:assembleDebug` builds a debug APK.
- `./gradlew :app:test` runs local JVM unit tests.
- `./gradlew :app:connectedAndroidTest` runs instrumentation tests on a connected device/emulator.
- `./gradlew :app:lint` runs Android Lint checks.

## Coding Style & Naming Conventions
- Kotlin code uses 4-space indentation.
- Prefer `CamelCase` for classes and `lowerCamelCase` for functions/variables.
- Compose screens should be in `ui/` with `FeatureScreen` naming (e.g., `NoiseScreen`).
- Keep imports explicit and organized by Android/Compose/local packages.

## Testing Guidelines
- Unit tests live in `app/src/test` using JUnit.
- Instrumentation tests live in `app/src/androidTest`.
- Name tests with the `*Test` suffix and keep test methods descriptive.

## Commit & Pull Request Guidelines
- Use Conventional Commits (e.g., `feat: add noise tab`, `fix: handle null settings`).
- Base branch is `main`, and the remote is `origin`.
- Branch names should follow Conventional Commits (e.g., `feat/noise-tab`).
- Keep PRs focused and include a clear description of changes and any UX impact.
- Add screenshots for UI changes when feasible.

## Agent Coordination Notes
- When asked to "go back to main", check out `main` and pull the latest from `origin`.
- When asked to "start a new task", go back to `main`, ask for the task, create a Conventional Commit branch, then begin.
- When asked to "raise the pr", commit, push, and create a PR with a description generated from the changes.
- When asked to "commit and raise pr", include all modified files in the commit.
- When asked to "[remember]" something, add it to this file.
- If we update the DB schema, update the seed script to make sure it still works.
- Always number questions so they are easy to answer.
