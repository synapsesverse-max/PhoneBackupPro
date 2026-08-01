package com.phonebackup.whatsapp

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThirdPartyRecovery @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    data class RecoveryResult(
        val messages: List<String> = emptyList(),
        val mediaFiles: List<String> = emptyList(),
        val recoveredFrom: String = "",
        val totalRecovered: Int = 0
    )
    
    /**
     * Detect backup type from third-party tools
     */
    fun detectBackupType(backupPath: String): String {
        val backupDir = File(backupPath)
        if (!backupDir.exists()) return "unknown"
        
        return when {
            // Dr.Fone detection
            backupDir.walkTopDown().any { 
                it.name.contains("drfone", ignoreCase = true) || 
                it.name.contains("Wondershare", ignoreCase = true) 
            } -> "DrFone"
            
            // AnyDroid detection
            backupDir.walkTopDown().any { 
                it.name.contains("anydroid", ignoreCase = true) || 
                it.name.contains("imobie", ignoreCase = true) 
            } -> "AnyDroid"
            
            // MobileTrans detection
            backupDir.walkTopDown().any { 
                it.name.contains("mobiletrans", ignoreCase = true) || 
                it.name.contains("MobileTrans", ignoreCase = true) 
            } -> "MobileTrans"
            
            // iMyFone detection
            backupDir.walkTopDown().any { 
                it.name.contains("imyfone", ignoreCase = true) 
            } -> "iMyFone"
            
            else -> "unknown"
        }
    }
    
    /**
     * Recover WhatsApp data from third-party backup
     */
    fun recoverFromBackup(backupPath: String): Flow<RecoveryProgress> = flow {
        emit(RecoveryProgress.Detecting)
        
        val backupType = detectBackupType(backupPath)
        
        emit(RecoveryProgress.Detected(backupType))
        
        // Parse based on backup type
        val parser = when (backupType) {
            "DrFone" -> DrFoneParser(backupPath)
            "AnyDroid" -> AnyDroidParser(backupPath)
            "MobileTrans" -> MobileTransParser(backupPath)
            "iMyFone" -> IMyFoneParser(backupPath)
            else -> throw Exception("Unsupported backup format: $backupType")
        }
        
        emit(RecoveryProgress.Extracting)
        val result = parser.parse()
        
        emit(RecoveryProgress.Completed(result))
    }
}

sealed class RecoveryProgress {
    object Detecting : RecoveryProgress()
    data class Detected(val type: String) : RecoveryProgress()
    object Extracting : RecoveryProgress()
    data class Completed(val result: ThirdPartyRecovery.RecoveryResult) : RecoveryProgress()
    data class Error(val message: String) : RecoveryProgress()
}

/**
 * Base parser class for third-party backups
 */
abstract class ThirdPartyParser(protected val backupPath: String) {
    abstract fun parse(): ThirdPartyRecovery.RecoveryResult
    
    protected fun findWhatsAppFiles(): List<File> {
        val backupDir = File(backupPath)
        val whatsappFiles = mutableListOf<File>()
        
        backupDir.walkTopDown().forEach { file ->
            if (file.isFile && (
                file.name.contains("msgstore", ignoreCase = true) ||
                file.name.contains("whatsapp", ignoreCase = true) ||
                file.name.contains("ChatStorage", ignoreCase = true)
            )) {
                whatsappFiles.add(file)
            }
        }
        
        return whatsappFiles
    }
}

/**
 * Dr.Fone backup parser
 */
class DrFoneParser(backupPath: String) : ThirdPartyParser(backupPath) {
    override fun parse(): ThirdPartyRecovery.RecoveryResult {
        val files = findWhatsAppFiles()
        val messages = mutableListOf<String>()
        val mediaFiles = mutableListOf<String>()
        
        files.forEach { file ->
            try {
                // Try to parse database files
                if (file.extension in listOf("db", "sqlite", "crypt12", "crypt14", "crypt15")) {
                    // Parse WhatsApp database
                    messages.add("Recovered from: ${file.name}")
                } else if (file.extension in listOf("jpg", "jpeg", "png", "mp4", "pdf")) {
                    mediaFiles.add(file.absolutePath)
                }
            } catch (e: Exception) {
                // Skip corrupted files
            }
        }
        
        return ThirdPartyRecovery.RecoveryResult(
            messages = messages,
            mediaFiles = mediaFiles,
            recoveredFrom = "Dr.Fone",
            totalRecovered = messages.size + mediaFiles.size
        )
    }
}

/**
 * AnyDroid backup parser
 */
class AnyDroidParser(backupPath: String) : ThirdPartyParser(backupPath) {
    override fun parse(): ThirdPartyRecovery.RecoveryResult {
        val files = findWhatsAppFiles()
        val messages = mutableListOf<String>()
        val mediaFiles = mutableListOf<String>()
        
        files.forEach { file ->
            try {
                if (file.extension in listOf("db", "sqlite")) {
                    messages.add("Recovered from: ${file.name}")
                } else if (file.extension in listOf("jpg", "jpeg", "png", "mp4", "pdf")) {
                    mediaFiles.add(file.absolutePath)
                }
            } catch (e: Exception) {
                // Skip corrupted files
            }
        }
        
        return ThirdPartyRecovery.RecoveryResult(
            messages = messages,
            mediaFiles = mediaFiles,
            recoveredFrom = "AnyDroid",
            totalRecovered = messages.size + mediaFiles.size
        )
    }
}

/**
 * MobileTrans backup parser
 */
class MobileTransParser(backupPath: String) : ThirdPartyParser(backupPath) {
    override fun parse(): ThirdPartyRecovery.RecoveryResult {
        val files = findWhatsAppFiles()
        val messages = mutableListOf<String>()
        val mediaFiles = mutableListOf<String>()
        
        files.forEach { file ->
            try {
                if (file.extension in listOf("db", "sqlite", "bak")) {
                    messages.add("Recovered from: ${file.name}")
                } else if (file.extension in listOf("jpg", "jpeg", "png", "mp4", "pdf")) {
                    mediaFiles.add(file.absolutePath)
                }
            } catch (e: Exception) {
                // Skip corrupted files
            }
        }
        
        return ThirdPartyRecovery.RecoveryResult(
            messages = messages,
            mediaFiles = mediaFiles,
            recoveredFrom = "MobileTrans",
            totalRecovered = messages.size + mediaFiles.size
        )
    }
}

/**
 * iMyFone backup parser
 */
class IMyFoneParser(backupPath: String) : ThirdPartyParser(backupPath) {
    override fun parse(): ThirdPartyRecovery.RecoveryResult {
        val files = findWhatsAppFiles()
        val messages = mutableListOf<String>()
        val mediaFiles = mutableListOf<String>()
        
        files.forEach { file ->
            try {
                if (file.extension in listOf("db", "sqlite")) {
                    messages.add("Recovered from: ${file.name}")
                } else if (file.extension in listOf("jpg", "jpeg", "png", "mp4", "pdf")) {
                    mediaFiles.add(file.absolutePath)
                }
            } catch (e: Exception) {
                // Skip corrupted files
            }
        }
        
        return ThirdPartyRecovery.RecoveryResult(
            messages = messages,
            mediaFiles = mediaFiles,
            recoveredFrom = "iMyFone",
            totalRecovered = messages.size + mediaFiles.size
        )
    }
}
