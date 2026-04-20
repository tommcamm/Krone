package com.sofato.krone.crypto

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Invariants of the canonicalization itself (independent of vector file).
 * The SigningVectorsTest covers reproducibility against the protocol vectors;
 * these guard the boundary cases.
 */
class CanonicalSigningTest {

    @Test
    fun request_signing_input_layout_matches_spec() {
        val body = "{}".toByteArray()
        val input = CanonicalSigning.buildRequestSigningInput(
            method = "post",
            path = "/devices",
            timestamp = 42,
            deviceIdHex = "aabbccddeeff00112233445566778899",
            body = body,
        )
        val expected =
            "krone-req-v1\n".toByteArray() +
                "42\n".toByteArray() +
                "aabbccddeeff00112233445566778899\n".toByteArray() +
                "POST\n".toByteArray() +
                "/devices\n".toByteArray() +
                CanonicalSigning.sha256(body)
        assertThat(input).isEqualTo(expected)
    }

    @Test
    fun response_signing_input_layout_matches_spec() {
        val body = "{\"status\":\"ok\"}".toByteArray()
        val input = CanonicalSigning.buildResponseSigningInput(
            requestId = "01HZY0000000000000000RID001",
            statusCode = 200,
            body = body,
        )
        val expected =
            "krone-res-v1\n".toByteArray() +
                "01HZY0000000000000000RID001\n".toByteArray() +
                "200\n".toByteArray() +
                CanonicalSigning.sha256(body)
        assertThat(input).isEqualTo(expected)
    }

    @Test
    fun method_is_uppercased() {
        val a = CanonicalSigning.buildRequestSigningInput(
            "get", "/x", 1, "00".repeat(16), ByteArray(0)
        )
        val b = CanonicalSigning.buildRequestSigningInput(
            "GET", "/x", 1, "00".repeat(16), ByteArray(0)
        )
        assertThat(a).isEqualTo(b)
    }
}
