package com.jrom.mynextfavartist.ui.states

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * MVI base: [State] is the single source of truth for what a screen renders, exposed as a
 * StateFlow so recomposition, process death, and re-subscription after a configuration change
 * all just re-read the latest value. [Effect] is for one-shot events (navigation, snackbars)
 * that must fire exactly once and must never replay on a new collector - the opposite of what
 * a screen's visible state should do. Subclasses get both channels plus [launchExclusive] for
 * free instead of re-wiring them per screen.
 */
abstract class BaseViewModel<State, Effect>(initialState: State) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<State> = _uiState.asStateFlow()

    // Channel over SharedFlow: effects (navigation, snackbars) must never be dropped for lack
    // of a collector, unlike state, which is fine to conflate.
    private val _uiEffect = Channel<Effect>(Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()

    // Keyed so two independent operations in the same ViewModel (e.g. DetailsViewModel's
    // favorite-status collector and its release-groups load) can't cancel each other.
    private val exclusiveJobs = mutableMapOf<Any, Job>()

    protected fun updateState(reducer: (State) -> State) {
        _uiState.update(reducer)
    }

    protected fun setState(state: State) {
        _uiState.update { state }
    }

    protected fun sendEffect(effect: Effect) {
        viewModelScope.launch {
            _uiEffect.send(effect)
        }
    }

    /**
     * Launches [block] in viewModelScope, cancelling any work still running under the same
     * [key]. Use for anything a retry, pull-to-refresh, or screen re-entry can re-trigger: a
     * one-shot load, or an open Flow collection.
     *
     * A coroutine operator (collectLatest/flatMapLatest) gives this same cancel-and-relaunch
     * behavior for free, but only when there's already a Flow to collect from -
     * SearchViewModel's flatMapLatest works because typed queries arrive on a continuous
     * StateFlow. The loads this guards are triggered by discrete UI actions (LoadArtists,
     * LoadArtistDetails), not a continuous upstream source, so there's nothing to collectLatest
     * over without inventing a trigger Flow purely to get the cancellation behavior - more
     * machinery than this plain cancel-and-relaunch needs.
     */
    protected fun launchExclusive(key: Any, block: suspend CoroutineScope.() -> Unit) {
        exclusiveJobs.remove(key)?.cancel()
        exclusiveJobs[key] = viewModelScope.launch(block = block)
    }
}
