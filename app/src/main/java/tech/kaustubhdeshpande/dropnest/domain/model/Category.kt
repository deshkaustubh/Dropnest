package tech.kaustubhdeshpande.dropnest.domain.model

import java.util.UUID

data class Category(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val emoji: String,
    val colorHex: String,
    val isDefault: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)