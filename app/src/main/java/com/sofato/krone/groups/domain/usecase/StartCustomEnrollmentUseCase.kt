package com.sofato.krone.groups.domain.usecase

import com.sofato.krone.groups.data.network.UrlPolicy
import com.sofato.krone.groups.domain.model.PendingEnrollment
import com.sofato.krone.groups.domain.repository.GroupsServerApi
import javax.inject.Inject

/**
 * Probes a user-entered URL and returns the fingerprint the UI should show
 * for out-of-band verification. No state persisted until confirmation.
 */
class StartCustomEnrollmentUseCase @Inject constructor(
    private val api: GroupsServerApi,
    private val urlPolicy: UrlPolicy,
) {
    suspend operator fun invoke(rawUrl: String): PendingEnrollment {
        val url = urlPolicy.validate(rawUrl)
        return api.fetchServerInfo(url)
    }
}
