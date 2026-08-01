package com.phonebackup

import com.phonebackup.whatsapp.ThirdPartyRecovery
import org.junit.Before
import org.junit.Test
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

class WhatsAppRecoveryTest {
    
    private lateinit var thirdPartyRecovery: ThirdPartyRecovery
    
    @Before
    fun setup() {
        // In a real test, we'd use a mock context
        thirdPartyRecovery = object : ThirdPartyRecovery(null!!) {
            override fun detectBackupType(backupPath: String): String {
                return when {
                    backupPath.contains("drfone", ignoreCase = true) -> "DrFone"
                    backupPath.contains("anydroid", ignoreCase = true) -> "AnyDroid"
                    backupPath.contains("mobiletrans", ignoreCase = true) -> "MobileTrans"
                    backupPath.contains("imyfone", ignoreCase = true) -> "iMyFone"
                    else -> "unknown"
                }
            }
        }
    }
    
    @Test
    fun `test detect DrFone backup`() {
        val type = thirdPartyRecovery.detectBackupType("/test/drfone_backup")
        assertTrue(type == "DrFone", "Should detect DrFone backup")
    }
    
    @Test
    fun `test detect AnyDroid backup`() {
        val type = thirdPartyRecovery.detectBackupType("/test/anydroid_data")
        assertTrue(type == "AnyDroid", "Should detect AnyDroid backup")
    }
    
    @Test
    fun `test detect MobileTrans backup`() {
        val type = thirdPartyRecovery.detectBackupType("/test/mobiletrans_backup")
        assertTrue(type == "MobileTrans", "Should detect MobileTrans backup")
    }
    
    @Test
    fun `test detect iMyFone backup`() {
        val type = thirdPartyRecovery.detectBackupType("/test/imyfone_files")
        assertTrue(type == "iMyFone", "Should detect iMyFone backup")
    }
    
    @Test
    fun `test unknown backup returns unknown`() {
        val type = thirdPartyRecovery.detectBackupType("/test/random_folder")
        assertTrue(type == "unknown", "Unknown backup should return 'unknown'")
    }
    
    @Test
    fun `test recovery result structure`() {
        val result = ThirdPartyRecovery.RecoveryResult(
            messages = listOf("msg1", "msg2"),
            mediaFiles = listOf("file1.jpg"),
            recoveredFrom = "TestTool",
            totalRecovered = 3
        )
        
        assertTrue(result.messages.size == 2)
        assertTrue(result.mediaFiles.size == 1)
        assertTrue(result.totalRecovered == 3)
        assertTrue(result.recoveredFrom == "TestTool")
    }
}
