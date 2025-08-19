package tech.kaustubhdeshpande.dropnest.domain.repository

import tech.kaustubhdeshpande.dropnest.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getAllCategories(): Flow<List<Category>>

    fun getCustomCategories(): Flow<List<Category>>

    fun getDefaultCategories(): Flow<List<Category>>

    suspend fun getCategoryById(categoryId: String): Category?

    fun getCategoryWithDropCount(categoryId: String): Flow<Pair<Category, Int>?>

    suspend fun createCategory(category: Category)

    suspend fun updateCategory(category: Category)

    /**
     * Update only the timestamp of a category.
     */
    suspend fun updateCategoryTimestamp(categoryId: String, timestamp: Long)

    suspend fun deleteCategory(categoryId: String)

    fun getCustomCategoryCount(): Flow<Int>
}