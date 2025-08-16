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

@Composable
fun DropItem(
    drop: Drop,
    isFromCurrentUser: Boolean,
    onMediaClick: (Drop) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 16.dp),
        contentAlignment = if (isFromCurrentUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        if (drop.isMedia) {
            DropMediaItem(
                drop = drop,
                onClick = { onMediaClick(drop) }
            )
        } else {
            // Text content (links, notes, etc.)
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

                    // Show text content
                    drop.text?.let { text ->
                        Text(
                            text = text,
                            color = if (isFromCurrentUser)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
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