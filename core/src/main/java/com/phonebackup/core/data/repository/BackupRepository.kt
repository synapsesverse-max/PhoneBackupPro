package com.phonebackup.core.data.repository

import com.phonebackup.core.data.local.dao.BackupDao
import com.phonebackup.core.data.local.model.BackupRecord
import com.phonebackup.core.data.local.model.BackupStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepository @Inject constructor(
    private val backupDao: BackupDao
) {
    
    fun getAllBackups(): Flow<List<BackupRecord>> {
        return backupDao.getAllBackups()
    }
    
    suspend fun getBackupByUUID(uuid: String): BackupRecord? {
        return backupDao.getBackupByUUID(uuid)
    }
    
    suspend fun insertBackup(backup: BackupRecord): Long {
        return backupDao.insertBackup(backup)
    }
    
    suspend fun updateBackup(backup: BackupRecord) {
        backupDao.updateBackup(backup)
    }
    
    suspend fun deleteBackup(backup: BackupRecord) {
        backupDao.deleteBackup(backup)
    }
    
    suspend fun deleteBackupByUUID(uuid: String) {
        backupDao.deleteBackupByUUID(uuid)
    }
    
    suspend fun updateBackupStatus(uuid: String, status: BackupStatus, endTime: Long = System.currentTimeMillis()) {
        backupDao.updateBackupStatus(uuid, status, endTime)
    }
    
    suspend fun getCompletedBackupCount(): Int {
        return backupDao.getCompletedBackupCount()
    }

    suspend fun getBackupsByStatus(status: BackupStatus): List<BackupRecord> {
        return backupDao.getBackupsByStatus(status)
    }
    
    suspend fun getTotalBackupSize(): Long {
        return backupDao.getTotalBackupSize()
    }
}
