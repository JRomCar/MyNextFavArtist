package com.jrom.mynextfavartist.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jrom.mynextfavartist.data.entities.ArtistDbData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

// Runs against a real in-memory Room database (not a mock), so the raw @Query strings, the mbid
// primary key, ORDER BY, and Room's Flow invalidation tracker are all genuinely exercised -
// none of that is expressible against a mocked ArtistDao.
@RunWith(AndroidJUnit4::class)
class ArtistDaoTest {

    private lateinit var database: ArtistDatabase
    private lateinit var artistDao: ArtistDao

    private val radiohead = ArtistDbData(
        mbid = "a74b1b7f-71a5-4011-9441-d0b5e4122711",
        name = "Radiohead",
        type = "Group",
        country = "GB",
        disambiguation = null,
    )
    private val theBeatles = ArtistDbData(
        mbid = "b10bbbfc-cf9e-42e0-be17-e2c3e1d2600d",
        name = "The Beatles",
        type = "Group",
        country = "GB",
        disambiguation = null,
    )

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(context, ArtistDatabase::class.java).build()
        artistDao = database.artistDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun observeAllArtists_ordersByNameAscending() = runBlocking {
        artistDao.saveArtist(radiohead)
        artistDao.saveArtist(theBeatles)

        val artists = artistDao.observeAllArtists().first()

        assertEquals(listOf(theBeatles, radiohead), artists)
    }

    @Test
    fun saveArtist_causesObserveAllArtists_toReEmit() = runBlocking {
        val emissions = mutableListOf<List<ArtistDbData>>()
        val collectorJob = launch { artistDao.observeAllArtists().collect { emissions.add(it) } }

        withTimeout(5_000) {
            while (emissions.isEmpty()) {
                delay(20)
            }
        }
        artistDao.saveArtist(radiohead)
        // Room's InvalidationTracker checks for table changes on a background thread rather than
        // synchronously with the write, so the re-emission isn't guaranteed to be immediate.
        withTimeout(5_000) {
            while (emissions.size < 2) {
                delay(20)
            }
        }
        collectorJob.cancel()

        assertEquals(emptyList<ArtistDbData>(), emissions[0])
        assertEquals(listOf(radiohead), emissions[1])
    }

    @Test
    fun observeIsFavorite_reflectsWhetherArtistIsSaved() = runBlocking {
        assertEquals(false, artistDao.observeIsFavorite(radiohead.mbid).first())

        artistDao.saveArtist(radiohead)

        assertEquals(true, artistDao.observeIsFavorite(radiohead.mbid).first())
    }

    @Test
    fun removeArtist_deletesMatchingRow_andReturnsRowCount() = runBlocking {
        artistDao.saveArtist(radiohead)

        val rowsDeleted = artistDao.removeArtist(radiohead.mbid)

        assertEquals(1, rowsDeleted)
        assertTrue(artistDao.observeAllArtists().first().isEmpty())
    }

    @Test
    fun removeArtist_returnsZero_whenNoRowMatched() = runBlocking {
        val rowsDeleted = artistDao.removeArtist("nonexistent-mbid")

        assertEquals(0, rowsDeleted)
    }

    @Test
    fun clearArtists_deletesAllRows_andReturnsRowCount() = runBlocking {
        artistDao.saveArtist(radiohead)
        artistDao.saveArtist(theBeatles)

        val rowsDeleted = artistDao.clearArtists()

        assertEquals(2, rowsDeleted)
        assertTrue(artistDao.observeAllArtists().first().isEmpty())
    }

    @Test
    fun saveArtist_replacesExistingRow_onConflict() = runBlocking {
        artistDao.saveArtist(radiohead)
        val updated = radiohead.copy(disambiguation = "English rock band")

        artistDao.saveArtist(updated)

        assertEquals(listOf(updated), artistDao.observeAllArtists().first())
    }
}
