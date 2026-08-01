package com.phonebackup.core.data.local.dao

import androidx.room.*
import com.phonebackup.core.data.local.model.MediaBackup
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
    
    @Query("SELECT * FROM media_backups ORDER BY date_added DESC")
    fun getAllMedia(): Flow<List<MediaBackup>>
    
    @Query("SELECT * FROM media_backups WHERE backup_uuid = :backupUUID")
    fun getMediaByBackupUUID(backupUUID: String): Flow<List<MediaBackup>>
    
    @Query("SELECT * FROM media_backups WHERE media_type = :mediaType")
    fun getMediaByType(mediaType: String): Flow<List<MediaBackup>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(mediaList: List<MediaBackup>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaItem(media: MediaBackup): Long
    
    @Update
    suspend fun updateMedia(media: MediaBackup)
    
    @Delete
    suspend fun deleteMedia(media: MediaBackup)
    
    @Query("DELETE FROM media_backups WHERE backup_uuid = :backupUUID")
    suspend fun deleteMediaByBackupUUID(backupUUID: String)
    
    @Query("SELECT SUM(file_size) FROM media_backups WHERE backup_uuid = :backupUUID")
    suspend fun getTotalMediaSize(backupUUID: String): Long
}
