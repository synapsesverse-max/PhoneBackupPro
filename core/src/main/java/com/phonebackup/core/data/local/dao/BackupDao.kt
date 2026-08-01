package com.phonebackup.core.data.local.dao

import androidx.room.*
import com.phonebackup.core.data.local.model.BackupRecord
import com.phonebackup.core.data.local.model.BackupStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface BackupDao {
    
    @Query("SELECT * FROM backup_records ORDER BY start_time DESC")
    fun getAllBackups(): Flow<List<BackupRecord>>
    
    @Query("SELECT * FROM backup_records WHERE backup_uuid = :uuid")
    suspend fun getBackupByUUID(uuid: String): BackupRecord?
    
    @Query("SELECT * FROM backup_records WHERE status = :status")
    suspend fun getBackupsByStatus(status: BackupStatus): List<BackupRecord>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBackup(backup: BackupRecord): Long
    
    @Update
    suspend fun updateBackup(backup: BackupRecord)
    
    @Delete
    suspend fun deleteBackup(backup: BackupRecord)
    
    @Query("DELETE FROM backup_records WHERE backup_uuid = :uuid")
    suspend fun deleteBackupByUUID(uuid: String)
    
    @Query("UPDATE backup_records SET status = :status, end_time = :endTime WHERE backup_uuid = :uuid")
    suspend fun updateBackupStatus(uuid: String, status: BackupStatus, endTime: Long = System.currentTimeMillis())
    
    @Query("SELECT COUNT(*) FROM backup_records WHERE status = 'COMPLETED'")
    suspend fun getCompletedBackupCount(): Int
    
    @Query("SELECT SUM(backup_size) FROM backup_records WHERE status = 'COMPLETED'")
    suspend fun getTotalBackupSize(): Long
}
