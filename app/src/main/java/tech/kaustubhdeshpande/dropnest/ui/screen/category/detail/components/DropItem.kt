package tech.kaustubhdeshpande.dropnest.ui.screen.category.detail.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import tech.kaustubhdeshpande.dropnest.domain.model.Drop
import tech.kaustubhdeshpande.dropnest.domain.model.DropType
import tech.kaustubhdeshpande.dropnest.util.LinkifyText
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DropItem(
    drop: Drop,
    isFromCurrentUser: Boolean = true,
    onMediaClick: (Drop) -> Unit,
    modifier: Modifier = Modifier,
    highlightText: String? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 16.dp),
        contentAlignment = if (isFromCurrentUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        when {
            drop.type == DropType.DOCUMENT -> {
                // Highlight only if highlightText matches the file name or title
                val fileName = drop.title ?: drop.uri?.substringAfterLast('/') ?: ""
                val shouldHighlight =
                    !highlightText.isNullOrBlank() &&
                            fileName.contains(highlightText, ignoreCase = true)
                DocumentDropItem(
                    drop = drop,
                    highlightText = if (shouldHighlight) highlightText else null
                )
            }
            drop.type == DropType.IMAGE -> {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .padding(vertical = 2.dp)
                        .clickable { onMediaClick(drop) }
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(drop.uri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.33f)
                    )
                }
            }
            drop.type == DropType.NOTE && isLinkMessage(drop.text) -> {
                LinkPreview(drop = drop)
            }
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
                        drop.title?.let { title ->
                            HighlightableText(
                                text = title,
                                highlight = highlightText,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = if (isFromCurrentUser)
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        drop.text?.let { text ->
                            if (highlightText.isNullOrBlank()) {
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
                            } else {
                                HighlightableText(
                                    text = text,
                                    highlight = highlightText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isFromCurrentUser)
                                        MaterialTheme.colorScheme.onPrimary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        drop.uri?.let { uri ->
                            if (drop.type == DropType.LINK && !highlightText.isNullOrBlank()) {
                                HighlightableText(
                                    text = uri,
                                    highlight = highlightText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
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

@Composable
fun HighlightableText(
    text: String,
    highlight: String?,
    style: androidx.compose.ui.text.TextStyle,
    color: Color
) {
    if (highlight.isNullOrBlank()) {
        Text(text = text, style = style, color = color)
        return
    }
    val input = text
    val query = highlight.trim().lowercase()
    val builder = AnnotatedString.Builder()
    var idx = 0
    while (idx < input.length) {
        val matchIdx = input.lowercase().indexOf(query, idx)
        if (matchIdx < 0) {
            builder.append(input.substring(idx))
            break
        }
        if (matchIdx > idx) {
            builder.append(input.substring(idx, matchIdx))
        }
        builder.withStyle(
            SpanStyle(
                background = Color.Yellow.copy(alpha = 0.6f),
                color = color,
                fontWeight = FontWeight.Bold
            )
        ) {
            builder.append(input.substring(matchIdx, matchIdx + query.length))
        }
        idx = matchIdx + query.length
    }
    Text(text = builder.toAnnotatedString(), style = style, color = color)
}

fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}