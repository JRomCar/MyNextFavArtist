package com.jrom.mynextfavartist.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jrom.mynextfavartist.data.entities.ArtistDbData

@Database(
    entities = [ArtistDbData::class],
    version = 1,
    exportSchema = false
)
abstract class ArtistDatabase : RoomDatabase() {
    abstract fun artistDao(): ArtistDao
}
