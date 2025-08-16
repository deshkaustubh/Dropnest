package tech.kaustubhdeshpande.dropnest.ui.screen.category.detail.components

import android.net.Uri
import android.text.format.DateUtils
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import tech.kaustubhdeshpande.dropnest.R
import tech.kaustubhdeshpande.dropnest.domain.model.Drop
import tech.kaustubhdeshpande.dropnest.domain.model.DropType

@Composable
fun DropItem(
    drop: Drop,
    modifier: Modifier = Modifier
) {
    DropItem(
        text = drop.text ?: "",
        uri = drop.uri,
        title = drop.title,
        type = drop.type,
        timestamp = drop.timestamp,
        isSystem = false,
        modifier = modifier
    )
}

@Composable
fun DropItem(
    text: String,
    uri: String? = null,
    title: String? = null,
    type: DropType = DropType.NOTE,
    timestamp: Long = System.currentTimeMillis(),
    isSystem: Boolean = false,
    senderName: String = "You",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val timeFormatted = DateUtils.getRelativeTimeSpanString(
        timestamp,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS
    ).toString()

    if (isSystem) {
        // System/DropNest message - left aligned with logo
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp)
        ) {
            // Sender label with DropNest logo
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // DropNest logo avatar
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF78C29A)) // Light green for DropNest
                ) {
                    // Using the actual logo from drawable resources
                    Image(
                        painter = painterResource(id = R.drawable.logo_dropnest),
                        contentDescription = "DropNest",
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = senderName,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Message bubble - left aligned with rounded corners
            Card(
                shape = RoundedCornerShape(
                    topStart = 4.dp,
                    topEnd = 16.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 16.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2A3731) // Dark green for system messages
                ),
                modifier = Modifier
                    .padding(end = 64.dp, start = 48.dp)
                    .align(Alignment.Start)
            ) {
                MessageContent(
                    text = text,
                    uri = uri,
                    title = title,
                    type = type,
                    timeFormatted = timeFormatted,
                    isUser = false
                )
            }
        }
    } else {
        // User message - right aligned WITHOUT avatar (as requested)
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp)
        ) {
            // Message bubble - aligned to the right
            Card(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 4.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 16.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF005C4B) // WhatsApp-like green
                ),
                modifier = Modifier
                    .padding(start = 64.dp)
                    .align(Alignment.End)
            ) {
                MessageContent(
                    text = text,
                    uri = uri,
                    title = title,
                    type = type,
                    timeFormatted = timeFormatted,
                    isUser = true
                )
            }
        }
    }
}

@Composable
private fun MessageContent(
    text: String,
    uri: String?,
    title: String?,
    type: DropType,
    timeFormatted: String,
    isUser: Boolean = false
) {
    val context = LocalContext.current
    val textColor = Color.White
    val linkColor = if (isUser) Color(0xFF9EE6FC) else Color(0xFFAED8FC)
    val timeColor = Color.White.copy(alpha = 0.7f)

    Column(modifier = Modifier.padding(12.dp)) {
        // Content based on drop type
        when (type) {
            DropType.NOTE -> {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor
                )
            }
            DropType.LINK -> {
                Text(
                    text = uri ?: text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = linkColor,
                    modifier = Modifier.clickable { /* Open link */ }
                )
            }
            DropType.IMAGE -> {
                uri?.let { imageUri ->
                    Image(
                        painter = rememberAsyncImagePainter(
                            ImageRequest.Builder(context)
                                .data(Uri.parse(imageUri))
                                .build()
                        ),
                        contentDescription = title ?: "Image",
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    title?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor
                        )
                    }
                }
            }
            DropType.PDF -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(
                            color = if (isUser) Color(0xFF004A3D) else Color(0xFF1F2C25),
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "PDF Document",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = title ?: uri?.substringAfterLast('/') ?: "PDF Document",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            else -> {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor
                )
            }
        }

        // Timestamp
        Text(
            text = timeFormatted,
            style = MaterialTheme.typography.labelSmall,
            color = timeColor,
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 4.dp)
        )
    }
}