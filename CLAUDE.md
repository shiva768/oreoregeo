# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Quick Reference

### Common Commands

#### Build
```bash
./gradlew assembleDebug      # Build debug APK
./gradlew build              # Full build (tests + APK)
```

#### Testing
```bash
./gradlew test               # Run unit tests (JVM-based in src/test/java)
./gradlew connectedAndroidTest  # Run instrumentation tests (app/src/androidTest)

# Single test
./gradlew test --tests com.zelretch.oreoregeo.CheckinSearchTest
./gradlew connectedAndroidTest --tests com.zelretch.oreoregeo.SearchScreenTest
```

#### Code Quality
```bash
./gradlew ktlintFormat       # Auto-format code
./gradlew ktlintCheck detekt # Check for style/design violations
```

#### Project Info
```bash
./gradlew tasks              # List all available tasks
./gradlew dependencies       # Show dependency tree
```

---

## Project Architecture

### Overview
**Oreoregeo** is a Swarm-compatible Android app for manually checking in to OpenStreetMap (OSM) locations. It works offline-first with local SQLite storage, syncs with Overpass API for nearby place discovery, and integrates with the OSM API v0.6 for place editing.

### Core Tech Stack
- **Language**: Kotlin + Java 17
- **UI**: Jetpack Compose (Material 3) with Navigation Compose
- **Database**: Room ORM (SQLite with WAL mode)
- **Async**: Kotlin Coroutines + Flow
- **HTTP**: OkHttp + Kotlinx Serialization (JSON)
- **External APIs**: Overpass API (read), OSM API v0.6 (write), Google Drive API (backup), OpenStreetMap OAuth
- **Maps**: osmdroid library for OpenStreetMap rendering
- **Minimum SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)

### Source Structure

```
app/src/main/java/com/zelretch/oreoregeo/
├── MainActivity.kt                    # Entry point; hosts Compose nav graph
├── OreoregeoApplication.kt            # App initialization
├── auth/
│   ├── OsmOAuthManager.kt            # OAuth flow for OSM editing
│   └── OAuthCallbackActivity.kt      # OAuth redirect handler
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt            # Room database setup (places, checkins)
│   │   ├── PlaceEntity.kt / PlaceDao.kt        # Places table (OSM nodes)
│   │   ├── CheckinEntity.kt / CheckinDao.kt    # Checkins table (user activity)
│   │   └── DatabaseMigrations.kt     # Schema versioning (if used)
│   ├── remote/
│   │   ├── OsmApiClient.kt           # OSM API v0.6 client (create/edit nodes)
│   │   ├── OverpassApiClient.kt      # Overpass API client (nearby search)
│   │   └── *Response.kt              # DTO classes for API responses
│   └── DriveBackupManager.kt         # Google Drive backup/restore
├── domain/
│   ├── Models.kt                     # Core domain models (Place, Checkin, etc.)
│   ├── Repository.kt                 # Data layer abstraction
│   └── [ViewModel logic should be minimal here]
├── ui/
│   ├── MainActivity.kt               # (if separate from root)
│   ├── SearchScreen.kt / SearchViewModel.kt            # Nearby places, manual search
│   ├── CheckinDialog.kt / CheckinViewModel.kt          # Manual check-in dialog
│   ├── HistoryScreen.kt / HistoryViewModel.kt          # Check-in history
│   ├── AddPlaceScreen.kt / AddPlaceState.kt            # Create new OSM node
│   ├── AddPlaceComponents.kt / AddPlaceDialogs.kt      # Composable components & dialogs
│   ├── EditTagsScreen.kt / OsmEditViewModel.kt         # Edit OSM node tags
│   ├── SettingsScreen.kt                               # Settings & backup
│   ├── SearchMapView.kt / MapPickerView.kt             # osmdroid MapViews
│   ├── PlaceCard.kt / CategoryValueField.kt            # Reusable UI components
│   └── Theme.kt (if present)                           # Compose Material 3 theme
└── util/
    ├── TagUtil.kt                    # Tag parsing & formatting (amenity, shop, tourism)
    └── NetworkUtil.kt                # Network state checking

app/src/test/java/com/zelretch/oreoregeo/
├── CheckinSearchTest.kt              # Checkin query tests
├── DataModelTest.kt                  # Entity/model serialization
├── PlaceKeyTest.kt                   # OSM place_key (osm:type:id) format
├── RemoteModelsTest.kt               # API response DTOs
├── TimeBucketTest.kt                 # 30-min bucket logic
├── TagExtractionTest.kt              # Tag parsing
├── DomainModelsTest.kt               # Domain logic tests
└── auth/OsmOAuthManagerTest.kt

app/src/androidTest/java/com/zelretch/oreoregeo/
├── SearchScreenTest.kt               # Nearby search UI
├── CheckinDialogTest.kt              # Check-in flow UI
├── HistoryScreenTest.kt              # History display
├── AddPlaceScreenTest.kt             # Create OSM node UI
├── EditTagsScreenTest.kt             # Edit node tags UI
├── SettingsScreenTest.kt             # Settings UI
└── MainActivityTest.kt               # Navigation & app state
```

### Data Models & Constraints

#### `places` Table
- **place_key** (PK): Format `osm:{type}:{id}` (e.g., `osm:node:123`)
- **name, category** (String): Display data
- **lat, lon** (Double): Location
- **updated_at** (Long): Epoch milliseconds when last refreshed from OSM/Overpass

#### `checkins` Table
- **id** (PK Auto): Unique check-in record
- **place_key** (FK): Links to places table
- **visited_at** (Long): Epoch milliseconds (UTC), when user checked in
- **note** (String?): Optional user note
- **visited_at_bucket** (Long): `visited_at / 1800000` (30-min quantized bucket)
- **Unique constraint**: `(place_key, visited_at_bucket)` prevents duplicate check-ins within 30 minutes

### Key Behaviors & Constraints

1. **Manual Check-in Only**: No automatic location-based check-ins; user explicitly taps to check in.
2. **OSM Node Editing Only**: Can create/edit nodes; ways and relations not supported.
3. **Local-First**: All data stored locally; Google Drive used only for backup/restore.
4. **30-Min Dedup**: Database constraint prevents checking in to the same place twice within 30 minutes.
5. **Offline Mode**: History readable offline; search and check-in disabled without network.
6. **Version Conflict Handling**: OSM API returns 409 if node version is stale; auto-fetch and retry.
7. **OAuth Credentials**: OSM OAuth (`write_api` scope) managed by `OsmOAuthManager`; stored in `EncryptedSharedPreferences`.
8. **Google Services**: `google-services.json` is required for Drive API and Firebase Analytics. CI uses a secret; local dev must place the real file in `app/`.

---

## Code Quality Standards

### Linting & Formatting

**Tools**: ktlint (formatting) + detekt (design/complexity)

**Quick checklist before committing:**
- [ ] `./gradlew ktlintFormat` (auto-fixes style)
- [ ] `./gradlew ktlintCheck detekt` (verify rules pass)
- [ ] No wildcard imports in production code (`import kotlin.math.*` is banned; use explicit imports)
- [ ] Test code may use wildcard imports (configured in `.editorconfig`)

**Reference**: See `docs/code-quality-ideal.md` for comprehensive detekt thresholds, exceptions, and refactoring guidance.

### Key Thresholds (Current)
- **CognitiveComplexMethod**: threshold=25 (UI layer excepted for now)
- **CyclomaticComplexMethod**: threshold=20
- **LongMethod**: threshold=60 lines
- **LargeClass**: threshold=400 lines
- **MagicNumber**: inactive (UI/theme colors exempt)

### Best Practices

1. **UI/ViewModel Split**: Keep Compose UI declarative; move complex logic to ViewModel or domain layer.
2. **Coroutines**: Use `viewModelScope` in ViewModels; prefer `Flow` over `LiveData`.
3. **Error Handling**: Display user-friendly messages; log detailed errors via Timber.
4. **Database**: Use `@Transaction` for multi-statement operations; WAL mode handles concurrency.
5. **API Resilience**: Implement exponential backoff for Overpass; handle 409 (version conflict) in OSM edits.
6. **Testing**: Unit tests in `src/test`; instrumentation tests in `androidTest`. Mock APIs and DB in unit tests.
7. **Naming**: Composables should follow PascalCase; Android Studio ignores naming when `@Composable` is present (configured).

---

## Important Files & Conventions

### Configuration
- **.editorconfig**: ktlint rules (no wildcard imports, line length, indentation)
- **config/detekt/detekt.yml**: Design/complexity rules, exclusions, thresholds
- **app/build.gradle.kts**: Dependencies, test runners, ktlint/detekt config, Firebase setup
- **gradle/libs.versions.toml**: Dependency versions (TOML convention)
- **local.properties** (local only): OSM OAuth credentials (`OSM_CLIENT_ID`, `OSM_CLIENT_SECRET`)
- **google-services.json**: Firebase config (dummy in repo; real file needed for Drive API)

### Key Utilities
- **TagUtil.kt**: Parsing/formatting amenity/shop/tourism tags; label extraction
- **NetworkUtil.kt**: Online/offline status checking
- **Models.kt**: Serializable data classes for API responses; coordinate validation

### Testing Setup
- **Instrumentation Runner**: `androidx.test.runner.AndroidJUnitRunner`
- **Test Dependencies**: JUnit 4, Mockito, Truth, Robolectric, Compose test harness
- **No Database Mocking**: Integration tests hit real Room DB (in-memory or temp file)

---

## Workflow & Git Conventions

### Branch & PR Guidelines
- Fixes/features branch from `main`
- PR titles: Prefix with `fix:`, `feat:`, `refactor:`, `docs:`, `chore:`, etc.
- Commits: Imperative mood, clear intent (e.g., "Add place duplicate detection")
- Code review: CI must pass (ktlint, detekt, tests)
- Merge: Squash or rebase preferred for a clean history

### Common Development Patterns

**Adding a New Screen:**
1. Create `*Screen.kt` Composable and `*ViewModel.kt`
2. Add ViewModel to `MainActivity.kt` navigation graph
3. Add route constant to sealed class or string constant
4. Write UI test in `androidTest`

**Adding an API Call:**
1. Define DTO response in `data/remote/*Response.kt`
2. Add method to API client (e.g., `OsmApiClient.kt`)
3. Call from `Repository.kt`, expose via `Flow<Result<T>>`
4. Subscribe in ViewModel with error handling
5. Write unit test mocking HTTP response

**Adding a Database Query:**
1. Define `@Entity` and `@Dao` in `data/local/*Entity.kt` and `*Dao.kt`
2. Register entity in `AppDatabase.kt`
3. Expose via Repository `Flow<List<T>>`
4. Test with real in-memory Room DB

---

## Performance & Offline Behavior

### Network State
- **SearchScreen**: Disabled when offline; message prompts user to re-enable
- **HistoryScreen**: Fully functional offline
- **AddPlaceScreen**: Drafts saved locally; submission queued/disabled offline
- **Overpass Queries**: 80m radius, targets `amenity`, `shop`, `tourism` tags
- **Caching**: No explicit cache layer; Overpass API responses not cached locally

### Database
- **WAL Mode**: Enabled for concurrency (allows reads during writes)
- **Indexes**: `(place_key, visited_at_bucket)` on checkins for dedup checks
- **Large Datasets**: History screen queries all checkins; consider pagination if >10k records

---

## Troubleshooting & Common Issues

### Build Issues
- **Gradle sync fails**: Run `./gradlew clean` and resync
- **Missing `google-services.json`**: CI uses Base64 secret; local dev must provide real file or dummy will be used
- **OSM OAuth credentials missing**: Set `OSM_CLIENT_ID` and `OSM_CLIENT_SECRET` in `local.properties` or env

### Testing Issues
- **Instrumentation tests timeout**: Increase test timeout in build config; ensure emulator/device is running
- **Unit tests fail with network error**: Mock HTTP calls in test; check Mockito setup
- **Room DB locking**: Ensure tests use `inMemoryDatabaseBuilder()` or close DB after tests

### Code Quality Warnings
- **ktlint line too long**: Split at logical points; max advised is 120 chars
- **detekt: Cognitive Complexity exceeded**: Move logic to ViewModel/domain; keep UI declarative
- **detekt: Magic Number**: Hardcoded values should be named constants (except colors/dp in theme)

---

## Documentation & Further Reading

- **README.md**: Feature overview, API examples, OAuth setup, constraints
- **docs/code-quality-ideal.md**: Detailed code style philosophy, phase-in plan, refactoring guidelines
- **Overpass API**: https://overpass-turbo.eu/ (test queries)
- **OSM API v0.6 Docs**: https://wiki.openstreetmap.org/wiki/API_v0.6
- **Room Persistence**: https://developer.android.com/training/data-storage/room
- **Jetpack Compose**: https://developer.android.com/jetpack/compose
- **Kotlin Coroutines**: https://kotlinlang.org/docs/coroutines-overview.html

---

## Notes for Future Contributors

- This is an early-stage project; refactoring for clarity is welcomed.
- UI tests are comprehensive; respect the test suite when modifying Compose screens.
- OSM edits are permanent; always test locally before submission.
- Japanese comments and documentation are used; preserve or translate as appropriate.
