package tech.kaustubhdeshpande.dropnest.domain.usecase.drop

import tech.kaustubhdeshpande.dropnest.domain.model.Drop
import tech.kaustubhdeshpande.dropnest.domain.repository.DropRepository
import javax.inject.Inject

class CreateDropUseCase @Inject constructor(
    private val dropRepository: DropRepository
) {
    suspend operator fun invoke(drop: Drop) {
        dropRepository.createDrop(drop)
    }
}