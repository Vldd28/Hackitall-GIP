package org.example.project.ui

// SAMPLE INTEGRATION - Copy this to your App.kt to get started!
// This shows how to connect all your UI screens with ViewModels

/*
import androidx.compose.runtime.*
import org.example.project.ui.theme.TravelCompanionTheme
import org.example.project.ui.screens.*
import org.example.project.viewmodel.*
import io.github.jan.supabase.auth.status.SessionStatus

@Composable
fun App() {
    TravelCompanionTheme {
        val authViewModel = remember { AuthViewModel() }
        val sessionStatus by authViewModel.sessionStatus.collectAsState()
        
        // Simple navigation state (replace with Voyager later)
        var currentScreen by remember { mutableStateOf<Screen>(Screen.Auth) }
        
        when {
            // If not logged in, show auth screen
            sessionStatus !is SessionStatus.Authenticated -> {
                AuthScreen(
                    viewModel = authViewModel,
                    onAuthSuccess = { currentScreen = Screen.EventList }
                )
            }
            
            // If logged in, show appropriate screen
            else -> {
                when (currentScreen) {
                    Screen.EventList -> {
                        val eventViewModel = remember { EventViewModel() }
                        val events by eventViewModel.publicEvents.collectAsState()
                        val isLoading by eventViewModel.isLoading.collectAsState()
                        
                        // Fetch events on first load
                        LaunchedEffect(Unit) {
                            eventViewModel.loadPublicEvents()
                        }
                        
                        EventListScreen(
                            events = events,
                            isLoading = isLoading,
                            onEventClick = { event -> 
                                // TODO: Navigate to event details
                                println("Clicked event: ${event.title}")
                            },
                            onCreateEventClick = { 
                                currentScreen = Screen.CreateEvent 
                            },
                            onProfileClick = { 
                                currentScreen = Screen.Profile 
                            }
                        )
                    }
                    
                    Screen.CreateEvent -> {
                        val eventViewModel = remember { EventViewModel() }
                        
                        CreateEventScreen(
                            onBackClick = { currentScreen = Screen.EventList },
                            onCreateClick = { formData ->
                                // Get current user ID from auth
                                val userId = authViewModel.sessionStatus.value
                                    .let { it as? SessionStatus.Authenticated }
                                    ?.session?.user?.id ?: return@CreateEventScreen
                                
                                // Convert form data to EventInsert
                                val eventInsert = EventInsert(
                                    creatorId = userId,
                                    title = formData.title,
                                    description = formData.description,
                                    locationName = formData.locationName,
                                    dateTime = formData.dateTime,
                                    maxParticipants = formData.maxParticipants,
                                    lat = 52.3676, // TODO: Get from map picker
                                    lng = 4.9041,
                                    keyInterests = formData.selectedInterests.mapIndexed { i, _ -> i + 1 }
                                )
                                
                                // Create event
                                viewModelScope.launch {
                                    runCatching { 
                                        eventViewModel.createEvent(eventInsert) 
                                    }.onSuccess {
                                        currentScreen = Screen.EventList
                                    }
                                }
                            }
                        )
                    }
                    
                    Screen.Profile -> {
                        val profileViewModel = remember { ProfileViewModel() }
                        val profile by profileViewModel.currentProfile.collectAsState()
                        
                        // Fetch profile on first load
                        LaunchedEffect(Unit) {
                            profileViewModel.loadCurrentUserProfile()
                        }
                        
                        profile?.let {
                            ProfileScreen(
                                profile = it,
                                interests = listOf("Art", "Hiking", "Music"), // TODO: Load from profile_interests
                                onEditClick = { 
                                    // TODO: Open edit profile screen
                                }
                            )
                        } ?: run {
                            // Loading state
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                    
                    Screen.Auth -> {
                        // Should not reach here, but fallback to auth
                        AuthScreen(
                            viewModel = authViewModel,
                            onAuthSuccess = { currentScreen = Screen.EventList }
                        )
                    }
                }
            }
        }
    }
}

// Simple screen enum for navigation
sealed class Screen {
    object Auth : Screen()
    object EventList : Screen()
    object CreateEvent : Screen()
    object Profile : Screen()
}

*/

// For a BETTER navigation solution, use Voyager:
// Add to build.gradle.kts:
//   implementation("cafe.adriel.voyager:voyager-navigator:1.0.0")
//   implementation("cafe.adriel.voyager:voyager-transitions:1.0.0")

/*
// VOYAGER EXAMPLE (Better for production):

import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition

@Composable
fun App() {
    TravelCompanionTheme {
        Navigator(AuthScreen()) { navigator ->
            SlideTransition(navigator)
        }
    }
}

// Each screen becomes a Voyager Screen:
class EventListScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        // Your EventListScreen composable here
    }
}
*/
