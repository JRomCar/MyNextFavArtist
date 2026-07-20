package com.jrom.mynextfavartist.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jrom.mynextfavartist.data.entities.HomeArtistCacheData
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

// The TTL check (WHERE cachedAt >= :minCachedAt) is real SQL, not something a mocked DAO can
// verify - this exercises the boundary directly against a real in-memory database.
@RunWith(AndroidJUnit4::class)
class HomeArtistCacheDaoTest {

    private lateinit var database: ArtistDatabase
    private lateinit var homeArtistCacheDao: HomeArtistCacheDao

    private val freshArtist = HomeArtistCacheData(
        mbid = "a74b1b7f-71a5-4011-9441-d0b5e4122711",
        name = "Radiohead",
        type = "Group",
        country = "GB",
        disambiguation = null,
        cachedAt = 10_000L,
    )
    private val staleArtist = HomeArtistCacheData(
        mbid = "b10bbbfc-cf9e-42e0-be17-e2c3e1d2600d",
        name = "The Beatles",
        type = "Group",
        country = "GB",
        disambiguation = null,
        cachedAt = 1_000L,
    )

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(context, ArtistDatabase::class.java).build()
        homeArtistCacheDao = database.homeArtistCacheDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun getFreshArtists_excludesRowsOlderThanMinCachedAt() = runBlocking {
        homeArtistCacheDao.insertAll(listOf(freshArtist, staleArtist))

        val fresh = homeArtistCacheDao.getFreshArtists(minCachedAt = 5_000L)

        assertEquals(listOf(freshArtist), fresh)
    }

    @Test
    fun getAllArtists_returnsEveryRowRegardlessOfAge() = runBlocking {
        homeArtistCacheDao.insertAll(listOf(freshArtist, staleArtist))

        val all = homeArtistCacheDao.getAllArtists()

        assertEquals(listOf(freshArtist, staleArtist), all)
    }

    @Test
    fun replaceAll_clearsPreviousRows_beforeInsertingNewOnes() = runBlocking {
        homeArtistCacheDao.insertAll(listOf(staleArtist))

        homeArtistCacheDao.replaceAll(listOf(freshArtist))

        assertEquals(listOf(freshArtist), homeArtistCacheDao.getAllArtists())
    }

    @Test
    fun getFreshArtists_returnsEmpty_whenCacheIsEmpty() = runBlocking {
        assertTrue(homeArtistCacheDao.getFreshArtists(minCachedAt = 0L).isEmpty())
    }
}
