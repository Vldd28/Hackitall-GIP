package org.example.project.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.data.model.Interest
import org.example.project.data.model.Profile
import org.example.project.data.model.ProfileUpdate
import org.example.project.data.repository.UserRepository

class ProfileViewModel(
    private val repository: UserRepository = UserRepository()
) : ViewModel() {

    private val _profile = MutableStateFlow<Profile?>(null)
    val profile = _profile.asStateFlow()

    private val _userInterests = MutableStateFlow<List<Interest>>(emptyList())
    val userInterests = _userInterests.asStateFlow()

    private val _allInterests = MutableStateFlow<List<Interest>>(emptyList())
    val allInterests = _allInterests.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun loadProfile(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            runCatching {
                _profile.value = repository.getProfile(userId)
                _userInterests.value = repository.getUserInterests(userId)
            }.onFailure {
                _error.value = it.message ?: "Failed to load profile"
            }
            _isLoading.value = false
        }
    }

    fun updateProfile(userId: String, update: ProfileUpdate) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            runCatching { _profile.value = repository.updateProfile(userId, update) }
                .onFailure { _error.value = it.message ?: "Failed to update profile" }
            _isLoading.value = false
        }
    }

    fun uploadAvatar(userId: String, data: ByteArray) {
        viewModelScope.launch {
            _isLoading.value = true
            runCatching {
                val url = repository.uploadAvatar(userId, data)
                _profile.value = _profile.value?.copy(avatarUrl = url)
            }.onFailure {
                _error.value = it.message ?: "Failed to upload avatar"
            }
            _isLoading.value = false
        }
    }

    fun loadAllInterests() {
        viewModelScope.launch {
            runCatching { _allInterests.value = repository.getAllInterests() }
                .onFailure { _error.value = it.message }
        }
    }

    fun setUserInterests(userId: String, interestIds: List<Int>) {
        viewModelScope.launch {
            _isLoading.value = true
            runCatching {
                repository.setUserInterests(userId, interestIds)
                _userInterests.value = repository.getUserInterests(userId)
            }.onFailure {
                _error.value = it.message ?: "Failed to save interests"
            }
            _isLoading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}
