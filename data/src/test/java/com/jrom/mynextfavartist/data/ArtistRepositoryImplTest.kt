package com.jrom.mynextfavartist.data

import com.jrom.mynextfavartist.data.MockData.radioheadEntity
import com.jrom.mynextfavartist.data.MockData.testArtistsEntityList
import com.jrom.mynextfavartist.data.MockData.testReleaseGroupsEntityList
import com.jrom.mynextfavartist.data.repository.ArtistDataSource
import com.jrom.mynextfavartist.data.repository.ArtistRepositoryImpl
import com.jrom.mynextfavartist.domain.Result
import com.jrom.mynextfavartist.domain.dataOrNull
import com.jrom.mynextfavartist.domain.error.DataError
import com.jrom.mynextfavartist.domain.errorOrNull
import com.jrom.mynextfavartist.domain.usecase.SeedArtists
import com.jrom.mynextfavartist.testutils.TestBase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class ArtistRepositoryImplTest : TestBase() {

    private val remote: ArtistDataSource.Remote = mock()
    private val local: ArtistDataSource.Local = mock()
    private val homeCache: ArtistDataSource.HomeCache = mock()

    private lateinit var sut: ArtistRepositoryImpl

    @Before
    fun setUp() {
        sut = ArtistRepositoryImpl(remote, local, homeCache)
    }

    @Test
    fun `searchArtists returns success when remote search is successful`() = runUnconfinedTest {
        val query = "Radiohead"
        whenever(remote.searchArtists(query, 30, 0)).thenReturn(Result.Success(testArtistsEntityList))

        val result = sut.searchArtists(query, 30, 0)

        assertTrue(result.isSuccess)
        assertEquals(testArtistsEntityList, result.dataOrNull)
    }

    @Test
    fun `searchArtists returns failure when remote search fails`() = runUnconfinedTest {
        val query = "Radiohead"
        val error = DataError.Network.SERVER_ERROR
        whenever(remote.searchArtists(query, 30, 0)).thenReturn(Result.Error(error))

        val result = sut.searchArtists(query, 30, 0)

        assertTrue(result.isFailure)
        assertEquals(error, result.errorOrNull)
    }

    @Test
    fun `getHomeArtists returns the fresh cache without calling remote`() = runUnconfinedTest {
        whenever(homeCache.getFreshHomeArtists(any())).thenReturn(testArtistsEntityList)

        val result = sut.getHomeArtists()

        assertTrue(result.isSuccess)
        assertEquals(testArtistsEntityList, result.dataOrNull)
        verify(remote, never()).searchByArtistIds(any())
    }

    @Test
    fun `getHomeArtists fetches from remote and replaces the cache when stale`() = runUnconfinedTest {
        whenever(homeCache.getFreshHomeArtists(any())).thenReturn(null)
        whenever(remote.searchByArtistIds(any())).thenReturn(Result.Success(testArtistsEntityList))

        val result = sut.getHomeArtists()

        assertTrue(result.isSuccess)
        assertEquals(testArtistsEntityList, result.dataOrNull)
        verify(homeCache).replaceHomeArtists(testArtistsEntityList)
        verify(remote).searchByArtistIds(SeedArtists.mbids)
    }

    @Test
    fun `getHomeArtists falls back to a stale cache when remote fails`() = runUnconfinedTest {
        whenever(homeCache.getFreshHomeArtists(any())).thenReturn(null)
        whenever(remote.searchByArtistIds(any())).thenReturn(Result.Error(DataError.Network.SERVER_ERROR))
        whenever(homeCache.getStaleHomeArtists()).thenReturn(testArtistsEntityList)

        val result = sut.getHomeArtists()

        assertTrue(result.isSuccess)
        assertEquals(testArtistsEntityList, result.dataOrNull)
    }

    @Test
    fun `getHomeArtists propagates the remote error when there is no cache at all`() = runUnconfinedTest {
        val error = DataError.Network.SERVER_ERROR
        whenever(homeCache.getFreshHomeArtists(any())).thenReturn(null)
        whenever(remote.searchByArtistIds(any())).thenReturn(Result.Error(error))
        whenever(homeCache.getStaleHomeArtists()).thenReturn(null)

        val result = sut.getHomeArtists()

        assertTrue(result.isFailure)
        assertEquals(error, result.errorOrNull)
    }

    @Test
    fun `getArtistReleaseGroups delegates to remote`() = runUnconfinedTest {
        whenever(remote.getArtistReleaseGroups(radioheadEntity.mbid))
            .thenReturn(Result.Success(testReleaseGroupsEntityList))

        val result = sut.getArtistReleaseGroups(radioheadEntity.mbid)

        assertTrue(result.isSuccess)
        assertEquals(testReleaseGroupsEntityList, result.dataOrNull)
    }

    @Test
    fun `observeFavoriteArtists delegates to local`() = runUnconfinedTest {
        whenever(local.observeAllArtists()).thenReturn(flowOf(Result.Success(testArtistsEntityList)))

        val result = sut.observeFavoriteArtists().first()

        assertTrue(result.isSuccess)
        assertEquals(testArtistsEntityList, result.dataOrNull)
    }

    @Test
    fun `saveFavoriteArtist delegates to local`() = runUnconfinedTest {
        whenever(local.saveFavoriteArtist(radioheadEntity)).thenReturn(Result.Success(Unit))

        val result = sut.saveFavoriteArtist(radioheadEntity)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `removeFavoriteArtist delegates to local`() = runUnconfinedTest {
        whenever(local.removeFavoriteArtist(radioheadEntity.mbid)).thenReturn(Result.Success(Unit))

        val result = sut.removeFavoriteArtist(radioheadEntity.mbid)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `removeAllFavoriteArtists delegates to local clearArtists`() = runUnconfinedTest {
        whenever(local.clearArtists()).thenReturn(Result.Success(Unit))

        val result = sut.removeAllFavoriteArtists()

        assertTrue(result.isSuccess)
    }

    @Test
    fun `observeIsFavorite delegates to local`() = runUnconfinedTest {
        whenever(local.observeIsFavorite(radioheadEntity.mbid)).thenReturn(flowOf(Result.Success(true)))

        val result = sut.observeIsFavorite(radioheadEntity.mbid).first()

        assertTrue(result.isSuccess)
        assertTrue(result.dataOrNull == true)
    }
}
