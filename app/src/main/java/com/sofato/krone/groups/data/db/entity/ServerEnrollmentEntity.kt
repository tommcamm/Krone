package com.sofato.krone.groups.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Single-row table recording the currently-enrolled groups server.
 * Client pins against `serverSigPk` on every response (application-layer TOFU).
 */
@Entity(tableName = "server_enrollment")
data class ServerEnrollmentEntity(
    @PrimaryKey val id: Int = SINGLE_ROW_ID,
    val url: String,
    val serverSigPk: ByteArray,
    val fingerprintWords: String,
    val fingerprintHex: String,
    val enrolledAt: Long,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ServerEnrollmentEntity) return false
        return id == other.id &&
            url == other.url &&
            serverSigPk.contentEquals(other.serverSigPk) &&
            fingerprintWords == other.fingerprintWords &&
            fingerprintHex == other.fingerprintHex &&
            enrolledAt == other.enrolledAt
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + url.hashCode()
        result = 31 * result + serverSigPk.contentHashCode()
        result = 31 * result + fingerprintWords.hashCode()
        result = 31 * result + fingerprintHex.hashCode()
        result = 31 * result + enrolledAt.hashCode()
        return result
    }

    companion object {
        const val SINGLE_ROW_ID = 1
    }
}
