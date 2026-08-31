package com.phonebackup.core.data.repository

import com.phonebackup.core.data.local.dao.ContactDao
import com.phonebackup.core.data.local.dao.MessageDao
import com.phonebackup.core.data.local.dao.MediaDao
import com.phonebackup.core.data.local.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RestoreRepository @Inject constructor(
    private val contactDao: ContactDao,
    private val messageDao: MessageDao,
    private val mediaDao: MediaDao,
    private val backupRepository: BackupRepository
) {
    
    suspend fun restoreContacts(backupUUID: String, contacts: List<ContactBackup>) {
        contactDao.deleteContactsByBackupUUID(backupUUID)
        contactDao.insertContacts(contacts)
    }
    
    suspend fun restoreMessages(backupUUID: String, messages: List<MessageBackup>) {
        messageDao.deleteMessagesByBackupUUID(backupUUID)
        messageDao.insertMessages(messages)
    }
    
    suspend fun restoreMedia(backupUUID: String, mediaList: List<MediaBackup>) {
        mediaDao.deleteMediaByBackupUUID(backupUUID)
        mediaDao.insertMedia(mediaList)
    }
    
    suspend fun getAvailableBackups(): List<BackupRecord> {
        return (backupRepository.getBackupsByStatus(BackupStatus.COMPLETED) +
            backupRepository.getBackupsByStatus(BackupStatus.VERIFIED)).distinctBy { it.backupUUID }
    }
    
    suspend fun markBackupAsRestoring(uuid: String) {
        backupRepository.updateBackupStatus(uuid, BackupStatus.RESTORING)
    }
    
    suspend fun markBackupAsRestored(uuid: String) {
        backupRepository.updateBackupStatus(uuid, BackupStatus.RESTORED)
    }
}
