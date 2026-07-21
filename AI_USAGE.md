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

**Research and planning.** Explored an earlier project of mine built on the same architecture
for its module layout, MVI pattern, and testing conventions. Checked the MusicBrainz API and
Cover Art Archive directly rather than assuming their behaviour, and verified the curated
seed-artist MBIDs against the live search API instead of recalling them. The resulting plan
was reviewed and approved before any code.

**Scaffolding.** Multi-module Gradle setup, Hilt modules, Room database/DAO, Retrofit
interface and DTOs — ported from that project and adapted to MusicBrainz.

**Features and tests.** All four screens (Home, Search, Favorites, Details) with their
ViewModels and navigation, plus ViewModel, repository, and data-source tests.

**Debugging.** Built and ran the app on an emulator, drove it through the real UI, and read
`adb logcat` to find bugs that code review wouldn't have caught.

## What was rewritten or rejected, and why

Porting an existing architecture meant deciding, repeatedly, whether the original made sense
here:

- **`Outcome` → `Result`.** The whole typed error-handling pattern (`Result`, `DataError`, and
  the `map`/`onSuccess`/`onFailure` helpers around them) is human-designed, ported from an
  earlier project of mine, not an AI invention. The plan first said to port the name `Outcome`
  verbatim; corrected before implementation to `Result`, the name already used for this
  reusable pattern.
- **`BaseUiState` made generic.** The whole `Base` states pattern (`BaseUiState`,
  `BaseUiEffect`, `BaseViewModel`) is human-designed, ported from an earlier project of mine,
  not an AI invention. The original hardcodes `Success` to a single entity list; this app has
  two payload types, so a straight port wouldn't have compiled — made generic as an
  improvement on top of the ported design.
- **`DetailsUiState` split into three fields.** Details has two independent async concerns
  (favourite toggle, release-group fetch). Sharing one state would blank the loaded album
  list every time you tapped the heart.
- **Material3 `PullToRefreshBox` used directly.** The original uses the experimental Material2
  API and wraps it in a custom `PullToRefresh` composable; matching the Material2 dependency
  wasn't worth it, so the port moved straight to Material3. The wrapper itself stuck around as
  a single-use pass-through until a later user review flagged it as unnecessary indirection;
  removed at the user's request and inlined into `HomeScreen`.
- **A global `lateinit var colorScheme` dropped.** The original sets a top-level mutable as a
  composition side effect and reads it from several components. Every port that used it now
  reads `MaterialTheme.colorScheme` directly.
- **An IPv6-routing bug, found only by running the app.** The emulator resolved an IPv6
  address for `musicbrainz.org` it couldn't route to. Fixed with an IPv4-preferring `Dns`,
  then removed once the same failures reproduced on a real device with the filter off —
  confirming it was never the cause.
- **An empty-state bug in `FavoritesViewModel`.** An empty favourites list was reported as
  `Success`, so first-time users saw a bare "Delete All" button over nothing. It was ported
  faithfully because nothing about the code looked wrong on inspection — only manual testing
  surfaced it. Empty results now use a dedicated `BaseUiState.Empty`.
- **`backup_rules.xml` and `data_extraction_rules.xml` removed.** Both were the untouched
  wizard-generated templates with no real `include`/`exclude` entries, and the app has no
  local data needing custom backup handling. Removed at the user's request after a review
  question, along with the corresponding manifest attributes.

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
