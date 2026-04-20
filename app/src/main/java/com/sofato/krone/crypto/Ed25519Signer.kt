package com.sofato.krone.crypto

import com.goterl.lazysodium.interfaces.Sign

/**
 * Ed25519 keypair + detached signing, backed by libsodium.
 *
 * Construct with a `Sign.Native` instance — `LazySodiumAndroid` on device,
 * `LazySodiumJava` in JVM unit tests. The two share this API.
 */
class Ed25519Signer(private val sodium: Sign.Native) {

    data class Keypair(val publicKey: ByteArray, val secretKey: ByteArray) {
        override fun equals(other: Any?) = other is Keypair &&
            publicKey.contentEquals(other.publicKey) &&
            secretKey.contentEquals(other.secretKey)
        override fun hashCode() = publicKey.contentHashCode() * 31 + secretKey.contentHashCode()
    }

    fun generateKeypair(): Keypair {
        val pk = ByteArray(Sign.PUBLICKEYBYTES)
        val sk = ByteArray(Sign.SECRETKEYBYTES)
        check(sodium.cryptoSignKeypair(pk, sk)) { "cryptoSignKeypair failed" }
        return Keypair(pk, sk)
    }

    fun keypairFromSeed(seed: ByteArray): Keypair {
        require(seed.size == Sign.SEEDBYTES) { "seed must be ${Sign.SEEDBYTES} bytes" }
        val pk = ByteArray(Sign.PUBLICKEYBYTES)
        val sk = ByteArray(Sign.SECRETKEYBYTES)
        check(sodium.cryptoSignSeedKeypair(pk, sk, seed)) { "cryptoSignSeedKeypair failed" }
        return Keypair(pk, sk)
    }

    fun sign(secretKey: ByteArray, message: ByteArray): ByteArray {
        require(secretKey.size == Sign.SECRETKEYBYTES) { "sk must be ${Sign.SECRETKEYBYTES} bytes" }
        val signature = ByteArray(Sign.BYTES)
        check(
            sodium.cryptoSignDetached(signature, message, message.size.toLong(), secretKey)
        ) { "cryptoSignDetached failed" }
        return signature
    }

    fun verify(publicKey: ByteArray, message: ByteArray, signature: ByteArray): Boolean {
        if (publicKey.size != Sign.PUBLICKEYBYTES) return false
        if (signature.size != Sign.BYTES) return false
        return sodium.cryptoSignVerifyDetached(signature, message, message.size, publicKey)
    }
}
