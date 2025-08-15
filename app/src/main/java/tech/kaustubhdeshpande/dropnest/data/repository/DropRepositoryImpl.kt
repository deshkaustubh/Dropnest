package tech.kaustubhdeshpande.dropnest.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tech.kaustubhdeshpande.dropnest.data.local.dao.DropDao
import tech.kaustubhdeshpande.dropnest.data.local.entity.DropEntity
import tech.kaustubhdeshpande.dropnest.domain.model.Drop
import tech.kaustubhdeshpande.dropnest.domain.model.DropType
import tech.kaustubhdeshpande.dropnest.domain.repository.DropRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DropRepositoryImpl @Inject constructor(
    private val dropDao: DropDao
) : DropRepository {

    override fun getAllDrops(): Flow<List<Drop>> {
        return dropDao.getAllDrops().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getDropsByCategory(categoryId: String): Flow<List<Drop>> {
        return dropDao.getDropsByCategory(categoryId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getDropsByType(type: DropType): Flow<List<Drop>> {
        return dropDao.getDropsByType(type.name).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getDropById(dropId: String): Drop? {
        return dropDao.getDropById(dropId)?.toDomain()
    }

    override suspend fun createDrop(drop: Drop) {
        dropDao.insertDrop(DropEntity.fromDomain(drop))
    }

    override suspend fun updateDrop(drop: Drop) {
        dropDao.updateDrop(DropEntity.fromDomain(drop))
    }

    override suspend fun deleteDrop(dropId: String) {
        dropDao.deleteDrop(dropId)
    }

    override fun searchDrops(query: String): Flow<List<Drop>> {
        return dropDao.searchDrops("%$query%").map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getLatestDrop(): Drop? {
        return dropDao.getLatestDrop()?.toDomain()
    }
}