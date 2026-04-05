# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Krone is a privacy-first, offline-first personal budget tracker for Android. It targets the Danish market with multi-currency support (DKK home currency). GPLv3 licensed. Package: `com.sofato.krone`.

## Build Commands

```bash
# Build debug APK (FOSS flavor)
./gradlew assembleFossDebug

# Build Google Play variant
./gradlew assembleGoogleDebug

# Build shareable APK (release-optimized, debug-signed)
./gradlew assembleFossShare

# Install to connected device
./gradlew installFossDebug

# Run unit tests
./gradlew test

# Run a single test class
./gradlew testFossDebugUnitTest --tests "com.sofato.krone.ExampleUnitTest"

# Run instrumented tests
./gradlew connectedFossDebugAndroidTest
```

Build variants: 2 flavors (`foss`, `google`) x 3 build types (`debug`, `release`, `share`).

## Architecture

Clean Architecture with MVVM. Three layers with strict separation:

- **domain/** — Models, repository interfaces, use cases. Pure Kotlin, no Android dependencies. Use cases are grouped by feature (budget, category, currency, expense, income, insights, onboarding, recurring, savings).
- **data/** — Repository implementations, Room database (entities, DAOs, migrations, mappers), Ktor network client (Frankfurter API for exchange rates), WorkManager workers, DataStore preferences, backup manager.
- **ui/** — Jetpack Compose screens paired 1:1 with ViewModels, type-safe Compose Navigation, Material 3 theme with dynamic color.
- **di/** — Four Hilt modules: DatabaseModule, NetworkModule, RepositoryModule, DataStoreModule.

Data flow: UI (ViewModel) → Use Case → Repository Interface (domain) → Repository Impl (data) → Room/Network.

## Key Technical Details

- **DI:** Hilt. All ViewModels use `@HiltViewModel`. Modules in `di/` package.
- **Database:** Room with schema export enabled (`app/schemas/`). Entities have dedicated mappers to/from domain models. 13 DAOs, 11+ entities. Database seeding in `KroneDatabaseCallback.kt`.
- **Navigation:** Type-safe Compose Navigation using `KroneNavHost` and `KroneDestination` data classes.
- **Networking:** Ktor client with OkHttp engine. Only external API is Frankfurter (ECB exchange rates). Background sync via WorkManager on 24h interval.
- **State management:** StateFlow in ViewModels, collected as Compose state.
- **Serialization:** kotlinx.serialization for JSON. kotlinx.datetime for date/time handling.
- **Build:** Gradle 9.3.1 with Kotlin DSL. Dependencies managed via version catalog (`gradle/libs.versions.toml`). KSP for annotation processing (Room, Hilt).
- **Min SDK:** 30 (Android 11). **Target SDK:** 36.

## Adding a Currency

Add a single entry in the database seed — see `KroneDatabaseCallback.kt`.
