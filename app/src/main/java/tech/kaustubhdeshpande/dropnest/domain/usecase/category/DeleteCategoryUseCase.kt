package tech.kaustubhdeshpande.dropnest.domain.usecase.category

import tech.kaustubhdeshpande.dropnest.domain.repository.CategoryRepository
import javax.inject.Inject

class DeleteCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(categoryId: String) {
        categoryRepository.deleteCategory(categoryId)
    }
}