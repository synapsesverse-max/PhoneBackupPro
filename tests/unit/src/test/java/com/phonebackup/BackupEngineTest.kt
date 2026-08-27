package com.phonebackup

import com.phonebackup.backup.BackupEngine
import com.phonebackup.backup.BackupOptions
import com.phonebackup.backup.BackupProgress
import com.phonebackup.core.data.local.model.BackupRecord
import com.phonebackup.core.data.local.model.BackupType
import com.phonebackup.core.data.repository.BackupRepository
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import kotlin.test.assertTrue

class BackupEngineTest {
    
    @Mock
    private lateinit var backupRepository: BackupRepository
    
    private lateinit var backupEngine: BackupEngine
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        // Note: In real test, context would be mocked
        backupEngine = mock(BackupEngine::class.java)
    }
    
    @Test
    fun `completed backup exposes verified artifact metadata`() {
        val artifact = java.io.File.createTempFile("phonebackup", ".pbp")
        artifact.writeText("verified backup")
        val completed = BackupProgress.Completed(
            backupDir = artifact.parentFile,
            artifact = artifact,
            checksum = "abc123"
        )
        assertTrue(completed.artifact.exists())
        assertTrue(completed.checksum.isNotBlank())
        artifact.delete()
    }
    
    @Test
    fun `test backup options default values`() {
        val options = BackupOptions()
        
        assertTrue(options.includeContacts)
        assertTrue(options.includeMessages)
        assertTrue(options.includeMedia)
        assertTrue(!options.includeWhatsApp)
        assertTrue(options.encryptionPassword == null)
    }
    
    @Test
    fun `test backup with encryption password`() {
        val options = BackupOptions(encryptionPassword = "test123")
        
        assertTrue(options.encryptionPassword == "test123")
    }
}
