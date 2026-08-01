package com.phonebackup.core.data.local.dao

import androidx.room.*
import com.phonebackup.core.data.local.model.ContactBackup
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    
    @Query("SELECT * FROM contact_backups ORDER BY name ASC")
    fun getAllContacts(): Flow<List<ContactBackup>>
    
    @Query("SELECT * FROM contact_backups WHERE backup_uuid = :backupUUID")
    suspend fun getContactsByBackupUUID(backupUUID: String): List<ContactBackup>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<ContactBackup>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactBackup): Long
    
    @Update
    suspend fun updateContact(contact: ContactBackup)
    
    @Delete
    suspend fun deleteContact(contact: ContactBackup)
    
    @Query("DELETE FROM contact_backups WHERE backup_uuid = :backupUUID")
    suspend fun deleteContactsByBackupUUID(backupUUID: String)
    
    @Query("SELECT COUNT(*) FROM contact_backups WHERE backup_uuid = :backupUUID")
    suspend fun getContactCount(backupUUID: String): Int
}
