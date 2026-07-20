package com.jrom.mynextfavartist.data

import com.jrom.mynextfavartist.data.MockData.radioheadDbData
import com.jrom.mynextfavartist.data.MockData.radioheadEntity
import com.jrom.mynextfavartist.data.db.ArtistDao
import com.jrom.mynextfavartist.data.repository.ArtistLocalDataSource
import com.jrom.mynextfavartist.domain.dataOrNull
import com.jrom.mynextfavartist.domain.error.DataError
import com.jrom.mynextfavartist.domain.errorOrNull
import com.jrom.mynextfavartist.testutils.TestBase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class ArtistLocalDataSourceTest : TestBase() {
    private val artistDao: ArtistDao = mock()
    private lateinit var sut: ArtistLocalDataSource

    @Before
    fun setUp() {
        sut = ArtistLocalDataSource(artistDao)
    }

    @Test
    fun `observeAllArtists calls observeAllArtists on ArtistDao`() = runUnconfinedTest {
        whenever(artistDao.observeAllArtists()).thenReturn(flowOf(listOf(radioheadDbData)))

        val result = sut.observeAllArtists().first()

        verify(artistDao).observeAllArtists()
        assertEquals(listOf(radioheadEntity), result.dataOrNull)
    }

    @Test
    fun `saveFavoriteArtist calls saveArtist on ArtistDao and succeeds`() = runUnconfinedTest {
        val result = sut.saveFavoriteArtist(radioheadEntity)

        verify(artistDao).saveArtist(radioheadDbData)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `removeFavoriteArtist succeeds when a row was deleted`() = runUnconfinedTest {
        whenever(artistDao.removeArtist(radioheadEntity.mbid)).thenReturn(1)

        val result = sut.removeFavoriteArtist(radioheadEntity.mbid)

        verify(artistDao).removeArtist(radioheadEntity.mbid)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `removeFavoriteArtist fails when no row matched`() = runUnconfinedTest {
        whenever(artistDao.removeArtist(radioheadEntity.mbid)).thenReturn(0)

        val result = sut.removeFavoriteArtist(radioheadEntity.mbid)

        assertEquals(DataError.Local.DB_WRITE_ERROR, result.errorOrNull)
    }

    @Test
    fun `clearArtists succeeds when rows were deleted`() = runUnconfinedTest {
        whenever(artistDao.clearArtists()).thenReturn(1)

        val result = sut.clearArtists()

        verify(artistDao).clearArtists()
        assertTrue(result.isSuccess)
    }

    @Test
    fun `clearArtists fails when nothing was deleted`() = runUnconfinedTest {
        whenever(artistDao.clearArtists()).thenReturn(0)

        val result = sut.clearArtists()

        assertEquals(DataError.Local.DB_WRITE_ERROR, result.errorOrNull)
    }

    @Test
    fun `observeIsFavorite calls observeIsFavorite on ArtistDao`() = runUnconfinedTest {
        whenever(artistDao.observeIsFavorite(radioheadEntity.mbid)).thenReturn(flowOf(true))

        val result = sut.observeIsFavorite(radioheadEntity.mbid).first()

        verify(artistDao).observeIsFavorite(radioheadEntity.mbid)
        assertEquals(true, result.dataOrNull)
    }
}
