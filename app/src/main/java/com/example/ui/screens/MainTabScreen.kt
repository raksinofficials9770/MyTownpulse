package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.EventEntity
import com.example.data.local.OrganizerEntity
import com.example.ui.theme.SunsetOrange
import com.example.ui.viewmodel.EventUiModel
import com.example.ui.viewmodel.EventViewModel
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTabScreen(
    viewModel: EventViewModel,
    onNavigateToEvent: (Int) -> Unit,
    onNavigateToOrganizer: (Int) -> Unit
) {
    var activeTab by remember { mutableStateOf("home") } // home, explore, create, my_events, profile
    
    // Dialog state for location customization
    var showLocationSheet by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = activeTab == "home",
                    onClick = { activeTab = "home" },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SunsetOrange,
                        selectedTextColor = SunsetOrange,
                        indicatorColor = SunsetOrange.copy(alpha = 0.15f)
                    )
                )
                NavigationBarItem(
                    selected = activeTab == "explore",
                    onClick = { activeTab = "explore" },
                    icon = { Icon(Icons.Default.Search, contentDescription = "Explore") },
                    label = { Text("Explore") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SunsetOrange,
                        selectedTextColor = SunsetOrange,
                        indicatorColor = SunsetOrange.copy(alpha = 0.15f)
                    )
                )
                NavigationBarItem(
                    selected = activeTab == "create",
                    onClick = { activeTab = "create" },
                    icon = { Icon(Icons.Default.AddCircle, contentDescription = "Create", modifier = Modifier.size(28.dp)) },
                    label = { Text("Create") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SunsetOrange,
                        selectedTextColor = SunsetOrange,
                        indicatorColor = SunsetOrange.copy(alpha = 0.15f)
                    )
                )
                NavigationBarItem(
                    selected = activeTab == "my_events",
                    onClick = { activeTab = "my_events" },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "My Events") },
                    label = { Text("My Events") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SunsetOrange,
                        selectedTextColor = SunsetOrange,
                        indicatorColor = SunsetOrange.copy(alpha = 0.15f)
                    )
                )
                NavigationBarItem(
                    selected = activeTab == "profile",
                    onClick = { activeTab = "profile" },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SunsetOrange,
                        selectedTextColor = SunsetOrange,
                        indicatorColor = SunsetOrange.copy(alpha = 0.15f)
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                "home" -> HomeTabContent(
                    viewModel = viewModel,
                    onNavigateToEvent = onNavigateToEvent,
                    onOpenLocationSetup = { showLocationSheet = true }
                )
                "explore" -> ExploreTabContent(
                    viewModel = viewModel,
                    onNavigateToEvent = onNavigateToEvent,
                    onNavigateToOrganizer = onNavigateToOrganizer
                )
                "create" -> CreateTabContent(
                    viewModel = viewModel,
                    onCreationSuccess = { activeTab = "home" }
                )
                "my_events" -> MyEventsTabContent(
                    viewModel = viewModel,
                    onNavigateToEvent = onNavigateToEvent,
                    onNavigateToOrganizer = onNavigateToOrganizer
                )
                "profile" -> ProfileTabContent(
                    viewModel = viewModel,
                    onOpenLocationSetup = { showLocationSheet = true }
                )
            }

            // Location settings bottom sheet modal
            if (showLocationSheet) {
                LocationPreferencesSheet(
                    viewModel = viewModel,
                    onDismiss = { showLocationSheet = false }
                )
            }
        }
    }
}

@Composable
fun HighlightBentoCard(
    model: EventUiModel,
    onClick: () -> Unit
) {
    val dateText = SimpleDateFormat("EEE, MMM dd • h:mm a", Locale.getDefault()).format(Date(model.event.datetimeEpoch))
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color.White.copy(alpha = 0.15f), Color.Transparent),
                            radius = 450f
                        )
                    )
                    .padding(18.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF00E676))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "FEATURED NOW",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }

                            Text(
                                text = model.event.category,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = model.event.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            lineHeight = 24.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = Color.White
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = dateText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${model.event.address} (${String.format("%.1f", model.distanceMiles)} mi)",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.8f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.25f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (model.event.price > 0.0) "$${String.format("%.2f", model.event.price)}" else "FREE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StandardBentoCard(
    model: EventUiModel,
    onClick: () -> Unit
) {
    val icon = when (model.event.category.lowercase()) {
        "markets" -> Icons.Default.Storefront
        "sports" -> Icons.Default.SportsBasketball
        "hobby" -> Icons.Default.Interests
        "civic" -> Icons.Default.AccountBalance
        "music" -> Icons.Default.MusicNote
        "kids" -> Icons.Default.ChildCare
        else -> Icons.Default.Event
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(175.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        border = BorderStroke(1.dp, Color(0xFFDED8E1))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(18.dp)
                            .align(Alignment.Center)
                    )
                }

                if (model.event.price == 0.0) {
                    Text(
                        text = "FREE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00796B),
                        modifier = Modifier
                            .background(Color(0xFFE0F2F1), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Column {
                Text(
                    text = model.event.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    lineHeight = 16.sp,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${String.format("%.1f", model.distanceMiles)} mi",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun WideBentoCard(
    model: EventUiModel,
    onClick: () -> Unit
) {
    val dateCalendar = Calendar.getInstance().apply {
        timeInMillis = model.event.datetimeEpoch
    }
    val monthName = SimpleDateFormat("MMM", Locale.getDefault()).format(dateCalendar.time)
    val dayNumber = dateCalendar.get(Calendar.DAY_OF_MONTH).toString()
    val timeLabel = SimpleDateFormat("h:mm a", Locale.getDefault()).format(dateCalendar.time)

    val icon = when (model.event.category.lowercase()) {
        "markets" -> Icons.Default.Storefront
        "sports" -> Icons.Default.SportsBasketball
        "hobby" -> Icons.Default.Interests
        "civic" -> Icons.Default.AccountBalance
        "music" -> Icons.Default.MusicNote
        "kids" -> Icons.Default.ChildCare
        else -> Icons.Default.Event
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE7E0EB))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFE8F5E9))
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = monthName.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF2E7D32)
                    )
                    Text(
                        text = dayNumber,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1B5E20),
                        lineHeight = 18.sp
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = model.event.category.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = model.event.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$timeLabel • ${model.event.address}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF3EDF7))
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Details",
                    tint = Color(0xFF49454F),
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.Center)
                )
            }
        }
    }
}


// --------------------------------------------------------------------------------------------------
// 1. HOME TAB VIEW IMPLEMENTATION
// --------------------------------------------------------------------------------------------------
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeTabContent(
    viewModel: EventViewModel,
    onNavigateToEvent: (Int) -> Unit,
    onOpenLocationSetup: () -> Unit
) {
    val prefs by viewModel.userPreferences.collectAsState()
    val events by viewModel.filteredEventsState.collectAsState()
    val happeningNowEvents by viewModel.happeningNowEventsState.collectAsState()
    
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedDateFilter by viewModel.selectedDateFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var isMapView by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // App bar top
        HomeTopBar(
            homeTown = prefs.homeTown,
            radius = prefs.radiusMiles,
            onOpenLocationSetup = onOpenLocationSetup,
            searchQuery = searchQuery,
            onSearchChange = { viewModel.searchQuery.value = it },
            isMapView = isMapView,
            onToggleView = { isMapView = !isMapView }
        )

        // Categories selector row (unless we are inside map view, or let it show always for filtering)
        HorizontalCategoryChips(
            selectedCategory = selectedCategory,
            onCategorySelected = { viewModel.selectedCategory.value = it }
        )

        if (isMapView) {
            // Elegant Canvas Map Representation of Events!
            InteractiveCanvasMap(
                events = events,
                currentTown = prefs.homeTown,
                onNavigateToEvent = onNavigateToEvent
            )
        } else {
            // Standard scroll list Feed - Styled as a responsive Bento Grid layout
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Happy Now banner/scroller
                if (happeningNowEvents.isNotEmpty() && searchQuery.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color.Red)
                                        .drawBehind {
                                            // Simulated neon ripple halo
                                        }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Happening Now near ${prefs.homeTown}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                items(happeningNowEvents) { model ->
                                    HappeningNowCard(model = model, onClick = { onNavigateToEvent(model.event.id) })
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider()
                        }
                    }
                }

                // Date Filters selection header
                item(span = { GridItemSpan(maxLineSpan) }) {
                    DateFilterHeader(
                        activeFilter = selectedDateFilter,
                        onFilterChange = { viewModel.selectedDateFilter.value = it }
                    )
                }

                if (events.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EventNote,
                                contentDescription = "Empty",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No local events found",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try widening your search radius or changing filters.",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    itemsIndexed(
                        items = events,
                        key = { index, model -> model.event.id },
                        span = { index, model ->
                            val patternIndex = index % 4
                            val spanSize = if (patternIndex == 0 || patternIndex == 3) maxLineSpan else 1
                            GridItemSpan(spanSize)
                        }
                    ) { index, model ->
                        val patternIndex = index % 4
                        when (patternIndex) {
                            0 -> HighlightBentoCard(model = model, onClick = { onNavigateToEvent(model.event.id) })
                            1, 2 -> StandardBentoCard(model = model, onClick = { onNavigateToEvent(model.event.id) })
                            3 -> WideBentoCard(model = model, onClick = { onNavigateToEvent(model.event.id) })
                            else -> StandardBentoCard(model = model, onClick = { onNavigateToEvent(model.event.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeTopBar(
    homeTown: String,
    radius: Int,
    onOpenLocationSetup: () -> Unit,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    isMapView: Boolean,
    onToggleView: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Location selector trigger
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenLocationSetup() }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Location",
                    tint = SunsetOrange,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = homeTown,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Within $radius miles radius",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = SunsetOrange
                )
            }

            // Map Toggle Button
            IconButton(onClick = onToggleView) {
                Icon(
                    imageVector = if (isMapView) Icons.Default.FormatListBulleted else Icons.Default.Map,
                    contentDescription = "Toggle Map",
                    tint = SunsetOrange
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search text field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Search barbecue, book club, vendors...", fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SunsetOrange) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SunsetOrange,
                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@Composable
fun HorizontalCategoryChips(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    val categories = listOf("All", "Markets", "Sports", "Hobby", "Civic", "Music", "Food", "Kids", "Other")
    
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { cat ->
            val isSelected = cat == selectedCategory
            val categoryColor = if (isSelected) SunsetOrange else MaterialTheme.colorScheme.surfaceVariant
            val textColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(categoryColor)
                    .clickable { onCategorySelected(cat) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val icon = when(cat.lowercase()) {
                        "all" -> Icons.Default.AllInclusive
                        "markets" -> Icons.Default.Storefront
                        "sports" -> Icons.Default.SportsBasketball
                        "hobby" -> Icons.Default.Interests
                        "civic" -> Icons.Default.AccountBalance
                        "music" -> Icons.Default.MusicNote
                        "food" -> Icons.Default.Restaurant
                        "kids" -> Icons.Default.ChildCare
                        else -> Icons.Default.Event
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = textColor
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = cat,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }
            }
        }
    }
}

@Composable
fun DateFilterHeader(
    activeFilter: String,
    onFilterChange: (String) -> Unit
) {
    val options = listOf("All", "Today", "Tomorrow", "This Weekend", "This Week")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Local Timeline",
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            options.forEach { option ->
                val isSelected = option == activeFilter
                Text(
                    text = option,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) SunsetOrange else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) SunsetOrange.copy(alpha = 0.1f) else Color.Transparent)
                        .clickable { onFilterChange(option) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun HappeningNowCard(
    model: EventUiModel,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(260.dp)
            .height(130.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Draw a subtle pulsating amber orange radial gradient inside card backgrounds
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(SunsetOrange.copy(alpha = 0.15f), Color.Transparent)
                        )
                    )
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = "Live",
                                tint = Color.Red,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "LIVE EVENT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.Red
                            )
                        }

                        // Category chip
                        Text(
                            text = model.event.category,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = SunsetOrange,
                            modifier = Modifier
                                .background(SunsetOrange.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Column {
                        Text(
                            text = model.event.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${model.event.address} (${String.format("%.1f", model.distanceMiles)} mi)",
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (model.event.category == "Sports" && model.event.openSpots > 0) {
                        Text(
                            text = "🔥 ${model.event.openSpots} spots left!",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00E676)
                        )
                    } else if (model.event.rsvpStatus == "GOING") {
                        Text(
                            text = "✓ You are attending",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SunsetOrange
                        )
                    } else {
                        Text(
                            text = "Happening now. Tap to RSVP!",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EventFeedCard(
    model: EventUiModel,
    onClick: () -> Unit
) {
    val dateText = SimpleDateFormat("EEE, MMM dd • h:mm a", Locale.getDefault()).format(Date(model.event.datetimeEpoch))
    
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            // Hero section inside card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = when (model.event.category.lowercase()) {
                                "markets" -> listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9))
                                "sports" -> listOf(Color(0xFFFFF3E0), Color(0xFFFFE0B2))
                                "hobby" -> listOf(Color(0xFFE1F5FE), Color(0xFFB3E5FC))
                                "kids" -> listOf(Color(0xFFF3E5F5), Color(0xFFE1BEE7))
                                else -> listOf(Color(0xFFFFEBEE), Color(0xFFFFCDD2))
                            }
                        )
                    )
            ) {
                // Background icon matching category
                val icon = when (model.event.category.lowercase()) {
                    "markets" -> Icons.Default.Storefront
                    "sports" -> Icons.Default.SportsBasketball
                    "hobby" -> Icons.Default.Interests
                    "civic" -> Icons.Default.AccountBalance
                    "music" -> Icons.Default.MusicNote
                    "kids" -> Icons.Default.ChildCare
                    else -> Icons.Default.Event
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = SunsetOrange.copy(alpha = 0.25f),
                        modifier = Modifier.size(96.dp)
                    )
                }

                // Banner items over image
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Category Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = model.event.category,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SunsetOrange
                        )
                    }

                    // Price tag
                    if (model.event.price > 0.0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.7f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "$${String.format("%.2f", model.event.price)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF00E676))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "FREE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                }

                // RSVP indicator overlay (if already committed)
                if (model.event.rsvpStatus != "NONE") {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SunsetOrange)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = model.event.rsvpStatus,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Description and details
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = model.event.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Date Time row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Time",
                        tint = SunsetOrange,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = dateText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (model.event.recurrence.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "• ${model.event.recurrence}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SunsetOrange
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Address Proximity row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Proximity",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${model.event.address}  •  ${String.format("%.1f", model.distanceMiles)} miles away",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = model.event.description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )

                // Sports open slots row
                if (model.event.category == "Sports" && model.event.capacity > 0) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = "Slots",
                            tint = Color(0xFF00E676),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${model.event.openSpots} of ${model.event.capacity} spots open! (${model.event.skillLevel})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00E676)
                        )
                    }
                }
            }
        }
    }
}

// --------------------------------------------------------------------------------------------------
// 1B. MOCK GEOSPATIAL MAP CANVAS VIEW
// --------------------------------------------------------------------------------------------------
@Composable
fun InteractiveCanvasMap(
    events: List<EventUiModel>,
    currentTown: String,
    onNavigateToEvent: (Int) -> Unit
) {
    var selectedPointEvent by remember { mutableStateOf<EventUiModel?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFFEAEAEA)) // Canvas map color
                    .drawBehind {
                        // Drawing custom river and roads in modern minimalistic vectors
                        val w = size.width
                        val h = size.height

                        // Draw Grid lines
                        val stepX = w / 10
                        val stepY = h / 10
                        for (i in 1..10) {
                            drawLine(
                                color = Color.LightGray.copy(alpha = 0.4f),
                                start = Offset(i * stepX, 0f),
                                end = Offset(i * stepX, h),
                                strokeWidth = 1f
                            )
                            drawLine(
                                color = Color.LightGray.copy(alpha = 0.4f),
                                start = Offset(0f, i * stepY),
                                end = Offset(w, i * stepY),
                                strokeWidth = 1f
                            )
                        }

                        // Drew a beautiful blue river curving diagonally (representing Willow Creek)
                        val riverPath = androidx.compose.ui.graphics.Path().apply {
                            moveTo(0f, h * 0.2f)
                            quadraticTo(w * 0.4f, h * 0.3f, w * 0.7f, h * 0.8f)
                            lineTo(w, h * 0.9f)
                        }
                        drawPath(
                            path = riverPath,
                            color = Color(0xFF29B6F6).copy(alpha = 0.5f),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 32f)
                        )

                        // Main Street Line
                        drawLine(
                            color = Color.DarkGray.copy(alpha = 0.3f),
                            start = Offset(0f, h * 0.5f),
                            end = Offset(w, h * 0.5f),
                            strokeWidth = 24f
                        )
                    }
            ) {
                // Render Interactive Map Pins
                // In our coordinate offsets, we can map events onto size bounds dynamically
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val mapWidth = maxWidth.value
                    val mapHeight = maxHeight.value

                    // Center Coordinate reference
                    val centerLat = 45.312
                    val centerLon = -122.493

                    events.forEach { model ->
                        // Calculate relative scale coordinates
                        val dx = ((model.event.longitude - centerLon) * 1200.0).toFloat()
                        val dy = ((model.event.latitude - centerLat) * 1200.0).toFloat()
                        
                        val posX = (mapWidth / 2 + dx).coerceIn(40f, mapWidth - 40f)
                        val posY = (mapHeight / 2 - dy).coerceIn(40f, mapHeight - 40f)

                        val isSelected = selectedPointEvent?.event?.id == model.event.id

                        // Map Icons Pin
                        Box(
                            modifier = Modifier
                                .offset(posX.dp - 18.dp, posY.dp - 36.dp)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) SunsetOrange else MaterialTheme.colorScheme.secondary)
                                .clickable { selectedPointEvent = model },
                            contentAlignment = Alignment.Center
                        ) {
                            val icon = when (model.event.category.lowercase()) {
                                "markets" -> Icons.Default.Storefront
                                "sports" -> Icons.Default.SportsBasketball
                                "hobby" -> Icons.Default.Interests
                                "civic" -> Icons.Default.AccountBalance
                                else -> Icons.Default.MusicNote
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Willow Creek Town center label
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(currentTown, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Preview Anchor Overlay details at the bottom of the map
            AnimatedVisibility(
                visible = selectedPointEvent != null,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                selectedPointEvent?.let { model ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .clickable { onNavigateToEvent(model.event.id) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SunsetOrange.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                val icon = when (model.event.category.lowercase()) {
                                    "markets" -> Icons.Default.Storefront
                                    "sports" -> Icons.Default.SportsBasketball
                                    "hobby" -> Icons.Default.Interests
                                    "civic" -> Icons.Default.AccountBalance
                                    else -> Icons.Default.MusicNote
                                }
                                Icon(icon, contentDescription = null, tint = SunsetOrange, modifier = Modifier.size(28.dp))
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = model.event.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${model.event.address} (${String.format("%.1f", model.distanceMiles)} mi)",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "View event page and RSVP →",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SunsetOrange
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --------------------------------------------------------------------------------------------------
// 2. EXPLORE TAB VIEW IMPLEMENTATION (Categories & Organizers)
// --------------------------------------------------------------------------------------------------
@Composable
fun ExploreTabContent(
    viewModel: EventViewModel,
    onNavigateToEvent: (Int) -> Unit,
    onNavigateToOrganizer: (Int) -> Unit
) {
    val organizers by viewModel.allOrganizersFlow.collectAsState(initial = emptyList())
    var currentSubTab by remember { mutableStateOf("organizers") } // categories, organizers
    
    val categories = listOf(
        Pair("Markets", Icons.Default.Storefront),
        Pair("Sports", Icons.Default.SportsBasketball),
        Pair("Hobby", Icons.Default.Interests),
        Pair("Civic", Icons.Default.AccountBalance),
        Pair("Music", Icons.Default.MusicNote),
        Pair("Food", Icons.Default.Restaurant),
        Pair("Kids", Icons.Default.ChildCare),
        Pair("Other", Icons.Default.Event)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Explore Willow Creek",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Browse by categories or check active groups & verified community planners.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Tab Row Switcher
        TabRow(
            selectedTabIndex = if (currentSubTab == "categories") 0 else 1,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[if (currentSubTab == "categories") 0 else 1]),
                    color = SunsetOrange
                )
            }
        ) {
            Tab(
                selected = currentSubTab == "categories",
                onClick = { currentSubTab = "categories" },
                text = { Text("Categories") },
                selectedContentColor = SunsetOrange,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Tab(
                selected = currentSubTab == "organizers",
                onClick = { currentSubTab = "organizers" },
                text = { Text("Groups & Organizers") },
                selectedContentColor = SunsetOrange,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (currentSubTab) {
            "categories" -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { (name, icon) ->
                        Card(
                            onClick = {
                                viewModel.selectedCategory.value = name
                                // Switch triggers home tab category but is easy to mock
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(icon, contentDescription = null, tint = SunsetOrange)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                                Icon(Icons.AutoMirrored.Default.ArrowForward, contentDescription = null, tint = SunsetOrange, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
            "organizers" -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (organizers.isEmpty()) {
                        item {
                            Text("Loading community organizers...", modifier = Modifier.padding(16.dp))
                        }
                    } else {
                        items(organizers) { org ->
                            OrganizerCard(
                                organizer = org,
                                onClick = { onNavigateToOrganizer(org.id) },
                                onFollowToggle = { viewModel.toggleFollowOrganizer(org.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrganizerCard(
    organizer: OrganizerEntity,
    onClick: () -> Unit,
    onFollowToggle: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile initial design avatar
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(SunsetOrange.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = organizer.name.take(1).uppercase(),
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = SunsetOrange
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = organizer.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (organizer.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified",
                            tint = Color(0xFF29B6F6),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${organizer.category} • ${organizer.followerCount} followers",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = organizer.bio,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Follow button
            Button(
                onClick = onFollowToggle,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (organizer.isFollowed) MaterialTheme.colorScheme.surfaceVariant else SunsetOrange
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Text(
                    text = if (organizer.isFollowed) "Following" else "Follow",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (organizer.isFollowed) MaterialTheme.colorScheme.onSurfaceVariant else Color.White
                )
            }
        }
    }
}

// --------------------------------------------------------------------------------------------------
// 3. CREATE TAB EVENTS FORM CONSOLE
// --------------------------------------------------------------------------------------------------
@Composable
fun CreateTabContent(
    viewModel: EventViewModel,
    onCreationSuccess: () -> Unit
) {
    val context = LocalContext.current
    
    // Inputs state
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Category") }
    var address by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var capacityStr by remember { mutableStateOf("") }
    var skillLevel by remember { mutableStateOf("") }
    var recurrence by remember { mutableStateOf("None") }
    var externalLink by remember { mutableStateOf("") }

    // DateTime
    val calendar = remember { Calendar.getInstance() }
    val sdf = SimpleDateFormat("EEE, MMM dd, yyyy  •  h:mm a", Locale.getDefault())
    var selectedDateTimeInMillis by remember { mutableLongStateOf(System.currentTimeMillis() + 3600 * 1000 * 2) }

    // Dropdown expanding
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showRecurrenceDropdown by remember { mutableStateOf(false) }

    val categoriesList = listOf("Markets", "Sports", "Hobby", "Civic", "Music", "Food", "Kids", "Other")
    val recurrenceOptions = listOf("None", "Weekly", "Monthly")

    // Error
    var errorMessage by remember { mutableStateOf("") }

    // Pre-population Duplication template helper
    fun duplicatePastEvent() {
        title = "Weekly Pick-up Coed Basketball Session"
        category = "Sports"
        address = "Civic Rec Center Gym"
        description = "This is our weekly coed recreational basketball pick-up league. Clean soles, standard half court games. All skill levels welcome!"
        priceStr = "0.0"
        capacityStr = "15"
        skillLevel = "Beginner to Intermediate"
        recurrence = "Weekly"
        externalLink = "https://willowcreekrec.gov/basketball"
        selectedDateTimeInMillis = System.currentTimeMillis() + 24 * 3600 * 1000 // Tomorrow
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Post Event in town",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Lightweight creation form completes in under 60 seconds.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Duplicate Quick Template Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SunsetOrange.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = SunsetOrange)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Speed up creation?", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Duplicate past template with one tap.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    TextButton(onClick = { duplicatePastEvent() }) {
                        Text("Duplicate", fontWeight = FontWeight.Black, color = SunsetOrange)
                    }
                }
            }
        }

        // Required parameters block
        item {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Event Title *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SunsetOrange)
            )
        }

        // Category dropdown
        item {
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { showCategoryDropdown = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (category == "Category") "Select Category *" else category, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = SunsetOrange)
                    }
                }

                DropdownMenu(
                    expanded = showCategoryDropdown,
                    onDismissRequest = { showCategoryDropdown = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    categoriesList.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = {
                                category = cat
                                showCategoryDropdown = false
                            }
                        )
                    }
                }
            }
        }

        // Date Picker Action
        item {
            OutlinedButton(
                onClick = {
                    val datePicker = DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            calendar.set(Calendar.YEAR, year)
                            calendar.set(Calendar.MONTH, month)
                            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                            
                            TimePickerDialog(
                                context,
                                { _, hourOfDay, minute ->
                                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                    calendar.set(Calendar.MINUTE, minute)
                                    selectedDateTimeInMillis = calendar.timeInMillis
                                },
                                calendar.get(Calendar.HOUR_OF_DAY),
                                calendar.get(Calendar.MINUTE),
                                false
                            ).show()
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    )
                    datePicker.show()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = SunsetOrange)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(sdf.format(Date(selectedDateTimeInMillis)))
                    }
                    Text("Select Time", color = SunsetOrange, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Address
        item {
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Location Address *") },
                placeholder = { Text("e.g. Town Square Pavilion, Main St") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = SunsetOrange) },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SunsetOrange)
            )
        }

        // Expanded collapsible toggle "Add more details"
        item {
            Text("Advanced Parameters", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        item {
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                placeholder = { Text("Details about parking, items to bring, organizers details...") },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                shape = RoundedCornerShape(12.dp),
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SunsetOrange)
            )
        }

        // Side-by-side Price & Capacity
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it },
                    label = { Text("Price ($)") },
                    placeholder = { Text("0.00 for Free") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SunsetOrange)
                )

                OutlinedTextField(
                    value = capacityStr,
                    onValueChange = { capacityStr = it },
                    label = { Text("Capacity") },
                    placeholder = { Text("Unlimited") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SunsetOrange)
                )
            }
        }

        // Skill level tag (only relevant if Sports category)
        item {
            OutlinedTextField(
                value = skillLevel,
                onValueChange = { skillLevel = it },
                label = { Text("Skill Levels Requirement (Optional)") },
                placeholder = { Text("e.g., Co-ed, Intermediate, Beginner Friendly") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SunsetOrange)
            )
        }

        // Recurrence dropdown selection
        item {
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { showRecurrenceDropdown = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Recurrence: $recurrence", fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = SunsetOrange)
                    }
                }

                DropdownMenu(
                    expanded = showRecurrenceDropdown,
                    onDismissRequest = { showRecurrenceDropdown = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    recurrenceOptions.forEach { opt ->
                        DropdownMenuItem(
                            text = { Text(opt) },
                            onClick = {
                                recurrence = opt
                                showRecurrenceDropdown = false
                            }
                        )
                    }
                }
            }
        }

        // External Link
        item {
            OutlinedTextField(
                value = externalLink,
                onValueChange = { externalLink = it },
                label = { Text("External Sign-up URL (Optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SunsetOrange)
            )
        }

        if (errorMessage.isNotEmpty()) {
            item {
                Text(errorMessage, color = CrimsonRedColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Publish actions
        item {
            Button(
                onClick = {
                    if (title.isBlank() || address.isBlank() || category == "Category") {
                        errorMessage = "Please enter all required (*) fields"
                    } else {
                        val pr = priceStr.toDoubleOrNull() ?: 0.0
                        val cap = capacityStr.toIntOrNull() ?: 0
                        val worked = viewModel.publishEvent(
                            title = title,
                            category = category,
                            dateTimeEpoch = selectedDateTimeInMillis,
                            address = address,
                            description = description,
                            price = pr,
                            capacity = cap,
                            skillLevel = skillLevel,
                            recurrence = if (recurrence == "None") "" else recurrence,
                            externalLink = externalLink
                        )
                        if (worked) {
                            onCreationSuccess()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SunsetOrange),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Publish Event Live", fontWeight = FontWeight.ExtraBold, color = Color.White)
            }
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

val CrimsonRedColor = Color(0xFFFF3333)

// --------------------------------------------------------------------------------------------------
// 4. MY EVENTS TAB
// --------------------------------------------------------------------------------------------------
@Composable
fun MyEventsTabContent(
    viewModel: EventViewModel,
    onNavigateToEvent: (Int) -> Unit,
    onNavigateToOrganizer: (Int) -> Unit
) {
    val rsvps by viewModel.myRsvpsState.collectAsState()
    val followedOrgs by viewModel.followedOrganizers.collectAsState()
    
    val userPrefs by viewModel.userPreferences.collectAsState()

    var activeSubTab by remember { mutableStateOf("rsvps") } // rsvps, groups

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "My Community Events",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Keep track of RSVPs, followed organizers, and notifications in ${userPrefs.homeTown}.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Switcher TabRow
        TabRow(
            selectedTabIndex = if (activeSubTab == "rsvps") 0 else 1,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[if (activeSubTab == "rsvps") 0 else 1]),
                    color = SunsetOrange
                )
            }
        ) {
            Tab(
                selected = activeSubTab == "rsvps",
                onClick = { activeSubTab = "rsvps" },
                text = { Text("My RSVPs (${rsvps.size})") },
                selectedContentColor = SunsetOrange,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Tab(
                selected = activeSubTab == "groups",
                onClick = { activeSubTab = "groups" },
                text = { Text("Followed Groups (${followedOrgs.size})") },
                selectedContentColor = SunsetOrange,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (activeSubTab) {
            "rsvps" -> {
                if (rsvps.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Inbox, contentDescription = null, sizeModifier(), tint = SunsetOrange)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No RSVPs yet.", fontWeight = FontWeight.Bold)
                            Text("Find local happenings on Home Feed and tap RSVPs!", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(rsvps) { event ->
                            Card(
                                onClick = { onNavigateToEvent(event.id) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(SunsetOrange.copy(alpha = 0.15f))
                                    ) {
                                        val icon = when (event.category.lowercase()) {
                                            "markets" -> Icons.Default.Storefront
                                            "sports" -> Icons.Default.SportsBasketball
                                            "hobby" -> Icons.Default.Interests
                                            else -> Icons.Default.MusicNote
                                        }
                                        Icon(icon, contentDescription = null, tint = SunsetOrange, modifier = Modifier.align(Alignment.Center))
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(event.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text("${event.category} • ${event.address}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(SunsetOrange)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(event.rsvpStatus, fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            "groups" -> {
                if (followedOrgs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.FavoriteBorder, contentDescription = null, sizeModifier(), tint = SunsetOrange)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No followed groups yet.", fontWeight = FontWeight.Bold)
                            Text("Follow organizer profiles under Explore directory!", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(followedOrgs) { org ->
                            OrganizerCard(
                                organizer = org,
                                onClick = { onNavigateToOrganizer(org.id) },
                                onFollowToggle = { viewModel.toggleFollowOrganizer(org.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

fun sizeModifier() = Modifier.size(54.dp)

// --------------------------------------------------------------------------------------------------
// 5. PROFILE TAB & LOCATION SETUPS
// --------------------------------------------------------------------------------------------------
@Composable
fun ProfileTabContent(
    viewModel: EventViewModel,
    onOpenLocationSetup: () -> Unit
) {
    val prefs by viewModel.userPreferences.collectAsState()
    var lightThemeToggle by remember { mutableStateOf(false) }
    var notificationToggle by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(SunsetOrange.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("U", fontSize = 28.sp, fontWeight = FontWeight.Black, color = SunsetOrange)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text("Local Resident", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Text("Willow Creek Citizen • Free Account", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        item {
            Text("General Preferences", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = SunsetOrange)
        }

        // Current Location summary
        item {
            Card(
                onClick = onOpenLocationSetup,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = SunsetOrange)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Resident Hometown", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("${prefs.homeTown} (${prefs.zipCode})", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Text("Change", color = SunsetOrange, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // Toggles
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = SunsetOrange)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Push Weekly Digest Digest", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Switch(
                            checked = notificationToggle,
                            onCheckedChange = { notificationToggle = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = SunsetOrange, checkedTrackColor = SunsetOrange.copy(alpha = 0.3f))
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.BrightnessMedium, contentDescription = null, tint = SunsetOrange)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Enforce Dark Theme Theme", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Switch(
                            checked = !lightThemeToggle,
                            onCheckedChange = { lightThemeToggle = !it },
                            colors = SwitchDefaults.colors(checkedThumbColor = SunsetOrange, checkedTrackColor = SunsetOrange.copy(alpha = 0.3f))
                        )
                    }
                }
            }
        }

        item {
            Text("About & Verification", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = SunsetOrange)
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF29B6F6))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Apply for Organizer Verification Badge", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Text(
                        "Local libraries, schools, governmental assembly capitals, and small business chambers qualify for a blue verification checks badge.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.AutoMirrored.Default.HelpOutline, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Version 1.0.0 (Willow Creek Pilot)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            }
        }
    }
}

// --------------------------------------------------------------------------------------------------
// 6. LOCATION PREFERENCES MODAL SHEET DIALOG
// --------------------------------------------------------------------------------------------------
@Composable
fun LocationPreferencesSheet(
    viewModel: EventViewModel,
    onDismiss: () -> Unit
) {
    val prefs by viewModel.userPreferences.collectAsState()
    
    var tempTown by remember { mutableStateOf(prefs.homeTown) }
    var tempZip by remember { mutableStateOf(prefs.zipCode) }
    var tempRadius by remember { mutableFloatStateOf(prefs.radiusMiles.toFloat()) }

    var suggestionsExpanded by remember { mutableStateOf(false) }
    val suggestions = listOf("Willow Creek", "Oakwood", "Pine Ridge", "Riverdale")

    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Change Community Area",
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Set your hometown community name and maximum perimeter filter diameter for nearby discovery.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Town Input
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = tempTown,
                        onValueChange = {
                            tempTown = it
                            suggestionsExpanded = true
                        },
                        label = { Text("Community Town Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { suggestionsExpanded = !suggestionsExpanded }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                    )

                    if (suggestionsExpanded) {
                        Surface(
                            modifier = Modifier.fillMaxWidth().height(120.dp).border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                            shape = RoundedCornerShape(8.dp),
                            tonalElevation = 4.dp
                        ) {
                            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                suggestions.forEach { sug ->
                                    Text(
                                        text = sug,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                tempTown = sug
                                                tempZip = when(sug) {
                                                    "Willow Creek" -> "97001"
                                                    "Oakwood" -> "97022"
                                                    "Pine Ridge" -> "97054"
                                                    "Riverdale" -> "97089"
                                                    else -> "97000"
                                                }
                                                suggestionsExpanded = false
                                            }
                                            .padding(12.dp),
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // ZIP Input
                OutlinedTextField(
                    value = tempZip,
                    onValueChange = { tempZip = it },
                    label = { Text("ZIP Code") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Radius slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Search Proximity Radius", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("${tempRadius.toInt()} miles", color = SunsetOrange, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Slider(
                        value = tempRadius,
                        onValueChange = { tempRadius = it },
                        valueRange = 5f..50f,
                        steps = 9,
                        colors = SliderDefaults.colors(activeTrackColor = SunsetOrange, thumbColor = SunsetOrange)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // GPS trigger
                ElevatedButton(
                    onClick = {
                        viewModel.detectLocationGps(context)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Auto Detect GPS Location", fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.changeLocationSettings(tempTown, tempZip, tempRadius.toInt())
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = SunsetOrange)
            ) {
                Text("Apply settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
