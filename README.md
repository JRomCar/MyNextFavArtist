# MyNextFavArtist

An Android app for browsing musicians and their discographies via the
[MusicBrainz API](https://musicbrainz.org/doc/MusicBrainz_API).

**This project exists to demonstrate clean practices in modern Android development.** It's a
reference implementation: clean architecture across independent Gradle modules, MVI with
unidirectional data flow, exception-free typed error handling, and a UI built entirely on
Jetpack Compose and Material 3. The feature set is deliberately small so the structure stays
legible — browse a curated artist list, search MusicBrainz by name, view an artist's
discography with cover art, and save favourites.

The architecture isn't applied in a vacuum: MusicBrainz's real constraints — a hard rate
limit, no browse-all endpoint, no artist images — force real design decisions, which is
part of what makes it worth reading. See
[Working with the MusicBrainz API](#working-with-the-musicbrainz-api).

## Requirements

- **JDK 17+** (the toolchain targets Java 11 bytecode, but the build itself needs 17)
- **Android SDK** with API 37 installed
- **minSdk 29** (Android 10) — any device or emulator at or above that

Gradle 9.4.1 comes via the wrapper; no local install needed.

## Build and run

From the project root:

```bash
./gradlew :app:assembleDebug          # build the APK
./gradlew :app:installDebug           # build and install on a connected device/emulator
```

On Windows, use `gradlew.bat` (or `./gradlew` from Git Bash).

You can also open the project in Android Studio and hit Run — no extra setup.

**Verification:**

```bash
./gradlew test          # unit tests (89: 61 in :data, 26 in :ui, 1 each in :domain and :app)
./gradlew lintDebug     # Android lint
```

### Setting a contact for release builds

MusicBrainz requires a meaningful `User-Agent` with a real contact address, and blocks
generic ones. Debug builds fall back to a placeholder so a real address is never accidentally
committed. Release builds fail fast if you haven't set one:

```bash
./gradlew :app:assembleRelease -PmbContact=you@example.com
```

Or add `mbContact=you@example.com` to your local (git-ignored) `gradle.properties`.

## The rate limit caveat

**MusicBrainz allows 1 request per second.** This is the single most important thing to know
when running the app.

Several design decisions exist to work around it:

- **`RateLimitInterceptor`** (`data/api/interceptor/`) spaces requests at least a second apart
  at the OkHttp layer, so every call is throttled automatically regardless of call site.
  Requests **queue rather than fail** — under rapid interaction a screen may sit on its loading
  state briefly while earlier calls clear.
- **Fan-out is designed away.** The Home screen fetches its entire curated list in a *single*
  request via a Lucene `arid:(id1 OR id2 OR ...)` query, rather than one lookup per artist,
  which would take a dozen seconds under this ceiling.
- **Cover art never spends the budget.** It comes from the separate Cover Art Archive, which
  has no such limit, over its own plain OkHttp client.
- **Every failure is recoverable.** Errors arrive as typed `DataError`s rather than exceptions,
  and each screen renders them with a working retry rather than a dead end.

**Even with all of this, the API still returns errors.** The 1 req/sec figure is the
documented ceiling, not a guarantee — MusicBrainz also throttles by shared IP and by
server-side load, so a well-behaved client can still be turned away. In practice this shows
up as a discography that occasionally fails to load, most often when you tap through several
artists in quick succession.

**That's the service pushing back, not a bug in the app.** Retry works. Short of caching
aggressively (see [Pending work](#pending-work)), there's no client-side fix that removes it
entirely — so the app is built to fail gracefully and recover rather than to pretend it can't
happen.

## Architecture

Clean architecture with strict dependency direction — the domain layer knows nothing about
the layers on either side of it:

```
   ui  ──────┐
             ├──►  domain  ◄──────  data
   app  ─────┘
```

| Module | Responsibility |
|---|---|
| `app/` | Entry point, `MainActivity`, Hilt modules and qualifiers wiring the graph together |
| `ui/` | Compose screens, ViewModels (MVI), navigation, theme |
| `domain/` | Use cases, entities, repository interfaces, the `Result`/`DataError` model |
| `data/` | Repository implementations, Retrofit API, Room persistence, interceptors |
| `test-utils/` | Shared test fixtures and helpers |

Only `:app` and `:ui` know Hilt exists — `:app` hosts the modules and qualifiers, `:ui`
annotates its ViewModels. `:domain` is a plain `kotlin("jvm")` module with no Android or DI
dependency at all, and `:data` is wired entirely through `@Provides` methods in `:app` rather
than annotating its own classes, so it needs no annotation processor either.

**MVI in the UI layer.** Each screen dispatches sealed `*UiAction`s through a single
`handleAction()`, and renders an immutable `StateFlow<BaseUiState<T>>`. One-shot events
(navigation, snackbars) go out as a separate `SharedFlow` of effects, so they can't be
replayed by a configuration change. `BaseUiState<T>` is generic over its payload because this
app loads two different lists — artists, and an artist's release groups.

**Typed errors, no exceptions across boundaries.** A `Result<D, E>` wrapper carries either
data or a `DataError`. Exceptions are caught *only* in data sources and mapped to typed
errors there; from that point on, failure is an ordinary value that the compiler forces you
to handle. ViewModels turn errors into UI strings and icons via `asUiText()`/`asUiIcon()`.

## Tech stack

- **Kotlin** 2.2.10, **AGP** 9.2.1, compileSdk 37
- **Jetpack Compose** (BOM 2026.02.01) + **Material 3** — declarative UI, custom theme with
  full light/dark schemes
- **Navigation 3** — type-safe, `NavKey`-based navigation
- **Hilt** — dependency injection
- **Retrofit + kotlinx.serialization / OkHttp** — networking, with custom interceptors for
  MusicBrainz's User-Agent and rate-limit requirements
- **Room** — local persistence of favourites
- **Coil 3** — cover art loading
- **Coroutines & StateFlow** — async and reactive state
- **JUnit 4 + Mockito-Kotlin + Truth** — testing

## Working with the MusicBrainz API

Beyond the rate limit above, two more API characteristics shaped real decisions here:

**No browse-all or trending endpoint.** Only free-text search and lookup-by-MBID exist, which
is why the Home screen shows a curated list (`data/repository/SeedArtists.kt`) rather than
anything dynamic — there is no "popular artists" to ask for.

**No artist-photo API.** Cover Art Archive covers releases, not artists. Artist rows use a
deterministic gradient-and-initials avatar derived from the name, so the same artist always
looks the same, and real artwork is reserved for albums where it actually exists.

## Pending work

Known and deliberately deferred, not oversights:

- **Compose UI tests.** The ViewModel, repository, and data-source layers are covered by unit
  tests; the Compose layer isn't. Screen-level tests over `BaseUiState` (each of
  Loading/Empty/Error/Success renders the right thing) would be the highest-value addition to
  the suite.
- **Search-result caching.** Repeating a search re-hits the network every time. A short-lived
  cache would matter more here than in most apps, given the 1 req/sec limit.
- **The search query lives in two places** — `SavedStateHandle` in `SearchViewModel` and
  `rememberSaveable` in `SearchView`. Both are currently load-bearing (one restores the query
  that re-drives the search, the other the visible text) and they were verified not to
  diverge, but the query belongs in the ViewModel with `SearchView` made stateless.
- **`UiText.StringResource` is unstable to the Compose compiler**, because `args: List<Any>`
  is an interface type. Harmless today under strong skipping, but an immutable list type would
  make it stable outright.

---

See [AI_USAGE.md](AI_USAGE.md) for how this project was built with AI assistance.
