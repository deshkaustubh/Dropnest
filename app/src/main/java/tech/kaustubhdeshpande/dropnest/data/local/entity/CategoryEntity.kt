package tech.kaustubhdeshpande.dropnest.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import tech.kaustubhdeshpande.dropnest.domain.model.Category

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val emoji: String,
    val colorHex: String,
    val isDefault: Boolean,
    val timestamp: Long
) {
    companion object {
        fun fromDomain(category: Category): CategoryEntity {
            return CategoryEntity(
                id = category.id,
                name = category.name,
                emoji = category.emoji,
                colorHex = category.colorHex,
                isDefault = category.isDefault,
                timestamp = category.timestamp
            )
        }
    }

    fun toDomain(): Category {
        return Category(
            id = id,
            name = name,
            emoji = emoji,
            colorHex = colorHex,
            isDefault = isDefault,
            timestamp = timestamp
        )
    }
}