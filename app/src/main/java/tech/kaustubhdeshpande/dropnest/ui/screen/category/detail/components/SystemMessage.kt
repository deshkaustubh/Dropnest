package tech.kaustubhdeshpande.dropnest.ui.screen.category.detail.components

import androidx.compose.runtime.Composable

@Composable
fun DropNestMessage(
    text: String,
    timestamp: Long = System.currentTimeMillis() - 10000
) {
    DropItem(
        text = text,
        isSystem = true,
        senderName = "DropNest",
        timestamp = timestamp
    )
}