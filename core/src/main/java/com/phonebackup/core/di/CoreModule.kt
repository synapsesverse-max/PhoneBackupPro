package com.phonebackup.core.di

import android.content.Context
import androidx.room.Room
import com.phonebackup.core.data.local.BackupDatabase
import com.phonebackup.core.data.local.dao.BackupDao
import com.phonebackup.core.data.local.dao.ContactDao
import com.phonebackup.core.data.local.dao.MediaDao
import com.phonebackup.core.data.local.dao.MessageDao
import com.phonebackup.core.encryption.EncryptionService
import com.phonebackup.core.encryption.EncryptionManager
import com.phonebackup.core.compression.CompressionService
import com.phonebackup.core.compression.CompressionManager
import com.phonebackup.core.network.NetworkService
import com.phonebackup.core.network.TransferManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoreModule {
    
    @Provides
    @Singleton
    fun provideEncryptionService(): EncryptionService {
        return EncryptionService()
    }
    
    @Provides
    @Singleton
    fun provideEncryptionManager(encryptionService: EncryptionService): EncryptionManager {
        return EncryptionManager(encryptionService)
    }
    
    @Provides
    @Singleton
    fun provideCompressionService(): CompressionService {
        return CompressionService()
    }
    
    @Provides
    @Singleton
    fun provideCompressionManager(compressionService: CompressionService): CompressionManager {
        return CompressionManager(compressionService)
    }
    
    @Provides
    @Singleton
    fun provideNetworkService(): NetworkService {
        return NetworkService()
    }
    
    @Provides
    @Singleton
    fun provideTransferManager(networkService: NetworkService): TransferManager {
        return TransferManager(networkService)
    }
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BackupDatabase {
        return Room.databaseBuilder(
            context,
            BackupDatabase::class.java,
            BackupDatabase.DATABASE_NAME
        ).build()
    }
    
    @Provides
    @Singleton
    fun provideBackupDao(database: BackupDatabase): BackupDao {
        return database.backupDao()
    }
    
    @Provides
    @Singleton
    fun provideContactDao(database: BackupDatabase): ContactDao {
        return database.contactDao()
    }
    
    @Provides
    @Singleton
    fun provideMessageDao(database: BackupDatabase): MessageDao {
        return database.messageDao()
    }
    
    @Provides
    @Singleton
    fun provideMediaDao(database: BackupDatabase): MediaDao {
        return database.mediaDao()
    }
}
