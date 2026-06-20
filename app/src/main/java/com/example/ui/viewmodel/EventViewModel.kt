package com.example.ui.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.*
import com.example.data.repository.EventRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class EventViewModel(private val repository: EventRepository) : ViewModel() {

    // Inputs/Filters to compose our reactive Feed list
    val selectedCategory = MutableStateFlow("All")
    val selectedDateFilter = MutableStateFlow("All") // All, Today, Tomorrow, This Weekend, Next Week
    val searchQuery = MutableStateFlow("")
    
    // UI state for location & search radius
    val userPreferences: StateFlow<UserPreferencesEntity> = repository.userPreferencesFlow
        .filterNotNull()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferencesEntity()
        )

    // Base databases observed reactively
    val allEventsFlow = repository.allEventsFlow
    val allOrganizersFlow = repository.allOrganizersFlow

    // Combined filtered events list
    val filteredEventsState: StateFlow<List<EventUiModel>> = combine(
        allEventsFlow,
        userPreferences,
        selectedCategory,
        selectedDateFilter,
        searchQuery
    ) { events, prefs, category, dateFilter, query ->
        events.map { event ->
            val dist = repository.calculateDistanceMiles(
                prefs.latitude, prefs.longitude,
                event.latitude, event.longitude
            )
            EventUiModel(event = event, distanceMiles = dist)
        }.filter { model ->
            // 1. Proximity Check
            val inRange = model.distanceMiles <= prefs.radiusMiles
            
            // 2. Category Check
            val matchesCategory = category == "All" || model.event.category.lowercase() == category.lowercase()
            
            // 3. Date Filter Check
            val matchesDate = checkDateFilter(model.event.datetimeEpoch, dateFilter)
            
            // 4. Text Query Check
            val matchesQuery = query.isEmpty() || 
                    model.event.title.contains(query, ignoreCase = true) ||
                    model.event.description.contains(query, ignoreCase = true) ||
                    model.event.address.contains(query, ignoreCase = true)

            inRange && matchesCategory && matchesDate && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Happening Now events (currently within active hours / started recently)
    val happeningNowEventsState: StateFlow<List<EventUiModel>> = combine(allEventsFlow, userPreferences) { events, prefs ->
        val now = System.currentTimeMillis()
        events.map { event ->
            val dist = repository.calculateDistanceMiles(
                prefs.latitude, prefs.longitude,
                event.latitude, event.longitude
            )
            EventUiModel(event = event, distanceMiles = dist)
        }.filter { model ->
            // Event is happening now if it started within past 4 hours and is within the radius
            val timeDiff = now - model.event.datetimeEpoch
            val isHappening = timeDiff in 0..(4 * 3600 * 1000L) // 4 hours window
            val inRange = model.distanceMiles <= prefs.radiusMiles
            isHappening && inRange
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Interactive RSVPs (Events list the user is attending)
    val myRsvpsState: StateFlow<List<EventEntity>> = allEventsFlow.map { list ->
        list.filter { it.rsvpStatus == "GOING" || it.rsvpStatus == "INTERESTED" }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Active Organizers the user follows
    val followedOrganizers: StateFlow<List<OrganizerEntity>> = allOrganizersFlow.map { list ->
        list.filter { it.isFollowed }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            repository.prepopulateIfEmpty()
        }
    }

    // Expose flow query operations from repository
    fun getEventByIdFlow(id: Int): Flow<EventEntity?> = repository.getEventByIdFlow(id)
    fun getOrganizerByIdFlow(id: Int): Flow<OrganizerEntity?> = repository.getOrganizerByIdFlow(id)
    fun getCommentsForEventFlow(eventId: Int): Flow<List<CommentEntity>> = repository.getCommentsForEventFlow(eventId)
    fun getVendorsForMarketFlow(marketId: Int): Flow<List<VendorEntity>> = repository.getVendorsForMarketFlow(marketId)

    // Interactive functions: RSVP toggling
    fun setRsvpStatus(eventId: Int, status: String) {
        viewModelScope.launch {
            val event = repository.getEventById(eventId)
            if (event != null) {
                var changeInSpots = 0
                if (event.category == "Sports" && event.capacity > 0) {
                    // Update remaining open spots if RSVP going holds spots!
                    if (event.rsvpStatus != "GOING" && status == "GOING") {
                        changeInSpots = -1
                    } else if (event.rsvpStatus == "GOING" && status != "GOING") {
                        changeInSpots = 1
                    }
                }
                
                val updatedSpots = (event.openSpots + changeInSpots).coerceAtLeast(0)
                repository.updateEvent(
                    event.copy(
                        rsvpStatus = status,
                        openSpots = if (event.category == "Sports" && event.capacity > 0) updatedSpots else event.openSpots
                    )
                )
            }
        }
    }

    // Toggle organizer follow status
    fun toggleFollowOrganizer(organizerId: Int) {
        viewModelScope.launch {
            val org = repository.getOrganizerById(organizerId)
            if (org != null) {
                val nextState = !org.isFollowed
                val nextCount = if (nextState) org.followerCount + 1 else (org.followerCount - 1).coerceAtLeast(0)
                repository.updateOrganizer(org.copy(isFollowed = nextState, followerCount = nextCount))
            }
        }
    }

    // Add event comments/Q&As
    fun submitComment(eventId: Int, author: String, text: String) {
        viewModelScope.launch {
            if (text.isNotBlank()) {
                repository.insertComment(
                    CommentEntity(
                        eventId = eventId,
                        authorName = author,
                        content = text
                    )
                )
            }
        }
    }

    // Location selection: Change town / zip manual
    fun changeLocationSettings(town: String, zip: String, maxMiles: Int) {
        viewModelScope.launch {
            val current = repository.getUserPreferences()
            
            // Geographic centers for manual coordinate offsets (to simulate small towns)
            val (lat, lon) = when (town.lowercase().trim()) {
                "willow creek" -> 45.3 to -122.5
                "oakwood" -> 45.42 to -122.38
                "pine ridge" -> 45.18 to -122.65
                "riverdale" -> 45.24 to -122.21
                else -> 45.3 + (Random().nextDouble() - 0.5) * 0.2 to -122.5 + (Random().nextDouble() - 0.5) * 0.2
            }

            repository.updateUserPreferences(
                current.copy(
                    homeTown = town,
                    zipCode = zip,
                    latitude = lat,
                    longitude = lon,
                    radiusMiles = maxMiles
                )
            )
        }
    }

    // GPS location detection
    @SuppressLint("MissingPermission")
    fun detectLocationGps(context: Context) {
        viewModelScope.launch {
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                fusedLocationClient.getCurrentLocation(
                    com.google.android.gms.location.Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                    CancellationTokenSource().token
                ).addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        viewModelScope.launch {
                            val current = repository.getUserPreferences()
                            // Simulate town detection based on bounds, defaults to "Sundance Valley" for customized GPS
                            repository.updateUserPreferences(
                                current.copy(
                                    homeTown = "Sundance Valley",
                                    zipCode = "97003",
                                    latitude = location.latitude,
                                    longitude = location.longitude
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Event creation
    fun publishEvent(
        title: String,
        category: String,
        dateTimeEpoch: Long,
        address: String,
        description: String,
        price: Double,
        capacity: Int,
        skillLevel: String,
        recurrence: String,
        externalLink: String
    ): Boolean {
        if (title.isBlank() || address.isBlank() || category == "Category") return false
        viewModelScope.launch {
            val event = EventEntity(
                title = title,
                category = category,
                datetimeEpoch = dateTimeEpoch,
                address = address,
                description = description,
                price = price,
                capacity = capacity,
                openSpots = if (category == "Sports") capacity else 0,
                skillLevel = skillLevel,
                recurrence = recurrence,
                externalLink = externalLink,
                latitude = 45.3 + (Random().nextDouble() - 0.5) * 0.1, // Assign coordinates near current area
                longitude = -122.5 + (Random().nextDouble() - 0.5) * 0.1,
                organizerId = 1, // Default self as Willow Creek Farm Guild
                coverPhotoResName = when(category.lowercase()) {
                    "markets" -> "market_bg"
                    "sports" -> "volleyball_bg"
                    "hobby" -> "library_bg"
                    "civic" -> "civic_bg"
                    "kids" -> "kids_bg"
                    else -> "music_bg"
                }
            )
            repository.insertEvent(event)
        }
        return true
    }

    // Report Event
    fun reportEvent(eventId: Int) {
        viewModelScope.launch {
            val event = repository.getEventById(eventId)
            if (event != null) {
                repository.updateEvent(event.copy(isReported = true))
            }
        }
    }

    // Helper: Determine if datetime matches search filter string
    private fun checkDateFilter(datetime: Long, filter: String): Boolean {
        if (filter == "All") return true
        val now = Calendar.getInstance()
        val eventCal = Calendar.getInstance().apply { timeInMillis = datetime }

        val diffHours = (datetime - System.currentTimeMillis()) / (3600 * 1000.0)

        return when (filter) {
            "Today" -> {
                now.get(Calendar.YEAR) == eventCal.get(Calendar.YEAR) &&
                        now.get(Calendar.DAY_OF_YEAR) == eventCal.get(Calendar.DAY_OF_YEAR)
            }
            "Tomorrow" -> {
                val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
                tomorrow.get(Calendar.YEAR) == eventCal.get(Calendar.YEAR) &&
                        tomorrow.get(Calendar.DAY_OF_YEAR) == eventCal.get(Calendar.DAY_OF_YEAR)
            }
            "This Weekend" -> {
                // Saturday & Sunday
                val day = eventCal.get(Calendar.DAY_OF_WEEK)
                val isWeekend = day == Calendar.SATURDAY || day == Calendar.SUNDAY
                // Within 5 days from now
                isWeekend && diffHours in -12.0..120.0
            }
            "This Week" -> {
                diffHours in -12.0..168.0 // Within next 7 days
            }
            else -> true
        }
    }
}

// Wrap EventEntity and distance metrics for list rendering
data class EventUiModel(
    val event: EventEntity,
    val distanceMiles: Double
)
