package com.phonebackup.core.encryption

import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import java.security.SecureRandom
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
        private const val ITERATION_COUNT = 10000
        private const val KEY_LENGTH = 256
    }
    
    /**
     * Encrypt data with password
     */
    fun encrypt(data: ByteArray, password: String): ByteArray {
        val salt = ByteArray(SALT_SIZE).apply {
            SecureRandom().nextBytes(this)
        }
        
        val key = deriveKey(password, salt)
        val iv = ByteArray(IV_SIZE).apply {
            SecureRandom().nextBytes(this)
        }
        
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, ALGORITHM), GCMParameterSpec(GCM_TAG_LENGTH, iv))
        
        val encrypted = cipher.doFinal(data)
        
        // Combine salt + iv + encrypted data
        return salt + iv + encrypted
    }
    
    /**
     * Decrypt data with password
     */
    fun decrypt(encryptedData: ByteArray, password: String): ByteArray {
        val salt = encryptedData.copyOfRange(0, SALT_SIZE)
        val iv = encryptedData.copyOfRange(SALT_SIZE, SALT_SIZE + IV_SIZE)
        val encrypted = encryptedData.copyOfRange(SALT_SIZE + IV_SIZE, encryptedData.size)
        
        val key = deriveKey(password, salt)
        
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, ALGORITHM), GCMParameterSpec(GCM_TAG_LENGTH, iv))
        
        return cipher.doFinal(encrypted)
    }
    
    /**
     * Encrypt file
     */
    fun encryptFile(inputFile: java.io.File, outputFile: java.io.File, password: String) {
        val fileData = inputFile.readBytes()
        val encrypted = encrypt(fileData, password)
        outputFile.writeBytes(encrypted)
    }
    
    /**
     * Decrypt file
     */
    fun decryptFile(inputFile: java.io.File, outputFile: java.io.File, password: String) {
        val encryptedData = inputFile.readBytes()
        val decrypted = decrypt(encryptedData, password)
        outputFile.writeBytes(decrypted)
    }
    
    /**
     * Derive encryption key from password
     */
    private fun deriveKey(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return factory.generateSecret(spec).encoded
    }
    
    /**
     * Generate random encryption key
     */
    fun generateKey(): ByteArray {
        val key = ByteArray(KEY_LENGTH / 8)
        SecureRandom().nextBytes(key)
        return key
    }
}
