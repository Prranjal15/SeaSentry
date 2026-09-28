package com.seasentry.app.auth

import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Cryptographic utilities for secure local PIN hashing and validation.
 * Uses SHA-256 with per-account random salt. Never stores plaintext PINs.
 */
object AuthCrypto {

    private val secureRandom = SecureRandom()

    /**
     * Generates a cryptographically secure random salt encoded as a hexadecimal string.
     */
    fun generateSalt(byteLength: Int = 16): String {
        val bytes = ByteArray(byteLength)
        secureRandom.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Hashes a PIN with a salt using SHA-256.
     * Computes SHA-256(pin + salt).
     */
    fun hashPin(pin: String, salt: String): String {
        val messageDigest = MessageDigest.getInstance("SHA-256")
        val inputBytes = (pin + salt).toByteArray(Charsets.UTF_8)
        val hashBytes = messageDigest.digest(inputBytes)
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Verifies if a raw PIN matches the stored PIN hash given the account salt.
     */
    fun verifyPin(rawPin: String, storedHash: String, salt: String): Boolean {
        val computedHash = hashPin(rawPin, salt)
        return MessageDigest.isEqual(
            computedHash.toByteArray(Charsets.UTF_8),
            storedHash.toByteArray(Charsets.UTF_8)
        )
    }

    /**
     * Validates that the PIN is 4 to 6 numeric digits.
     */
    fun isValidPin(pin: String): Boolean {
        return pin.length in 4..6 && pin.all { it.isDigit() }
    }
}
