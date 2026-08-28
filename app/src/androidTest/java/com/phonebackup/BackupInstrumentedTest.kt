package com.phonebackup

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.phonebackup.core.data.local.BackupDatabase
import com.phonebackup.core.encryption.EncryptionService
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue

@RunWith(AndroidJUnit4::class)
class BackupInstrumentedTest {

    private lateinit var context: Context
    private lateinit var database: BackupDatabase
    private lateinit var encryptionService: EncryptionService

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        encryptionService = EncryptionService()
    }

    @Test
    fun `test app context is valid`() {
        assertNotNull(context)
        assertTrue(context.packageName.contains("phonebackup"))
    }

    @Test
    fun `test encryption service works on device`() {
        val data = "Test data for instrumented test".toByteArray()
        val password = "device_test_password"

        val encrypted = encryptionService.encrypt(data, password)
        val decrypted = encryptionService.decrypt(encrypted, password)

        assertTrue(encrypted.isNotEmpty())
        assertTrue(decrypted.isNotEmpty())
    }

    @Test
    fun `test backup directory is accessible`() {
        val backupDir = requireNotNull(context.getExternalFilesDir(null))
        assertTrue(backupDir.exists() || backupDir.mkdirs())
    }

    @Test
    fun `test file creation in backup directory`() {
        val backupDir = requireNotNull(context.getExternalFilesDir(null))
        val testFile = java.io.File(backupDir, "test_backup.txt")

        testFile.writeText("Test backup content")
        assertTrue(testFile.exists())

        val content = testFile.readText()
        assertTrue(content == "Test backup content")

        testFile.delete()
    }

    @After
    fun cleanup() {
        // Clean up any test data
    }
}
