package com.jrom.mynextfavartist.ui.favorites

import com.jrom.mynextfavartist.domain.Result
import com.jrom.mynextfavartist.domain.error.DataError
import com.jrom.mynextfavartist.domain.usecase.ObserveFavoriteArtists
import com.jrom.mynextfavartist.domain.usecase.RemoveAllFavoriteArtists
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
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest : TestBase() {

    private val observeFavoriteArtists: ObserveFavoriteArtists = mock()
    private val removeAllFavoriteArtists: RemoveAllFavoriteArtists = mock()

    private lateinit var sut: FavoritesViewModel

    private val artistsList = MockData.testArtistsEntityList

    @Before
    fun setUp() {
        sut = FavoritesViewModel(
            observeFavoriteArtists = observeFavoriteArtists,
            removeAllFavoriteArtists = removeAllFavoriteArtists,
        )
    }

    @Test
    fun `loadFavoriteArtists success with results - uiState is Success with artists`() = runUnconfinedTest {
        whenever(observeFavoriteArtists()).thenReturn(flowOf(Result.Success(artistsList)))

        sut.handleAction(FavoritesUiAction.LoadArtists)
        advanceUntilIdle()

        assertEquals(BaseUiState.Success(artistsList), sut.uiState.value)
    }

    @Test
    fun `loadFavoriteArtists success with empty list - uiState falls back to Initial`() = runUnconfinedTest {
        whenever(observeFavoriteArtists()).thenReturn(flowOf(Result.Success(emptyList())))

        sut.handleAction(FavoritesUiAction.LoadArtists)
        advanceUntilIdle()

        assertEquals(BaseUiState.Initial, sut.uiState.value)
    }

    @Test
    fun `loadFavoriteArtists failure - uiState is Error`() = runUnconfinedTest {
        whenever(observeFavoriteArtists()).thenReturn(flowOf(Result.Failure(DataError.Local.DB_READ_ERROR)))

        sut.handleAction(FavoritesUiAction.LoadArtists)
        advanceUntilIdle()

        val errorState = sut.uiState.value as BaseUiState.Error
        val errorText = errorState.errorText as UiText.StringResource
        assertEquals(R.string.db_read_error, errorText.id)
        assertEquals(DataError.Local.DB_READ_ERROR.asUiIcon(), errorState.errorIcon)
    }

    @Test
    fun `removeAllFavorites success - resets to Initial`() = runUnconfinedTest {
        whenever(removeAllFavoriteArtists()).thenReturn(Result.Success(Unit))

        sut.handleAction(FavoritesUiAction.ClearAllSavedArtists)
        advanceUntilIdle()

        assertEquals(BaseUiState.Initial, sut.uiState.value)
    }

    @Test
    fun `removeAllFavorites failure - uiState is Error`() = runUnconfinedTest {
        whenever(removeAllFavoriteArtists()).thenReturn(Result.Failure(DataError.Local.DB_WRITE_ERROR))

        sut.handleAction(FavoritesUiAction.ClearAllSavedArtists)
        advanceUntilIdle()

        val errorState = sut.uiState.value as BaseUiState.Error
        val errorText = errorState.errorText as UiText.StringResource
        assertEquals(R.string.db_write_error, errorText.id)
        assertEquals(DataError.Local.DB_WRITE_ERROR.asUiIcon(), errorState.errorIcon)
    }

    @Test
    fun `artist clicked emits navigate ui effect`() = runUnconfinedTest {
        val emissions = mutableListOf<BaseUiEffect>()
        val effectJob = launch(unconfinedTestDispatcher) {
            sut.uiEffect.collect { emissions.add(it) }
        }

        sut.handleAction(FavoritesUiAction.ArtistClicked(MockData.radioheadEntity))
        advanceUntilIdle()

        assertEquals(listOf(BaseUiEffect.NavigateToDetail(MockData.radioheadEntity)), emissions)

        effectJob.cancel()
    }
}
