package com.phonebackup.backup

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.phonebackup.core.data.local.model.BackupRecord
import com.phonebackup.core.data.repository.BackupRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class BackupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val backupEngine: BackupEngine,
    private val backupRepository: BackupRepository
) : CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): Result {
        val backupUUID = inputData.getString("backup_uuid") ?: return Result.failure()
        val backup = backupRepository.getBackupByUUID(backupUUID) ?: return Result.failure()
        
        return try {
            backupEngine.executeFullBackup(backup).collect { progress ->
                when (progress) {
                    is BackupProgress.Processing -> {
                        setProgress(workDataOf("progress" to progress.progress))
                    }
                    is BackupProgress.Completed -> {
                        // Success
                    }
                    is BackupProgress.Error -> {
                        throw Exception(progress.message)
                    }
                    else -> {}
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
    
    companion object {
        fun createRequest(backupUUID: String): OneTimeWorkRequest {
            val data = workDataOf("backup_uuid" to backupUUID)
            
            return OneTimeWorkRequestBuilder<BackupWorker>()
                .setInputData(data)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .setRequiresStorageNotLow(true)
                        .build()
                )
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    30,
                    TimeUnit.SECONDS
                )
                .build()
        }
    }
}
