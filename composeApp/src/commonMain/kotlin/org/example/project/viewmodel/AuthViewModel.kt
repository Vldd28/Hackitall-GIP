package org.example.project.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.data.repository.AuthRepository

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    // Expose Supabase's own session flow — screens can use this to react to
    // login/logout without any extra mapping layer.
    val sessionStatus: StateFlow<SessionStatus> = repository.sessionStatus

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            runCatching { repository.signIn(email, password) }
                .onSuccess { _uiState.value = AuthUiState.Success }
                .onFailure { _uiState.value = AuthUiState.Error(it.message ?: "Sign in failed") }
        }
    }

    fun signUp(email: String, password: String, username: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            runCatching { repository.signUp(email, password, username) }
                .onSuccess { _uiState.value = AuthUiState.Success }
                .onFailure { _uiState.value = AuthUiState.Error(it.message ?: "Sign up failed") }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            runCatching { repository.signOut() }
            _uiState.value = AuthUiState.Idle
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}
