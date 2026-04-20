package com.sofato.krone.crypto

import com.goterl.lazysodium.LazySodiumJava
import com.goterl.lazysodium.SodiumJava
import com.goterl.lazysodium.interfaces.Sign
import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.BeforeClass
import org.junit.Test
import java.io.File
import java.util.Base64

/**
 * Byte-for-byte reproduction of the canonical vectors pinned in the protocol
 * submodule at `protocol/vectors/signing_vectors.json`. Every implementation
 * (this client, the Rust server, any future client) must match all three
 * independently-verifiable layers: body hash, signing-input, signature.
 */
class SigningVectorsTest {

    companion object {
        private val JSON = Json { ignoreUnknownKeys = true }
        private lateinit var vectors: SigningVectors
        private lateinit var signer: Ed25519Signer

        @JvmStatic
        @BeforeClass
        fun loadVectors() {
            val candidates = listOf(
                File("../protocol/vectors/signing_vectors.json"),
                File("protocol/vectors/signing_vectors.json"),
            )
            val file = candidates.firstOrNull { it.isFile }
                ?: error("signing_vectors.json not found (tried: $candidates; cwd=${File(".").canonicalPath})")
            val text = file.readText(Charsets.UTF_8)
            vectors = JSON.decodeFromString(SigningVectors.serializer(), text)
            val sodium = LazySodiumJava(SodiumJava())
            signer = Ed25519Signer(sodium as Sign.Native)
        }
    }

    @Test
    fun request_vectors_reproduce_exactly() {
        assertThat(vectors.request_vectors).isNotEmpty()
        for (vec in vectors.request_vectors) {
            val body = vec.body_utf8.toByteArray(Charsets.UTF_8)

            val bodyDigest = HexCodec.encode(CanonicalSigning.sha256(body))
            assertThat(bodyDigest).isEqualTo(vec.body_sha256_hex)

            val input = CanonicalSigning.buildRequestSigningInput(
                method = vec.method,
                path = vec.path,
                timestamp = vec.timestamp,
                deviceIdHex = vectors.clients.getValue(vec.signer).device_id_hex,
                body = body,
            )
            assertThat(HexCodec.encode(input)).isEqualTo(vec.signing_input_hex)

            val seed = HexCodec.decode(vectors.clients.getValue(vec.signer).seed_hex)
            val keypair = signer.keypairFromSeed(seed)
            assertThat(HexCodec.encode(keypair.publicKey))
                .isEqualTo(vectors.clients.getValue(vec.signer).public_key_hex)

            val sig = signer.sign(keypair.secretKey, input)
            assertThat(Base64.getEncoder().encodeToString(sig)).isEqualTo(vec.signature_b64)

            val parsed = Base64.getDecoder().decode(vec.signature_b64)
            assertThat(signer.verify(keypair.publicKey, input, parsed)).isTrue()
        }
    }

    @Test
    fun response_vectors_reproduce_exactly() {
        assertThat(vectors.response_vectors).isNotEmpty()
        val seed = HexCodec.decode(vectors.server.seed_hex)
        val keypair = signer.keypairFromSeed(seed)
        assertThat(HexCodec.encode(keypair.publicKey)).isEqualTo(vectors.server.public_key_hex)

        for (vec in vectors.response_vectors) {
            val body = vec.body_utf8.toByteArray(Charsets.UTF_8)
            assertThat(HexCodec.encode(CanonicalSigning.sha256(body)))
                .isEqualTo(vec.body_sha256_hex)

            val input = CanonicalSigning.buildResponseSigningInput(
                requestId = vec.request_id,
                statusCode = vec.status_code,
                body = body,
            )
            assertThat(HexCodec.encode(input)).isEqualTo(vec.signing_input_hex)

            val sig = signer.sign(keypair.secretKey, input)
            assertThat(Base64.getEncoder().encodeToString(sig)).isEqualTo(vec.signature_b64)

            val parsed = Base64.getDecoder().decode(vec.signature_b64)
            assertThat(signer.verify(keypair.publicKey, input, parsed)).isTrue()
        }
    }
}

@Serializable
internal data class SigningVectors(
    val format_version: Int,
    val server: ServerSeed,
    val clients: Map<String, ClientSeed>,
    val request_vectors: List<RequestVector>,
    val response_vectors: List<ResponseVector>,
)

@Serializable
internal data class ServerSeed(val seed_hex: String, val public_key_hex: String)

@Serializable
internal data class ClientSeed(
    val seed_hex: String,
    val public_key_hex: String,
    val device_id_hex: String,
)

@Serializable
internal data class RequestVector(
    val name: String,
    val signer: String,
    val method: String,
    val path: String,
    val timestamp: Long,
    val body_utf8: String,
    val body_sha256_hex: String,
    val signing_input_hex: String,
    val signature_b64: String,
)

@Serializable
internal data class ResponseVector(
    val name: String,
    val request_id: String,
    val status_code: Int,
    val body_utf8: String,
    val body_sha256_hex: String,
    val signing_input_hex: String,
    val signature_b64: String,
)
