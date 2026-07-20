package com.jrom.mynextfavartist.ui.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.jrom.mynextfavartist.domain.entities.Artist
import com.jrom.mynextfavartist.domain.error.DataError
import com.jrom.mynextfavartist.domain.fold
import com.jrom.mynextfavartist.domain.usecase.SearchArtists
import com.jrom.mynextfavartist.ui.error.asUiIcon
import com.jrom.mynextfavartist.ui.error.asUiText
import com.jrom.mynextfavartist.ui.states.BaseUiEffect
import com.jrom.mynextfavartist.ui.states.BaseUiState
import com.jrom.mynextfavartist.ui.states.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchArtists: SearchArtists,
    private val savedStateHandle: SavedStateHandle,
    initialState: @JvmSuppressWildcards BaseUiState<List<Artist>> = BaseUiState.Initial,
) : BaseViewModel<BaseUiState<List<Artist>>, BaseUiEffect>(initialState) {

    // Backed by SavedStateHandle so the typed query survives process death
    private val searchQuery: StateFlow<String> = savedStateHandle.getStateFlow(SEARCH_QUERY_KEY, "")

    init {
        observeSearchQuery()
    }

    fun handleAction(action: SearchUiAction) {
        when (action) {
            is SearchUiAction.SearchRequest -> savedStateHandle[SEARCH_QUERY_KEY] = action.query
            is SearchUiAction.ArtistClicked -> onArtistClicked(action.artist)
        }
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun observeSearchQuery() {
        searchQuery
            .debounce(300.milliseconds)
            .filter { query -> query.length >= 2 }
            .distinctUntilChanged() // Only process if the query has actually changed
            .onEach { setState(BaseUiState.Loading) }
            .flatMapLatest { query ->
                searchArtists(query).map { result ->
                    result.fold(
                        onSuccess = { setState(BaseUiState.Success(it)) },
                        onFailure = { error -> setState(BaseUiState.Error(error.asUiText(), error.asUiIcon())) },
                    )
                }
            }.catch {
                val error = DataError.Network.UNKNOWN
                setState(BaseUiState.Error(error.asUiText(), error.asUiIcon()))
            }
            .launchIn(viewModelScope)
    }

    private fun onArtistClicked(artist: Artist) {
        sendEffect(BaseUiEffect.NavigateToDetail(artist))
    }

    private companion object {
        const val SEARCH_QUERY_KEY = "search_query"
    }
}
