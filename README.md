# MyNextFavArtist

A clean-architecture Android app for browsing musicians and their discographies via the
[MusicBrainz API](https://musicbrainz.org/doc/MusicBrainz_API), built with modern Android
best practices. This project shares its architecture with a companion project,
[PlanetFinder](https://github.com/JRomCar/PlanetFinder) (a Star Wars planet search app), and
was built by porting and adapting that architecture to a different API and domain.

## Project Overview

MyNextFavArtist lets users browse a curated list of well-known artists, search MusicBrainz
for any artist by name, view an artist's discography (release groups) with album artwork
from the Cover Art Archive, and save favorite artists for later. The app is designed to be
scalable, maintainable, and easy to extend, following industry standards for Android
development.

## Tech Stack
- **Kotlin**: Primary language for all modules
- **MVI**: Model-View-Intent — sealed `*UiAction`s dispatched through a single `handleAction()`, immutable `StateFlow<UiState>` output, one-shot side effects via `SharedFlow`
- **Hilt**: Dependency injection
- **Jetpack Compose**: Declarative UI framework
- **Compose Navigation 3**: Navigation between screens
- **Retrofit + Gson / OkHttp**: Networking (MusicBrainz), with custom interceptors for the API's User-Agent and 1 request/second rate-limit requirements
- **Coil3**: Album cover art loading from the Cover Art Archive
- **Room**: Local persistence of favorite artists
- **Coroutines & StateFlow**: Asynchronous and reactive programming
- **Typed error handling**: An exception-free `Result<D, E>` wrapper with a typed `DataError` hierarchy, mapped to UI strings/icons in the ViewModels

## Architecture

The app follows a clean architecture pattern, separating concerns into distinct layers:

- **UI Layer** (`ui/`): Contains Composables and ViewModels following the MVI pattern, plus navigation logic. Uses `StateFlow` for state and `SharedFlow` for one-shot effects.
- **Domain Layer** (`domain/`): Contains business logic, use cases, entities, and the `Result`/`DataError` error model. Decoupled from data and UI layers.
- **Data Layer** (`data/`): Handles data sources, repositories, API calls (Retrofit), and persistence (Room). Implements the repository pattern and is the only layer that catches exceptions, mapping them to typed `DataError`s.
- **Test Utilities** (`test-utils/`): Shared test utilities and mocks.

## Project Structure

```
app/           # Application entry point, Hilt DI modules, MainActivity
ui/            # UI layer: Composables, ViewModels, navigation
domain/        # Domain layer: use cases, models, interfaces
data/          # Data layer: repositories, API, entities
test-utils/    # Shared test utilities
```

## Working with the MusicBrainz API

Two things about MusicBrainz shaped several architecture decisions here:

- **No browse-all or trending endpoint.** Only free-text search (`/artist?query=...`) and
  lookup-by-MBID exist. The Home screen shows a small curated list of well-known artists
  (`domain/usecase/SeedArtists.kt`), fetched in a **single** request via a Lucene
  `arid:(id1 OR id2 OR ...)` query against the same search endpoint used for user search —
  not one request per artist, which would be far too slow under the rate limit below.
- **1 request/second rate limit, and a required User-Agent.** `RateLimitInterceptor` and
  `UserAgentInterceptor` (`data/api/interceptor/`) enforce both at the OkHttp layer, so every
  API call goes through them automatically regardless of call site.

Album artwork comes from the separate [Cover Art Archive](https://musicbrainz.org/doc/Cover_Art_Archive)
service, which has no rate limit or User-Agent requirement of its own — cover art loading
uses a plain OkHttp client via Coil, not the MusicBrainz-specific one.

MusicBrainz has no artist-photo API (Cover Art Archive only covers releases/release-groups),
so artist rows use a deterministic initials avatar instead of a loaded image; real cover art
is reserved for release-group (album) covers, which MusicBrainz does provide.

## Dependency Injection

Hilt is used for dependency injection, ensuring loose coupling and easy testing. Modules are
defined for providing dependencies across layers (`app/di/`).

## UI Layer

Jetpack Compose is used for building the UI in a declarative way. Navigation between screens
is handled by Compose Navigation 3, allowing for type-safe and modular navigation.

## State Management

- **StateFlow**: Used in ViewModels for reactive state updates.
- **Sealed Classes**: `BaseUiState<T>` (generic over its success payload, since this app has
  two independently-loading lists — artists and an artist's release groups) represents
  Loading/Success/Error/Initial for type safety and clarity.
- **Error Handling**: Exception-free, typed error handling via a `Result<D, E>` wrapper and a
  sealed `DataError` hierarchy. Exceptions are caught only in data sources; errors propagate
  as values and are mapped to UI strings/icons (`asUiText()`/`asUiIcon()`) in the ViewModels.

## Best Practices

- **Null Safety**: Kotlin null-safe operators are used throughout.
- **Coroutines**: Asynchronous operations are handled with coroutines for performance and thread safety.
- **Resource Management**: Proper cleanup and lifecycle awareness in ViewModels and repositories.
- **Code Organization**: Classes are focused, cohesive, and grouped by functionality.
- **Consistent Formatting**: Follows Kotlin and Android style guidelines.

## Before publishing

MusicBrainz requires a meaningful, identifying `User-Agent` with a real contact string. This
project defaults the contact segment to a placeholder (`contact@example.com`) via the
`mbContact` Gradle property so a real one is never accidentally committed. Set it with:

```
./gradlew assembleDebug -PmbContact=you@example.com
```

or add `mbContact=you@example.com` to your local (git-ignored) `gradle.properties`.

## Extensibility / Roadmap

To add new features or modules:
- Create new use cases in the domain layer
- Implement repositories in the data layer
- Add new ViewModels and Composables in the UI layer
- Register dependencies with Hilt modules

Known gaps, deliberately left for later:
- **Search-result caching**: PlanetFinder caches search results for 3 minutes to reduce
  redundant network calls; this project doesn't yet, though it would help further given the
  1 req/sec ceiling.
- **UI Testing**: Add Compose UI tests to complement the ViewModel/repository unit tests.

## Summary

This project serves as a reference for building scalable, maintainable Android apps using
modern tools and patterns, and for demonstrating how to adapt a proven architecture to a new
API with real constraints (rate limiting, no browse endpoint, a separate image service).

See [AI_USAGE.md](AI_USAGE.md) for how this project was built with AI assistance.
