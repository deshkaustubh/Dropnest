package tech.kaustubhdeshpande.dropnest.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class CategoryWithDrops(
    @Embedded val category: CategoryEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "categoryId"
    )
    val drops: List<DropEntity>
)