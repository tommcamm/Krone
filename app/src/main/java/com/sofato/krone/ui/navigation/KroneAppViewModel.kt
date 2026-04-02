package com.sofato.krone.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sofato.krone.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KroneAppViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _hasCompletedOnboarding = MutableStateFlow(false)
    val hasCompletedOnboarding: StateFlow<Boolean> = _hasCompletedOnboarding.asStateFlow()

    val darkModeOverride: StateFlow<String> = userPreferencesRepository.darkModeOverride
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")

    val isDynamicColorEnabled: StateFlow<Boolean> = userPreferencesRepository.isDynamicColorEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    init {
        viewModelScope.launch {
            _hasCompletedOnboarding.value = userPreferencesRepository.hasCompletedOnboarding.first()
            _isLoading.value = false
        }
    }

    fun onOnboardingComplete() {
        _hasCompletedOnboarding.value = true
    }
}
