package com.example.a1lab.domain

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object PasswordVerifier {
    private const val ITERATIONS = 20_000
    private val random = SecureRandom()

    fun register(password: CharArray): PasswordCredentials {
        val salt = ByteArray(16).also(random::nextBytes)
        return PasswordCredentials(salt.toHex(), derive(password, salt).toHex())
    }

    fun matches(password: CharArray, credentials: PasswordCredentials): Boolean {
        val salt = credentials.saltHex.hexToBytes() ?: return false
        val expected = credentials.hashHex.hexToBytes() ?: return false
        return MessageDigest.isEqual(derive(password, salt), expected)
    }

    private fun derive(password: CharArray, salt: ByteArray): ByteArray {
        val passwordBytes = password.concatToString().toByteArray(Charsets.UTF_8)
        try {
            val mac = Mac.getInstance("HmacSHA256").apply {
                init(SecretKeySpec(passwordBytes, "HmacSHA256"))
            }
            mac.update(salt)
            mac.update(byteArrayOf(0, 0, 0, 1))
            var block = mac.doFinal()
            val result = block.copyOf()
            repeat(ITERATIONS - 1) {
                block = mac.doFinal(block)
                result.indices.forEach { index -> result[index] = (result[index].toInt() xor block[index].toInt()).toByte() }
            }
            return result
        } finally {
            passwordBytes.fill(0)
        }
    }

    private fun ByteArray.toHex() = joinToString(separator = "") { byte -> "%02x".format(byte) }

    private fun String.hexToBytes(): ByteArray? {
        if (length % 2 != 0) return null
        return runCatching {
            ByteArray(length / 2) { index -> substring(index * 2, index * 2 + 2).toInt(16).toByte() }
        }.getOrNull()
    }
}
