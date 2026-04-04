package org.example.project

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.jan.supabase.auth.status.SessionStatus
import org.example.project.viewmodel.AuthUiState
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
private fun AuthScreen(
    uiState: AuthUiState,
    onSignIn: (String, String) -> Unit,
    onSignUp: (String, String, String) -> Unit,
    onResetError: () -> Unit
) {
    var isSignUp by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isSignUp) "Create Account" else "Sign In",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        if (isSignUp) {
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(24.dp))

        when (uiState) {
            is AuthUiState.Loading -> CircularProgressIndicator()
            is AuthUiState.Error -> {
                Text(
                    text = uiState.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(8.dp))
                Button(onClick = onResetError) { Text("Try Again") }
            }
            else -> {
                Button(
                    onClick = {
                        if (isSignUp) onSignUp(email, password, username)
                        else onSignIn(email, password)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = email.isNotBlank() && password.isNotBlank() &&
                            (!isSignUp || username.isNotBlank())
                ) {
                    Text(if (isSignUp) "Sign Up" else "Sign In")
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        TextButton(onClick = { isSignUp = !isSignUp; onResetError() }) {
            Text(if (isSignUp) "Already have an account? Sign In" else "No account? Sign Up")
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
