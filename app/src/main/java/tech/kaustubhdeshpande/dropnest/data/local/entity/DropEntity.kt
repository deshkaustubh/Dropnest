package tech.kaustubhdeshpande.dropnest.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import tech.kaustubhdeshpande.dropnest.domain.model.Drop
import tech.kaustubhdeshpande.dropnest.domain.model.DropType

@Entity(
    tableName = "drops",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("categoryId")]
)
data class DropEntity(
    @PrimaryKey val id: String,
    val type: String,
    val uri: String?,
    val text: String?,
    val title: String?,
    val categoryId: String,
    val timestamp: Long,
    val isPinned: Boolean,
    val tags: String // Stored as comma-separated values
) {
    companion object {
        fun fromDomain(drop: Drop): DropEntity {
            return DropEntity(
                id = drop.id,
                type = drop.type.name,
                uri = drop.uri,
                text = drop.text,
                title = drop.title,
                categoryId = drop.categoryId,
                timestamp = drop.timestamp,
                isPinned = drop.isPinned,
                tags = drop.tags.joinToString(",")
            )
        }
    }

    fun toDomain(): Drop {
        return Drop(
            id = id,
            type = DropType.valueOf(type),
            uri = uri,
            text = text,
            title = title,
            categoryId = categoryId,
            timestamp = timestamp,
            isPinned = isPinned,
            tags = if (tags.isEmpty()) emptyList() else tags.split(",")
        )
    }
}