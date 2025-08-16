package tech.kaustubhdeshpande.dropnest.ui.screen.category.detail.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tech.kaustubhdeshpande.dropnest.domain.model.Drop
import tech.kaustubhdeshpande.dropnest.domain.model.DropType
import tech.kaustubhdeshpande.dropnest.util.LinkifyText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DropItem(
    drop: Drop,
    isFromCurrentUser: Boolean = true, // Default to true since most drops will be from the user
    onMediaClick: (Drop) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 16.dp),
        contentAlignment = if (isFromCurrentUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        when {
            drop.isMedia -> {
                DropMediaItem(
                    drop = drop,
                    onClick = { onMediaClick(drop) }
                )
            }

            // Special case for link-only messages - display as link cards
            drop.type == DropType.NOTE && isLinkMessage(drop.text) -> {
                LinkPreview(drop = drop)
            }

            // Regular text messages (may contain embedded links)
            else -> {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isFromCurrentUser)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth(0.75f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // If title exists, show it
                        drop.title?.let { title ->
                            Text(
                                text = title,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                ),
                                color = if (isFromCurrentUser)
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Show text content with clickable links
                        drop.text?.let { text ->
                            LinkifyText(
                                text = text,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = if (isFromCurrentUser)
                                        MaterialTheme.colorScheme.onPrimary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                linkColor = if (isFromCurrentUser)
                                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                                else
                                    MaterialTheme.colorScheme.primary
                            )
                        }

                        // Timestamp
                        Text(
                            text = formatTimestamp(drop.timestamp),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isFromCurrentUser)
                                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }
        }
    }
}

// Helper function to format timestamp
fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}