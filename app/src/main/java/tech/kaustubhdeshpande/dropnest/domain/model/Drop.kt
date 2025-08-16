package tech.kaustubhdeshpande.dropnest.domain.model

import java.util.UUID

data class Drop(
    val id: String = UUID.randomUUID().toString(),
    val type: DropType,
    val uri: String? = null,     // For media references
    val text: String? = null,    // For links/notes content
    val title: String? = null,   // Optional title
    val categoryId: String,      // Associated category
    val timestamp: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val tags: List<String> = emptyList(),
    val mimeType: String? = null // Added MIME type field for better file handling
) {
    val isMedia: Boolean
        get() = type == DropType.IMAGE || type == DropType.DOCUMENT ||
                type == DropType.VIDEO || type == DropType.AUDIO
}