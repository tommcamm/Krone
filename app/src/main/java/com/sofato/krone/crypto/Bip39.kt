package com.sofato.krone.crypto

/**
 * Raw BIP-39 encoding — splits a bit string into 11-bit groups, each indexing
 * the English wordlist. No checksum (we are encoding an already-truncated
 * fingerprint, not generating a wallet seed).
 *
 * For an N-byte input where N*8 is a multiple of 11, produces N*8/11 words.
 * We use 11 bytes (88 bits) → 8 words for server/device fingerprints.
 */
class Bip39(private val wordlist: List<String>) {

    private val indexByWord: Map<String, Int> = wordlist.withIndex().associate { it.value to it.index }

    init {
        require(wordlist.size == WORDLIST_SIZE) {
            "BIP-39 wordlist must have $WORDLIST_SIZE entries, got ${wordlist.size}"
        }
    }

    fun encode(bytes: ByteArray): List<String> {
        val bitCount = bytes.size * 8
        require(bitCount % BITS_PER_WORD == 0) {
            "input bit count ($bitCount) must be divisible by $BITS_PER_WORD"
        }
        val wordCount = bitCount / BITS_PER_WORD
        val out = ArrayList<String>(wordCount)
        for (w in 0 until wordCount) {
            val startBit = w * BITS_PER_WORD
            var index = 0
            for (b in 0 until BITS_PER_WORD) {
                val bitPos = startBit + b
                val byte = bytes[bitPos / 8].toInt() and 0xFF
                val bit = (byte ushr (7 - (bitPos % 8))) and 1
                index = (index shl 1) or bit
            }
            out += wordlist[index]
        }
        return out
    }

    fun decode(words: List<String>): ByteArray {
        val bitCount = words.size * BITS_PER_WORD
        require(bitCount % 8 == 0) {
            "word count (${words.size}) must produce whole bytes ($bitCount bits)"
        }
        val out = ByteArray(bitCount / 8)
        for (w in words.indices) {
            val idx = indexByWord[words[w]]
                ?: throw IllegalArgumentException("unknown BIP-39 word: ${words[w]}")
            for (b in 0 until BITS_PER_WORD) {
                val bit = (idx ushr (BITS_PER_WORD - 1 - b)) and 1
                val bitPos = w * BITS_PER_WORD + b
                if (bit == 1) {
                    val byteIdx = bitPos / 8
                    val shift = 7 - (bitPos % 8)
                    out[byteIdx] = (out[byteIdx].toInt() or (1 shl shift)).toByte()
                }
            }
        }
        return out
    }

    companion object {
        const val BITS_PER_WORD = 11
        const val WORDLIST_SIZE = 2048

        /**
         * Loads the bundled English wordlist from classpath resource `bip39_english.txt`
         * (available in both the Android APK and JVM test classpath).
         */
        fun loadEnglish(classLoader: ClassLoader = Bip39::class.java.classLoader!!): Bip39 {
            val stream = classLoader.getResourceAsStream(WORDLIST_RESOURCE)
                ?: error("BIP-39 wordlist resource '$WORDLIST_RESOURCE' not found on classpath")
            val words = stream.bufferedReader(Charsets.UTF_8).use { reader ->
                reader.lineSequence().map { it.trim() }.filter { it.isNotEmpty() }.toList()
            }
            return Bip39(words)
        }

        private const val WORDLIST_RESOURCE = "bip39_english.txt"
    }
}
