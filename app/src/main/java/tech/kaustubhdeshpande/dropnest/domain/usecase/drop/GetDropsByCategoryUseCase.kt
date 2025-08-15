package tech.kaustubhdeshpande.dropnest.domain.usecase.drop

import tech.kaustubhdeshpande.dropnest.domain.model.Drop
import tech.kaustubhdeshpande.dropnest.domain.repository.DropRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDropsByCategoryUseCase @Inject constructor(
    private val dropRepository: DropRepository
) {
    operator fun invoke(categoryId: String): Flow<List<Drop>> {
        return dropRepository.getDropsByCategory(categoryId)
    }
}