package tech.kaustubhdeshpande.dropnest.domain.usecase.category

import tech.kaustubhdeshpande.dropnest.domain.model.Category
import tech.kaustubhdeshpande.dropnest.domain.repository.CategoryRepository
import javax.inject.Inject

class GetCategoryByIdUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(categoryId: String): Category? {
        return categoryRepository.getCategoryById(categoryId)
    }
}