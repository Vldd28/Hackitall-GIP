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

@Composable
fun App() {
    MaterialTheme {
        val viewModel = viewModel { AuthViewModel() }
        val uiState by viewModel.uiState.collectAsState()
        val sessionStatus by viewModel.sessionStatus.collectAsState()

        when (sessionStatus) {
            is SessionStatus.Authenticated -> MainScreen(
                userId = (sessionStatus as SessionStatus.Authenticated).session.user?.id ?: "unknown",
                onSignOut = { viewModel.signOut() }
            )
            else -> AuthScreen(
                uiState = uiState,
                onSignIn = { email, password -> viewModel.signIn(email, password) },
                onSignUp = { email, password, username -> viewModel.signUp(email, password, username) },
                onResetError = { viewModel.resetState() }
            )
        }
    }
}
