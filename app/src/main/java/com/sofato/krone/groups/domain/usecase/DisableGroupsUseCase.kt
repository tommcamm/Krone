package com.sofato.krone.groups.domain.usecase

import com.sofato.krone.domain.repository.UserPreferencesRepository
import com.sofato.krone.groups.domain.repository.DeviceIdentityRepository
import com.sofato.krone.groups.domain.repository.GroupsServerApi
import com.sofato.krone.groups.domain.repository.ServerEnrollmentRepository
import javax.inject.Inject

/**
 * Purge Groups: best-effort `DELETE /devices/self` against the current server,
 * then wipe identity, enrollment, and the opt-in preference regardless of
 * whether the server call succeeded. The server deletion is courtesy — the
 * client-side purge is authoritative.
 */
class DisableGroupsUseCase @Inject constructor(
    private val prefs: UserPreferencesRepository,
    private val identity: DeviceIdentityRepository,
    private val enrollment: ServerEnrollmentRepository,
    private val api: GroupsServerApi,
) {
    suspend operator fun invoke() {
        val device = identity.get()
        val server = enrollment.get()
        if (device != null && server != null) {
            runCatching { api.deleteSelf(server.url, device, server.serverSigPk) }
        }
        enrollment.clear()
        identity.clear()
        prefs.setGroupsEnabled(false)
    }
}
