package com.phonebackup.core.compression

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.apache.commons.compress.archivers.zip.ZipFile
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CompressionService @Inject constructor() {
    
    companion object {
        private const val BUFFER_SIZE = 8192
    }
    
    /**
     * Compress directory to ZIP
     */
    fun compressDirectory(sourceDir: File): File {
        val zipFile = File(sourceDir.parent, "${sourceDir.name}.zip")
        
        ZipArchiveOutputStream(FileOutputStream(zipFile)).use { zos ->
            sourceDir.walkTopDown().forEach { file ->
                if (file.isFile) {
                    val entryName = file.relativeTo(sourceDir).path
                    val entry = ZipArchiveEntry(file, entryName)
                    zos.putArchiveEntry(entry)
                    
                    FileInputStream(file).use { input ->
                        input.copyTo(zos, BUFFER_SIZE)
                    }
                    
                    zos.closeArchiveEntry()
                }
            }
        }
        
        return zipFile
    }
    
    /**
     * Decompress ZIP to directory
     */
    fun decompressToDirectory(zipFile: File, outputDir: File = File(zipFile.parent, zipFile.nameWithoutExtension)) {
        outputDir.mkdirs()
        
        ZipFile(zipFile).use { zip ->
            val entries = zip.entries
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                val outputFile = File(outputDir, entry.name)
                
                if (entry.isDirectory) {
                    outputFile.mkdirs()
                } else {
                    outputFile.parentFile.mkdirs()
                    zip.getInputStream(entry).use { input ->
                        FileOutputStream(outputFile).use { output ->
                            input.copyTo(output, BUFFER_SIZE)
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Compress byte array
     */
    fun compress(data: ByteArray): ByteArray {
        val outputStream = java.io.ByteArrayOutputStream()
        java.util.zip.GZIPOutputStream(outputStream).use { gzip ->
            gzip.write(data)
        }
        return outputStream.toByteArray()
    }
    
    /**
     * Decompress byte array
     */
    fun decompress(data: ByteArray): ByteArray {
        val inputStream = java.util.zip.GZIPInputStream(java.io.ByteArrayInputStream(data))
        return inputStream.readBytes()
    }
}
