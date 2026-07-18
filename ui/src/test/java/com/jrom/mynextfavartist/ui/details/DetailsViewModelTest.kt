package com.jrom.mynextfavartist.ui.details

import com.jrom.mynextfavartist.domain.Result
import com.jrom.mynextfavartist.domain.error.DataError
import com.jrom.mynextfavartist.domain.usecase.CheckIfArtistIsFavorite
import com.jrom.mynextfavartist.domain.usecase.GetArtistReleaseGroups
import com.jrom.mynextfavartist.domain.usecase.RemoveFavoriteArtist
import com.jrom.mynextfavartist.domain.usecase.SaveFavoriteArtist
import com.jrom.mynextfavartist.testutils.TestBase
import com.jrom.mynextfavartist.ui.MockData
import com.jrom.mynextfavartist.ui.states.BaseUiState
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class DetailsViewModelTest : TestBase() {
    private val checkIfArtistIsFavorite: CheckIfArtistIsFavorite = mock()
    private val saveFavoriteArtist: SaveFavoriteArtist = mock()
    private val removeFavoriteArtist: RemoveFavoriteArtist = mock()
    private val getArtistReleaseGroups: GetArtistReleaseGroups = mock()

    private lateinit var sut: DetailsViewModel

    private val artist = MockData.radioheadEntity

    @Before
    fun setUp() {
        sut = DetailsViewModel(
            checkIfArtistIsFavorite = checkIfArtistIsFavorite,
            saveFavoriteArtist = saveFavoriteArtist,
            removeFavoriteArtist = removeFavoriteArtist,
            getArtistReleaseGroups = getArtistReleaseGroups,
        )
    }

    @Test
    fun `load artist details sets isFavorite and release groups`() = runUnconfinedTest {
        whenever(checkIfArtistIsFavorite(artist.mbid)).thenReturn(Result.Success(true))
        whenever(getArtistReleaseGroups(artist.mbid)).thenReturn(
            Result.Success(MockData.testReleaseGroupsEntityList)
        )

        sut.handleAction(DetailsUiAction.LoadArtistDetails(artist))
        advanceUntilIdle()

        val state = sut.uiState.value
        assertTrue(state.isFavorite)
        assertEquals(
            BaseUiState.Success(MockData.testReleaseGroupsEntityList),
            state.releaseGroups
        )
    }

    @Test
    fun `release groups failure sets Error state`() = runUnconfinedTest {
        whenever(checkIfArtistIsFavorite(artist.mbid)).thenReturn(Result.Success(false))
        whenever(getArtistReleaseGroups(artist.mbid)).thenReturn(
            Result.Error(DataError.Network.UNKNOWN)
        )

        sut.handleAction(DetailsUiAction.LoadArtistDetails(artist))
        advanceUntilIdle()

        assertTrue(sut.uiState.value.releaseGroups is BaseUiState.Error)
    }

    @Test
    fun `toggle favorite calls saveFavorite when not already favorite`() = runUnconfinedTest {
        whenever(saveFavoriteArtist(artist)).thenReturn(Result.Success(true))

        sut.handleAction(DetailsUiAction.ToggleFavorite(artist))
        advanceUntilIdle()

        val state = sut.uiState.value
        assertTrue(state.isFavorite)
        assertFalse(state.isFavoriteActionInProgress)
    }

    @Test
    fun `toggle favorite calls removeFavorite when already favorite`() = runUnconfinedTest {
        whenever(saveFavoriteArtist(artist)).thenReturn(Result.Success(true))
        sut.handleAction(DetailsUiAction.ToggleFavorite(artist))
        advanceUntilIdle()
        assertTrue(sut.uiState.value.isFavorite)

        whenever(removeFavoriteArtist(artist.mbid)).thenReturn(Result.Success(true))
        sut.handleAction(DetailsUiAction.ToggleFavorite(artist))
        advanceUntilIdle()

        val state = sut.uiState.value
        assertFalse(state.isFavorite)
        assertFalse(state.isFavoriteActionInProgress)
    }

    @Test
    fun `save favorite soft failure emits ShowMessage effect and resets progress flag`() = runUnconfinedTest {
        whenever(saveFavoriteArtist(artist)).thenReturn(Result.Success(false))

        val emissions = mutableListOf<DetailsUiEffect>()
        val effectJob = launch(unconfinedTestDispatcher) {
            sut.uiEffect.collect { emissions.add(it) }
        }

        sut.handleAction(DetailsUiAction.ToggleFavorite(artist))
        advanceUntilIdle()

        assertFalse(sut.uiState.value.isFavorite)
        assertFalse(sut.uiState.value.isFavoriteActionInProgress)
        assertTrue(emissions.any { it is DetailsUiEffect.ShowMessage })

        effectJob.cancel()
    }

    @Test
    fun `on back request emits navigate back ui effect`() = runUnconfinedTest {
        val emissions = mutableListOf<DetailsUiEffect>()
        val effectJob = launch(unconfinedTestDispatcher) {
            sut.uiEffect.collect { emissions.add(it) }
        }

        sut.handleAction(DetailsUiAction.OnBackRequest)
        advanceUntilIdle()

        assertEquals(listOf(DetailsUiEffect.NavigateBack), emissions)

        effectJob.cancel()
    }
}
