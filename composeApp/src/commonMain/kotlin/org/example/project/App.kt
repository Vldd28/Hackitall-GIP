package org.example.project

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.jan.supabase.auth.status.SessionStatus
import org.example.project.ui.screens.AuthScreen
import org.example.project.ui.screens.MainScreen
import org.example.project.viewmodel.AuthViewModel
import org.example.project.viewmodel.ProfileViewModel

@Composable
fun App() {
    MaterialTheme {
        val authViewModel = viewModel { AuthViewModel() }
        val profileViewModel = viewModel { ProfileViewModel() }
        val uiState by authViewModel.uiState.collectAsState()
        val sessionStatus by authViewModel.sessionStatus.collectAsState()

        when (sessionStatus) {
            is SessionStatus.Authenticated -> MainScreen(
                userId = (sessionStatus as SessionStatus.Authenticated).session.user?.id ?: "unknown",
                onSignOut = { authViewModel.signOut() },
                profileViewModel = profileViewModel
            )
            else -> AuthScreen(
                uiState = uiState,
                onSignIn = { email, password -> authViewModel.signIn(email, password) },
                onSignUp = { email, password, username -> authViewModel.signUp(email, password, username) },
                onResetError = { authViewModel.resetState() }
            )
        }
    }
}
