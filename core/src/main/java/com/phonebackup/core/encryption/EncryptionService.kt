package com.phonebackup.core.encryption

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptionService @Inject constructor() {
    companion object {
        private const val ALGORITHM = "AES"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 128
        private const val IV_SIZE = 12
        private const val SALT_SIZE = 16
        private const val KEY_LENGTH = 256
        private const val ITERATION_COUNT = 310_000
        private const val MAGIC = 0x50425045 // PBPE
        private const val FORMAT_VERSION = 1
        private const val BUFFER_SIZE = 64 * 1024
    }

    fun encrypt(data: ByteArray, password: String): ByteArray {
        val salt = randomBytes(SALT_SIZE)
        val iv = randomBytes(IV_SIZE)
        return salt + iv + createCipher(Cipher.ENCRYPT_MODE, password, salt, iv).doFinal(data)
    }

    fun decrypt(encryptedData: ByteArray, password: String): ByteArray {
        require(encryptedData.size >= SALT_SIZE + IV_SIZE + 16) { "Encrypted payload is truncated" }
        val salt = encryptedData.copyOfRange(0, SALT_SIZE)
        val iv = encryptedData.copyOfRange(SALT_SIZE, SALT_SIZE + IV_SIZE)
        return createCipher(Cipher.DECRYPT_MODE, password, salt, iv)
            .doFinal(encryptedData, SALT_SIZE + IV_SIZE, encryptedData.size - SALT_SIZE - IV_SIZE)
    }

    fun encryptFile(inputFile: File, outputFile: File, password: String) {
        require(inputFile.isFile) { "Input file does not exist: ${inputFile.absolutePath}" }
        outputFile.parentFile?.mkdirs()
        val salt = randomBytes(SALT_SIZE)
        val iv = randomBytes(IV_SIZE)
        DataOutputStream(BufferedOutputStream(FileOutputStream(outputFile))).use { raw ->
            raw.writeInt(MAGIC)
            raw.writeInt(FORMAT_VERSION)
            raw.writeInt(SALT_SIZE)
            raw.writeInt(IV_SIZE)
            raw.write(salt)
            raw.write(iv)
            raw.flush()
            CipherOutputStream(raw, createCipher(Cipher.ENCRYPT_MODE, password, salt, iv)).use { encrypted ->
                BufferedInputStream(FileInputStream(inputFile)).use { input ->
                    input.copyTo(encrypted, BUFFER_SIZE)
                }
            }
        }
    }

    fun decryptFile(inputFile: File, outputFile: File, password: String) {
        require(inputFile.isFile) { "Input file does not exist: ${inputFile.absolutePath}" }
        outputFile.parentFile?.mkdirs()
        DataInputStream(BufferedInputStream(FileInputStream(inputFile))).use { raw ->
            require(raw.readInt() == MAGIC) { "Unsupported encrypted backup format" }
            require(raw.readInt() == FORMAT_VERSION) { "Unsupported encrypted backup version" }
            require(raw.readInt() == SALT_SIZE && raw.readInt() == IV_SIZE) { "Corrupt encrypted backup header" }
            val salt = ByteArray(SALT_SIZE)
            val iv = ByteArray(IV_SIZE)
            raw.readFully(salt)
            raw.readFully(iv)
            CipherInputStream(raw, createCipher(Cipher.DECRYPT_MODE, password, salt, iv)).use { decrypted ->
                BufferedOutputStream(FileOutputStream(outputFile)).use { output ->
                    decrypted.copyTo(output, BUFFER_SIZE)
                }
            }
        }
    }

    fun generateKey(): ByteArray = randomBytes(KEY_LENGTH / 8)

    private fun createCipher(mode: Int, password: String, salt: ByteArray, iv: ByteArray): Cipher {
        val key = deriveKey(password, salt)
        return Cipher.getInstance(TRANSFORMATION).apply {
            init(mode, SecretKeySpec(key, ALGORITHM), GCMParameterSpec(GCM_TAG_LENGTH, iv))
            key.fill(0)
        }
    }

    private fun deriveKey(password: String, salt: ByteArray): ByteArray {
        require(password.isNotEmpty()) { "Password must not be empty" }
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH)
        return try {
            val factory = try {
                SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            } catch (_: java.security.NoSuchAlgorithmException) {
                // Android API 24 does not expose PBKDF2WithHmacSHA256 in every provider.
                // PBKDF2WithHmacSHA1 is the platform-compatible fallback for legacy devices.
                SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
            }
            factory.generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    private fun randomBytes(size: Int): ByteArray = ByteArray(size).also { SecureRandom().nextBytes(it) }
}
