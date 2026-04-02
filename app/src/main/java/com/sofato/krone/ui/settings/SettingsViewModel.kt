package com.sofato.krone.ui.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sofato.krone.data.backup.DatabaseBackupManager
import com.sofato.krone.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val databaseBackupManager: DatabaseBackupManager,
) : ViewModel() {

    val darkModeOverride: StateFlow<String> = userPreferencesRepository.darkModeOverride
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")

    val isDynamicColorEnabled: StateFlow<Boolean> = userPreferencesRepository.isDynamicColorEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    private val _events = MutableSharedFlow<SettingsEvent>()
    val events = _events.asSharedFlow()

    sealed interface SettingsEvent {
        data object ExportSuccess : SettingsEvent
        data class ExportFailed(val message: String) : SettingsEvent
        data object ImportComplete : SettingsEvent
        data object ResetComplete : SettingsEvent
    }

    fun setDarkMode(mode: String) {
        viewModelScope.launch {
            userPreferencesRepository.setDarkModeOverride(mode)
        }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setDynamicColorEnabled(enabled)
        }
    }

    fun exportDatabase(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                databaseBackupManager.exportTo(uri)
                _events.emit(SettingsEvent.ExportSuccess)
            } catch (e: Exception) {
                _events.emit(SettingsEvent.ExportFailed(e.message ?: "Export failed"))
            }
        }
    }

    fun importDatabase(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            databaseBackupManager.importFrom(uri)
            _events.emit(SettingsEvent.ImportComplete)
        }
    }

    fun resetEverything() {
        viewModelScope.launch(Dispatchers.IO) {
            databaseBackupManager.deleteAll()
            userPreferencesRepository.clearAll()
            _events.emit(SettingsEvent.ResetComplete)
        }
    }
}
