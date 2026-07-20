package com.jrom.mynextfavartist.data

import com.jrom.mynextfavartist.data.MockData.radioheadDbData
import com.jrom.mynextfavartist.data.MockData.radioheadEntity
import com.jrom.mynextfavartist.data.db.HomeArtistCacheDao
import com.jrom.mynextfavartist.data.entities.HomeArtistCacheData
import com.jrom.mynextfavartist.data.repository.HomeArtistsCacheDataSource
import com.jrom.mynextfavartist.testutils.TestBase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class HomeArtistsCacheDataSourceTest : TestBase() {

    private val homeArtistCacheDao: HomeArtistCacheDao = mock()
    private lateinit var sut: HomeArtistsCacheDataSource

    private val cachedRadiohead = HomeArtistCacheData(
        mbid = radioheadDbData.mbid,
        name = radioheadDbData.name,
        type = radioheadDbData.type,
        country = radioheadDbData.country,
        disambiguation = radioheadDbData.disambiguation,
        cachedAt = 1_000L,
    )

    @Before
    fun setUp() {
        sut = HomeArtistsCacheDataSource(homeArtistCacheDao)
    }

    @Test
    fun `getFreshHomeArtists returns mapped artists when the cache has fresh rows`() = runUnconfinedTest {
        whenever(homeArtistCacheDao.getFreshArtists(any())).thenReturn(listOf(cachedRadiohead))

        val result = sut.getFreshHomeArtists(maxAgeMillis = 60_000L)

        assertEquals(listOf(radioheadEntity), result)
    }

    @Test
    fun `getFreshHomeArtists returns null when there are no fresh rows`() = runUnconfinedTest {
        whenever(homeArtistCacheDao.getFreshArtists(any())).thenReturn(emptyList())

        val result = sut.getFreshHomeArtists(maxAgeMillis = 60_000L)

        assertNull(result)
    }

    @Test
    fun `getStaleHomeArtists returns null when the cache is empty`() = runUnconfinedTest {
        whenever(homeArtistCacheDao.getAllArtists()).thenReturn(emptyList())

        assertNull(sut.getStaleHomeArtists())
    }

    @Test
    fun `getStaleHomeArtists returns mapped artists regardless of age`() = runUnconfinedTest {
        whenever(homeArtistCacheDao.getAllArtists()).thenReturn(listOf(cachedRadiohead))

        assertEquals(listOf(radioheadEntity), sut.getStaleHomeArtists())
    }

    @Test
    fun `replaceHomeArtists stamps every row with the same cachedAt`() = runUnconfinedTest {
        sut.replaceHomeArtists(listOf(radioheadEntity))

        val captor = argumentCaptor<List<HomeArtistCacheData>>()
        verify(homeArtistCacheDao).replaceAll(captor.capture())
        val cached = captor.firstValue.single()
        assertEquals(radioheadEntity.mbid, cached.mbid)
        assertEquals(radioheadEntity.name, cached.name)
    }
}
