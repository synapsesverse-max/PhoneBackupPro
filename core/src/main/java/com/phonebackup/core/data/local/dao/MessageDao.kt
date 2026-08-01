package com.phonebackup.core.data.local.dao

import androidx.room.*
import com.phonebackup.core.data.local.model.MessageBackup
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    
    @Query("SELECT * FROM message_backups ORDER BY date DESC")
    fun getAllMessages(): Flow<List<MessageBackup>>
    
    @Query("SELECT * FROM message_backups WHERE backup_uuid = :backupUUID ORDER BY date DESC")
    fun getMessagesByBackupUUID(backupUUID: String): Flow<List<MessageBackup>>
    
    @Query("SELECT * FROM message_backups WHERE address = :phoneNumber ORDER BY date DESC")
    fun getMessagesByPhoneNumber(phoneNumber: String): Flow<List<MessageBackup>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageBackup>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageBackup): Long
    
    @Update
    suspend fun updateMessage(message: MessageBackup)
    
    @Delete
    suspend fun deleteMessage(message: MessageBackup)
    
    @Query("DELETE FROM message_backups WHERE backup_uuid = :backupUUID")
    suspend fun deleteMessagesByBackupUUID(backupUUID: String)
    
    @Query("SELECT COUNT(*) FROM message_backups WHERE backup_uuid = :backupUUID")
    suspend fun getMessageCount(backupUUID: String): Int
}
