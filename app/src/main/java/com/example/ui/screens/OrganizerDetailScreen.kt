package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.EventEntity
import com.example.ui.theme.SunsetOrange
import com.example.ui.viewmodel.EventViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizerDetailScreen(
    organizerId: Int,
    viewModel: EventViewModel,
    onBack: () -> Unit,
    onNavigateToEvent: (Int) -> Unit
) {
    val organizerFlow = remember(organizerId) { viewModel.getOrganizerByIdFlow(organizerId) }
    val organizer by organizerFlow.collectAsState(initial = null)

    val allEvents by viewModel.allEventsFlow.collectAsState(initial = emptyList())
    
    // Filter events by organizer
    val upcomingEvents = remember(allEvents, organizerId) {
        allEvents.filter { it.organizerId == organizerId && it.datetimeEpoch >= System.currentTimeMillis() - 3600 * 1000 }
    }
    val pastEvents = remember(allEvents, organizerId) {
        allEvents.filter { it.organizerId == organizerId && it.datetimeEpoch < System.currentTimeMillis() - 3600 * 1000 }
    }

    if (organizer == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = SunsetOrange)
        }
        return
    }

    val activeOrg = organizer!!

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Organizer Profile", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile header block
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(SunsetOrange.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = activeOrg.name.take(1).uppercase(),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = SunsetOrange
                        )
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = activeOrg.name,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            if (activeOrg.isVerified) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Verified Profile",
                                    tint = Color(0xFF29B6F6),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${activeOrg.category} • ${activeOrg.followerCount} followers",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Bio
            item {
                Column {
                    Text("About", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = activeOrg.bio,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
            }

            // Follow Actions bar
            item {
                Button(
                    onClick = { viewModel.toggleFollowOrganizer(activeOrg.id) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeOrg.isFollowed) MaterialTheme.colorScheme.surfaceVariant else SunsetOrange
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (activeOrg.isFollowed) "Following Organizer" else "Follow Organizer",
                        fontWeight = FontWeight.Bold,
                        color = if (activeOrg.isFollowed) MaterialTheme.colorScheme.onSurfaceVariant else Color.White
                    )
                }
            }

            // Tab-like section sections
            item {
                Text(
                    text = "Upcoming Events (${upcomingEvents.size})",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = SunsetOrange
                )
            }

            if (upcomingEvents.isEmpty()) {
                item {
                    Text(
                        "No upcoming events scheduled right now.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            } else {
                items(upcomingEvents) { event ->
                    OrganizerSubEventCard(event = event, onClick = { onNavigateToEvent(event.id) })
                }
            }

            item {
                Text(
                    text = "Past Gatherings (${pastEvents.size})",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (pastEvents.isEmpty()) {
                item {
                    Text(
                        "No past events in archive.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            } else {
                items(pastEvents) { event ->
                    OrganizerSubEventCard(event = event, onClick = { onNavigateToEvent(event.id) })
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
fun OrganizerSubEventCard(
    event: EventEntity,
    onClick: () -> Unit
) {
    val dateText = SimpleDateFormat("EEE, MMM dd, yyyy  •  h:mm a", Locale.getDefault()).format(Date(event.datetimeEpoch))
    
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SunsetOrange.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                val icon = when (event.category.lowercase()) {
                    "markets" -> Icons.Default.Schedule
                    "sports" -> Icons.Default.Schedule
                    else -> Icons.Default.Schedule
                }
                Icon(icon, contentDescription = null, tint = SunsetOrange)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = dateText,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
