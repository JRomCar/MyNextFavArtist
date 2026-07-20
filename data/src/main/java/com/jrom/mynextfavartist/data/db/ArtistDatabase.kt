package com.jrom.mynextfavartist.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jrom.mynextfavartist.data.entities.ArtistDbData
import com.jrom.mynextfavartist.data.entities.HomeArtistCacheData

// Schema JSON is exported to data/schemas (see the room.schemaLocation ksp arg in
// data/build.gradle.kts) so future version bumps can be tested against a real prior schema.
// Deliberately no fallbackToDestructiveMigration() in DatabaseModule.kt - a version bump without
// a hand-written Migration should fail loudly during development, not silently wipe favorites on
// upgrade.
@Database(
    entities = [ArtistDbData::class, HomeArtistCacheData::class],
    version = 2,
    exportSchema = true
)
abstract class ArtistDatabase : RoomDatabase() {
    abstract fun artistDao(): ArtistDao
    abstract fun homeArtistCacheDao(): HomeArtistCacheDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `home_artists_cache` (" +
                "`mbid` TEXT NOT NULL, `name` TEXT NOT NULL, `type` TEXT, `country` TEXT, " +
                "`disambiguation` TEXT, `cachedAt` INTEGER NOT NULL, PRIMARY KEY(`mbid`))"
        )
    }
}
