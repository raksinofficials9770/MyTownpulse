package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.room.Room
import com.example.data.local.EventDatabase
import com.example.data.repository.EventRepository
import com.example.ui.screens.EventDetailScreen
import com.example.ui.screens.MainTabScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.OrganizerDetailScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.EventViewModel

class MainActivity : ComponentActivity() {

    // Lazy initialization of Database and Repository as recommended
    private val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            EventDatabase::class.java,
            EventDatabase.DATABASE_NAME
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    private val repository by lazy { EventRepository(database.eventDao) }

    // ViewModel instantiation with Factory constructor injection
    private val eventViewModel: EventViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(EventViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return EventViewModel(repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                val userPrefs by eventViewModel.userPreferences.collectAsState()

                // Check if user has updated/completed location onboarding once
                val startDestination = if (userPrefs.homeTown == "Willow Creek" && userPrefs.zipCode == "97001") {
                    "onboarding"
                } else {
                    "main"
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = startDestination
                    ) {
                        // 1. Onboarding Flow
                        composable("onboarding") {
                            OnboardingScreen(
                                viewModel = eventViewModel,
                                onOnboardingComplete = {
                                    navController.navigate("main") {
                                        popUpTo("onboarding") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 2. Mains Multi-Tabs Console (Home, Explore, Create, My Events, Profile)
                        composable("main") {
                            MainTabScreen(
                                viewModel = eventViewModel,
                                onNavigateToEvent = { eventId ->
                                    navController.navigate("event_detail/$eventId")
                                },
                                onNavigateToOrganizer = { organizerId ->
                                    navController.navigate("organizer_detail/$organizerId")
                                }
                            )
                        }

                        // 3. Event Detail Screen
                        composable(
                            route = "event_detail/{eventId}",
                            arguments = listOf(navArgument("eventId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val eventId = backStackEntry.arguments?.getInt("eventId") ?: 0
                            EventDetailScreen(
                                eventId = eventId,
                                viewModel = eventViewModel,
                                onBack = { navController.popBackStack() },
                                onNavigateToOrganizer = { organizerId ->
                                    navController.navigate("organizer_detail/$organizerId")
                                }
                            )
                        }

                        // 4. Organizer Detail Screen
                        composable(
                            route = "organizer_detail/{organizerId}",
                            arguments = listOf(navArgument("organizerId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val organizerId = backStackEntry.arguments?.getInt("organizerId") ?: 0
                            OrganizerDetailScreen(
                                organizerId = organizerId,
                                viewModel = eventViewModel,
                                onBack = { navController.popBackStack() },
                                onNavigateToEvent = { eventId ->
                                    navController.navigate("event_detail/$eventId")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
