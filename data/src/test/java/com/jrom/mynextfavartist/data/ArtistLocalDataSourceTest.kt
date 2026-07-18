package com.jrom.mynextfavartist.data

import com.jrom.mynextfavartist.data.MockData.radioheadDbData
import com.jrom.mynextfavartist.data.MockData.radioheadEntity
import com.jrom.mynextfavartist.data.db.ArtistDao
import com.jrom.mynextfavartist.data.repository.ArtistLocalDataSource
import com.jrom.mynextfavartist.domain.dataOrNull
import com.jrom.mynextfavartist.testutils.TestBase
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
    fun `getAllArtists calls getAllArtists on ArtistDao`() = runUnconfinedTest {
        whenever(artistDao.getAllArtists()).thenReturn(listOf(radioheadDbData))

        val result = sut.getAllArtists()

        verify(artistDao).getAllArtists()
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
    fun `checkIfArtistIsFavorite calls getArtist on ArtistDao`() = runUnconfinedTest {
        sut.checkIfArtistIsFavorite(radioheadEntity.mbid)

        verify(artistDao).getArtist(radioheadEntity.mbid)
    }
}
