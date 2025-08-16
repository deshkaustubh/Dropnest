package tech.kaustubhdeshpande.dropnest.util

import android.content.Intent
import android.net.Uri
import android.text.util.Linkify
import android.widget.TextView
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import java.util.regex.Pattern

/**
 * Utility class for handling links in text
 */
object LinkUtils {
    // URL regex pattern - matches most common URL formats
    private val URL_PATTERN = Pattern.compile(
        "((https?|ftp)://|(www\\.)|(mailto:))[a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)",
        Pattern.CASE_INSENSITIVE
    )

    /**
     * Creates an AnnotatedString with clickable links
     */
    fun createAnnotatedStringWithLinks(text: String): AnnotatedString {
        return buildAnnotatedString {
            append(text)

            // Find all URLs in the text
            val matcher = URL_PATTERN.matcher(text)

            // For each URL found, add a clickable span annotation
            while (matcher.find()) {
                val url = matcher.group()
                val startIndex = matcher.start()
                val endIndex = matcher.end()

                // Add URL styling (color and underline)
                addStyle(
                    style = SpanStyle(
                        color = Color(0xFF2196F3), // Material blue
                        textDecoration = TextDecoration.Underline
                    ),
                    start = startIndex,
                    end = endIndex
                )

                // Add clickable annotation with URL as tag
                addStringAnnotation(
                    tag = "URL",
                    annotation = url,
                    start = startIndex,
                    end = endIndex
                )
            }
        }
    }
}

/**
 * A composable that displays text with clickable links
 */
@Composable
fun LinkifyText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    linkColor: Color = MaterialTheme.colorScheme.primary
) {
    val context = LocalContext.current

    // Create an AnnotatedString with clickable links
    val annotatedText = remember(text) {
        LinkUtils.createAnnotatedStringWithLinks(text)
    }

    ClickableText(
        text = annotatedText,
        modifier = modifier,
        style = style,
        onClick = { offset ->
            // Find URL annotation at clicked position
            annotatedText.getStringAnnotations(
                tag = "URL",
                start = offset,
                end = offset
            ).firstOrNull()?.let { annotation ->
                // Get the URL
                val url = annotation.item

                // Format URL properly with http/https if needed
                val formattedUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    "https://$url"
                } else {
                    url
                }

                // Create and start intent to open URL
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(formattedUrl))
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Could show toast here for error handling
                }
            }
        }
    )
}