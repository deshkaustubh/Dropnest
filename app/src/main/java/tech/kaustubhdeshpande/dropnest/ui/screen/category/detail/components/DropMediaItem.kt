package tech.kaustubhdeshpande.dropnest.ui.screen.category.detail.components

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.TableView
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import tech.kaustubhdeshpande.dropnest.domain.model.Drop
import tech.kaustubhdeshpande.dropnest.util.FileManager
import java.io.File
import java.text.DecimalFormat

@Composable
fun DocumentDropItem(
    drop: Drop,
    highlightText: String? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val fileManager = remember { FileManager(context) }
    val uri = drop.uri ?: return
    val file = File(uri)
    val fileSize = formatFileSize(file.length())
    val fileName = drop.title ?: file.name

    val extension = fileManager.getFileExtension(Uri.parse(uri))
    val mimeType = fileManager.getMimeType(Uri.parse(uri))
    val docLabel = fileManager.getDocumentTypeName(Uri.parse(uri), mimeType)

    val docInfo = detectDocumentTypeForIconAndColor(extension, mimeType)

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
            .fillMaxWidth(0.7f)
            .clip(RoundedCornerShape(8.dp))
            .clickable {
                try {
                    val contentUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        file
                    )

                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(contentUri, mimeType ?: "application/octet-stream")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }

                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Log.e("DocumentDropItem", "No app to open this document type: ${e.message}")
                        Toast.makeText(
                            context,
                            "No app found to open this type of document",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    Log.e("DocumentDropItem", "Error opening document: ${e.message}")
                    e.printStackTrace()
                }
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Document icon
            Icon(
                imageVector = docInfo.icon,
                contentDescription = docLabel,
                tint = docInfo.color,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Document info
            Column(modifier = Modifier.weight(1f)) {
                HighlightableText(
                    text = fileName,
                    highlight = highlightText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "$fileSize • $docLabel",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            // Timestamp
            Text(
                text = formatTimestamp(drop.timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

// Data class to store document type information
private data class DocumentInfo(
    val icon: ImageVector,
    val label: String,
    val color: Color
)

// Helper to map extension/mime to icon/label/color
private fun detectDocumentTypeForIconAndColor(extension: String, mimeType: String?): DocumentInfo {
    val ext = extension.lowercase()
    return when {
        ext == "pdf" -> DocumentInfo(
            icon = Icons.Filled.PictureAsPdf,
            label = "PDF",
            color = Color(0xFFE57373)
        )
        ext == "doc" || ext == "docx" -> DocumentInfo(
            icon = Icons.Filled.Description,
            label = "WORD",
            color = Color(0xFF4FC3F7)
        )
        ext == "xls" || ext == "xlsx" || ext == "csv" -> DocumentInfo(
            icon = Icons.Filled.TableView,
            label = "EXCEL",
            color = Color(0xFF81C784)
        )
        ext == "ppt" || ext == "pptx" -> DocumentInfo(
            icon = Icons.Filled.Slideshow,
            label = "PPT",
            color = Color(0xFFFFB74D)
        )
        ext == "txt" || ext == "text" || ext == "md" || ext == "rtf" -> DocumentInfo(
            icon = Icons.AutoMirrored.Filled.Article,
            label = "TXT",
            color = Color(0xFF9575CD)
        )
        ext == "zip" || ext == "rar" || ext == "7z" || ext == "tar" || ext == "gz" -> DocumentInfo(
            icon = Icons.AutoMirrored.Filled.InsertDriveFile,
            label = "ARCHIVE",
            color = Color(0xFF90A4AE)
        )
        else -> DocumentInfo(
            icon = Icons.AutoMirrored.Filled.InsertDriveFile,
            label = ext.uppercase().take(6).ifBlank { "DOC" },
            color = Color(0xFF78909C)
        )
    }
}

// Helper to format file size
private fun formatFileSize(size: Long): String {
    if (size <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
    return String.format("%.1f %s", size / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}