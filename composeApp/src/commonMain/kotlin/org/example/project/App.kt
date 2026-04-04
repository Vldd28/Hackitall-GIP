package org.example.project

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.jan.supabase.auth.status.SessionStatus
import org.example.project.ui.screens.AuthScreen
import org.example.project.viewmodel.AuthViewModel

@Composable
fun App() {
    MaterialTheme {
        val viewModel = viewModel { AuthViewModel() }
        val uiState by viewModel.uiState.collectAsState()
        val sessionStatus by viewModel.sessionStatus.collectAsState()

        when (sessionStatus) {
            is SessionStatus.Authenticated -> LoggedInScreen(
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

@Composable
private fun LoggedInScreen(userId: String, onSignOut: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Logged in!", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text("User ID: $userId", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onSignOut) { Text("Sign Out") }
    }
}
