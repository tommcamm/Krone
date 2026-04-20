package com.sofato.krone.groups.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sofato.krone.domain.repository.UserPreferencesRepository
import com.sofato.krone.groups.domain.model.DeviceIdentity
import com.sofato.krone.groups.domain.model.ServerEnrollment
import com.sofato.krone.groups.domain.repository.DeviceIdentityRepository
import com.sofato.krone.groups.domain.repository.ServerEnrollmentRepository
import com.sofato.krone.groups.domain.usecase.DisableGroupsUseCase
import com.sofato.krone.groups.domain.usecase.EnableGroupsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupsSettingsViewModel @Inject constructor(
    private val prefs: UserPreferencesRepository,
    private val enableGroups: EnableGroupsUseCase,
    private val disableGroups: DisableGroupsUseCase,
    deviceIdentity: DeviceIdentityRepository,
    enrollment: ServerEnrollmentRepository,
) : ViewModel() {

    data class UiState(
        val enabled: Boolean = false,
        val identity: DeviceIdentity? = null,
        val enrollment: ServerEnrollment? = null,
        val busy: Boolean = false,
    )

    sealed interface Event {
        data class Error(val message: String) : Event
        data object Disabled : Event
    }

    private val busy = MutableStateFlow(false)
    val state: StateFlow<UiState> = combine(
        prefs.isGroupsEnabled,
        deviceIdentity.observe(),
        enrollment.observe(),
        busy,
    ) { enabled, identity, server, busyFlag ->
        UiState(
            enabled = enabled,
            identity = identity,
            enrollment = server,
            busy = busyFlag,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState())

    private val _events = Channel<Event>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun enable() {
        viewModelScope.launch {
            busy.value = true
            runCatching { enableGroups() }
                .onFailure { _events.send(Event.Error(it.message ?: "Failed to enable groups")) }
            busy.value = false
        }
    }

    fun disable() {
        viewModelScope.launch {
            busy.value = true
            runCatching { disableGroups() }
                .onSuccess { _events.send(Event.Disabled) }
                .onFailure { _events.send(Event.Error(it.message ?: "Failed to disable groups")) }
            busy.value = false
        }
    }
}
