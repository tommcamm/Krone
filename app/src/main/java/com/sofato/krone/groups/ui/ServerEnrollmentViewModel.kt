package com.sofato.krone.groups.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sofato.krone.crypto.HexCodec
import com.sofato.krone.groups.data.config.GroupsBuildConfigProvider
import com.sofato.krone.groups.domain.model.InsecureSchemeNotAllowed
import com.sofato.krone.groups.domain.model.PendingEnrollment
import com.sofato.krone.groups.domain.model.PinnedFingerprintMismatch
import com.sofato.krone.groups.domain.usecase.ConfirmCustomEnrollmentUseCase
import com.sofato.krone.groups.domain.usecase.EnrollDonatedServerUseCase
import com.sofato.krone.groups.domain.usecase.StartCustomEnrollmentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ServerEnrollmentViewModel @Inject constructor(
    config: GroupsBuildConfigProvider,
    private val startCustom: StartCustomEnrollmentUseCase,
    private val confirmCustom: ConfirmCustomEnrollmentUseCase,
    private val enrollDonated: EnrollDonatedServerUseCase,
) : ViewModel() {

    data class UiState(
        val donatedUrl: String,
        val donatedPkHex: String,
        val allowHttp: Boolean,
        val customUrl: String = "",
        val pending: PendingEnrollment? = null,
        val busy: Boolean = false,
        val errorMessage: String? = null,
    )

    sealed interface Event {
        data object EnrollmentCompleted : Event
        data class Error(val message: String) : Event
    }

    private val _state = MutableStateFlow(
        UiState(
            donatedUrl = config.donatedServerUrl,
            donatedPkHex = config.donatedServerPkHex,
            allowHttp = config.allowHttp,
        )
    )
    val state: StateFlow<UiState> = _state.asStateFlow()

    private val _events = Channel<Event>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun updateCustomUrl(url: String) {
        _state.value = _state.value.copy(customUrl = url, errorMessage = null)
    }

    fun enrollDonated() {
        viewModelScope.launch {
            _state.value = _state.value.copy(busy = true, errorMessage = null)
            runCatching { enrollDonated.invoke() }
                .onSuccess { _events.send(Event.EnrollmentCompleted) }
                .onFailure { e ->
                    val msg = when (e) {
                        is PinnedFingerprintMismatch ->
                            "Donated server fingerprint mismatch. Possible tampering — enrollment aborted."
                        else -> e.message ?: "Enrollment failed"
                    }
                    _state.value = _state.value.copy(errorMessage = msg)
                    _events.send(Event.Error(msg))
                }
            _state.value = _state.value.copy(busy = false)
        }
    }

    fun fetchCustomFingerprint() {
        viewModelScope.launch {
            _state.value = _state.value.copy(busy = true, errorMessage = null)
            runCatching { startCustom(_state.value.customUrl) }
                .onSuccess { _state.value = _state.value.copy(pending = it) }
                .onFailure { e ->
                    val msg = when (e) {
                        is InsecureSchemeNotAllowed ->
                            "http:// is only allowed on debug builds. Use https://."
                        else -> e.message ?: "Couldn't reach server"
                    }
                    _state.value = _state.value.copy(errorMessage = msg)
                    _events.send(Event.Error(msg))
                }
            _state.value = _state.value.copy(busy = false)
        }
    }

    fun confirmCustom() {
        val pending = _state.value.pending ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(busy = true, errorMessage = null)
            runCatching { confirmCustom.invoke(pending) }
                .onSuccess { _events.send(Event.EnrollmentCompleted) }
                .onFailure { e ->
                    val msg = e.message ?: "Enrollment failed"
                    _state.value = _state.value.copy(errorMessage = msg)
                    _events.send(Event.Error(msg))
                }
            _state.value = _state.value.copy(busy = false)
        }
    }

    /** Human-readable fingerprint preview for the pinned donated pubkey. */
    fun pinnedDonatedPkBytes(): ByteArray = HexCodec.decode(_state.value.donatedPkHex)
}
