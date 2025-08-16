package tech.kaustubhdeshpande.dropnest.ui.screen.category.detail.components

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.InsertDriveFile
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.request.ImageRequest
import tech.kaustubhdeshpande.dropnest.domain.model.Drop
import tech.kaustubhdeshpande.dropnest.domain.model.DropType
import java.io.File
import java.text.DecimalFormat

@Composable
fun DropMediaItem(
    drop: Drop,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (drop.type) {
        DropType.IMAGE -> ImageDropItem(drop, onClick, modifier)
        DropType.DOCUMENT -> DocumentDropItem(drop, modifier)
        else -> {} // Handle other media types if needed
    }
}

@Composable
fun ImageDropItem(
    drop: Drop,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uri = drop.uri ?: return

    // Create a content URI using FileProvider
    val contentUri = remember(uri) {
        try {
            val file = File(uri)
            if (file.exists()) {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
            } else {
                // Fallback to direct parsing
                Uri.parse(uri)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Uri.parse(uri)
        }
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
            .fillMaxWidth(0.7f)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
    ) {
        Box {
            // Image with proper caching and error handling
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(contentUri)
                    .crossfade(true)
                    .error(android.R.drawable.ic_menu_gallery)
                    .build(),
                contentDescription = "Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.33f)
            )

            // Timestamp in bottom-right
            Text(
                text = formatTimestamp(drop.timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
            )
        }
    }
}

// Data class to store document type information
private data class DocumentInfo(
    val icon: ImageVector,
    val label: String,
    val color: Color,
    val mimeType: String
)

@Composable
fun DocumentDropItem(
    drop: Drop,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uri = drop.uri ?: return
    val file = File(uri)
    val fileSize = formatFileSize(file.length())
    val fileName = drop.title ?: file.name

    // Get file extension and determine document type
    val extension = uri.substringAfterLast('.', "").lowercase()

    // Get document info based on file type and MIME type
    val docInfo = detectDocumentType(extension, drop.mimeType)

    // Create a card-like document representation
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
            .fillMaxWidth(0.7f)
            .clip(RoundedCornerShape(8.dp))
            .clickable {
                // Open document with appropriate viewer
                try {
                    val contentUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        file
                    )

                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(contentUri, docInfo.mimeType)
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
                contentDescription = docInfo.label,
                tint = docInfo.color,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Document info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = fileName,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "$fileSize • ${docInfo.label}",
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

// Helper function to accurately detect document type
@Composable
private fun detectDocumentType(extension: String, mimeType: String?): DocumentInfo {
    val ext = extension.lowercase()

    // First check by extension which is most reliable
    when (ext) {
        "pdf" -> return DocumentInfo(
            icon = Icons.Default.PictureAsPdf,
            label = "PDF",
            color = Color(0xFFE57373), // Red
            mimeType = "application/pdf"
        )

        "doc", "docx", "docm", "dot", "dotx", "dotm" -> return DocumentInfo(
            icon = Icons.Default.Description,
            label = "WORD",
            color = Color(0xFF4FC3F7), // Blue
            mimeType = if (ext.contains("x"))
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            else
                "application/msword"
        )

        "xls", "xlsx", "xlsm", "xlt", "xltx", "xltm", "csv" -> return DocumentInfo(
            icon = Icons.Default.TableView,
            label = "EXCEL",
            color = Color(0xFF81C784), // Green
            mimeType = if (ext.contains("x"))
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            else
                "application/vnd.ms-excel"
        )

        "ppt", "pptx", "pptm", "pps", "ppsx", "ppsm" -> return DocumentInfo(
            icon = Icons.Default.Slideshow,
            label = "PPT",
            color = Color(0xFFFFB74D), // Orange
            mimeType = if (ext.contains("x"))
                "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            else
                "application/vnd.ms-powerpoint"
        )

        "txt", "text", "md", "markdown" -> return DocumentInfo(
            icon = Icons.Default.Article,
            label = "TXT",
            color = Color(0xFF9575CD), // Purple
            mimeType = "text/plain"
        )
    }

    // If extension check failed, try MIME type
    mimeType?.let {
        when {
            it.contains("pdf") -> return DocumentInfo(
                icon = Icons.Default.PictureAsPdf,
                label = "PDF",
                color = Color(0xFFE57373), // Red
                mimeType = "application/pdf"
            )

            it.contains("word") || it.contains("document") && !it.contains("sheet") && !it.contains("presentation") -> return DocumentInfo(
                icon = Icons.Default.Description,
                label = "WORD",
                color = Color(0xFF4FC3F7), // Blue
                mimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            )

            it.contains("excel") || it.contains("sheet") -> return DocumentInfo(
                icon = Icons.Default.TableView,
                label = "EXCEL",
                color = Color(0xFF81C784), // Green
                mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            )

            it.contains("powerpoint") || it.contains("presentation") -> return DocumentInfo(
                icon = Icons.Default.Slideshow,
                label = "PPT",
                color = Color(0xFFFFB74D), // Orange
                mimeType = "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            )

            it.contains("text/plain") -> return DocumentInfo(
                icon = Icons.Default.Article,
                label = "TXT",
                color = Color(0xFF9575CD), // Purple
                mimeType = "text/plain"
            )
        }
    }

    // Default case if we can't identify the document type
    return DocumentInfo(
        icon = Icons.Default.InsertDriveFile,
        label = "DOC",
        color = Color(0xFF78909C), // Blue gray
        mimeType = "application/octet-stream"
    )
}

// Helper function to format file size
private fun formatFileSize(size: Long): String {
    if (size <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
    return DecimalFormat("#,##0.#").format(size / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
}