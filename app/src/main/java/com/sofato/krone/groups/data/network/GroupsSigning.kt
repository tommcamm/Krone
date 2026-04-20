package com.sofato.krone.groups.data.network

import android.util.Base64
import com.sofato.krone.crypto.CanonicalSigning
import com.sofato.krone.crypto.Ed25519Signer
import com.sofato.krone.groups.domain.model.DeviceIdentity
import com.sofato.krone.groups.domain.model.ServerResponseSignatureInvalid

/**
 * Signing helper shared by every client → server call. Builds the krone-req-v1
 * canonical input and returns the three HTTP headers, plus verifies the
 * krone-res-v1 `x-server-signature` response header.
 */
class GroupsSigning(
    private val signer: Ed25519Signer,
    private val clock: () -> Long = { System.currentTimeMillis() / 1000L },
) {

    data class Headers(
        val deviceId: String,
        val timestamp: String,
        val signature: String,
    )

    fun signRequest(
        identity: DeviceIdentity,
        method: String,
        path: String,
        body: ByteArray,
    ): Headers {
        val timestamp = clock()
        val input = CanonicalSigning.buildRequestSigningInput(
            method = method,
            path = path,
            timestamp = timestamp,
            deviceIdHex = identity.deviceIdHex,
            body = body,
        )
        val sig = signer.sign(identity.identitySigSk, input)
        return Headers(
            deviceId = identity.deviceIdHex,
            timestamp = timestamp.toString(),
            signature = Base64.encodeToString(sig, Base64.NO_WRAP),
        )
    }

    fun verifyResponse(
        serverSigPk: ByteArray,
        requestId: String,
        statusCode: Int,
        body: ByteArray,
        signatureBase64: String,
    ) {
        val input = CanonicalSigning.buildResponseSigningInput(requestId, statusCode, body)
        val sig = Base64.decode(signatureBase64, Base64.DEFAULT)
        if (!signer.verify(serverSigPk, input, sig)) {
            throw ServerResponseSignatureInvalid(requestId)
        }
    }
}
