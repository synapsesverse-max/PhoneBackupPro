package com.phonebackup.pro.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.phonebackup.core.data.local.model.BackupStatus

@Composable
fun StatusBadge(status: BackupStatus) {
    val (color, icon) = when (status) {
        BackupStatus.COMPLETED -> Pair(
            MaterialTheme.colorScheme.primary,
            Icons.Default.CheckCircle
        )
        BackupStatus.IN_PROGRESS -> Pair(
            MaterialTheme.colorScheme.tertiary,
            Icons.Default.HourglassTop
        )
        BackupStatus.FAILED -> Pair(
            MaterialTheme.colorScheme.error,
            Icons.Default.Error
        )
        BackupStatus.VERIFIED -> Pair(
            MaterialTheme.colorScheme.primary,
            Icons.Default.Verified
        )
        else -> Pair(
            MaterialTheme.colorScheme.outline,
            Icons.Default.Info
        )
    }
    
    AssistChip(
        onClick = {},
        label = { Text(status.name) },
        leadingIcon = {
            Icon(icon, contentDescription = null, tint = color)
        }
    )
}

@Composable
fun BackupSizeDisplay(sizeBytes: Long) {
    val formattedSize = when {
        sizeBytes < 1024 -> "$sizeBytes B"
        sizeBytes < 1024 * 1024 -> "${sizeBytes / 1024} KB"
        sizeBytes < 1024 * 1024 * 1024 -> "${sizeBytes / (1024 * 1024)} MB"
        else -> "%.2f GB".format(sizeBytes.toDouble() / (1024 * 1024 * 1024))
    }
    
    Text(
        text = formattedSize,
        style = MaterialTheme.typography.bodySmall
    )
}

@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun LoadingOverlay(isLoading: Boolean) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilePickerCard(
    title: String,
    selectedPath: String,
    onPickFile: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = selectedPath,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = onPickFile) {
                        Icon(Icons.Default.FolderOpen, "Browse")
                    }
                }
            )
        }
    }
}
