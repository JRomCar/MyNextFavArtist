package com.jrom.mynextfavartist.ui.search

import androidx.lifecycle.SavedStateHandle
import com.jrom.mynextfavartist.domain.Result
import com.jrom.mynextfavartist.domain.entities.Artist
import com.jrom.mynextfavartist.domain.error.DataError
import com.jrom.mynextfavartist.domain.usecase.SearchArtists
import com.jrom.mynextfavartist.testutils.TestBase
import com.jrom.mynextfavartist.ui.MockData
import com.jrom.mynextfavartist.ui.R
import com.jrom.mynextfavartist.ui.error.UiText
import com.jrom.mynextfavartist.ui.error.asUiIcon
import com.jrom.mynextfavartist.ui.states.BaseUiEffect
import com.jrom.mynextfavartist.ui.states.BaseUiState
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest : TestBase() {
    private val searchArtists: SearchArtists = mock()

    private lateinit var sut: SearchViewModel

    private val artistsList = MockData.testArtistsEntityList
    private val artistsListFlow = flowOf<Result<List<Artist>, DataError.Network>>(
        Result.Success(artistsList)
    )

    @Before
    fun setup() {
        whenever(searchArtists("radio")).thenReturn(artistsListFlow)
        sut = SearchViewModel(searchArtists, SavedStateHandle())
    }

    @Test
    fun `search request updates state to Loading then Success`() = runUnconfinedTest {
        sut.handleAction(SearchUiAction.SearchRequest("radio"))
        advanceTimeBy(300) // pass debounce
        advanceUntilIdle()

        val state = sut.uiState.value
        assertEquals(artistsList, (state as BaseUiState.Success).data)
    }

    @Test
    fun `search request updates state to Error on failure`() = runUnconfinedTest {
        whenever(searchArtists("bad")).thenReturn(flowOf(Result.Failure(DataError.Network.UNKNOWN)))

        sut.handleAction(SearchUiAction.SearchRequest("bad"))
        advanceTimeBy(300)
        advanceUntilIdle()

        val state = sut.uiState.value
        val errorState = state as BaseUiState.Error
        val errorText = errorState.errorText as UiText.StringResource
        assertEquals(R.string.unknown_error, errorText.id)
        assertEquals(DataError.Network.UNKNOWN.asUiIcon(), errorState.errorIcon)
    }

    @Test
    fun `query shorter than 2 chars is not searched`() = runUnconfinedTest {
        sut.handleAction(SearchUiAction.SearchRequest("r"))
        advanceTimeBy(300)
        advanceUntilIdle()

        assertEquals(BaseUiState.Initial, sut.uiState.value)
    }

    @Test
    fun `artist clicked emits navigate ui effect`() = runUnconfinedTest {
        val emissions = mutableListOf<BaseUiEffect>()
        val effectJob = launch(unconfinedTestDispatcher) {
            sut.uiEffect.collect { emissions.add(it) }
        }

        sut.handleAction(SearchUiAction.ArtistClicked(MockData.radioheadEntity))
        advanceUntilIdle()

        assertEquals(listOf(BaseUiEffect.NavigateToDetail(MockData.radioheadEntity)), emissions)

        effectJob.cancel()
    }
}
