package com.sofato.krone.groups.domain.usecase

import com.sofato.krone.crypto.HexCodec
import com.sofato.krone.groups.data.config.GroupsBuildConfigProvider
import com.sofato.krone.groups.domain.model.PinnedFingerprintMismatch
import com.sofato.krone.groups.domain.model.ServerEnrollment
import com.sofato.krone.groups.domain.repository.DeviceIdentityRepository
import com.sofato.krone.groups.domain.repository.GroupsServerApi
import com.sofato.krone.groups.domain.repository.ServerEnrollmentRepository
import javax.inject.Inject

/**
 * Enrolls against the pinned "donated" server. Auto-compares the server's
 * returned pubkey against the value baked into BuildConfig; on mismatch the
 * enrollment is aborted (likely tampered APK or impersonating server).
 */
class EnrollDonatedServerUseCase @Inject constructor(
    private val config: GroupsBuildConfigProvider,
    private val identityRepo: DeviceIdentityRepository,
    private val enrollmentRepo: ServerEnrollmentRepository,
    private val api: GroupsServerApi,
) {
    suspend operator fun invoke(): ServerEnrollment {
        val pending = api.fetchServerInfo(config.donatedServerUrl)
        val expectedPk = HexCodec.decode(config.donatedServerPkHex)
        if (!pending.serverSigPk.contentEquals(expectedPk)) {
            throw PinnedFingerprintMismatch(
                expectedHex = config.donatedServerPkHex,
                actualHex = HexCodec.encode(pending.serverSigPk),
            )
        }
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
