package tech.kaustubhdeshpande.dropnest.ui.screen.category.detail.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import tech.kaustubhdeshpande.dropnest.domain.model.Drop
import java.net.URL

/**
 * A composable that displays a link preview card
 * This can be enhanced later to fetch metadata from links
 */
@Composable
fun LinkPreview(
    drop: Drop,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val url = drop.text ?: return

    // Normalize URL to ensure it has proper scheme
    val normalizedUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
        "https://$url"
    } else {
        url
    }

    // Extract domain name for display
    val domain = try {
        URL(normalizedUrl).host
    } catch (e: Exception) {
        url // Fallback to showing the full URL
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
            .fillMaxWidth(0.85f)
            .clip(RoundedCornerShape(12.dp))
            .clickable {
                // Open URL in browser when clicked
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(normalizedUrl))
                    context.startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Could show a toast here
                }
            }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Link icon and domain
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Link icon
                Icon(
                    imageVector = Icons.Default.Link,
                    contentDescription = "Link",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.CenterStart)
                )

                // Domain name
                Text(
                    text = domain,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 36.dp)
                )

                // Open in browser icon
                Icon(
                    imageVector = Icons.Default.OpenInBrowser,
                    contentDescription = "Open in browser",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // URL display
            Text(
                text = url,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Timestamp
            Text(
                text = formatTimestamp(drop.timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

/**
 * Function to determine if a message is primarily a URL
 */
fun isLinkMessage(text: String?): Boolean {
    if (text == null) return false

    val trimmedText = text.trim()

    // Check if the entire text is likely a URL
    val urlRegex = Regex("^(https?://|www\\.)[a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)$")
    return urlRegex.matches(trimmedText)
}