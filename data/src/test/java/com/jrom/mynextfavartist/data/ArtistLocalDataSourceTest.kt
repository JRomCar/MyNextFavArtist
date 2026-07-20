package com.jrom.mynextfavartist.data

import com.jrom.mynextfavartist.data.MockData.radioheadDbData
import com.jrom.mynextfavartist.data.MockData.radioheadEntity
import com.jrom.mynextfavartist.data.db.ArtistDao
import com.jrom.mynextfavartist.data.repository.ArtistLocalDataSource
import com.jrom.mynextfavartist.domain.dataOrNull
import com.jrom.mynextfavartist.testutils.TestBase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
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
    fun `saveFavoriteArtist calls saveArtist on ArtistDao`() = runUnconfinedTest {
        sut.saveFavoriteArtist(radioheadEntity)

        verify(artistDao).saveArtist(radioheadDbData)
    }

    @Test
    fun `removeFavoriteArtist calls removeArtist on ArtistDao`() = runUnconfinedTest {
        sut.removeFavoriteArtist(radioheadEntity.mbid)

        verify(artistDao).removeArtist(radioheadEntity.mbid)
    }

    @Test
    fun `clearArtists calls clearArtists on ArtistDao`() = runUnconfinedTest {
        sut.clearArtists()

        verify(artistDao).clearArtists()
    }

    @Test
    fun `observeIsFavorite calls observeIsFavorite on ArtistDao`() = runUnconfinedTest {
        whenever(artistDao.observeIsFavorite(radioheadEntity.mbid)).thenReturn(flowOf(true))

        val result = sut.observeIsFavorite(radioheadEntity.mbid).first()

        verify(artistDao).observeIsFavorite(radioheadEntity.mbid)
        assertEquals(true, result.dataOrNull)
    }
}
