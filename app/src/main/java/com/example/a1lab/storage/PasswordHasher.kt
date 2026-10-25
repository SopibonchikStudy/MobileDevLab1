package com.example.a1lab.storage

import android.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.security.SecureRandom

class PasswordHasher(
    private val iterations: Int = 20_000,
    private val random: SecureRandom = SecureRandom()
) {
    data class StoredPassword(val salt: String, val hash: String)

    fun create(password: CharArray): StoredPassword {
        val salt = ByteArray(16).also(random::nextBytes)
        return StoredPassword(encode(salt), encode(derive(password, salt, iterations)))
    }

    fun verify(password: CharArray, salt: String, expectedHash: String): Boolean {
        val saltBytes = Base64.decode(salt, Base64.NO_WRAP)
        val actual = derive(password, saltBytes, iterations)
        return constantTimeEquals(actual, Base64.decode(expectedHash, Base64.NO_WRAP))
    }

    private fun derive(password: CharArray, salt: ByteArray, iterations: Int): ByteArray {
        val passwordBytes = password.concatToString().toByteArray(Charsets.UTF_8)
        try {
            val mac = Mac.getInstance("HmacSHA256")
            mac.init(SecretKeySpec(passwordBytes, "HmacSHA256"))
            mac.update(salt)
            mac.update(byteArrayOf(0, 0, 0, 1))
            var block = mac.doFinal()
            val result = block.copyOf()
            repeat(iterations - 1) {
                mac.update(block)
                block = mac.doFinal()
                for (index in result.indices) result[index] = (result[index].toInt() xor block[index].toInt()).toByte()
            }
            return result
        } finally {
            passwordBytes.fill(0)
        }
    }

    private fun constantTimeEquals(left: ByteArray, right: ByteArray): Boolean {
        if (left.size != right.size) return false
        var difference = 0
        for (index in left.indices) difference = difference or (left[index].toInt() xor right[index].toInt())
        return difference == 0
    }

    private fun encode(bytes: ByteArray): String = Base64.encodeToString(bytes, Base64.NO_WRAP)
}
