package com.example.data.local

import androidx.room.*

@Database(
    entities = [
        EventEntity::class,
        OrganizerEntity::class,
        CommentEntity::class,
        VendorEntity::class,
        UserPreferencesEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class EventDatabase : RoomDatabase() {
    abstract val eventDao: EventDao

    companion object {
        const val DATABASE_NAME = "mypulse_events_db"
    }
}
