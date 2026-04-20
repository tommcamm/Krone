package com.sofato.krone.crypto

/** Lowercase hex encode/decode without external deps. */
object HexCodec {
    private val HEX_CHARS = "0123456789abcdef".toCharArray()

    fun encode(bytes: ByteArray): String {
        val out = CharArray(bytes.size * 2)
        for (i in bytes.indices) {
            val v = bytes[i].toInt() and 0xFF
            out[i * 2] = HEX_CHARS[v ushr 4]
            out[i * 2 + 1] = HEX_CHARS[v and 0x0F]
        }
        return String(out)
    }

    fun decode(hex: String): ByteArray {
        require(hex.length % 2 == 0) { "hex length must be even" }
        val out = ByteArray(hex.length / 2)
        for (i in out.indices) {
            val hi = Character.digit(hex[i * 2], 16)
            val lo = Character.digit(hex[i * 2 + 1], 16)
            require(hi >= 0 && lo >= 0) { "invalid hex char at index $i" }
            out[i] = ((hi shl 4) or lo).toByte()
        }
        return out
    }
}
