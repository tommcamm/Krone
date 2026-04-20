package com.sofato.krone.crypto

import java.security.MessageDigest

/**
 * Canonical signing-input construction for krone-protocol v1.
 *
 * Layout matches `protocol/vectors/signing_vectors.json`. The trailing component
 * is the **raw 32-byte SHA-256 digest** of the body, not its hex encoding.
 */
object CanonicalSigning {

    private const val REQUEST_TAG = "krone-req-v1"
    private const val RESPONSE_TAG = "krone-res-v1"
    private const val NL: Byte = 0x0A

    fun buildRequestSigningInput(
        method: String,
        path: String,
        timestamp: Long,
        deviceIdHex: String,
        body: ByteArray,
    ): ByteArray {
        val digest = sha256(body)
        return buildByteArray {
            appendUtf8(REQUEST_TAG); append(NL)
            appendUtf8(timestamp.toString()); append(NL)
            appendUtf8(deviceIdHex); append(NL)
            appendUtf8(method.uppercase()); append(NL)
            appendUtf8(path); append(NL)
            append(digest)
        }
    }

    fun buildResponseSigningInput(
        requestId: String,
        statusCode: Int,
        body: ByteArray,
    ): ByteArray {
        val digest = sha256(body)
        return buildByteArray {
            appendUtf8(RESPONSE_TAG); append(NL)
            appendUtf8(requestId); append(NL)
            appendUtf8(statusCode.toString()); append(NL)
            append(digest)
        }
    }

    fun sha256(bytes: ByteArray): ByteArray =
        MessageDigest.getInstance("SHA-256").digest(bytes)
}

private inline fun buildByteArray(block: ByteArrayBuilder.() -> Unit): ByteArray =
    ByteArrayBuilder().apply(block).toByteArray()

private class ByteArrayBuilder {
    private val buffer = java.io.ByteArrayOutputStream()
    fun append(b: Byte) { buffer.write(b.toInt()) }
    fun append(bytes: ByteArray) { buffer.write(bytes) }
    fun appendUtf8(s: String) { buffer.write(s.toByteArray(Charsets.UTF_8)) }
    fun toByteArray(): ByteArray = buffer.toByteArray()
}
