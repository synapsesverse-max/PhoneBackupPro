package com.phonebackup.core.encryption

import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptionManager @Inject constructor(
    private val encryptionService: EncryptionService
) {
    
    /**
     * Encrypt file with password
     */
    fun encryptFile(inputFile: File, password: String): File {
        val outputFile = File(inputFile.parent, "${inputFile.name}.enc")
        encryptionService.encryptFile(inputFile, outputFile, password)
        return outputFile
    }
    
    /**
     * Decrypt file with password
     */
    fun decryptFile(inputFile: File, password: String): File {
        val outputFile = File(inputFile.parent, inputFile.name.removeSuffix(".enc"))
        encryptionService.decryptFile(inputFile, outputFile, password)
        return outputFile
    }
    
    /**
     * Generate and store encryption key
     */
    fun generateKey(): ByteArray {
        return encryptionService.generateKey()
    }
    
    /**
     * Encrypt data with key
     */
    fun encryptData(data: ByteArray, password: String): ByteArray {
        return encryptionService.encrypt(data, password)
    }
    
    /**
     * Decrypt data with key
     */
    fun decryptData(data: ByteArray, password: String): ByteArray {
        return encryptionService.decrypt(data, password)
    }
}
