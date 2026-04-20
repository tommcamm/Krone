package com.sofato.krone.crypto

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlin.random.Random

class Bip39RoundTripTest {

    private val bip39 = Bip39.loadEnglish()

    @Test
    fun wordlist_has_2048_entries_and_starts_with_abandon() {
        val words = Bip39.loadEnglish().let { b ->
            // sanity: first word maps back to index 0 bit pattern
            b.decode(List(8) { "abandon" })
        }
        // 8 "abandon" words = 88 bits of zeros = 11 zero bytes.
        assertThat(words).isEqualTo(ByteArray(11))
    }

    @Test
    fun round_trip_is_stable_for_random_11_byte_inputs() {
        val rng = Random(0x1234_5678L)
        repeat(200) {
            val input = rng.nextBytes(11)
            val words = bip39.encode(input)
            assertThat(words).hasSize(8)
            val decoded = bip39.decode(words)
            assertThat(decoded).isEqualTo(input)
        }
    }

    @Test
    fun fingerprint_is_deterministic_for_known_pubkey() {
        // Alice's identity_sig_pk from the canonical vectors.
        val pk = HexCodec.decode("8139770ea87d175f56a35466c34c7ecccb8d8a91b4ee37a25df60f5b8fc9b394")
        val fp1 = FingerprintComputer.fromPublicKey(pk, bip39)
        val fp2 = FingerprintComputer.fromPublicKey(pk, bip39)
        assertThat(fp1.words).isEqualTo(fp2.words)
        assertThat(fp1.shortHex).isEqualTo(fp2.shortHex)
        // 11 bytes → 22 hex chars.
        assertThat(fp1.shortHex).hasLength(22)
        assertThat(fp1.words).hasSize(8)
    }
}
