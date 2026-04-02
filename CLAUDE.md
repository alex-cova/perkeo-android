# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

> See `AGENTS.md` for full architecture details, data flows, edge cases, and a quick-checklist for code modifications.

## Build & Test Commands

```bash
./gradlew build                          # Full build
./gradlew :app:testDebugUnitTest         # Unit tests (fast)
./gradlew :app:connectedAndroidTest      # Instrumented tests (requires device/emulator)
./gradlew installDebug                   # Deploy to connected device/emulator
```

**Single test class:**
```bash
./gradlew :app:testDebugUnitTest --tests "com.alexcova.perkeo.domain.DomainParityTest"
```

## Architecture Overview

Single-module Android app (`app/`) with a clean layered architecture:

- **`domain/`** — Pure Kotlin business logic; no Android deps. Engine in `domain/engine/` (analysis, RNG, game structs/enums); utilities in `domain/util/` (seed format, math, generators).
- **`data/`** — Room database, repository implementations, asset loading. `AppGraph.kt` is the manual DI container (no Hilt).
- **`features/`** — One folder per screen: `*ViewModel.kt` (StateFlow), `*UiState.kt` (immutable data class), `*Screen.kt` (Compose). Exception: `CommunityScreen` is stateless.
- **`ui/`** — Material 3 theme, `RootScaffold` (bottom nav + `NavHost`), sprite rendering.
- **`navigation/`** — `AppDestination` enum (4 routes), `AppNavHost` composable that creates remembered ViewModels.
- **`integrations/`** — Dynamic shortcuts, broadcast receiver, home-screen widget.

### Key patterns
- **State**: `MutableStateFlow` private, `StateFlow` public; updates via `.copy()`.
- **Analysis**: Runs on `Dispatchers.Default`; previous job cancelled on new trigger.
- **Seed normalization**: Typed input → filter alphanumeric → take 8 → uppercase (in ViewModel). Clipboard paste → `SeedFormat.isValid()` then `SeedFormat.normalize()`.
- **Sheets/modals**: Controlled by boolean flags in UiState; not separate nav routes.
- **Parity reference**: iOS `Functions.swift` / `Balatro.swift` is the authoritative reference for engine behavior when porting or fixing analysis logic.

### iOS source
The iOS app lives under `ios-app/balatroseeds/` and is the parity reference for the analysis engine and seed format. The Android engine in `domain/engine/` should match its deterministic output.

## Constraints
- Material 3 only — no Material 2 components.
- No `@Preview` composables exist yet; validate UI on emulator/device.
- Check Room migrations (`PerkeoDatabase`) when modifying the saved-seed schema.
- `AnalyzerViewModel` currently lacks a `SeedRepository` reference — save flow is partially wired.
