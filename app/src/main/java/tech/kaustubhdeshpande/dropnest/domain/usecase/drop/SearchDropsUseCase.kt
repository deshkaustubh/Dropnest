package tech.kaustubhdeshpande.dropnest.domain.usecase.drop

import tech.kaustubhdeshpande.dropnest.domain.model.Drop
import tech.kaustubhdeshpande.dropnest.domain.repository.DropRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchDropsUseCase @Inject constructor(
    private val dropRepository: DropRepository
) {
    operator fun invoke(query: String): Flow<List<Drop>> {
        return dropRepository.searchDrops(query)
    }
}