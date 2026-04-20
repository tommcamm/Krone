package com.sofato.krone.crypto

/**
 * 88-bit fingerprint of an Ed25519 public key, rendered both as 8 BIP-39
 * words (for voice/OOB verification) and as a short hex string.
 *
 * Protocol does not prescribe the encoding; it is a client-side UX affordance.
 * 88 bits ≈ 2^44 work to grind a collision — adequate for trust-on-first-use.
 */
data class Fingerprint(
    val bytes: ByteArray,
    val words: List<String>,
    val shortHex: String,
) {
    override fun equals(other: Any?) = other is Fingerprint && bytes.contentEquals(other.bytes)
    override fun hashCode() = bytes.contentHashCode()

    fun matches(other: Fingerprint): Boolean = constantTimeEquals(bytes, other.bytes)
}

object FingerprintComputer {
    private const val TRUNCATED_BYTES = 11 // 88 bits → 8 BIP-39 words

    fun fromPublicKey(publicKey: ByteArray, bip39: Bip39): Fingerprint {
        val hash = CanonicalSigning.sha256(publicKey)
        val truncated = hash.copyOfRange(0, TRUNCATED_BYTES)
        return Fingerprint(
            bytes = truncated,
            words = bip39.encode(truncated),
            shortHex = HexCodec.encode(truncated),
        )
    }
}

internal fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
    if (a.size != b.size) return false
    var diff = 0
    for (i in a.indices) diff = diff or (a[i].toInt() xor b[i].toInt())
    return diff == 0
}
