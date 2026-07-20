package com.jrom.mynextfavartist.ui.home

import com.jrom.mynextfavartist.domain.Result
import com.jrom.mynextfavartist.domain.error.DataError
import com.jrom.mynextfavartist.domain.usecase.GetHomeArtists
import com.jrom.mynextfavartist.testutils.TestBase
import com.jrom.mynextfavartist.ui.MockData
import com.jrom.mynextfavartist.ui.R
import com.jrom.mynextfavartist.ui.error.UiText
import com.jrom.mynextfavartist.ui.error.asUiIcon
import com.jrom.mynextfavartist.ui.states.BaseUiEffect
import com.jrom.mynextfavartist.ui.states.BaseUiState
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest : TestBase() {

    private val getHomeArtists: GetHomeArtists = mock()

    private lateinit var sut: HomeViewModel

    private val artistsList = MockData.testArtistsEntityList

    @Before
    fun setUp() {
        sut = HomeViewModel(getHomeArtists = getHomeArtists)
    }

    @Test
    fun `load artists success - uiState is Success with artists`() = runUnconfinedTest {
        whenever(getHomeArtists()).thenReturn(Result.Success(artistsList))

        sut.handleAction(HomeUiAction.LoadArtists)
        advanceUntilIdle()

        assertEquals(BaseUiState.Success(artistsList), sut.uiState.value.artists)
        assertEquals(false, sut.uiState.value.isRefreshing)
    }

    @Test
    fun `load artists failure - uiState is Error`() = runUnconfinedTest {
        whenever(getHomeArtists()).thenReturn(Result.Failure(DataError.Network.UNKNOWN))

        sut.handleAction(HomeUiAction.LoadArtists)
        advanceUntilIdle()

        val errorState = sut.uiState.value.artists as BaseUiState.Error
        val errorText = errorState.errorText as UiText.StringResource
        assertEquals(R.string.unknown_error, errorText.id)
        assertEquals(DataError.Network.UNKNOWN.asUiIcon(), errorState.errorIcon)
        assertEquals(false, sut.uiState.value.isRefreshing)
    }

    @Test
    fun `reload failure after a prior success keeps showing the cached list`() = runUnconfinedTest {
        whenever(getHomeArtists()).thenReturn(Result.Success(artistsList))
        sut.handleAction(HomeUiAction.LoadArtists)
        advanceUntilIdle()
        assertEquals(BaseUiState.Success(artistsList), sut.uiState.value.artists)

        whenever(getHomeArtists()).thenReturn(Result.Failure(DataError.Network.UNKNOWN))
        sut.handleAction(HomeUiAction.LoadArtists)
        advanceUntilIdle()

        assertEquals(BaseUiState.Success(artistsList), sut.uiState.value.artists)
        assertEquals(false, sut.uiState.value.isRefreshing)
    }

    @Test
    fun `refreshing a populated list sets isRefreshing without clearing the cached list`() = runUnconfinedTest {
        whenever(getHomeArtists()).thenReturn(Result.Success(artistsList))
        sut.handleAction(HomeUiAction.LoadArtists)
        advanceUntilIdle()
        assertEquals(BaseUiState.Success(artistsList), sut.uiState.value.artists)

        val states = mutableListOf<Boolean>()
        val job = launch(unconfinedTestDispatcher) {
            sut.uiState.collect { states.add(it.isRefreshing) }
        }

        sut.handleAction(HomeUiAction.LoadArtists)
        advanceUntilIdle()

        assertEquals(listOf(false, true, false), states)
        assertEquals(BaseUiState.Success(artistsList), sut.uiState.value.artists)

        job.cancel()
    }

    @Test
    fun `artist clicked emits navigate ui effect`() = runUnconfinedTest {
        val emissions = mutableListOf<BaseUiEffect>()
        val effectJob = launch(unconfinedTestDispatcher) {
            sut.uiEffect.collect { emissions.add(it) }
        }

        sut.handleAction(HomeUiAction.ArtistClicked(MockData.radioheadEntity))
        advanceUntilIdle()

        assertEquals(listOf(BaseUiEffect.NavigateToDetail(MockData.radioheadEntity)), emissions)

        effectJob.cancel()
    }
}
