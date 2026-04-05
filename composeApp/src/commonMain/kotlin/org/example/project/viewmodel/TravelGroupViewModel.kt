package org.example.project.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.data.model.Event
import org.example.project.data.model.TravelGroup
import org.example.project.data.model.TravelGroupInsert
import org.example.project.data.model.TravelGroupMember
import org.example.project.data.repository.TravelGroupRepository

class TravelGroupViewModel(
    private val repository: TravelGroupRepository = TravelGroupRepository()
) : ViewModel() {

    private val _groups = MutableStateFlow<List<TravelGroup>>(emptyList())
    val groups = _groups.asStateFlow()

    private val _selectedGroup = MutableStateFlow<TravelGroup?>(null)
    val selectedGroup = _selectedGroup.asStateFlow()

    private val _userGroups = MutableStateFlow<List<TravelGroup>>(emptyList())
    val userGroups = _userGroups.asStateFlow()

    private val _members = MutableStateFlow<List<TravelGroupMember>>(emptyList())
    val members = _members.asStateFlow()

    private val _groupEvents = MutableStateFlow<List<Event>>(emptyList())
    val groupEvents = _groupEvents.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun loadOpenGroups() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            runCatching { _groups.value = repository.getOpenGroups() }
                .onFailure { _error.value = it.message ?: "Failed to load groups" }
            _isLoading.value = false
        }
    }

    fun loadGroupById(groupId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            runCatching { _selectedGroup.value = repository.getGroupById(groupId) }
                .onFailure { _error.value = it.message ?: "Failed to load group" }
            _isLoading.value = false
        }
    }

    fun createGroup(insert: TravelGroupInsert, onSuccess: (TravelGroup) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            runCatching { repository.createGroup(insert) }
                .onSuccess { group ->
                    _groups.value = _groups.value + group
                    _userGroups.value = _userGroups.value + group
                    onSuccess(group)
                }
                .onFailure { _error.value = it.message ?: "Failed to create group" }
            _isLoading.value = false
        }
    }

    fun joinGroup(groupId: String, userId: String) {
        viewModelScope.launch {
            runCatching { repository.joinGroup(groupId, userId) }
                .onSuccess { loadGroupMembers(groupId) }
                .onFailure { _error.value = it.message ?: "Failed to join group" }
        }
    }

    fun leaveGroup(groupId: String, userId: String) {
        viewModelScope.launch {
            runCatching { repository.leaveGroup(groupId, userId) }
                .onSuccess {
                    _userGroups.value = _userGroups.value.filter { it.id != groupId }
                    loadGroupMembers(groupId)
                }
                .onFailure { _error.value = it.message ?: "Failed to leave group" }
        }
    }

    fun loadUserGroups(userId: String) {
        viewModelScope.launch {
            runCatching { _userGroups.value = repository.getUserGroups(userId) }
                .onFailure { _error.value = it.message }
        }
    }

    fun loadGroupMembers(groupId: String) {
        viewModelScope.launch {
            runCatching { _members.value = repository.getGroupMembers(groupId) }
                .onFailure { _error.value = it.message }
        }
    }

    fun loadGroupEvents(groupId: String) {
        viewModelScope.launch {
            runCatching { _groupEvents.value = repository.getGroupEvents(groupId) }
                .onFailure { _error.value = it.message }
        }
    }

    fun clearError() { _error.value = null }
}
