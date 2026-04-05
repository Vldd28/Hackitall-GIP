package org.example.project.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.data.model.Friendship
import org.example.project.data.model.FriendshipStatus
import org.example.project.data.repository.FriendshipRepository

class FriendshipViewModel(
    private val repository: FriendshipRepository = FriendshipRepository()
) : ViewModel() {

    private val _friends = MutableStateFlow<List<Friendship>>(emptyList())
    val friends = _friends.asStateFlow()

    private val _pendingRequests = MutableStateFlow<List<Friendship>>(emptyList())
    val pendingRequests = _pendingRequests.asStateFlow()

    // Cached status for a specific profile being viewed (e.g. on a user's profile page)
    private val _viewedUserStatus = MutableStateFlow<FriendshipStatus?>(null)
    val viewedUserStatus = _viewedUserStatus.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun loadFriends(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            runCatching { _friends.value = repository.getFriends(userId) }
                .onFailure { _error.value = it.message ?: "Failed to load friends" }
            _isLoading.value = false
        }
    }

    fun loadPendingRequests(userId: String) {
        viewModelScope.launch {
            runCatching { _pendingRequests.value = repository.getPendingRequests(userId) }
                .onFailure { _error.value = it.message }
        }
    }

    fun checkFriendshipStatus(currentUserId: String, otherUserId: String) {
        viewModelScope.launch {
            runCatching { _viewedUserStatus.value = repository.getFriendshipStatus(currentUserId, otherUserId) }
                .onFailure { _error.value = it.message }
        }
    }

    fun sendFriendRequest(requesterId: String, receiverId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            runCatching { repository.sendRequest(requesterId, receiverId) }
                .onSuccess { _viewedUserStatus.value = FriendshipStatus.pending }
                .onFailure { _error.value = it.message ?: "Failed to send request" }
            _isLoading.value = false
        }
    }

    fun acceptRequest(requesterId: String, receiverId: String) {
        viewModelScope.launch {
            runCatching { repository.acceptRequest(requesterId, receiverId) }
                .onSuccess {
                    _pendingRequests.value = _pendingRequests.value.filter { it.requesterId != requesterId }
                    loadFriends(receiverId)
                }
                .onFailure { _error.value = it.message ?: "Failed to accept request" }
        }
    }

    fun blockUser(requesterId: String, receiverId: String) {
        viewModelScope.launch {
            runCatching { repository.blockUser(requesterId, receiverId) }
                .onSuccess {
                    _friends.value = _friends.value.filter {
                        it.requesterId != receiverId && it.receiverId != receiverId
                    }
                    _viewedUserStatus.value = FriendshipStatus.blocked
                }
                .onFailure { _error.value = it.message ?: "Failed to block user" }
        }
    }

    fun removeFriend(currentUserId: String, otherUserId: String) {
        viewModelScope.launch {
            runCatching { repository.removeFriendship(currentUserId, otherUserId) }
                .onSuccess {
                    _friends.value = _friends.value.filter { friendship ->
                        repository.getFriendId(currentUserId, friendship) != otherUserId
                    }
                    _viewedUserStatus.value = null
                }
                .onFailure { _error.value = it.message ?: "Failed to remove friend" }
        }
    }

    fun clearError() { _error.value = null }
}
