package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.EventEntity
import com.example.ui.theme.SunsetOrange
import com.example.ui.viewmodel.EventViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventId: Int,
    viewModel: EventViewModel,
    onBack: () -> Unit,
    onNavigateToOrganizer: (Int) -> Unit
) {
    val eventFlow = remember(eventId) { viewModel.getEventByIdFlow(eventId) }
    val event by eventFlow.collectAsState(initial = null)
    
    val commentsFlow = remember(eventId) { viewModel.getCommentsForEventFlow(eventId) }
    val comments by commentsFlow.collectAsState(initial = emptyList())

    val vendorsFlow = remember(eventId) { viewModel.getVendorsForMarketFlow(eventId) }
    val vendors by vendorsFlow.collectAsState(initial = emptyList())

    val context = LocalContext.current
    var isVerifiedGroup = false

    // Comment Input
    var commentText by remember { mutableStateOf("") }
    var authorName by remember { mutableStateOf("Anonymous") }

    if (event == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = SunsetOrange)
        }
        return
    }

    val activeEvent = event!!
    val dateText = SimpleDateFormat("EEEE, MMMM dd, yyyy  •  h:mm a", Locale.getDefault()).format(Date(activeEvent.datetimeEpoch))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(activeEvent.category, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Report Button
                    IconButton(onClick = {
                        viewModel.reportEvent(activeEvent.id)
                        onBack()
                    }) {
                        Icon(imageVector = Icons.Default.Flag, contentDescription = "Report", tint = Color.Red)
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
            // Category Header Banner background
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = when (activeEvent.category.lowercase()) {
                                    "markets" -> listOf(Color(0xFFC8E6C9), Color(0xFF81C784))
                                    "sports" -> listOf(Color(0xFFFFE0B2), Color(0xFFFFB74D))
                                    "hobby" -> listOf(Color(0xFFB3E5FC), Color(0xFF64B5F6))
                                    "kids" -> listOf(Color(0xFFE1BEE7), Color(0xFFBA68C8))
                                    else -> listOf(Color(0xFFFFCDD2), Color(0xFFE57373))
                                }
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val categoryIcon = when (activeEvent.category.lowercase()) {
                        "markets" -> Icons.Default.Storefront
                        "sports" -> Icons.Default.SportsBasketball
                        "hobby" -> Icons.Default.Interests
                        "civic" -> Icons.Default.AccountBalance
                        "music" -> Icons.Default.MusicNote
                        "food" -> Icons.Default.Restaurant
                        "kids" -> Icons.Default.ChildCare
                        else -> Icons.Default.Event
                    }
                    Icon(categoryIcon, contentDescription = null, tint = Color.White, modifier = Modifier.size(80.dp))
                }
            }

            // Title
            item {
                Column {
                    Text(
                        text = activeEvent.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    if (activeEvent.recurrence.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "🔄 ${activeEvent.recurrence}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SunsetOrange
                        )
                    }
                }
            }

            // Interactive RSVP selection panel
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Are you going?", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Going check
                        FilterChip(
                            selected = activeEvent.rsvpStatus == "GOING",
                            onClick = {
                                val next = if (activeEvent.rsvpStatus == "GOING") "NONE" else "GOING"
                                viewModel.setRsvpStatus(activeEvent.id, next)
                            },
                            label = { Text("Going") },
                            leadingIcon = {
                                if (activeEvent.rsvpStatus == "GOING") Icon(Icons.Default.Check, null)
                            }
                        )

                        // Interested check
                        FilterChip(
                            selected = activeEvent.rsvpStatus == "INTERESTED",
                            onClick = {
                                val next = if (activeEvent.rsvpStatus == "INTERESTED") "NONE" else "INTERESTED"
                                viewModel.setRsvpStatus(activeEvent.id, next)
                            },
                            label = { Text("Interested") },
                            leadingIcon = {
                                if (activeEvent.rsvpStatus == "INTERESTED") Icon(Icons.Default.Star, null)
                            }
                        )
                    }
                }
            }

            // Event details block
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Date/Time
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = SunsetOrange)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Date & Time", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(dateText, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        // Location
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = SunsetOrange)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Location", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(activeEvent.address, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            TextButton(
                                onClick = {
                                    val geoUri = Uri.parse("geo:0,0?q=${Uri.encode(activeEvent.address)}")
                                    val mapIntent = Intent(Intent.ACTION_VIEW, geoUri)
                                    if (mapIntent.resolveActivity(context.packageManager) != null) {
                                        context.startActivity(mapIntent)
                                    } else {
                                        // Backup web search
                                        val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(activeEvent.address)}")
                                        context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
                                    }
                                }
                            ) {
                                Text("Directions", color = SunsetOrange, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Slots or Pricing
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Capacity
                            Row(verticalAlignment = Alignment.Top, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Group, contentDescription = null, tint = SunsetOrange)
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text("Attendance Capacity", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(
                                        text = if (activeEvent.capacity > 0) "${activeEvent.capacity} spots maximum" else "Open Attendance",
                                        fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Price
                            Row(verticalAlignment = Alignment.Top, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.LocalActivity, contentDescription = null, tint = SunsetOrange)
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text("Entrance Fee", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(
                                        text = if (activeEvent.price > 0.0) "$${String.format("%.2f", activeEvent.price)}" else "FREE Event",
                                        fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Sports tags specialized description
                        if (activeEvent.category == "Sports" && activeEvent.skillLevel.isNotEmpty()) {
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(Icons.Default.SportsBasketball, contentDescription = null, tint = SunsetOrange)
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text("Play Level Requirement", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(activeEvent.skillLevel, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            // Description Expandable Text
            item {
                Column {
                    Text("About the Event", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = activeEvent.description,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 22.sp
                    )

                    if (activeEvent.externalLink.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                val urlIntent = Intent(Intent.ACTION_VIEW, Uri.parse(activeEvent.externalLink))
                                context.startActivity(urlIntent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SunsetOrange),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Link, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Official Registration Web Link")
                        }
                    }
                }
            }

            // Share deep link / export to calendar button
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_INSERT).apply {
                                data = CalendarContract.Events.CONTENT_URI
                                putExtra(CalendarContract.Events.TITLE, activeEvent.title)
                                putExtra(CalendarContract.Events.EVENT_LOCATION, activeEvent.address)
                                putExtra(CalendarContract.Events.DESCRIPTION, activeEvent.description)
                                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, activeEvent.datetimeEpoch)
                                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, activeEvent.datetimeEpoch + 3600 * 1000 * 2) // mock 2 hours
                            }
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = SunsetOrange)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add to Device Calendar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Button(
                        onClick = {
                            val msg = "Hey! Let's go to this small town event: ${activeEvent.title} on ${dateText} at ${activeEvent.address}. Discover other happenings on MyTownPulse!"
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, msg)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Event via"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = SunsetOrange)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Share via SMS / Messaging", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Farmers Market Vendors Sub-modules if Category is Market
            if (activeEvent.category == "Markets" && vendors.isNotEmpty()) {
                item {
                    Column {
                        Text("This Week's Local Vendors", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        Text("Organizer-managed list of attending small farmers and crafters.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                items(vendors) { vendor ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, SunsetOrange.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(vendor.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SunsetOrange)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(vendor.products, fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(vendor.bio, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
                        }
                    }
                }
            }

            // Q&A Comment string
            item {
                Text("Q&A Discussion Board", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            }

            if (comments.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No questions asked yet. Ask if parking is available or items to bring!", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(comments) { comment ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(comment.authorName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SunsetOrange)
                                val dateStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(comment.timestampEpoch))
                                Text(dateStr, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(comment.content, fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                }
            }

            // Add comment boxes
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Have a Question?", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Name
                            OutlinedTextField(
                                value = authorName,
                                onValueChange = { authorName = it },
                                label = { Text("Display Name") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(verticalAlignment = Alignment.Bottom) {
                            OutlinedTextField(
                                value = commentText,
                                onValueChange = { commentText = it },
                                placeholder = { Text("Is parking free? Can I bring dogs?") },
                                modifier = Modifier.weight(1f),
                                maxLines = 3
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Button(
                                onClick = {
                                    if (commentText.isNotBlank()) {
                                        viewModel.submitComment(activeEvent.id, authorName, commentText)
                                        commentText = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SunsetOrange),
                                contentPadding = PaddingValues(12.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Default.Reply, contentDescription = "Submit")
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}
