package tech.kaustubhdeshpande.dropnest.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import tech.kaustubhdeshpande.dropnest.data.local.dao.CategoryDao
import tech.kaustubhdeshpande.dropnest.data.local.dao.DropDao
import tech.kaustubhdeshpande.dropnest.data.local.entity.CategoryEntity
import tech.kaustubhdeshpande.dropnest.domain.model.Category
import tech.kaustubhdeshpande.dropnest.domain.repository.CategoryRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao,
    private val dropDao: DropDao
) : CategoryRepository {

    override fun getAllCategories(): Flow<List<Category>> {
        return categoryDao.getAllCategories().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getCustomCategories(): Flow<List<Category>> {
        return categoryDao.getCustomCategories().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getDefaultCategories(): Flow<List<Category>> {
        return categoryDao.getDefaultCategories().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getCategoryById(categoryId: String): Category? {
        return categoryDao.getCategoryById(categoryId)?.toDomain()
    }

    override fun getCategoryWithDropCount(categoryId: String): Flow<Pair<Category, Int>?> {
        // Using flow builder to properly handle suspend function call
        val categoryFlow = flow {
            val category = categoryDao.getCategoryById(categoryId)?.toDomain()
            emit(category)
        }

        val countFlow = dropDao.getDropCountForCategory(categoryId)

        return combine(
            categoryFlow,
            countFlow
        ) { category, count ->
            category?.let { Pair(it, count) }
        }
    }

    override suspend fun createCategory(category: Category) {
        categoryDao.insertCategory(CategoryEntity.fromDomain(category))
    }

    override suspend fun updateCategory(category: Category) {
        categoryDao.updateCategory(CategoryEntity.fromDomain(category))
    }

    override suspend fun deleteCategory(categoryId: String) {
        categoryDao.deleteCategory(categoryId)
    }

    override fun getCustomCategoryCount(): Flow<Int> {
        return categoryDao.getCustomCategoryCount()
    }
}