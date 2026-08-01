package com.phonebackup.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.phonebackup.core.data.local.dao.*
import com.phonebackup.core.data.local.model.*

@Database(
    entities = [
        BackupRecord::class,
        ContactBackup::class,
        MessageBackup::class,
        MediaBackup::class,
        AppDataBackup::class,
        TransferSession::class,
        CloudSyncRecord::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class BackupDatabase : RoomDatabase() {
    abstract fun backupDao(): BackupDao
    abstract fun contactDao(): ContactDao
    abstract fun messageDao(): MessageDao
    abstract fun mediaDao(): MediaDao
    abstract fun transferDao(): TransferDao
    abstract fun cloudSyncDao(): CloudSyncDao
    
    companion object {
        const val DATABASE_NAME = "phonebackup_pro.db"
    }
}
