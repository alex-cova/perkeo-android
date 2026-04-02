# Perkeo iOS → Android (Jetpack Compose) Conversion Plan

## Scope
Convert the iOS app under `ios-app/` to Android using Kotlin + Jetpack Compose (Material 3 only), preserving behavior, interactions, and visual hierarchy as closely as possible.

---

## 1) Swift file inventory and Kotlin/Compose mapping

### App entry and app shell
| iOS file | Current responsibility | Android target |
|---|---|---|
| `ios-app/balatroseeds/balatroseedsApp.swift` | App entry, root environment injection (`AnalyzerViewModel`, `JokerFile`), SwiftData container wiring | `app/src/main/java/com/alexcova/perkeo/PerkeoApp.kt` (Application + Hilt), `MainActivity.kt`, `navigation/AppNavHost.kt` |
| `ios-app/balatroseeds/ContentView.swift` | Root tab structure, toolbar/sheets, tab variant for iOS17/18 | `ui/root/RootScaffold.kt` + `navigation/AppNavHost.kt` + `components/RootTopBar.kt` |
| `ios-app/balatroseeds/LookAndFeel.swift` | Global UIKit appearance overrides and typography setup | `ui/theme/Theme.kt`, `ui/theme/Type.kt`, `ui/theme/Shape.kt`, and component-level defaults |

### View models and state holders
| iOS file | Current responsibility | Android target |
|---|---|---|
| `ios-app/balatroseeds/AnalyzerViewModel.swift` | Main state and actions, analysis orchestration, debounce, persistence actions | `features/analyzer/AnalyzerViewModel.kt` + `features/analyzer/AnalyzerUiState.kt` |
| `ios-app/balatroseeds/views/PiFreak.swift` (contains `JAMLViewModel`) | Finder clause state (`must/should/mustNot`), drag/drop transitions | `features/finder/FinderViewModel.kt` + `features/finder/FinderClauseState.kt` |
| `ios-app/balatroseeds/perkeo/Perkeo.swift` (`ItemEdition`, stateful wrappers) | Item wrappers + mutable edition state | `domain/model/ItemEdition.kt` (immutable data), mutable selection handled in VM state |

### Screens (SwiftUI views)
| iOS file | Current responsibility | Android target |
|---|---|---|
| `ios-app/balatroseeds/views/AnalyzerView.swift` | Analyzer home and initial empty state | `features/analyzer/AnalyzerScreen.kt` |
| `ios-app/balatroseeds/views/FinderView.swift` | Seed finder screen, search controls, drag/drop zones, result list | `features/finder/FinderScreen.kt` |
| `ios-app/balatroseeds/views/SavedSeedsView.swift` | Persisted seed list and navigation to play details | `features/saved/SavedSeedsScreen.kt` |
| `ios-app/balatroseeds/views/CommunityView.swift` | Community seeds grid and navigation | `features/community/CommunityScreen.kt` |
| `ios-app/balatroseeds/views/ResumeView.swift` | Run summary/stats and animated rows | `features/summary/RunSummaryScreen.kt` or `RunSummarySheet.kt` |

### Reusable UI components
| iOS file | Current responsibility | Android target |
|---|---|---|
| `ios-app/balatroseeds/components/Components.swift` | `TabItem`, custom interactive tab bar, shared labels/render helpers | `ui/components/InteractiveBottomBar.kt`, `navigation/AppDestination.kt`, `ui/components/CommonUi.kt` |
| `ios-app/balatroseeds/components/PlayView.swift` | Ante/pack/shop rendering | `features/analyzer/components/PlayView.kt` |
| `ios-app/balatroseeds/components/ConfigView.swift` | Config sheet, ante/deck/stake/voucher/joker filters | `features/config/ConfigSheet.kt` |
| `ios-app/balatroseeds/components/SeedInput.swift` | Seed input modal | `features/analyzer/components/SeedInputSheet.kt` |
| `ios-app/balatroseeds/components/SaveSeedView.swift` | Save metadata modal | `features/saved/components/SaveSeedSheet.kt` |
| `ios-app/balatroseeds/components/JokerSelectorView.swift` | Searchable joker selector and drag sources | `features/finder/components/JokerSelectorSheet.kt` |
| `ios-app/balatroseeds/components/ToastView.swift` | App-level toast | `ui/components/AppSnackbarHost.kt` (or custom toast composable) |
| `ios-app/balatroseeds/components/LoaderView.swift` | Overlay loading indicator | `ui/components/LoadingOverlay.kt` |
| `ios-app/balatroseeds/components/InfiniteScrollView.swift` | Animated infinite scroll behavior | `ui/components/InfiniteCarousel.kt` |
| `ios-app/balatroseeds/components/SpriteView.swift` | Sprite clipping and drawing | `ui/components/SpriteView.kt` |
| `ios-app/balatroseeds/components/Sprite.swift` | Sprite metadata loading | `data/sprite/SpriteRepository.kt` + `data/sprite/SpriteMeta.kt` |
| `ios-app/balatroseeds/components/Extensions.swift` | Font/color/string/item extensions | split into `ui/theme/*`, `domain/util/SeedUtils.kt`, `ui/sprite/ItemSpriteResolver.kt` |

### Domain, engine, and algorithm layer
| iOS file | Current responsibility | Android target |
|---|---|---|
| `ios-app/balatroseeds/perkeo/Enums.swift` | Core game enums/protocols/item taxonomy | `domain/model/GameEnums.kt` + `domain/model/Item.kt` |
| `ios-app/balatroseeds/perkeo/Structs.swift` | Core data structures and wrappers | `domain/model/GameStructs.kt` |
| `ios-app/balatroseeds/perkeo/Play.swift` | `Run`, `Ante`, shop/pack querying helpers | `domain/model/Run.kt` |
| `ios-app/balatroseeds/perkeo/RunScorer.swift` | Run score calculation | `domain/engine/RunScorer.kt` |
| `ios-app/balatroseeds/perkeo/Balatro.swift` | High-level analysis orchestration | `domain/engine/BalatroAnalyzer.kt` |
| `ios-app/balatroseeds/perkeo/Functions.swift` | Core deterministic generator functions and lock mechanics | `domain/engine/Functions.kt` |
| `ios-app/balatroseeds/perkeo/Lock.swift` | Locking/unlocking constraints | `domain/engine/LockState.kt` |
| `ios-app/balatroseeds/perkeo/Cache.swift` | Cached node values | `domain/engine/Cache.kt` |
| `ios-app/balatroseeds/perkeo/Util.swift` | Numeric and helper utilities | `domain/util/AnalysisMath.kt` |
| `ios-app/balatroseeds/perkeo/LuaRandom.swift` | Lua-like RNG behavior | `domain/util/LuaRandom.kt` |
| `ios-app/balatroseeds/perkeo/Seed32bit.swift` | Seed encoding/decoding | `domain/util/Seed32Bit.kt` |
| `ios-app/balatroseeds/perkeo/Perkeo.swift` | Draggable item models, compressed file readers/search cache | `domain/model/DraggableItem.kt`, `data/cache/JokerFileRepository.kt` |

### Data and persistence
| iOS file | Current responsibility | Android target |
|---|---|---|
| `ios-app/balatroseeds/SeedModel.swift` | SwiftData entity for saved seeds | `data/local/entity/SeedEntity.kt` + `SeedDao.kt` + `PerkeoDatabase.kt` |

### Intents/widget/platform integration
| iOS file | Current responsibility | Android target |
|---|---|---|
| `ios-app/balatroseeds/Intents.swift` | App Shortcuts intent (random seed copy) | `shortcuts/PerkeoShortcuts.kt` (ShortcutManager + intent receiver/activity alias) |
| `ios-app/PerkeoWidget/PerkeoWidget.swift` | Widget timeline + rendering | `widget/PerkeoWidgetProvider.kt` (Glance/AppWidget) |
| `ios-app/PerkeoWidget/PerkeoWidgetBundle.swift` | Widget bundle declaration | Android manifest receiver/provider entries + widget metadata XML |

### Tests
| iOS file | Current responsibility | Android target |
|---|---|---|
| `ios-app/balatroseedsTests/balatroseedsTests.swift` | Engine/randomness tests | `app/src/test/.../domain/*Test.kt` |
| `ios-app/balatroseedsUITests/balatroseedsUITests.swift` | Basic UI tests | `app/src/androidTest/.../*Test.kt` (Compose UI tests) |
| `ios-app/balatroseedsUITests/balatroseedsUITestsLaunchTests.swift` | Launch/perf tests | Android startup benchmark (optional module) or smoke launch test |

---

## 2) iOS dependencies/frameworks → Android replacements

| iOS dependency/framework | Usage in iOS app | Android replacement |
|---|---|---|
| SwiftUI | Entire UI layer | Jetpack Compose Material 3 |
| Combine (`@Published`, debounce) | ViewModel reactive updates | Kotlin Coroutines + `StateFlow`/`SharedFlow` + Flow operators |
| SwiftData (`@Model`, `@Query`, `ModelContainer`) | Saved seeds persistence | Room (`@Entity`, `@Dao`, `RoomDatabase`) + Flow |
| AppIntents | Random-seed shortcut intent | ShortcutManager + deep link/activity intent handling |
| WidgetKit | Home screen widget | Glance AppWidget / AppWidgetProvider |
| CryptoKit (SHA256) | Daily seed generation | `MessageDigest` SHA-256 or Kotlin crypto lib |
| UIKit pasteboard | Copy/paste seed | Android `ClipboardManager` |
| UIKit haptics | Button/search feedback | `HapticFeedbackType` / `Vibrator` APIs |
| UIKit share sheet | Sharing run data | Android Sharesheet (`Intent.ACTION_SEND`) |
| UIKit appearance APIs | Global visual tuning | Material 3 theme + per-component styling |
| Transferable drag/drop | Finder item DnD | Compose drag-and-drop (`dragAndDropSource`/`dragAndDropTarget`) + VM state routing |
| InputStream binary reads | `.jkr` loading | `InputStream`/`BufferedInputStream` on Android assets/files |

### Required Android libraries (latest stable at implementation time)
- `androidx.navigation:navigation-compose`
- `io.coil-kt:coil-compose`
- `com.squareup.retrofit2:retrofit`
- `com.google.dagger:hilt-android`
- `androidx.hilt:hilt-navigation-compose`
- `androidx.room:room-runtime`
- `androidx.room:room-compiler` (KSP)
- `androidx.datastore:datastore-preferences`
- `org.jetbrains.kotlinx:kotlinx-serialization-json`
- `androidx.lifecycle:lifecycle-viewmodel-compose`
- `androidx.lifecycle:lifecycle-runtime-compose`

---

## 3) Architecture decisions and platform-specific adaptations

## 3.1 State management decisions
- `@State` → local `remember { mutableStateOf(...) }`.
- `@StateObject` / `@ObservedObject` → `@HiltViewModel` + immutable `UiState` exposed via `StateFlow`.
- `@EnvironmentObject` global objects (`AnalyzerViewModel`, `JokerFile`) → DI-scoped repositories + VM injection through `hiltViewModel()`.
- `@Binding` → state hoisting (`value`, `onValueChange`) in composables.
- `@Published` flags/events (`toast`, `copyEvent`, sheet flags) → `StateFlow` for state + `SharedFlow` for one-off events.
- Combine debounce pipeline on seed typing → Flow chain: `debounce(500).map/transform`.

## 3.2 Navigation and modal decisions
- `NavigationView/NavigationLink` → `NavHost` + typed route model.
- `TabView` + custom tab behavior → `Scaffold` + custom M3 bottom bar composable.
- `.sheet` modals (`Config`, `SeedInput`, `SaveSeed`, `Summary`) → `ModalBottomSheet` or dialog destinations.
- `dismiss` semantics → `navController.popBackStack()`.

## 3.3 Domain layer decisions
- Keep full deterministic engine parity by porting algorithm classes first:
  `LuaRandom` → `Util` → `Lock/Cache` → `Functions` → `Balatro` → `RunScorer`.
- Preserve enum identifiers and ordering semantics (`rawValue`, `ordinal`, `index`) to avoid behavior drift.
- Replace protocol-heavy Swift polymorphism with Kotlin interfaces + sealed hierarchies where needed.

## 3.4 Persistence and local data decisions
- SwiftData saved seeds model maps to Room schema:
  - `seed` (PK or unique index)
  - `timestamp`
  - optional `title`, `level`, `score`
- Finder cache files (`perkeo.jkr`, `canio.jkr`) handled through repository abstraction:
  - source from app assets / files
  - decode on `Dispatchers.IO`
  - expose results as Flow state for finder screens.
- Settings toggles (where persisted) go to DataStore preferences.

## 3.5 UI adaptation decisions
- Material 3 only; no Material 2 components.
- Custom palette/typography from `Extensions.swift` and `LookAndFeel.swift` mapped into `ColorScheme` + custom `Typography`.
- Preserve dark background and red-accent design language.
- Sprite rendering implemented as cached bitmap region extraction for smooth scrolling lists.
- Existing iOS animations (`repeatForever`, transitions, scroll effects) approximated with Compose animation APIs while preserving user intent and visual hierarchy.

## 3.6 Platform adaptations
- iOS haptics and clipboard actions replaced with Android equivalents.
- App Intents/Widget behaviors adapted to Android shortcuts/widgets with similar user-facing outcomes.
- iOS-specific `UIPasteboard` and sheet detents adapted to Compose bottom sheets/dialogs.

---

## 4) Implementation sequence

1. **Android scaffold and dependencies**
   - Add Gradle dependencies and Hilt/KSP/Room setup.
   - Establish package layout and app navigation shell.

2. **Domain port (parity-critical)**
   - Port deterministic engine + models + tests.
   - Validate output parity using known seeds from iOS tests.

3. **Data layer**
   - Room entities/DAO/repositories for saved seeds.
   - Finder data cache loader and decoding path.

4. **ViewModels and state**
   - Port analyzer and finder state machines.
   - Wire event flows for toasts, sheets, and progress.

5. **UI feature port**
   - Analyzer, Play view, Saved, Finder (including DnD semantics), Community, Summary.
   - Config/input/save sheets and reusable components.

6. **Integrations**
   - Widget + shortcuts.

7. **Verification**
   - Build/debug compile checks.
   - Unit tests for engine and mapper logic.
   - Android instrumentation/Compose tests for critical flows.
   - Consistency pass (imports, naming, no TODO/FIXME/placeholders).

---

## 5) Acceptance criteria for conversion completion

- Every Swift source listed in this plan has an implemented Kotlin counterpart with equivalent behavior.
- All required interactions work: seed input/copy/paste, analyzer run rendering, finder filtering/search, saved seed management, config toggles.
- Material 3 theme is applied globally and visually aligned to iOS style.
- Project compiles with no missing imports/errors; tests for core engine pass.
- No placeholder code (`TODO`, `FIXME`, stubs) remains in migrated features.
