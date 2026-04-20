package com.sofato.krone.groups.domain.usecase

import com.sofato.krone.groups.domain.model.PendingEnrollment
import com.sofato.krone.groups.domain.model.ServerEnrollment
import com.sofato.krone.groups.domain.repository.DeviceIdentityRepository
import com.sofato.krone.groups.domain.repository.GroupsServerApi
import com.sofato.krone.groups.domain.repository.ServerEnrollmentRepository
import javax.inject.Inject

/**
 * After the user eyeballs the fingerprint and confirms, persist the enrollment
 * and register this device's pubkey with the server so subsequent signed
 * requests can be verified.
 */
class ConfirmCustomEnrollmentUseCase @Inject constructor(
    private val identityRepo: DeviceIdentityRepository,
    private val enrollmentRepo: ServerEnrollmentRepository,
    private val api: GroupsServerApi,
) {
    suspend operator fun invoke(pending: PendingEnrollment): ServerEnrollment {
        val identity = identityRepo.getOrCreate()
        val enrollment = ServerEnrollment(
            url = pending.url,
            serverSigPk = pending.serverSigPk,
            fingerprint = pending.fingerprint,
            enrolledAtEpochMs = System.currentTimeMillis(),
        )
        enrollmentRepo.save(enrollment)
        api.registerDevice(pending.url, identity, pending.serverSigPk)
        return enrollment
    }
}
