# Perkeo: Coding Agent Guide

## Project Overview
**Perkeo** is a multi-platform seed analyzer for the game Balatro. It performs complex combinatorial analysis of game seeds to predict joker drops, card interactions, and optimal game progression across different stakes and decks.

- **Android**: Kotlin + Jetpack Compose (Material 3), Room DB for saved seeds
- **iOS**: SwiftUI + SwiftData (conversion in progress from iOS to Android)
- **Scope**: Seed validation, game state simulation, sprite rendering, persistence

---

## Critical Architecture Patterns

### 1. **Seed Analysis Engine (Cross-Platform Core)**
The Android analysis engine now lives in `domain/engine/BalatroAnalyzer.kt`, backed by `Functions.kt`, `GamePlay.kt`, `GameStructs.kt`, `GameEnums.kt`, and `domain/util/AnalysisMath.kt`. The iOS `perkeo/Balatro.swift` / `Functions.swift` pair is still the parity reference. It:
- Takes seed string + configuration (deck, stake, ante range) 
- Generates `Run` objects containing per-ante analysis (`Ante` class)
- Uses `Functions` class for pseudo-random number generation and item probability lookup
- Configuration parameters: `deck`, `stake`, `maxDepth` (max ante), `startingAnte`, `showman` flag, disabled items list

**Key files:**
- iOS: `Balatro.swift`, `Functions.swift`, `Structs.swift` (JokerData, InstanceParams, Stickers)
- Android: `domain/engine/BalatroAnalyzer.kt`, `domain/engine/Functions.kt`, `domain/engine/Game*.kt`, `domain/util/AnalysisMath.kt`

### 2. **ViewModel State Pattern (Compose/SwiftUI)**
- **AnalyzerViewModel**: Holds game state (seed, ante config, run results) + orchestrates analysis
- State flows through `@Published` (iOS) / `StateFlow` (Android)  
- **Android example** (`features/analyzer/AnalyzerViewModel.kt`):
  - Sanitizes typed seed input immediately in `onSeedInputChanged()` by filtering to 8 alphanumeric chars and uppercasing
  - Triggers analysis from explicit actions (`analyze()`, `randomSeed()`, `seedOfTheDay()`, `paste()`, `changeSeed()`)
  - Updates UI state immutably: `_uiState.update { it.copy(...) }`

### 3. **Data Persistence Layer**
- **Android**: Room DB (`PerkeoDatabase`) → `SeedDao` → `RoomSeedRepository`
- **iOS**: SwiftData (model container in AnalyzerViewModel)
- Saved seeds store: seed, timestamp, optional title, optional level, and optional score
- No network sync—local only

### 4. **Navigation & Tab Structure**
- **Bottom navigation bar** with 4 destinations (Material 3 NavigationBar on Android)
  - Analyzer: Seed input + run display
  - Saved: Persisted seed list
  - Finder: Cache-loading UI for finder assets (`perkeo.jkr`, `canio.jkr`)
  - Community: Static seed grid placeholder
- **Navigation state**: `ui/root/RootScaffold.kt` owns `NavigationBar` + `rememberNavController()`, and `navigation/AppNavHost.kt` creates remembered feature ViewModels from `AppGraph`

---

## Developer Workflows

### Build & Run Android
```bash
# From project root
./gradlew build
./gradlew :app:testDebugUnitTest
./gradlew :app:connectedAndroidTest  # Run instrumented tests
./gradlew installDebug    # Deploy to device/emulator
```
- **Min SDK**: 29, **Target**: 36 (Android 15)
- **Compose compiler**: Kotlin 2.2.10
- **Key dependencies**: Lifecycle, Navigation, Room, DataStore, Retrofit, Coil (images)

### Hot Reload (Compose Preview)
- Compose preview tooling is enabled in `app/build.gradle.kts`, but the current Android screens do **not** define `@Preview` composables
- Expect to validate most UI changes on an emulator/device unless you add a local preview while editing

### iOS Build (Xcode)
```bash
cd ios-app/balatroseeds
xcodebuild -scheme balatroseeds -configuration Release
# Or: open balatroseeds.xcodeproj in Xcode
```

### Seed Format Validation
- Typed input is filtered to the first 8 alphanumeric characters in `features/analyzer/AnalyzerViewModel.kt`
- Utility functions in `domain/util/SeedFormat.kt` (Android) / `SeedFormat.swift` (iOS)
- Android `SeedFormat.normalize()` uppercases and replaces `0` with `O`; `SeedFormat.isValid()` only accepts raw 1–8 character alphanumeric strings
- Validation happens client-side before analysis kicks off

---

## Project-Specific Patterns & Conventions

### 1. **Seed Input Normalization**
**Pattern**: Filter + Normalize + Analyze on explicit action  
- User types → filter non-alphanumeric → take first 8 chars → uppercase in `AnalyzerViewModel.onSeedInputChanged()`
- Clipboard paste uses `SeedFormat.isValid()` and then `SeedFormat.normalize()` (`uppercase()` + `0` → `O`)
- Done in ViewModel, not UI layer—keeps state single source of truth
- Analysis runs when the user accepts the sheet or picks an action from the analyzer top bar; there is no debounce flow in the Android ViewModel
- Example: User types `abcd12!!` → normalized to `ABCD12` (if valid)

### 2. **Feature Folder Structure**
```
features/
├── analyzer/          # Main seed analysis UI
│   ├── AnalyzerViewModel.kt
│   ├── AnalyzerUiState.kt
│   └── AnalyzerScreen.kt
├── finder/           # Cache-backed finder controls
│   ├── FinderViewModel.kt
│   ├── FinderUiState.kt
│   └── FinderScreen.kt
├── saved/            # Local DB list
│   ├── SavedSeedsViewModel.kt
│   ├── SavedSeedsUiState.kt
│   └── SavedSeedsScreen.kt
└── community/        # Static seed gallery
    └── CommunityScreen.kt
```
- **Stateful features** use a dedicated `*ViewModel.kt`; `CommunityScreen.kt` is currently stateless
- **UiState** data class holds all UI state (immutable)
- **Stateful screens** receive a VM as param and collect `uiState` via `collectAsState()`

### 3. **Repository Pattern for Data Access**
- **Abstract interfaces** in `domain/repository/` (e.g., `SeedRepository`, `FinderCacheRepository`)
- **Implementations** in `data/repository/` (e.g., `RoomSeedRepository`, `AssetFinderCacheRepository`)
- **AppGraph** (DI container) wires repos at app startup:
  ```kotlin
  class AppGraph(context: Context) {
      val seedRepository = RoomSeedRepository(db.seedDao())
      val finderCacheRepository = AssetFinderCacheRepository(context)
  }
  ```

### 4. **Joker/Item Metadata from JSON Assets**
- **Android assets**: `app/src/main/assets/jokers.json`, `bosses.json`, `tarots.json`, `vouchers.json`, `tags.json`
- **Finder cache assets**: `app/src/main/assets/perkeo.jkr` and `canio.jkr`
- The JSON assets define sprite positions and item enums (e.g., `CommonJoker.Misprint`)
- **AssetFinderCacheRepository** loads `.jkr` finder caches on demand and memoizes either the legendary-item list or instant-search compressed list in memory
- **Sprite system**: Grid-based sprite sheets with `x,y` lookup → Coil image loading on Android

### 5. **Debounced Analysis**
- Analyzer input is **not** debounced on Android; analysis only starts from explicit actions
- `AnalyzerViewModel.analyze()` cancels the previous `analyzeJob`, sets `isLoading`, and runs `BalatroAnalyzer.performAnalysis()` on `Dispatchers.Default`
- Finder currently loads cache files when a mode switch is enabled; the query field is stored in state but does not trigger filtering yet

### 6. **Sheet/Modal Navigation (Android)**
- **Composer pattern**: Use state bools in ViewModel to trigger sheets
- Example: `showSaveView: Boolean` in uiState → `if (showSaveView) SaveSeedSheet(...)`
- Sheets receive ViewModel for callbacks (save, cancel)
- **No separate nav routes for sheets**—they're modal overlays, not destinations

### 7. **Performance Considerations**
- **Analysis is CPU-heavy**: Offload to background thread in iOS (`DispatchQueue.global(qos: .utility)`), or coroutine in Android
- **Finder cache loading**: `AssetFinderCacheRepository` loads `.jkr` assets on demand and keeps only one cache flavor memoized at a time
- **Sprite rendering**: Coil handles caching; don't load sprites in loops

### 8. **Testing Conventions**
- **Unit tests**: `app/src/test/` (notably `domain/DomainParityTest.kt` for RNG/hash/seed codec determinism)
- **Instrumented tests**: `app/src/androidTest/` (currently scaffolded with `ExampleInstrumentedTest.kt`)
- Compose UI test dependencies are present in `app/build.gradle.kts`, but there are no feature-level Compose tests yet
- There are currently no Android `@Preview` blocks in `app/src/main/java/`

---

## Cross-Component Integration Points

### Data Flow: Seed Analysis
1. User enters a seed from the analyzer input sheet or top-bar actions in **AnalyzerScreen**
2. `AnalyzerViewModel.onSeedInputChanged()` sanitizes typed input; `paste()` validates clipboard text with `SeedFormat.isValid()` and then normalizes it
3. User accepts the sheet or picks an action like random / seed-of-the-day / paste → `AnalyzerViewModel.analyze()`
4. ViewModel cancels any in-flight `analyzeJob` and offloads analysis to `Dispatchers.Default`
5. `BalatroAnalyzer.performAnalysis(...)` returns a `Run`
6. ViewModel updates `_uiState.copy(run = result, isLoading = false, showInput = false)`
7. Screen recomposes and displays `PlayView(run = state.run!!)` plus summary/save sheets when toggled

### Data Flow: Save Seed
1. `SavedSeedsViewModel` subscribes to `seedRepository.observeSavedSeeds()` in `init`
2. `SavedSeedsScreen` currently exposes a sample add action (`saveSeed("IGSPUNF")`) and per-row delete buttons
3. `RoomSeedRepository` maps domain models to `SeedEntity` and upserts/deletes through `SeedDao`
4. Room emits the updated list and `SavedSeedsScreen` recomposes from `SavedSeedsUiState`

### Integration: Finder (Joker Search)
- The current Android Finder is a cache-loader UI, not the full iOS clause builder
- `FinderViewModel` tracks `useLegendarySearch`, `useInstantSearch`, `loadingCache`, cached entry counts, and the raw `query` string
- Enabling one switch disables the other and triggers either `finderCacheRepository.readJokerData()` or `readInstant()`
- The query field currently updates state only; no result filtering/pagination is wired in the Compose Finder screen yet

---

## Key Files Reference

| Layer | File | Purpose |
|-------|------|---------|
| **App Entry** | `PerkeoApp.kt`, `MainActivity.kt` | App class, DI init; Activity entry |
| **Navigation** | `ui/root/RootScaffold.kt`, `navigation/AppNavHost.kt`, `AppDestination.kt` | Bottom-bar shell, route definitions, NavHost |
| **Theme** | `ui/theme/Theme.kt` | Material 3 color scheme, typography |
| **Screens** | `features/*/Screen.kt` | Composable UI per feature |
| **ViewModels** | `features/*/ViewModel.kt` | State, side effects, business logic |
| **Domain** | `domain/engine/*.kt`, `domain/util/*.kt` | Analyzer engine, seed validation, random, math logic |
| **Data/Repo** | `data/repository/*.kt` | DB, asset loading, caching |
| **Database** | `data/local/PerkeoDatabase.kt` | Room schema definition |
| **Integrations** | `integrations/shortcuts/*.kt`, `integrations/widget/PerkeoWidgetProvider.kt` | Dynamic shortcut registration, broadcast receiver, home-screen widget |

---

## Edge Cases & Gotchas

1. **Seed Normalization Is Path-Dependent**: Typed input is filtered/truncated to 8 uppercase alphanumeric chars, but clipboard paste requires the raw text to already satisfy `SeedFormat.isValid()` before `normalize()` runs.
2. **Invalid Seed Display**: If seed is empty, analyzer shows welcome/onboarding UI instead of running analysis.
3. **Concurrent Analysis**: Rapid seed changes cancel the previous `analyzeJob` before starting a new `Dispatchers.Default` analysis.
4. **Sticker Logic**: Jokers can have `eternal`, `perishable`, or `rental` stickers—these are **not** editions, but additional state on `JokerData`.
5. **Deck/Stake Interactions**: Some stakes modify shop rates, sticker odds, card pools—Android now encodes this in `domain/engine/Functions.kt` / `BalatroAnalyzer.kt`; use `ios-app/balatroseeds/perkeo/Functions.swift` as the parity reference when comparing behavior.
6. **Finder Feature Parity**: The Compose Finder screen currently loads `.jkr` cache files and reports counts; the iOS-style JAML / drag-drop matcher is not implemented here yet.
7. **Analyzer Save Flow Is Partially Wired**: `AnalyzerViewModel.saveSeed(...)` needs a `SeedRepository`, but `AppNavHost` currently constructs `AnalyzerViewModel()` without one.
8. **Sprite Atlas**: All joker/card sprites are in single tileset images—coordinates are `x, y` grid positions, not pixel offsets.

---

## For AI Agents: Quick Checklist

When modifying code:
- [ ] Update VM state immutably (use `.copy()` on data classes)
- [ ] Keep analysis off main thread (use `viewModelScope.launch`)
- [ ] Use `onSeedInputChanged()` for typed input and `SeedFormat.isValid()` / `normalize()` for external inputs like clipboard text
- [ ] Test seed normalization with edge cases: `""`, `"abcd12!!"`, `"ABCD12"`, `"ABCD1230"`
- [ ] Update `DomainParityTest.kt` or add focused unit coverage when changing RNG, seed codecs, or analysis math
- [ ] Use Material 3 components (no Material 2 deprecations)
- [ ] Check Room database schema migrations if modifying `PerkeoDatabase`
- [ ] Don't assume previews or finder/save parity already exist on Android—verify the current Compose implementation before porting iOS behavior

