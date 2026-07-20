package com.jrom.mynextfavartist.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jrom.mynextfavartist.data.entities.ArtistDbData

// Schema JSON is exported to data/schemas (see the room.schemaLocation ksp arg in
// data/build.gradle.kts) so future version bumps can be tested against a real prior schema.
// Deliberately no fallbackToDestructiveMigration() in DatabaseModule.kt - a version bump without
// a hand-written Migration should fail loudly during development, not silently wipe favorites on
// upgrade.
@Database(
    entities = [ArtistDbData::class],
    version = 1,
    exportSchema = true
)
abstract class ArtistDatabase : RoomDatabase() {
    abstract fun artistDao(): ArtistDao
}
