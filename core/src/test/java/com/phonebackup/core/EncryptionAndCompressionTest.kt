package com.phonebackup.core

import com.phonebackup.core.compression.CompressionService
import com.phonebackup.core.encryption.EncryptionService
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class EncryptionAndCompressionTest {
    private val encryption = EncryptionService()
    private val compression = CompressionService()

    @Test
    fun byteArrayRoundTripAndWrongPasswordFail() {
        val original = "PhoneBackupPro verified payload".toByteArray()
        val encrypted = encryption.encrypt(original, "correct horse battery staple")
        assertArrayEquals(original, encryption.decrypt(encrypted, "correct horse battery staple"))
        assertThrows(Exception::class.java) { encryption.decrypt(encrypted, "wrong password") }
    }

    @Test
    fun fileEncryptionStreamsAndRoundTrips() {
        val input = File.createTempFile("phonebackup-input", ".bin")
        val encrypted = File.createTempFile("phonebackup-encrypted", ".pbp")
        val output = File.createTempFile("phonebackup-output", ".bin")
        try {
            input.writeBytes(ByteArray(256 * 1024) { (it % 251).toByte() })
            encryption.encryptFile(input, encrypted, "password")
            encryption.decryptFile(encrypted, output, "password")
            assertArrayEquals(input.readBytes(), output.readBytes())
        } finally {
            input.delete(); encrypted.delete(); output.delete()
        }
    }

    @Test
    fun compressedDirectoryRoundTrips() {
        val root = createTempDir(prefix = "phonebackup-dir")
        try {
            File(root, "nested/data.txt").apply { parentFile.mkdirs(); writeText("verified") }
            val zip = compression.compressDirectory(root)
            val output = File(root.parentFile, "restored-${System.nanoTime()}")
            compression.decompressToDirectory(zip, output)
            assertEquals("verified", File(output, "nested/data.txt").readText())
            output.deleteRecursively(); zip.delete()
        } finally { root.deleteRecursively() }
    }

    @Test
    fun archiveTraversalIsRejected() {
        val zip = File.createTempFile("phonebackup-malicious", ".zip")
        val output = File.createTempFile("phonebackup-output", "").apply { delete() }
        try {
            java.util.zip.ZipOutputStream(zip.outputStream()).use { zos ->
                zos.putNextEntry(java.util.zip.ZipEntry("../../escape.txt")); zos.write("bad".toByteArray()); zos.closeEntry()
            }
            assertThrows(IllegalArgumentException::class.java) { compression.decompressToDirectory(zip, output) }
        } finally { zip.delete(); output.deleteRecursively() }
    }
}
