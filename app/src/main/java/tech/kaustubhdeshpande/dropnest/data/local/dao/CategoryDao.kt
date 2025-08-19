package tech.kaustubhdeshpande.dropnest.data.local.dao

import androidx.room.*
import tech.kaustubhdeshpande.dropnest.data.local.entity.CategoryEntity
import tech.kaustubhdeshpande.dropnest.data.local.entity.CategoryWithDrops
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY timestamp DESC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE isDefault = 0 ORDER BY timestamp DESC")
    fun getCustomCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE isDefault = 1 ORDER BY name ASC")
    fun getDefaultCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :categoryId")
    suspend fun getCategoryById(categoryId: String): CategoryEntity?

    @Transaction
    @Query("SELECT * FROM categories WHERE id = :categoryId")
    fun getCategoryWithDrops(categoryId: String): Flow<CategoryWithDrops?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :categoryId")
    suspend fun deleteCategory(categoryId: String)

    @Query("SELECT COUNT(*) FROM categories WHERE isDefault = 0")
    fun getCustomCategoryCount(): Flow<Int>

    @Query("UPDATE categories SET timestamp = :timestamp WHERE id = :categoryId")
    suspend fun updateCategoryTimestamp(categoryId: String, timestamp: Long)
}