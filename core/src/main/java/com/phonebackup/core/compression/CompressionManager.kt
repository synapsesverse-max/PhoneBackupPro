package com.phonebackup.core.compression

import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CompressionManager @Inject constructor(
    private val compressionService: CompressionService
) {
    
    /**
     * Create compressed backup
     */
    fun createBackup(sourceDir: File): File {
        return compressionService.compressDirectory(sourceDir)
    }
    
    /**
     * Extract backup
     */
    fun extractBackup(zipFile: File, outputDir: File) {
        compressionService.decompressToDirectory(zipFile, outputDir)
    }
    
    /**
     * Get compressed size estimation
     */
    fun estimateCompressedSize(sourceDir: File): Long {
        val compressed = compressTest(sourceDir)
        return compressed.length()
    }
    
    private fun compressTest(sourceDir: File): File {
        // Create a test compression to estimate size
        return compressionService.compressDirectory(sourceDir)
    }
}
