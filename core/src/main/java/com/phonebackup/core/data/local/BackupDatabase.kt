package com.phonebackup.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.phonebackup.core.data.local.dao.BackupDao
import com.phonebackup.core.data.local.dao.ContactDao
import com.phonebackup.core.data.local.dao.MediaDao
import com.phonebackup.core.data.local.dao.MessageDao
import com.phonebackup.core.data.local.model.BackupRecord
import com.phonebackup.core.data.local.model.ContactBackup
import com.phonebackup.core.data.local.model.MediaBackup
import com.phonebackup.core.data.local.model.MessageBackup

@Database(
    entities = [
        BackupRecord::class,
        ContactBackup::class,
        MessageBackup::class,
        MediaBackup::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BackupDatabase : RoomDatabase() {
    abstract fun backupDao(): BackupDao
    abstract fun contactDao(): ContactDao
    abstract fun messageDao(): MessageDao
    abstract fun mediaDao(): MediaDao
    
    companion object {
        const val DATABASE_NAME = "phonebackup_pro.db"
    }
}
