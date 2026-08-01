package com.phonebackup.whatsapp

import android.content.Context
import android.os.Environment
import com.phonebackup.core.encryption.EncryptionService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WhatsAppBackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val encryptionService: EncryptionService
) {
    
    data class WhatsAppBackupResult(
        val messagesCount: Int = 0,
        val mediaCount: Int = 0,
        val totalSize: Long = 0,
        val backupPath: String = ""
    )
    
    /**
     * Create complete WhatsApp backup
     */
    suspend fun createBackup(backupDir: File): Result<WhatsAppBackupResult> {
        return withContext(Dispatchers.IO) {
            try {
                val result = WhatsAppBackupResult()
                
                // 1. Backup databases
                val dbResult = backupDatabases(backupDir)
                
                // 2. Backup media
                val mediaResult = backupMedia(backupDir)
                
                // 3. Backup key file (requires root or special access)
                backupKeyFile(backupDir)
                
                Result.success(
                    result.copy(
                        messagesCount = dbResult.first,
                        mediaCount = mediaResult.first,
                        totalSize = dbResult.second + mediaResult.second,
                        backupPath = backupDir.absolutePath
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Backup WhatsApp databases
     */
    private fun backupDatabases(backupDir: File): Pair<Int, Long> {
        val sourceDir = File(Environment.getExternalStorageDirectory(), "WhatsApp/Databases")
        if (!sourceDir.exists()) return Pair(0, 0L)
        
        val targetDir = File(backupDir, "Databases")
        targetDir.mkdirs()
        
        var count = 0
        var size = 0L
        
        sourceDir.listFiles()?.forEach { file ->
            if (file.name.startsWith("msgstore") || file.name.startsWith("wa.db")) {
                file.copyTo(File(targetDir, file.name), overwrite = true)
                count++
                size += file.length()
            }
        }
        
        return Pair(count, size)
    }
    
    /**
     * Backup WhatsApp media files
     */
    private fun backupMedia(backupDir: File): Pair<Int, Long> {
        val sourceDir = File(Environment.getExternalStorageDirectory(), "WhatsApp/Media")
        if (!sourceDir.exists()) return Pair(0, 0L)
        
        val targetDir = File(backupDir, "Media")
        targetDir.mkdirs()
        
        var count = 0
        var size = 0L
        
        sourceDir.walkTopDown().forEach { file ->
            if (file.isFile) {
                val relativePath = file.relativeTo(sourceDir)
                val targetFile = File(targetDir, relativePath.path)
                targetFile.parentFile?.mkdirs()
                file.copyTo(targetFile, overwrite = true)
                count++
                size += file.length()
            }
        }
        
        return Pair(count, size)
    }
    
    /**
     * Backup WhatsApp encryption key
     */
    private fun backupKeyFile(backupDir: File) {
        try {
            val keyFile = File("/data/data/com.whatsapp/files/key")
            if (keyFile.exists()) {
                keyFile.copyTo(File(backupDir, "key"), overwrite = true)
            }
        } catch (e: Exception) {
            // Key file access may require root
        }
    }
    
    /**
     * Detect available backups
     */
    fun detectBackups(): List<WhatsAppBackupInfo> {
        val backups = mutableListOf<WhatsAppBackupInfo>()
        
        // Local backups
        val localDir = File(Environment.getExternalStorageDirectory(), "WhatsApp/Databases")
        if (localDir.exists()) {
            localDir.listFiles()?.forEach { file ->
                if (file.name.matches(Regex("msgstore-\d{4}-\d{2}-\d{2}.*"))) {
                    backups.add(
                        WhatsAppBackupInfo(
                            name = file.name,
                            path = file.absolutePath,
                            size = file.length(),
                            lastModified = file.lastModified(),
                            type = "local"
                        )
                    )
                }
            }
        }
        
        return backups
    }
}

data class WhatsAppBackupInfo(
    val name: String,
    val path: String,
    val size: Long,
    val lastModified: Long,
    val type: String
)
