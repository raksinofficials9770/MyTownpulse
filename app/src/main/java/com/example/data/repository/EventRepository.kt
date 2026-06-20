package com.example.data.repository

import com.example.data.local.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlin.math.*

class EventRepository(private val eventDao: EventDao) {

    val allEventsFlow: Flow<List<EventEntity>> = eventDao.getAllEventsFlow()
    val allOrganizersFlow: Flow<List<OrganizerEntity>> = eventDao.getAllOrganizersFlow()
    val userPreferencesFlow: Flow<UserPreferencesEntity?> = eventDao.getUserPreferencesFlow()

    fun getEventByIdFlow(id: Int): Flow<EventEntity?> = eventDao.getEventByIdFlow(id)
    fun getOrganizerByIdFlow(id: Int): Flow<OrganizerEntity?> = eventDao.getOrganizerByIdFlow(id)
    fun getCommentsForEventFlow(eventId: Int): Flow<List<CommentEntity>> = eventDao.getCommentsForEventFlow(eventId)
    fun getVendorsForMarketFlow(marketId: Int): Flow<List<VendorEntity>> = eventDao.getVendorsForMarketFlow(marketId)

    suspend fun getEventById(id: Int): EventEntity? = eventDao.getEventById(id)
    suspend fun getOrganizerById(id: Int): OrganizerEntity? = eventDao.getOrganizerById(id)
    suspend fun insertEvent(event: EventEntity): Int = eventDao.insertEvent(event).toInt()
    suspend fun updateEvent(event: EventEntity) = eventDao.updateEvent(event)
    suspend fun deleteEvent(event: EventEntity) = eventDao.deleteEvent(event)

    suspend fun updateOrganizer(organizer: OrganizerEntity) = eventDao.updateOrganizer(organizer)
    suspend fun insertComment(comment: CommentEntity) = eventDao.insertComment(comment)
    suspend fun insertVendor(vendor: VendorEntity) = eventDao.insertVendor(vendor)

    suspend fun updateUserPreferences(prefs: UserPreferencesEntity) {
        eventDao.insertUserPreferences(prefs)
    }

    suspend fun getUserPreferences(): UserPreferencesEntity {
        return eventDao.getUserPreferences() ?: UserPreferencesEntity().also {
            eventDao.insertUserPreferences(it)
        }
    }

    // Advanced filtering in Repository (used to implement local-first radius logic)
    fun calculateDistanceMiles(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 3958.8 // Radius of the Earth in miles
        val latDistance = Math.toRadians(lat2 - lat1)
        val lonDistance = Math.toRadians(lon2 - lon1)
        val a = sin(latDistance / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(lonDistance / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    // Seeding logic to populate the app on first run
    suspend fun prepopulateIfEmpty() {
        // 1. Initial User Preferences
        val currentPrefs = eventDao.getUserPreferences()
        if (currentPrefs == null) {
            eventDao.insertUserPreferences(UserPreferencesEntity())
        }

        // 2. Organizers
        val existingOrganizers = eventDao.getAllOrganizersFlow().firstOrNull() ?: emptyList()
        if (existingOrganizers.isEmpty()) {
            val orgs = listOf(
                OrganizerEntity(
                    id = 1,
                    name = "Willow Creek Farm Guild",
                    bio = "Cooperative of local organic farms, artisan bakers, and handcrafted goods in the valley.",
                    avatarResName = "farm_guild",
                    category = "Business",
                    followerCount = 142,
                    isFollowed = true,
                    isVerified = true
                ),
                OrganizerEntity(
                    id = 2,
                    name = "Community Rec Center",
                    bio = "Your home for pickup leagues, municipal sports sessions, family fun, and fitness programs.",
                    avatarResName = "rec_center",
                    category = "Civic",
                    followerCount = 89,
                    isFollowed = false,
                    isVerified = true
                ),
                OrganizerEntity(
                    id = 3,
                    name = "Public Library Council",
                    bio = "Enriching the mind of Willow Creek residents with book talks, children sessions, and tech help.",
                    avatarResName = "library_council",
                    category = "Civic",
                    followerCount = 203,
                    isFollowed = true,
                    isVerified = true
                ),
                OrganizerEntity(
                    id = 4,
                    name = "Willow Creek Acoustic Circle",
                    bio = "A group of acoustic music lovers and local singers organizing jam circles and garden sessions.",
                    avatarResName = "acoustic_circle",
                    category = "Hobby Club",
                    followerCount = 54,
                    isFollowed = false,
                    isVerified = false
                ),
                OrganizerEntity(
                    id = 5,
                    name = "Town Civic Assembly",
                    bio = "Official public services announcements, neighborhood councils, and town hall organizers.",
                    avatarResName = "civic_assembly",
                    category = "Government",
                    followerCount = 312,
                    isFollowed = false,
                    isVerified = true
                )
            )
            eventDao.insertOrganizers(orgs)
        }

        // 3. Events
        val existingEvents = eventDao.getAllEventsFlow().firstOrNull() ?: emptyList()
        if (existingEvents.isEmpty()) {
            val now = System.currentTimeMillis()
            val dayMs = 24 * 3600 * 1000L

            val events = listOf(
                EventEntity(
                    id = 1,
                    title = "Saturday Farmers & Artisans Market",
                    category = "Markets",
                    datetimeEpoch = now + (dayMs * 0) + (1000 * 3600 * 2), // Today, in 2 hours
                    recurrence = "Weekly (Every Sat)",
                    latitude = 45.312,
                    longitude = -122.493,
                    address = "Town Square Pavilion, Main St",
                    description = "Join us for the premier weekly event in Willow Creek! Over 20 vendors offering locally grown organic strawberries, microgreens, homemade honey, sourdough bread, local cheeses, and handcrafted soaps. Live fiddle music starts at 10 AM by the fountain.",
                    organizerId = 1,
                    price = 0.0,
                    capacity = 1000,
                    openSpots = 0,
                    coverPhotoResName = "market_bg",
                    rsvpStatus = "GOING",
                    isVerified = true
                ),
                EventEntity(
                    id = 2,
                    title = "Happening Now: Pickup Volleyball Slots",
                    category = "Sports",
                    datetimeEpoch = now - (1000 * 1800), // Started 30 mins ago, in progress!
                    recurrence = "Weekly (Wed & Sat)",
                    latitude = 45.321,
                    longitude = -122.502,
                    address = "Fieldhouse Court B, Rec Center",
                    description = "Fun, recreational co-ed pickup volleyball with friendly people. Standard court setup, intermediate/soft rules. We have 6 remaining open spots! Just RSVP and show up ready to play. Great for newcomers to the town to make friends.",
                    organizerId = 2,
                    price = 0.0,
                    capacity = 18,
                    openSpots = 6,
                    skillLevel = "Intermediate/Coed",
                    coverPhotoResName = "volleyball_bg",
                    rsvpStatus = "NONE",
                    isVerified = true
                ),
                EventEntity(
                    id = 3,
                    title = "Willow Creek Writers & Readers Circle",
                    category = "Hobby",
                    datetimeEpoch = now + dayMs + (1000 * 3600 * 4), // Tomorrow
                    recurrence = "Monthly (First Sun)",
                    latitude = 45.305,
                    longitude = -122.511,
                    address = "East Reading Wing, Public Library",
                    description = "Bring your current read or writing piece to share! This month we are discussing regional history books and local folklore. Tea and shortbread will be provided by the library association. Open discussion and open to all.",
                    organizerId = 3,
                    price = 0.0,
                    capacity = 30,
                    openSpots = 12,
                    skillLevel = "All Lovers of Books",
                    coverPhotoResName = "library_bg",
                    rsvpStatus = "INTERESTED",
                    isVerified = true
                ),
                EventEntity(
                    id = 4,
                    title = "Acoustic Sunset Concert on the Lawn",
                    category = "Music & Arts",
                    datetimeEpoch = now + (dayMs * 2) + (1000 * 3600 * 8), // Weekend
                    recurrence = "",
                    latitude = 45.315,
                    longitude = -122.485,
                    address = "Riverbend Park Lawn, Riverside Dr",
                    description = "Unwind under the shade trees by the river with acoustic performances from three local singer-songwriters. Bring your own lawn chairs or picnic blankets. Food trucks (Valley Tacos and Sweet Crepes) will be parked nearby starting at 5 PM.",
                    organizerId = 4,
                    price = 5.0,
                    capacity = 250,
                    openSpots = 0,
                    coverPhotoResName = "music_bg",
                    rsvpStatus = "GOING",
                    isVerified = false
                ),
                EventEntity(
                    id = 5,
                    title = "Annual Downtown Parade Planning Assembly",
                    category = "Civic",
                    datetimeEpoch = now + (dayMs * 4), // Midweek next week
                    recurrence = "",
                    latitude = 45.309,
                    longitude = -122.497,
                    address = "Civic Hall Conference Room B",
                    description = "Calling all community captains, marching units, and local shop owners! We are finalizing the route, volunteer roles, and safety points for this year's Harvest Parade. If you or your organization wants to construct a float or host a street game, please attend this public assembly.",
                    organizerId = 5,
                    price = 0.0,
                    capacity = 80,
                    openSpots = 0,
                    coverPhotoResName = "civic_bg",
                    rsvpStatus = "NONE",
                    isVerified = true
                ),
                EventEntity(
                    id = 6,
                    title = "Little Diggers: Children Seed Planting Class",
                    category = "Kids",
                    datetimeEpoch = now + (dayMs * 1) + (1000 * 3600 * 1), // Tomorrow morning
                    recurrence = "One-time workshop",
                    latitude = 45.314,
                    longitude = -122.515,
                    address = "Community Greenhouses & Gardens, Maple Rd",
                    description = "A potting workshop for toddlers and kids aged 4-10! Under the guidance of master gardeners, children will plant heirloom cherry tomatoes and marigolds in decorated bio-pots to take home. Gardening gloves, soil, seeds, and paint craft supplies provided free of charge.",
                    organizerId = 3,
                    price = 0.0,
                    capacity = 25,
                    openSpots = 8,
                    coverPhotoResName = "kids_bg",
                    rsvpStatus = "NONE",
                    isVerified = true
                ),
                EventEntity(
                    id = 7,
                    title = "Neighboring Town Craft Fair (Sparsity Test)",
                    category = "Markets",
                    datetimeEpoch = now + (dayMs * 3),
                    latitude = 45.485, // Roughly 18 miles north of Willow Creek center
                    longitude = -122.312,
                    address = "Oakwood Community Park, Oakwood",
                    description = "Discover rural woodcarving, artisan quilts, and homemade jams from Oakwood and neighboring valley artisans. This is slightly further away but is highly recommended for families wanting a scenic weekend drive.",
                    organizerId = 1,
                    price = 2.0,
                    capacity = 500,
                    openSpots = 0,
                    coverPhotoResName = "craft_bg",
                    rsvpStatus = "NONE",
                    isVerified = true
                )
            )
            eventDao.insertEvents(events)
        }

        // 4. Vendors
        val existingVendors = eventDao.getVendorsForMarketFlow(1).firstOrNull() ?: emptyList()
        if (existingVendors.isEmpty()) {
            val vendors = listOf(
                VendorEntity(
                    id = 1,
                    marketId = 1,
                    name = "Larsen Sourdough Bakery",
                    bio = "Artisan wild-yeast breads, rustic baguettes, soft cinnamon swirls, and morning croissants.",
                    products = "Rustic Sourdough, Raisin Walnuts, Focaccia, Jams",
                    logoResName = "bread_logo"
                ),
                VendorEntity(
                    id = 2,
                    marketId = 1,
                    name = "Honey & Clover Apothecary",
                    bio = "Raw, unfiltered wildflower honey harvested directly from hives in the east valley hills. Plus wax candles and honey tea infusions.",
                    products = "Wildflower Honey, Lavender Candles, Honeycomb Jar, Lip Balms",
                    logoResName = "honey_logo"
                ),
                VendorEntity(
                    id = 3,
                    marketId = 1,
                    name = "Red Soil Berry Co.",
                    bio = "Sweet, fresh-picked chemical-free strawberries, blackberries, and blueberries grown locally.",
                    products = "Strawberries Basket, Raspberries Pack, Homemade Blueberry Syrup",
                    logoResName = "berry_logo"
                )
            )
            eventDao.insertVendors(vendors)
        }

        // 5. Initial Comments
        val existingComments = eventDao.getCommentsForEventFlow(1).firstOrNull() ?: emptyList()
        if (existingComments.isEmpty()) {
            val comments = listOf(
                CommentEntity(
                    id = 1,
                    eventId = 1,
                    authorName = "Clara Vance",
                    content = "Will Larsen Sourdough have their dark chocolate croissants this Saturday? They were amazing last week!",
                    timestampEpoch = System.currentTimeMillis() - 8 * 3600 * 1000
                ),
                CommentEntity(
                    id = 2,
                    eventId = 1,
                    authorName = "Larsen Sourdough Bakery (Organizer)",
                    content = "Hi Clara! Yes, we have doubled our batch of chocolate croissants for this week. See you early on Saturday!",
                    timestampEpoch = System.currentTimeMillis() - 6 * 3600 * 1000
                ),
                CommentEntity(
                    id = 3,
                    eventId = 2,
                    authorName = "Coach Mike",
                    content = "Hey players, we have nets and balls set up. Feel free to bring knee pads if you prefer. See you soon!",
                    timestampEpoch = System.currentTimeMillis() - 2 * 3600 * 1000
                )
            )
            for (comment in comments) {
                eventDao.insertComment(comment)
            }
        }
    }
}
