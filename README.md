# Perkeo — Balatro Seed Analyzer (Android)

Perkeo is an Android companion app for [Balatro](https://www.playbalatro.com/) that analyzes game seeds to predict joker drops, card interactions, and optimal run progression across any deck, stake, and ante range.

## Features

- **Seed analysis** — Enter any 8-character seed to simulate joker shop drops, vouchers, bosses, tags, and packs across all antes
- **Joker finder** — Search the legendary and instant-match joker cache to locate seeds with specific jokers
- **Saved seeds** — Persist seeds locally with an optional title, score, and difficulty level
- **Community seeds** — Browse a curated grid of notable seeds
- **Seed of the day / random seed** — Quick-access actions from the analyzer toolbar
- **Home-screen widget & dynamic shortcuts** — Launch a random seed or open the analyzer directly from the launcher

## Requirements

- Android 10+ (API 29)
- A connected device or emulator for instrumented tests

## Build & Run

```bash
# Full build
./gradlew build

# Install on connected device / emulator
./gradlew installDebug

# Unit tests (no device needed)
./gradlew :app:testDebugUnitTest

# Instrumented tests (requires device/emulator)
./gradlew :app:connectedAndroidTest

# Single test class
./gradlew :app:testDebugUnitTest --tests "com.alexcova.perkeo.domain.DomainParityTest"
```

## Architecture

Single-module app (`app/`) with a clean layered architecture:

```
app/src/main/java/com/alexcova/perkeo/
├── domain/
│   ├── engine/          # Pure Kotlin analysis engine (BalatroAnalyzer, Functions, GameStructs…)
│   └── util/            # Seed format, math helpers, RNG, daily/random generators
├── data/
│   ├── local/           # Room database, DAOs, entities
│   ├── repository/      # Repository implementations
│   └── mapper/          # Domain ↔ entity mappers
├── features/
│   ├── analyzer/        # Main seed input + run display
│   ├── finder/          # Joker cache search
│   ├── saved/           # Saved seeds list
│   └── community/       # Community seed gallery (stateless)
├── ui/
│   ├── theme/           # Material 3 color scheme & typography
│   ├── components/      # Shared composables (AnimatedTitle, TribouleteView…)
│   ├── sprite/          # Sprite sheet rendering via Coil
│   └── root/            # RootScaffold — bottom nav + NavHost shell
├── navigation/          # AppDestination enum, AppNavHost
└── integrations/        # Widget, dynamic shortcuts, broadcast receiver
```

**Key patterns:**
- State: `MutableStateFlow` (private) / `StateFlow` (public), updated via `.copy()`
- Analysis runs on `Dispatchers.Default`; previous job is cancelled on new trigger
- Manual DI via `AppGraph` — no Hilt
- Sheets/modals controlled by boolean flags in `UiState`, not separate nav routes

## Seed Format

Seeds are 1–8 uppercase alphanumeric characters. The app normalizes input automatically:
- Typed input: filters non-alphanumeric characters, takes the first 8, uppercases
- Clipboard paste: validated with `SeedFormat.isValid()` then normalized (`0` → `O`, uppercase)

## Tech Stack

| | |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Navigation | Navigation Compose |
| Persistence | Room + DataStore |
| Image loading | Coil |
| Serialization | kotlinx.serialization |
| Min SDK | API 29 (Android 10) |
| Target SDK | API 36 (Android 15) |
