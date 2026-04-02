<p align="center">
  <img src="app/src/main/res/drawable/ic_launcher_foreground.xml" width="0" height="0" />
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="https://img.shields.io/badge/Krone-Personal_Budget_Tracker-006874?style=for-the-badge&labelColor=006874&color=ffffff">
    <img alt="Krone" src="https://img.shields.io/badge/Krone-Personal_Budget_Tracker-006874?style=for-the-badge&labelColor=006874&color=ffffff">
  </picture>
</p>

<h1 align="center">Krone</h1>

<p align="center">
  <b>A privacy-first, offline-first personal budget tracker for Android.</b><br>
  No account. No server. No signup. Your money, your data, your device.
</p>

<p align="center">
  <a href="https://www.gnu.org/licenses/gpl-3.0"><img src="https://img.shields.io/badge/License-GPLv3-blue.svg?style=flat-square" alt="License: GPLv3"></a>
  <a href="#"><img src="https://img.shields.io/badge/Min_SDK-30_(Android_11)-green?style=flat-square" alt="Min SDK 30"></a>
  <a href="#"><img src="https://img.shields.io/badge/Target_SDK-36-green?style=flat-square" alt="Target SDK 36"></a>
  <a href="#"><img src="https://img.shields.io/badge/Kotlin-2.2-7F52FF?style=flat-square&logo=kotlin&logoColor=white" alt="Kotlin"></a>
  <a href="#"><img src="https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white" alt="Compose"></a>
</p>

---

## Why Krone?

The budgeting app space is full of apps that are paywalled, ugly, over-engineered, or abandoned. Krone is different:

- **Completely offline-first** --- works on day one with zero internet
- **Open source (GPLv3)** --- hackable, auditable, trustworthy
- **Multi-currency native** --- DKK home currency with first-class EUR, USD, NZD support and live exchange rates
- **Savings-aware** --- treats savings goals as first-class citizens, not an afterthought
- **Actually polished** --- Material 3 with dynamic color, fluid animations, edge-to-edge design
- **Denmark-aware** --- understands Danish expense patterns (A-kasse, SU, betalingsservice, typical Danish cost structures)

Everything is free. Forever.

## Features

| Feature | Description |
|---|---|
| **Daily budget** | "You can spend X kr today" --- the number that matters, front and center |
| **Quick-add expenses** | Amount + category in 3 taps. One-handed use. |
| **Multi-currency** | Log expenses in any currency; auto-converts to home currency at that day's rate |
| **Savings buckets** | Emergency fund, ASK, pension, custom goals --- all tracked with progress arcs |
| **Budget overview** | Income vs. fixed costs vs. savings vs. discretionary at a glance |
| **Recurring expenses** | Set once, auto-posts monthly. Edit only when things change. |
| **Insights & analytics** | Spending heatmaps, category breakdowns, trend lines, streak tracking |
| **Guided onboarding** | Income -> fixed costs -> savings -> daily budget. Under 2 minutes. |
| **Backup & restore** | Export/import your data. Your data is yours. |
| **Live exchange rates** | Daily rates from Frankfurter API (ECB data), cached locally |

<!-- 
## Screenshots

TODO: Add screenshots
<p align="center">
  <img src="docs/screenshots/dashboard.png" width="200" />
  <img src="docs/screenshots/budget.png" width="200" />
  <img src="docs/screenshots/savings.png" width="200" />
  <img src="docs/screenshots/insights.png" width="200" />
</p>
-->

## Architecture

Krone follows **Clean Architecture** with the MVVM pattern:

```
app/
├── data/           # Room database, DAOs, repositories, network, DataStore
├── domain/         # Models, use cases, repository interfaces
├── ui/             # Compose screens, ViewModels, theme, navigation
├── di/             # Hilt dependency injection modules
└── util/           # Extensions and formatters
```

**Key decisions:**
- All data lives in a local Room (SQLite) database --- no cloud dependency
- Repository pattern with domain interfaces, data implementations
- Hilt for dependency injection across the app
- Ktor for lightweight exchange rate fetching
- WorkManager for background rate sync (24h interval)
- DataStore for user preferences

## Tech Stack

| | |
|---|---|
| **Language** | Kotlin 2.2 |
| **UI** | Jetpack Compose + Material 3 |
| **Database** | Room 2.8 |
| **DI** | Hilt / Dagger |
| **Networking** | Ktor 3.1 (OkHttp engine) |
| **Navigation** | Compose Navigation (type-safe) |
| **Async** | Kotlin Coroutines + Flow |
| **Background** | WorkManager |
| **Preferences** | DataStore |
| **Serialization** | kotlinx.serialization |
| **Build** | Gradle with Kotlin DSL + version catalog |

## Build

### Requirements

- Android Studio Ladybug or later
- JDK 11+
- Android SDK 36

### Build variants

Krone has two **product flavors** and three **build types**:

| Flavor | Description |
|---|---|
| `foss` | Fully open-source variant, no proprietary dependencies |
| `google` | Google Play variant (may include Google-specific integrations) |

| Build type | Description |
|---|---|
| `debug` | Development builds with debug tooling |
| `release` | Minified production builds |
| `share` | Release-optimized but signed with the standard debug keystore --- anyone can build and share test APKs without a custom signing setup |

### Building from source

```bash
# Clone the repo
git clone https://github.com/tommcamm/krone.git
cd krone

# Build a debug APK (FOSS flavor)
./gradlew assembleFossDebug

# Build a shareable APK (signed with the common debug keystore)
./gradlew assembleFossShare

# Install directly to a connected device
./gradlew installFossDebug
```

The `share` build type uses the standard Android debug keystore (`~/.android/debug.keystore`) that every developer already has --- no additional signing setup needed.

## Contributing

Contributions are welcome! Whether it's bug fixes, new features, translations, or documentation.

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/my-feature`)
3. Make your changes
4. Run the tests (`./gradlew test`)
5. Submit a pull request

### Adding a currency

The currency system is an open registry. Adding a new currency is a single entry in the database seed --- see `KroneDatabaseCallback.kt`. PRs for new currencies are always welcome.

## License

Krone is licensed under the **GNU General Public License v3.0**.

See [LICENSE](LICENSE) for the full text.

```
Copyright (C) 2026 Krone contributors

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
```
