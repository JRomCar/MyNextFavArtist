package com.jrom.mynextfavartist.ui.details

import com.jrom.mynextfavartist.domain.Result
import com.jrom.mynextfavartist.domain.error.DataError
import com.jrom.mynextfavartist.domain.usecase.GetArtistReleaseGroups
import com.jrom.mynextfavartist.domain.usecase.ObserveIsFavorite
import com.jrom.mynextfavartist.domain.usecase.RemoveFavoriteArtist
import com.jrom.mynextfavartist.domain.usecase.SaveFavoriteArtist
import com.jrom.mynextfavartist.testutils.TestBase
import com.jrom.mynextfavartist.ui.MockData
import com.jrom.mynextfavartist.ui.states.BaseUiState
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class DetailsViewModelTest : TestBase() {
    private val observeIsFavorite: ObserveIsFavorite = mock()
    private val saveFavoriteArtist: SaveFavoriteArtist = mock()
    private val removeFavoriteArtist: RemoveFavoriteArtist = mock()
    private val getArtistReleaseGroups: GetArtistReleaseGroups = mock()

    private lateinit var sut: DetailsViewModel

    private val artist = MockData.radioheadEntity

    @Before
    fun setUp() {
        // Safe default so subscribing to uiState (which triggers onSubscribed's
        // favorite-status load) never hits an unstubbed mock in tests that don't care about the
        // loaded value itself - override per-test as needed. getArtistReleaseGroups can't be
        // defaulted here too since it's suspend and this isn't a suspend function; tests that
        // don't stub it themselves do so inline instead.
        whenever(observeIsFavorite(artist.mbid)).thenReturn(flowOf(Result.Success(false)))
        sut = DetailsViewModel(
            artist = artist,
            observeIsFavorite = observeIsFavorite,
            saveFavoriteArtist = saveFavoriteArtist,
            removeFavoriteArtist = removeFavoriteArtist,
            getArtistReleaseGroups = getArtistReleaseGroups,
        )
    }

    @Test
    fun `load artist details sets isFavorite and release groups`() = runUnconfinedTest {
        whenever(observeIsFavorite(artist.mbid)).thenReturn(flowOf(Result.Success(true)))
        whenever(getArtistReleaseGroups(artist.mbid)).thenReturn(
            Result.Success(MockData.testReleaseGroupsEntityList)
        )

        val stateJob = launch(unconfinedTestDispatcher) { sut.uiState.collect {} }
        advanceUntilIdle()

        val state = sut.uiState.value
        assertTrue(state.isFavorite)
        assertEquals(
            BaseUiState.Success(MockData.testReleaseGroupsEntityList),
            state.releaseGroups
        )

        stateJob.cancel()
    }

    @Test
    fun `release groups failure sets Error state`() = runUnconfinedTest {
        whenever(observeIsFavorite(artist.mbid)).thenReturn(flowOf(Result.Success(false)))
        whenever(getArtistReleaseGroups(artist.mbid)).thenReturn(
            Result.Failure(DataError.Network.UNKNOWN)
        )

        val stateJob = launch(unconfinedTestDispatcher) { sut.uiState.collect {} }
        advanceUntilIdle()

        assertTrue(sut.uiState.value.releaseGroups is BaseUiState.Error)

        stateJob.cancel()
    }

    @Test
    fun `retrying release groups only re-fetches release groups, not favorite status`() = runUnconfinedTest {
        whenever(getArtistReleaseGroups(artist.mbid)).thenReturn(Result.Failure(DataError.Network.UNKNOWN))

        val stateJob = launch(unconfinedTestDispatcher) { sut.uiState.collect {} }
        advanceUntilIdle()
        assertTrue(sut.uiState.value.releaseGroups is BaseUiState.Error)

        whenever(getArtistReleaseGroups(artist.mbid)).thenReturn(
            Result.Success(MockData.testReleaseGroupsEntityList)
        )
        sut.handleAction(DetailsUiAction.RetryReleaseGroups)
        advanceUntilIdle()

        assertEquals(
            BaseUiState.Success(MockData.testReleaseGroupsEntityList),
            sut.uiState.value.releaseGroups
        )
        // observeIsFavorite's collector was only ever started once, by onSubscribed -
        // the retry above didn't restart it.
        verify(observeIsFavorite).invoke(artist.mbid)

        stateJob.cancel()
    }

    @Test
    fun `toggle favorite calls saveFavorite when not already favorite`() = runUnconfinedTest {
        whenever(getArtistReleaseGroups(artist.mbid)).thenReturn(Result.Success(emptyList()))
        whenever(saveFavoriteArtist(artist)).thenReturn(Result.Success(Unit))

        val stateJob = launch(unconfinedTestDispatcher) { sut.uiState.collect {} }
        advanceUntilIdle()

        sut.handleAction(DetailsUiAction.ToggleFavorite)
        advanceUntilIdle()

        verify(saveFavoriteArtist).invoke(artist)
        assertFalse(sut.uiState.value.isFavoriteActionInProgress)

        stateJob.cancel()
    }

    @Test
    fun `toggle favorite calls removeFavorite when already favorite`() = runUnconfinedTest {
        // isFavorite now comes solely from the observed Flow (single source of truth), not
        // from a prior toggle's write - so "already favorite" is set up via the Flow here.
        whenever(observeIsFavorite(artist.mbid)).thenReturn(flowOf(Result.Success(true)))
        whenever(getArtistReleaseGroups(artist.mbid)).thenReturn(
            Result.Success(MockData.testReleaseGroupsEntityList)
        )
        whenever(removeFavoriteArtist(artist.mbid)).thenReturn(Result.Success(Unit))

        val stateJob = launch(unconfinedTestDispatcher) { sut.uiState.collect {} }
        advanceUntilIdle()
        assertTrue(sut.uiState.value.isFavorite)

        sut.handleAction(DetailsUiAction.ToggleFavorite)
        advanceUntilIdle()

        verify(removeFavoriteArtist).invoke(artist.mbid)
        assertFalse(sut.uiState.value.isFavoriteActionInProgress)

        stateJob.cancel()
    }

    @Test
    fun `save favorite failure emits ShowMessage effect and resets progress flag`() = runUnconfinedTest {
        whenever(getArtistReleaseGroups(artist.mbid)).thenReturn(Result.Success(emptyList()))
        whenever(saveFavoriteArtist(artist)).thenReturn(Result.Failure(DataError.Local.DB_WRITE_ERROR))

        val emissions = mutableListOf<DetailsUiEffect>()
        val effectJob = launch(unconfinedTestDispatcher) {
            sut.uiEffect.collect { emissions.add(it) }
        }

        val stateJob = launch(unconfinedTestDispatcher) { sut.uiState.collect {} }
        advanceUntilIdle()

        sut.handleAction(DetailsUiAction.ToggleFavorite)
        advanceUntilIdle()

        assertFalse(sut.uiState.value.isFavorite)
        assertFalse(sut.uiState.value.isFavoriteActionInProgress)
        assertTrue(emissions.any { it is DetailsUiEffect.ShowMessage })

        effectJob.cancel()
        stateJob.cancel()
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
