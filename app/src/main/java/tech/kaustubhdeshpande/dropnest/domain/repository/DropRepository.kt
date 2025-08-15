package tech.kaustubhdeshpande.dropnest.domain.repository

import tech.kaustubhdeshpande.dropnest.domain.model.Drop
import tech.kaustubhdeshpande.dropnest.domain.model.DropType
import kotlinx.coroutines.flow.Flow

interface DropRepository {
    fun getAllDrops(): Flow<List<Drop>>

    fun getDropsByCategory(categoryId: String): Flow<List<Drop>>

    fun getDropsByType(type: DropType): Flow<List<Drop>>

    suspend fun getDropById(dropId: String): Drop?

    suspend fun createDrop(drop: Drop)

    suspend fun updateDrop(drop: Drop)

    suspend fun deleteDrop(dropId: String)

    fun searchDrops(query: String): Flow<List<Drop>>

    suspend fun getLatestDrop(): Drop?
}