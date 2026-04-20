package com.sofato.krone.groups.domain.usecase

import com.sofato.krone.domain.repository.UserPreferencesRepository
import com.sofato.krone.groups.domain.model.DeviceIdentity
import com.sofato.krone.groups.domain.repository.DeviceIdentityRepository
import javax.inject.Inject

/**
 * Turn the feature on: sets the preference and ensures a device identity exists.
 * Does not enroll with any server — that is a separate step.
 */
class EnableGroupsUseCase @Inject constructor(
    private val prefs: UserPreferencesRepository,
    private val identity: DeviceIdentityRepository,
) {
    suspend operator fun invoke(): DeviceIdentity {
        val device = identity.getOrCreate()
        prefs.setGroupsEnabled(true)
        return device
    }
}
