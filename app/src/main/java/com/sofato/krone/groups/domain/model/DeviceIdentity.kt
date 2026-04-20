package com.sofato.krone.groups.domain.model

import com.sofato.krone.crypto.Fingerprint

/**
 * This device's long-term signing identity.
 * `deviceIdHex` = first 16 bytes of SHA-256(identitySigPk) as lowercase hex.
 */
data class DeviceIdentity(
    val deviceIdHex: String,
    val identitySigPk: ByteArray,
    val identitySigSk: ByteArray,
    val fingerprint: Fingerprint,
    val createdAtEpochMs: Long,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DeviceIdentity) return false
        return deviceIdHex == other.deviceIdHex &&
            identitySigPk.contentEquals(other.identitySigPk) &&
            identitySigSk.contentEquals(other.identitySigSk) &&
            fingerprint == other.fingerprint &&
            createdAtEpochMs == other.createdAtEpochMs
    }

    override fun hashCode(): Int {
        var result = deviceIdHex.hashCode()
        result = 31 * result + identitySigPk.contentHashCode()
        result = 31 * result + identitySigSk.contentHashCode()
        result = 31 * result + fingerprint.hashCode()
        result = 31 * result + createdAtEpochMs.hashCode()
        return result
    }
}
