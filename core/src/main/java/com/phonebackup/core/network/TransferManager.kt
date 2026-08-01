package com.phonebackup.core.network

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.ServerSocket
import java.net.Socket
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransferManager @Inject constructor(
    private val networkService: NetworkService
) {
    
    companion object {
        private const val DEFAULT_PORT = 9876
        private const val BUFFER_SIZE = 65536
    }
    
    /**
     * Start file server for receiving files
     */
    fun startFileServer(outputDir: File, port: Int = DEFAULT_PORT): ServerSocket {
        val serverSocket = ServerSocket(port)
        
        Thread {
            try {
                while (true) {
                    val clientSocket = serverSocket.accept()
                    handleClient(clientSocket, outputDir)
                }
            } catch (e: Exception) {
                // Server stopped
            }
        }.start()
        
        return serverSocket
    }
    
    /**
     * Send file to server
     */
    fun sendFile(host: String, file: File, port: Int = DEFAULT_PORT): Boolean {
        return try {
            val socket = Socket(host, port)
            FileInputStream(file).use { input ->
                socket.getOutputStream().use { output ->
                    input.copyTo(output, BUFFER_SIZE)
                }
            }
            socket.close()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    private fun handleClient(clientSocket: Socket, outputDir: File) {
        try {
            val receivedFile = File(outputDir, "received_backup_${System.currentTimeMillis()}.zip")
            clientSocket.getInputStream().use { input ->
                FileOutputStream(receivedFile).use { output ->
                    input.copyTo(output, BUFFER_SIZE)
                }
            }
            clientSocket.close()
        } catch (e: Exception) {
            // Handle error
        }
    }
    
    /**
     * Get available port
     */
    fun getAvailablePort(): Int {
        return try {
            val socket = ServerSocket(0)
            val port = socket.localPort
            socket.close()
            port
        } catch (e: Exception) {
            DEFAULT_PORT
        }
    }
}
