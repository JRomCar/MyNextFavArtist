# AI Usage

How AI assistance was used to build MyNextFavArtist, per the project's transparency
requirements.

## How the work was split

**Claude Code** (Anthropic's agentic CLI) did essentially the whole build, in two roles:

- **Sonnet 5 as the builder** — planning, implementation across all layers, tests, and
  on-device debugging.
- **Opus 4.8 as the reviewer** — a separate pass per layer (domain, data, ui), reading for
  latent bugs rather than writing features. Findings were re-verified before being trusted;
  one claimed bug (an `InterruptedException` path in `RateLimitInterceptor`) didn't hold up
  and was discarded. Confirmed findings that weren't fixed are listed under
  [Pending work](README.md#pending-work).

The one exception is the UI overhaul, where Opus was the builder — see below.

## What it was used for

**Research and planning.** Explored [PlanetFinder](https://github.com/JRomCar/PlanetFinder)
(a companion app with the same architecture) for its module layout, MVI pattern, and testing
conventions. Checked the MusicBrainz API and Cover Art Archive directly rather than assuming
their behaviour, and verified the curated seed-artist MBIDs against the live search API
instead of recalling them. The resulting plan was reviewed and approved before any code.

**Scaffolding.** Multi-module Gradle setup, Hilt modules, Room database/DAO, Retrofit
interface and DTOs — ported from PlanetFinder and adapted to MusicBrainz.

**Features and tests.** All four screens (Home, Search, Favorites, Details) with their
ViewModels and navigation, plus ViewModel, repository, and data-source tests.

**Debugging.** Built and ran the app on an emulator, drove it through the real UI, and read
`adb logcat` to find bugs that code review wouldn't have caught.

## What was rewritten or rejected, and why

Porting from PlanetFinder meant deciding, repeatedly, whether the original made sense here:

- **`Outcome` → `Result`.** The plan first said to port the name verbatim. Corrected before
  implementation, following a standing preference for this reusable pattern.
- **`BaseUiState` made generic.** PlanetFinder hardcodes `Success` to `List<Planet>`; this
  app has two payload types, so a straight port wouldn't have compiled.
- **`DetailsUiState` split into three fields.** Details has two independent async concerns
  (favourite toggle, release-group fetch). Sharing one state would blank the loaded album
  list every time you tapped the heart.
- **`PullToRefresh` rebuilt on Material3.** PlanetFinder uses the experimental Material2 API;
  matching it would have meant adding a dependency this project doesn't need.
- **PlanetFinder's global `lateinit var colorScheme` dropped.** A top-level mutable set as a
  composition side effect, read from several components. Every port that used it now reads
  `MaterialTheme.colorScheme` directly.
- **An IPv6-routing bug, found only by running the app.** The emulator resolved an IPv6
  address for `musicbrainz.org` it couldn't route to. Fixed with an IPv4-preferring `Dns`,
  then removed once the same failures reproduced on a real device with the filter off —
  confirming it was never the cause.
- **An empty-state bug in `FavoritesViewModel`.** An empty favourites list was reported as
  `Success`, so first-time users saw a bare "Delete All" button over nothing. The same bug
  exists in PlanetFinder; only manual testing surfaced it. Empty results now use a dedicated
  `BaseUiState.Empty`.

## UI overhaul

The screens shipped as working scaffolding — default Material colours, one typography token,
a spinner for every loading state. Opus rebuilt the presentation layer:

- A brand palette with real light and dark schemes, and the full type scale.
- Shared components instead of repetition: one empty-state layout behind every "nothing to
  show" screen (errors included), one section header, one skeleton loader.
- Details switched from a scrolling `Column` to a `LazyColumn` — artists like the Rolling
  Stones have ~1900 release groups, all of which were being composed at once.
- Clearing all favourites now asks first.

Verified on a Pixel 10 emulator in light and dark.

## What could not be fully verified

Partway through on-device testing, `musicbrainz.org` stopped being reachable from the test
network (ICMP fine, TCP:443 timing out, across two emulator restarts). The same
request/response/error path had already been exercised successfully several times before
that, including a full search and a full Details load with correct sorting and working retry.
The Search screen's live-query path and the favourite-toggle write path were covered by unit
tests but not re-confirmed against the live API afterwards.

MusicBrainz's 1 req/sec rate limit also means album lists occasionally fail to load during
rapid manual testing. That's the API pushing back, not a bug.
