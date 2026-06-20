package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    // Event queries
    @Query("SELECT * FROM events WHERE isReported = 0 ORDER BY datetimeEpoch ASC")
    fun getAllEventsFlow(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :id")
    fun getEventByIdFlow(id: Int): Flow<EventEntity?>

    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun getEventById(id: Int): EventEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<EventEntity>)

    @Update
    suspend fun updateEvent(event: EventEntity)

    @Delete
    suspend fun deleteEvent(event: EventEntity)

    // Organizer queries
    @Query("SELECT * FROM organizers ORDER BY name ASC")
    fun getAllOrganizersFlow(): Flow<List<OrganizerEntity>>

    @Query("SELECT * FROM organizers WHERE id = :id")
    fun getOrganizerByIdFlow(id: Int): Flow<OrganizerEntity?>

    @Query("SELECT * FROM organizers WHERE id = :id")
    suspend fun getOrganizerById(id: Int): OrganizerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrganizer(organizer: OrganizerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrganizers(organizers: List<OrganizerEntity>)

    @Update
    suspend fun updateOrganizer(organizer: OrganizerEntity)

    // Comments queries
    @Query("SELECT * FROM comments WHERE eventId = :eventId ORDER BY timestampEpoch ASC")
    fun getCommentsForEventFlow(eventId: Int): Flow<List<CommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity): Long

    // Vendors queries
    @Query("SELECT * FROM vendors WHERE marketId = :marketId")
    fun getVendorsForMarketFlow(marketId: Int): Flow<List<VendorEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVendor(vendor: VendorEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVendors(vendors: List<VendorEntity>)

    // User preferences queries
    @Query("SELECT * FROM user_preferences WHERE userId = :userId")
    fun getUserPreferencesFlow(userId: String = "local_user"): Flow<UserPreferencesEntity?>

    @Query("SELECT * FROM user_preferences WHERE userId = :userId")
    suspend fun getUserPreferences(userId: String = "local_user"): UserPreferencesEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserPreferences(prefs: UserPreferencesEntity)
}
