package com.sofato.krone.groups.domain.model

import com.sofato.krone.crypto.Fingerprint

/** Persisted record of a successfully-enrolled groups server. */
data class ServerEnrollment(
    val url: String,
    val serverSigPk: ByteArray,
    val fingerprint: Fingerprint,
    val enrolledAtEpochMs: Long,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ServerEnrollment) return false
        return url == other.url &&
            serverSigPk.contentEquals(other.serverSigPk) &&
            fingerprint == other.fingerprint &&
            enrolledAtEpochMs == other.enrolledAtEpochMs
    }

    override fun hashCode(): Int {
        var result = url.hashCode()
        result = 31 * result + serverSigPk.contentHashCode()
        result = 31 * result + fingerprint.hashCode()
        result = 31 * result + enrolledAtEpochMs.hashCode()
        return result
    }
}

/**
 * A server's `/server-info` response has been fetched and the fingerprint is
 * ready to show to the user. Upgraded to a [ServerEnrollment] on confirmation.
 */
data class PendingEnrollment(
    val url: String,
    val serverSigPk: ByteArray,
    val fingerprint: Fingerprint,
    val protocolVersion: String,
    val serverVersion: String,
    val policy: ServerPolicy,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PendingEnrollment) return false
        return url == other.url &&
            serverSigPk.contentEquals(other.serverSigPk) &&
            fingerprint == other.fingerprint &&
            protocolVersion == other.protocolVersion &&
            serverVersion == other.serverVersion &&
            policy == other.policy
    }

    override fun hashCode(): Int {
        var result = url.hashCode()
        result = 31 * result + serverSigPk.contentHashCode()
        result = 31 * result + fingerprint.hashCode()
        result = 31 * result + protocolVersion.hashCode()
        result = 31 * result + serverVersion.hashCode()
        result = 31 * result + policy.hashCode()
        return result
    }
}

data class ServerPolicy(
    val ttlSeconds: Int,
    val maxEnvelopeBytes: Int,
    val maxInboxPerDevice: Int,
    val maxEnvelopesPerDevicePerHour: Int,
    val clockSkewSeconds: Int,
)
