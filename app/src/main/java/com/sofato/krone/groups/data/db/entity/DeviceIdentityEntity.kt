package com.sofato.krone.groups.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Single-row table holding this device's long-term Ed25519 identity.
 * The secret key is stored as AES-GCM ciphertext (Keystore-wrapped) + IV.
 */
@Entity(tableName = "device_identity")
data class DeviceIdentityEntity(
    @PrimaryKey val id: Int = SINGLE_ROW_ID,
    val sigPk: ByteArray,
    val sigSkEncIv: ByteArray,
    val sigSkEnc: ByteArray,
    val createdAt: Long,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DeviceIdentityEntity) return false
        return id == other.id &&
            sigPk.contentEquals(other.sigPk) &&
            sigSkEncIv.contentEquals(other.sigSkEncIv) &&
            sigSkEnc.contentEquals(other.sigSkEnc) &&
            createdAt == other.createdAt
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + sigPk.contentHashCode()
        result = 31 * result + sigSkEncIv.contentHashCode()
        result = 31 * result + sigSkEnc.contentHashCode()
        result = 31 * result + createdAt.hashCode()
        return result
    }

    companion object {
        const val SINGLE_ROW_ID = 1
    }
}
