package com.sofato.krone.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * AES-256-GCM wrapper over the Android Keystore. Used to encrypt long-term
 * Ed25519 secret keys at rest so they survive DB backups as unusable
 * ciphertext (the Keystore-resident AES key does not leave the device).
 *
 * For MVP we set `setUserAuthenticationRequired(false)` so a background
 * WorkManager job can sign outgoing requests without a biometric prompt.
 */
class KeystoreWrapper(private val keyAlias: String = DEFAULT_ALIAS) {

    data class WrappedBlob(val iv: ByteArray, val ciphertext: ByteArray) {
        override fun equals(other: Any?) = other is WrappedBlob &&
            iv.contentEquals(other.iv) && ciphertext.contentEquals(other.ciphertext)
        override fun hashCode() = iv.contentHashCode() * 31 + ciphertext.contentHashCode()
    }

    fun wrap(plaintext: ByteArray): WrappedBlob {
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        }
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(plaintext)
        return WrappedBlob(iv = iv, ciphertext = ciphertext)
    }

    fun unwrap(blob: WrappedBlob): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(TAG_BITS, blob.iv))
        }
        return cipher.doFinal(blob.ciphertext)
    }

    fun clear() {
        val ks = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        if (ks.containsAlias(keyAlias)) ks.deleteEntry(keyAlias)
    }

    private fun getOrCreateKey(): SecretKey {
        val ks = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (ks.getKey(keyAlias, null) as? SecretKey)?.let { return it }

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val spec = KeyGenParameterSpec.Builder(
            keyAlias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setUserAuthenticationRequired(false)
            .build()
        generator.init(spec)
        return generator.generateKey()
    }

    companion object {
        const val DEFAULT_ALIAS = "krone.groups.identity_v1"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val TAG_BITS = 128
    }
}
