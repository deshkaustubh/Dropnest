package tech.kaustubhdeshpande.dropnest.domain.usecase.drop

import tech.kaustubhdeshpande.dropnest.domain.repository.DropRepository
import javax.inject.Inject

class DeleteDropUseCase @Inject constructor(
    private val dropRepository: DropRepository
) {
    suspend operator fun invoke(dropId: String) {
        dropRepository.deleteDrop(dropId)
    }
}