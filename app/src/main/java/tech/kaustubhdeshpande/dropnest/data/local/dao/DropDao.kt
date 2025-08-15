package tech.kaustubhdeshpande.dropnest.data.local.dao

import androidx.room.*
import tech.kaustubhdeshpande.dropnest.data.local.entity.DropEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DropDao {
    @Query("SELECT * FROM drops ORDER BY timestamp DESC")
    fun getAllDrops(): Flow<List<DropEntity>>

    @Query("SELECT * FROM drops WHERE categoryId = :categoryId ORDER BY isPinned DESC, timestamp DESC")
    fun getDropsByCategory(categoryId: String): Flow<List<DropEntity>>

    @Query("SELECT * FROM drops WHERE type = :type ORDER BY timestamp DESC")
    fun getDropsByType(type: String): Flow<List<DropEntity>>

    @Query("SELECT * FROM drops WHERE id = :dropId")
    suspend fun getDropById(dropId: String): DropEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrop(drop: DropEntity)

    @Update
    suspend fun updateDrop(drop: DropEntity)

    @Query("DELETE FROM drops WHERE id = :dropId")
    suspend fun deleteDrop(dropId: String)

    @Query("DELETE FROM drops WHERE categoryId = :categoryId")
    suspend fun deleteDropsByCategoryId(categoryId: String)

    @Query("SELECT COUNT(*) FROM drops WHERE categoryId = :categoryId")
    fun getDropCountForCategory(categoryId: String): Flow<Int>

    @Query("SELECT * FROM drops WHERE text LIKE :searchQuery OR title LIKE :searchQuery ORDER BY timestamp DESC")
    fun searchDrops(searchQuery: String): Flow<List<DropEntity>>

    @Query("SELECT * FROM drops ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestDrop(): DropEntity?
}