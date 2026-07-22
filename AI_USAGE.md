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

**Tests.** ViewModel tests for all four screens, repository and data-source tests, and two
with no equivalent in the earlier project — `RateLimitInterceptorTest`, and `GetHomeArtistsTest`
for the seed-query construction. Written to match the existing structure and stack (JUnit4 +
Mockito-Kotlin + Truth), currently 89 tests.

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

**Documentation.** The README and this file were both drafted by Claude Code and then edited
down by the developer, repeatedly. The models overexplain: they reach for a paragraph where a
sentence carries the same information, restate in a summary what the section above already
said, and pad a finding with the reasoning that produced it. One pass cut this document by a
third; the README lost a "Best Practices" section listing null safety and consistent
formatting, true of any Kotlin project and therefore worth nothing to a reader.

It is a standing tendency rather than a one-off — this file has since grown past its
post-trim length again, and will need the same treatment.

## What the AI produced that was rewritten or rejected

- **`Outcome` → `Result`** — *caught by the developer, before implementation.* The typed
  error-handling pattern itself is human-designed, ported from the developer's earlier project,
  not an AI invention. The generated plan proposed keeping the original name `Outcome` for
  fidelity. The developer's standing preference for this reusable pattern is `Result`, so the
  plan was corrected before a line of it was written.
- **A single-use `PullToRefresh` wrapper** — *caught by the developer, in review.* Written as a
  pass-through around Material3's `PullToRefreshBox` that added nothing. It compiled, it worked,
  and it would have survived indefinitely; the developer read it, called it indirection for its
  own sake, and had it inlined into `HomeScreen`.
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
  ended with an on-device test showing the two never diverge. The duplication is real and is
  logged under [Pending work](README.md#pending-work), but it isn't the bug it looked like.

**Claims were made to produce evidence.** "It doesn't recompose" was not accepted as an answer;
the compiler reports were turned on to check. "The IPv6 filter fixed it" was not accepted
either — the developer disabled it and reproduced the failure without it. The rate limit,
the skipping behaviour, and the icon's safe-zone geometry were all confirmed by running
something, not by reasoning about it.

**Scope was held.** Suggested work that was real but out of scope was deferred deliberately
rather than absorbed — the search-query hoist is recorded in Pending work instead of being
folded into a UI commit.

**The prose was edited too, not just the code.** Generated documentation reads as finished
long before it is: confident, well-organised, and a third longer than it needs to be. Both
documents in this repo were cut down by the developer — sections removed for saying nothing a
reader couldn't assume, explanations compressed to their conclusion, and in this file, an
inconsistent voice that referred to the same person as both "mine" and "the user" unified.
Reviewing generated prose is the same job as reviewing generated code, and it is easier to
skip, because nothing fails when the writing is merely bloated.

## What could not be fully verified

Partway through on-device testing, `musicbrainz.org` stopped being reachable from the test
network (ICMP fine, TCP:443 timing out, across two emulator restarts). The same
request/response/error path had already been exercised successfully several times before
that, including a full search and a full Details load with correct sorting and working retry.
The Search screen's live-query path and the favourite-toggle write path were covered by unit
tests but not re-confirmed against the live API afterwards.

MusicBrainz's 1 req/sec rate limit also means album lists occasionally fail to load during
rapid manual testing. That's the API pushing back, not a bug.
