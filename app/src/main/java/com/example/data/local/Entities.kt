package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String, // Markets, Sports, Hobby, Civic, Music, Food, Kids, Other
    val datetimeEpoch: Long, // timestamp in ms
    val recurrence: String = "", // e.g., "Weekly", "Monthly", or empty for none
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val description: String,
    val organizerId: Int,
    val price: Double = 0.0, // 0.0 means free
    val capacity: Int = 0, // 0 means no limit
    val openSpots: Int = 0, // for Sports/Hobby spots counter
    val skillLevel: String = "", // e.g., "Beginner", "All", etc.
    val externalLink: String = "",
    val coverPhotoResName: String = "", // Name of mock asset drawable or generic theme
    val rsvpStatus: String = "NONE", // NONE, GOING, INTERESTED
    val isReported: Boolean = false,
    val isVerified: Boolean = false
)

@Entity(tableName = "organizers")
data class OrganizerEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val bio: String,
    val avatarResName: String = "",
    val category: String = "", // "Business", "Civic", "Hobby Club", "Government"
    val followerCount: Int = 0,
    val isFollowed: Boolean = false,
    val isVerified: Boolean = false
)

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val eventId: Int,
    val authorName: String,
    val content: String,
    val timestampEpoch: Long = System.currentTimeMillis()
)

@Entity(tableName = "vendors")
data class VendorEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val marketId: Int, // EventEntity.id (of type Markets)
    val name: String,
    val bio: String,
    val products: String, // Comma separated list of goods
    val logoResName: String = ""
)

@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
    @PrimaryKey val userId: String = "local_user",
    val homeTown: String = "Willow Creek",
    val zipCode: String = "97001",
    val latitude: Double = 45.3,
    val longitude: Double = -122.5,
    val radiusMiles: Int = 15,
    val followedCategories: String = "Markets,Sports,Hobby,Civic,Music,Food,Kids,Other" // Comma-separated list
)
