package tech.kaustubhdeshpande.dropnest.presentation.event

sealed class VaultEvent {
    data object LoadCategories : VaultEvent()
    data object LoadAllDrops : VaultEvent()
    data class SearchDrops(val query: String) : VaultEvent()
    data class SelectCategory(val categoryId: String) : VaultEvent()
    data class DeleteDrop(val dropId: String) : VaultEvent()
    data class ToggleDropPin(val drop: tech.kaustubhdeshpande.dropnest.domain.model.Drop) : VaultEvent()
}