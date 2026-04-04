package org.example.project

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.jan.supabase.auth.status.SessionStatus
import org.example.project.ui.components.MapView
import org.example.project.ui.screens.AuthScreen
import org.example.project.viewmodel.AuthUiState
import org.example.project.viewmodel.AuthViewModel
import org.example.project.viewmodel.EventViewModel

@Composable
fun App() {
    MaterialTheme {
        val authViewModel = viewModel { AuthViewModel() }
        val eventViewModel = viewModel { EventViewModel() }
        val sessionStatus by authViewModel.sessionStatus.collectAsState()
        val uiState by authViewModel.uiState.collectAsState()
        val events by eventViewModel.events.collectAsState()

        when (sessionStatus) {
            is SessionStatus.Authenticated -> {
                LaunchedEffect(Unit) {
                    eventViewModel.loadPublicEvents()
                }
                MapView(
                    events = events,
                    onEventClick = {},
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> AuthScreen(
                uiState = uiState,
                onSignIn = { email, password -> authViewModel.signIn(email, password) },
                onSignUp = { email, password, username -> authViewModel.signUp(email, password, username) },
                onResetError = { authViewModel.resetState() }
            )
        }
    }
}
