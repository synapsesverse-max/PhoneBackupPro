package com.phonebackup.pro.di

import android.content.Context
import com.phonebackup.backup.BackupEngine
import com.phonebackup.backup.BackupViewModel
import com.phonebackup.restore.RestoreEngine
import com.phonebackup.restore.RestoreViewModel
import com.phonebackup.transfer.CrossPlatformTransfer
import com.phonebackup.transfer.TransferViewModel
import com.phonebackup.whatsapp.WhatsAppBackupManager
import com.phonebackup.whatsapp.WhatsAppRecoveryViewModel
import com.phonebackup.whatsapp.ThirdPartyRecovery
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideBackupEngine(
        @ApplicationContext context: Context,
        backupRepository: com.phonebackup.core.data.repository.BackupRepository
    ): BackupEngine {
        return BackupEngine(context, backupRepository)
    }
    
    @Provides
    @Singleton
    fun provideRestoreEngine(
        @ApplicationContext context: Context,
        restoreRepository: com.phonebackup.core.data.repository.RestoreRepository
    ): RestoreEngine {
        return RestoreEngine(context, restoreRepository)
    }
    
    @Provides
    @Singleton
    fun provideCrossPlatformTransfer(
        @ApplicationContext context: Context
    ): CrossPlatformTransfer {
        return CrossPlatformTransfer(context)
    }
    
    @Provides
    @Singleton
    fun provideWhatsAppBackupManager(
        @ApplicationContext context: Context
    ): WhatsAppBackupManager {
        return WhatsAppBackupManager(context)
    }
    
    @Provides
    @Singleton
    fun provideThirdPartyRecovery(
        @ApplicationContext context: Context
    ): ThirdPartyRecovery {
        return ThirdPartyRecovery(context)
    }
}
