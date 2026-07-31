# AI Usage

How AI assistance was used to build MyNextFavArtist, per the project's transparency
requirements: which tools were used, what for, and what they produced that was rewritten or
rejected — and, running through all of it, how the output was directed and checked rather than
taken on trust.

Throughout, **the developer** means the human engineer directing the work. Where something was
caught, questioned, or overruled, this document says who did it.

## Tools used

- **[Claude Code](https://claude.com/claude-code)** (Anthropic's agentic CLI) — the whole
  build, in two roles described below. Two models: **Claude Sonnet 5** and **Claude Opus 4.8**.
- **[Android CLI](https://developer.android.com/tools/agents/android-cli)** (Google) — the
  agent's route to a real device. Installed on the development machine, not a project
  dependency; see [Device tooling](#device-tooling).
- **`adb` and `emulator`** from the Android SDK, used directly for most device work.
- **Compose compiler reports** (`reportsDestination`), enabled temporarily to settle questions
  about recomposition with evidence instead of argument, then switched back off.

## How the work was split

**Claude Code** (Anthropic's agentic CLI) did essentially the whole build, in two roles:

- **Sonnet 5 as the builder** — planning, implementation across all layers, tests, and
  on-device debugging.
- **Opus 4.8 as the reviewer** — a separate pass per layer (domain, data, ui), reading for
  latent bugs rather than writing features. The one exception is the UI overhaul, where Opus
  was the builder — see below.

Every finding was re-verified before being acted on, and a fair number didn't survive that
check. For example:

- An `InterruptedException` path in `RateLimitInterceptor`, claimed as a swallowed
  cancellation — it didn't hold up on re-reading and was discarded.
- A suspected divergence between the search query held in `SavedStateHandle` and the copy in
  `rememberSaveable`. Testing it on the emulator showed both reset together, because switching
  tabs disposes the whole navigation entry. Real duplication, but no bug.
- Two separate worries that lambdas passed to composables (in `NavDisplay`'s entries, and in
  `FavoritesContent`'s `when`) were forcing recompositions. The Compose compiler reports showed
  every screen `restartable skippable`, so neither was costing anything.

Confirmed findings that weren't fixed are listed under
[Pending work](README.md#pending-work).

## Device tooling

Google's [Android CLI](https://developer.android.com/tools/agents/android-cli) is installed on
the development machine so the agent can drive a real device rather than reason about the app
from source alone. It wraps the SDK in commands meant for agents — start an emulator, deploy a
build, capture the screen, dump the layout tree — and registers a skill that tells the agent
how to use them.

That is what makes the on-device work described below possible: the IPv6 routing bug, the
empty-favourites bug, and the UI verification were all found or confirmed by running the app,
and none of them are visible in the code.

Two caveats worth stating plainly:

- **It's a machine-level tool, not a project dependency.** Nothing in this repo requires it,
  and `./gradlew` builds and tests the project without it. Cloning the repo doesn't install it.
- **Not everything went through it.** Much of the device work used `adb` and `emulator` from
  the SDK directly — installing builds, `input` taps and swipes, `exec-out screencap`, and
  reading `settings` values back to check whether a UI toggle had actually registered.

## What it was used for

**Research and planning.** Explored an earlier project by the same developer, built on the
same architecture, for its module layout, MVI pattern, and testing conventions. Checked the
MusicBrainz API and Cover Art Archive directly rather than assuming their behaviour, and
verified the curated seed-artist MBIDs against the live search API instead of recalling them.
The resulting plan was reviewed and approved by the developer before any code.

**Scaffolding.** Multi-module Gradle setup, Hilt modules, Room database/DAO, Retrofit
interface and DTOs — ported from that project and adapted to MusicBrainz.

**Features.** All four screens (Home, Search, Favorites, Details) with their ViewModels,
actions/effects, and navigation wiring.

**Tests.** ViewModel tests for all four screens, repository and data-source tests, and
`RateLimitInterceptorTest`, which has no equivalent in the earlier project. Written to match the
existing structure and stack (JUnit4 + Mockito-Kotlin + Truth), currently 100 tests.

A `GetHomeArtistsTest` was written alongside these but later removed — see
[Rejected](#what-the-ai-produced-that-was-rewritten-or-rejected) below.

**Debugging.** Built and ran the app on an emulator, drove it through the real UI, and read
`adb logcat` to find bugs that code review wouldn't have caught.

**Architectural discussion.** Several decisions were reached by asking rather than assuming:

- *What the Home screen should show*, given MusicBrainz has no trending or browse-all
  endpoint. A curated seed list was chosen over a favourites-first landing screen or folding
  Home into Search.
- *What to name the `Result<D, E>` wrapper* — see the rejections below.
- *Whether to invent a component* to fill the empty space in a hero with no artist image. The
  developer decided against it: better to let the title carry the space than to manufacture
  content for it.

**Documentation.** The README, this file, and the KDoc and inline comments across the codebase
were all drafted by Claude Code and then edited by hand, repeatedly. The models overexplain:
they reach for a paragraph where a sentence carries the same information, restate in a summary
what the section above already said, and pad a finding with the reasoning that produced it.
One pass cut this document by a third; the README lost a "Best Practices" section listing null
safety and consistent formatting, true of any Kotlin project and therefore worth nothing to a
reader.

The same editing went into the code comments, one class at a time. Generated comments tend to
narrate — restating in prose what the line below plainly does — and the useful ones are the
ones explaining a decision the code can't show you: why `RateLimitInterceptor` blocks with
`Thread.sleep` and measures with `nanoTime` rather than `currentTimeMillis`, why
`heroTopClearance` exists at all, why `EmptyStateAction` groups a label with its handler
instead of taking two nullable parameters. Getting from the first kind to the second was manual
work, done by the developer file by file, not something the models arrived at on their own.

It is a standing tendency rather than a solved problem — this file has since grown past its
post-trim length again, and will need the same treatment.

## What the AI produced that was rewritten or rejected

This happened considerably more often than the list below suggests. Rejections that were
resolved in conversation — a suggestion turned down before it was written, a generated approach
replaced during a back-and-forth — mostly went unrecorded at the time, and reconstructing them
after the fact would mean guessing at details this document has no business guessing at. What
follows is what could be attributed accurately, not a complete tally.

- **`Outcome` → `Result`** — *caught by the developer, before implementation.* The typed
  error-handling pattern itself is human-designed, ported from the developer's earlier project,
  not an AI invention. The generated plan proposed keeping the original name `Outcome` for
  fidelity. The developer's standing preference for this reusable pattern is `Result`, so the
  plan was corrected before a line of it was written.
- **A single-use `PullToRefresh` wrapper** — *caught by the developer, in review.* Written as a
  pass-through around Material3's `PullToRefreshBox` that added nothing. It compiled, it worked,
  and it would have survived indefinitely; the developer read it, called it indirection for its
  own sake, and had it inlined into `HomeScreen`.
- **A `:core-di` module built on a premise nobody checked** — *caught by the developer, in
  review.* An earlier commit correctly removed Hilt and KSP from `:domain`, which had been
  pulling the whole annotation processor in to host one 7-line qualifier. But it moved that
  qualifier into a brand-new Gradle module, reasoning that `:data` also consumed `@IoDispatcher`
  and couldn't depend on `:app` without inverting the graph. The developer asked why a module
  existed for seven lines. It turned out `:data` has no Hilt, no Dagger processor, and no
  `@Inject` anywhere: `ArtistRemoteDataSource` is hand-constructed in `:app`'s `DataModule` with
  positional arguments, so the annotation on its constructor was decorative — retained at
  runtime, read by nothing. `:app` was the only real consumer all along. The module is gone and
  the qualifier now sits beside the module that provides the binding.
- **A misdiagnosed IPv6-routing bug** — *caught by the developer, on the emulator.* The
  emulator resolved an IPv6 address for `musicbrainz.org` it appeared unable to route to, so an
  IPv4-preferring `Dns` was added. The developer tested that theory directly: disabled the
  filter, saw the same failures, then reproduced them in the device browser with the app out of
  the picture entirely — and made the call to remove it. The diagnosis was wrong and the fix was
  treating a symptom of something else.
- **`backup_rules.xml` and `data_extraction_rules.xml`** — *caught by the developer, in review.*
  Wizard-generated templates carried into the project unexamined and never given real
  `include`/`exclude` entries. The developer asked what they were actually doing; the answer was
  nothing, so they went, along with the corresponding manifest attributes.
- **A broken first draft of `AlbumArtCard`'s fallback** — *caught during implementation.* An
  early version tried to build the "no cover art" placeholder from
  `rememberAsyncImagePainter(model = null)`, which doesn't produce a meaningful image at all.
  Rewritten to use a local vector drawable before it was ever built.
- **Assorted UI-overhaul output**, listed under [UI overhaul](#ui-overhaul) below — all of it
  caught by the developer — plus the review findings that didn't survive verification, listed
  at the top.
- **`GetHomeArtistsTest`** — *caught by the developer, months after it was written.* Named
  `` `delegates to the repository's cached getHomeArtists` ``, mirroring `GetHomeArtists`'s own
  KDoc about Room/TTL caching — but the test mocked `ArtistRepository` outright, so no caching
  behaviour was ever exercised. It only proved the same trivial pass-through every other
  `domain` use case has, undistinguished from them by a name that claimed otherwise. Deleted.

## What was not ported faithfully, and why

Separately from the above: porting an existing architecture meant deciding, repeatedly,
whether the original made sense in this app. These reject inherited code — the earlier
project's, or the Studio wizard's — rather than anything the AI invented.

- **`BaseUiState` made generic.** The `Base` states pattern (`BaseUiState`, `BaseUiEffect`,
  `BaseViewModel`) is human-designed and was ported wholesale. But the original hardcodes
  `Success` to a single entity list, and this app has two payload types, so a straight port
  wouldn't have compiled — made generic as an improvement on top of the ported design.
- **`DetailsUiState` split into three fields.** Details has two independent async concerns
  (favourite toggle, release-group fetch). Sharing one state would blank the loaded album
  list every time you tapped the heart.
- **Material3 `PullToRefreshBox` used directly.** The original uses the experimental Material2
  API, which would have meant adding a dependency this project doesn't otherwise need.
- **A global `lateinit var colorScheme` dropped.** The original sets a top-level mutable as a
  composition side effect and reads it from several components. Every port that used it now
  reads `MaterialTheme.colorScheme` directly.
- **An empty-state bug in `FavoritesViewModel`.** An empty favourites list was reported as
  `Success`, so first-time users saw a bare "Delete All" button over nothing. It was ported
  faithfully because nothing about the code looked wrong on inspection — only manual testing
  surfaced it. Empty results now use a dedicated `BaseUiState.Empty`.

## Passing the navigation argument to DetailsViewModel

Directed by the developer, including the exact mechanism to use. `DetailsViewModel` used to
learn which artist it was showing after construction: the screen fired a
`LaunchedEffect(artist) { viewModel.handleAction(LoadArtistDetails(artist)) }` once composed,
and the ViewModel stashed it in a `MutableStateFlow<Artist?>` that its loaders awaited with
`.filterNotNull().first()`. It worked, but every load carried a race it didn't need to: nothing
stopped a load from starting before the artist arrived.

The developer asked to look for a Navigation 3-native way to supply the artist instead of
working around its absence, and pointed the agent at [Android CLI](https://developer.android.com/tools/agents/android-cli)'s
documentation search rather than general web search. That surfaced Google's own recipe for
exactly this — [Passing Arguments to ViewModels (Hilt)](https://developer.android.com/guide/navigation/navigation-3/recipes/passingarguments):
`@HiltViewModel(assistedFactory = ...)` with `@AssistedInject`/`@Assisted`, and
`hiltViewModel(creationCallback = ...)` at the call site. `DetailsViewModel` now takes `artist`
as an `@Assisted` constructor parameter, supplied once when `DetailsScreen` creates it — no
`MutableStateFlow`, no `filterNotNull().first()`, and no window where a load could start before
knowing what to load.

`DetailsUiAction.LoadArtistDetails` and `ToggleFavorite` dropped their `artist` payload as a
direct consequence — the ViewModel already has it — rather than as a separate cleanup.

## Lazy ViewModel state loading

*Directed by the developer*, including the exact scope. Every screen's initial data load moved
out of `init {}` (only `SearchViewModel` used it) and out of a `LaunchedEffect(Unit)` dispatching
a load action on first composition (`Home`, `Favorites`, `Details`) — both start a fetch that
isn't tied to whether anything is actually observing it yet. The developer's brief covered not
just "fix the one with `init`" but all four ViewModels, and a change to `BaseViewModel` itself
rather than four separate workarounds.

`BaseViewModel.uiState` now derives from `stateIn(viewModelScope,
SharingStarted.WhileSubscribed(5_000), initialState)`, with a new `onSubscribed()` hook
subclasses override instead of loading from a constructor or a Compose effect. The load starts
the first time a screen actually subscribes; the 5-second grace window means a quick tab switch
and back doesn't restart it, while a longer absence does. `DetailsViewModel`'s retry action now
only re-fetches release groups, not favourite status too — the two were previously bundled
because both were triggered by the same `LoadArtistDetails` action, and re-running favourite
status on every release-groups retry was always redundant once it became its own continuously
running collector.

One consequence scoped as deliberately out of this change, logged under
[Pending work](README.md#pending-work): the ongoing collectors (Favorites, Search, Details'
favourite-status) keep running once started rather than stopping when unsubscribed. Landing this
after `DetailsViewModel`'s assisted-injection change (above) meant Details didn't need a
composable-side `LaunchedEffect` to kick off its load at all — `onSubscribed` already has the
artist it needs, so both of its loads simply start the moment `uiState` gets a subscriber.

The test migration was the larger part of the diff. Every existing ViewModel test read
`uiState.value` without ever collecting `uiState`, which stops working once state delivery is
subscription-gated — `StateFlow.value` doesn't count as a subscriber, so an uncollected `uiState`
would sit frozen at its initial value. Fixed by adding an explicit collector to each test before
reading state, matching a pattern one test (`HomeViewModelTest`'s refresh test) already used for
an unrelated reason.

## Hoisting the search query into SearchViewModel

Directed by the developer, including the exact target design: "the query belongs in the
ViewModel with `SearchView` made stateless." `SearchViewModel` already held the query in a
`SavedStateHandle`-backed flow to drive the actual search; `SearchView` separately held its own
copy in `rememberSaveable` to drive what the text field displayed. Both survived process death
independently, and an earlier on-device check had confirmed they never drifted apart — but only
because `SearchView` forwarded every keystroke to the ViewModel already, which made the second
copy redundant rather than necessary.

`SearchViewModel`'s query flow became public instead of private, and `SearchView` dropped its
local state entirely to become a controlled component: its `TextField` value now comes from a
`query` parameter, and both the typed-character and clear-button paths call `onQueryChange`
directly instead of writing to local state first. One flow, read in two places, instead of two
flows kept in sync by convention.

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

**Nothing here went in unreviewed.** The developer read the changeset file by file before it
was committed. What changed as a result:

- **The details hero lost a pair of redundant `Box` wrappers** — the developer asked whether
  both were necessary. Neither was: two chained `background` modifiers on the `Column` paint in
  the same order, so the wrappers and their `matchParentSize` came out.
- **`ArtistInformation` moved from `Spacer`s to `Arrangement.spacedBy`**, both gaps collapsed
  to 4dp, after the developer asked what a `Spacer` bought over a margin.
- **`EmptyStateView` was cut from nine parameters to seven** — the developer holds a standing
  limit of seven and caught the breach, along with a hardcoded `.dp` that should have been a
  token. Its label and click handler became one `EmptyStateAction`.
- **A proposal to invent a component** filling the empty space in a picture-less hero was
  rejected outright: "we can make the best we can with just the title."

Several of the discarded findings listed at the top came out of this same pass.

## Directing the tools, and checking their work

The three requirements above are about the tools. This section is about the part that isn't:
the output was steered and audited, not accepted.

**Standing constraints, set once and enforced.** A seven-parameter ceiling on functions. No
hardcoded dimensions — every `.dp` goes through the `Dimensions` token object. Both were
enforced by the developer catching breaches in review, not by a linter.

**Questions rather than instructions.** Most corrections above started as a question — "is
this necessary?", "what does this buy us?", "why is there so much space here?" — which leaves
room for the answer to be "it is necessary, and here's why". Several times it was:

- Two separate questions about lambdas causing recompositions ended with the Compose compiler
  reports showing every screen already `restartable skippable`. No change made.
- A question about the search query living in both `SavedStateHandle` and `rememberSaveable`
  ended with an on-device test showing the two never diverge. The duplication was real but not
  the bug it looked like at the time — it was removed later anyway, once it was in scope; see
  [Hoisting the search query into SearchViewModel](#hoisting-the-search-query-into-searchviewmodel).

**Claims were made to produce evidence.** "It doesn't recompose" was not accepted as an answer;
the compiler reports were turned on to check. "The IPv6 filter fixed it" was not accepted
either — the developer disabled it and reproduced the failure without it. "`:data` needs this
module" was settled by deleting the dependency and running the build, which passed. The rate
limit, the skipping behaviour, and the icon's safe-zone geometry were all confirmed by running
something, not by reasoning about it.

**Structural decisions were questioned, not just code.** The `:core-di` removal started as
"why do we have a whole module for this?" — a question about a build-level choice that had
been committed weeks earlier, was working, and was never going to announce itself. Generated
architecture is the hardest output to audit precisely because it looks deliberate: a module
boundary reads as a decision someone made, even when the reasoning behind it was never checked.

**Scope was held.** Suggested work that was real but out of scope was deferred deliberately
rather than absorbed — the search-query hoist sat recorded in Pending work for a while rather
than being folded into an unrelated commit, and was only picked up once it was its own
deliberate piece of work; see
[Hoisting the search query into SearchViewModel](#hoisting-the-search-query-into-searchviewmodel).

**The prose was edited too, not just the code.** Generated writing reads as finished long
before it is: confident, well-organised, and a third longer than it needs to be. Both markdown
documents were cut down by the developer — sections removed for saying nothing a reader
couldn't assume, explanations compressed to their conclusion, and in this file, an inconsistent
voice that called the same person both "mine" and "the user" unified. The same pass went
through the KDoc and comments in the source, class by class, replacing narration of what the
code does with the reason it does it.

This is the easiest review to skip, because nothing fails when the writing is merely bloated —
the build stays green and the tests still pass. It is also the part a future reader depends on
most, since a comment that restates the code is worse than no comment: it costs a line and
teaches nothing.

## What could not be fully verified

Partway through on-device testing, `musicbrainz.org` stopped being reachable from the test
network (ICMP fine, TCP:443 timing out, across two emulator restarts). The same
request/response/error path had already been exercised successfully several times before
that, including a full search and a full Details load with correct sorting and working retry.
The Search screen's live-query path and the favourite-toggle write path were covered by unit
tests but not re-confirmed against the live API afterwards.

MusicBrainz's 1 req/sec rate limit also means album lists occasionally fail to load during
rapid manual testing. That's the API pushing back, not a bug.
