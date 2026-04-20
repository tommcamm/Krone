package com.sofato.krone.groups.domain.repository

import com.sofato.krone.groups.domain.model.DeviceIdentity
import com.sofato.krone.groups.domain.model.PendingEnrollment

/** Thin contract over the few endpoints Phase 0 exercises. */
interface GroupsServerApi {

    /** Unsigned probe used during enrollment to learn the server's pubkey + policy. */
    suspend fun fetchServerInfo(baseUrl: String): PendingEnrollment

    /**
     * Registers this device's identity pubkey with the server. Signed with the
     * device's own key (the protocol exempts /devices from pubkey lookup).
     * The server's response signature is verified against [serverSigPk].
     */
    suspend fun registerDevice(
        baseUrl: String,
        identity: DeviceIdentity,
        serverSigPk: ByteArray,
    )

    /**
     * Deletes this device's registration on the server. Signed request,
     * response signature verified against [serverSigPk].
     */
    suspend fun deleteSelf(
        baseUrl: String,
        identity: DeviceIdentity,
        serverSigPk: ByteArray,
    )
}
