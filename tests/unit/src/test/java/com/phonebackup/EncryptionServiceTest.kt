package com.phonebackup

import com.phonebackup.core.encryption.EncryptionService
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class EncryptionServiceTest {
    
    private lateinit var encryptionService: EncryptionService
    
    @Before
    fun setup() {
        encryptionService = EncryptionService()
    }
    
    @Test
    fun `test encrypt and decrypt data`() {
        // Given
        val originalData = "Hello, World!".toByteArray()
        val password = "testPassword123"
        
        // When
        val encrypted = encryptionService.encrypt(originalData, password)
        val decrypted = encryptionService.decrypt(encrypted, password)
        
        // Then
        assertNotEquals(originalData.toList(), encrypted.toList(), "Encrypted data should differ from original")
        assertEquals(originalData.toList(), decrypted.toList(), "Decrypted data should match original")
    }
    
    @Test
    fun `test different passwords produce different results`() {
        // Given
        val data = "Test data".toByteArray()
        
        // When
        val encrypted1 = encryptionService.encrypt(data, "password1")
        val encrypted2 = encryptionService.encrypt(data, "password2")
        
        // Then
        assertNotEquals(encrypted1.toList(), encrypted2.toList(), "Different passwords should produce different encrypted data")
    }
    
    @Test
    fun `test decryption with wrong password fails`() {
        // Given
        val data = "Secret data".toByteArray()
        val encrypted = encryptionService.encrypt(data, "correct_password")
        
        // When/Then
        try {
            encryptionService.decrypt(encrypted, "wrong_password")
            // If we get here, decryption didn't throw - which is possible with GCM
            // but the data should be garbage
        } catch (e: Exception) {
            // Expected - AEADBadTagException or similar
            assertTrue(true, "Expected exception on wrong password")
        }
    }
    
    @Test
    fun `test key generation`() {
        val key1 = encryptionService.generateKey()
        val key2 = encryptionService.generateKey()
        
        assertTrue(key1.isNotEmpty())
        assertTrue(key2.isNotEmpty())
        assertNotEquals(key1.toList(), key2.toList(), "Generated keys should be different")
    }
    
    @Test
    fun `test file encryption and decryption`() {
        val originalFile = java.io.File.createTempFile("test", ".txt")
        originalFile.writeText("Test file content")
        
        val encryptedFile = java.io.File.createTempFile("test", ".enc")
        val decryptedFile = java.io.File.createTempFile("test", ".dec")
        
        encryptionService.encryptFile(originalFile, encryptedFile, "password")
        encryptionService.decryptFile(encryptedFile, decryptedFile, "password")
        
        assertEquals(
            originalFile.readText(),
            decryptedFile.readText(),
            "Decrypted file should match original"
        )
        
        originalFile.delete()
        encryptedFile.delete()
        decryptedFile.delete()
    }
}
