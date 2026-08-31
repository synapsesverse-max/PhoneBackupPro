package com.phonebackup.restore

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class RestoreWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val restoreEngine: RestoreEngine
) : CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): Result {
        val backupUUID = inputData.getString("backup_uuid") ?: return Result.failure()
        
        val options = RestoreOptions(
            restoreContacts = inputData.getBoolean("restore_contacts", true),
            restoreMessages = inputData.getBoolean("restore_messages", true),
            restoreMedia = inputData.getBoolean("restore_media", true),
            restoreWhatsApp = inputData.getBoolean("restore_whatsapp", false)
        )
        
        return try {
            restoreEngine.executeRestore(backupUUID, options).collect { progress ->
                when (progress) {
                    is RestoreProgress.Processing -> {
                        setProgress(workDataOf("progress" to progress.progress))
                    }
                    is RestoreProgress.Completed -> {}
                    is RestoreProgress.Error -> {
                        throw Exception(progress.message)
                    }
                    else -> {}
                }
            }
            Result.success()
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(workDataOf("error" to (e.message ?: "Restore failed")))
        }
    }
    
    companion object {
        fun createRequest(backupUUID: String, options: RestoreOptions): OneTimeWorkRequest {
            val data = workDataOf(
                "backup_uuid" to backupUUID,
                "restore_contacts" to options.restoreContacts,
                "restore_messages" to options.restoreMessages,
                "restore_media" to options.restoreMedia,
                "restore_whatsapp" to options.restoreWhatsApp
            )
            
            return OneTimeWorkRequestBuilder<RestoreWorker>()
                .setInputData(data)
                .build()
        }
    }
}
