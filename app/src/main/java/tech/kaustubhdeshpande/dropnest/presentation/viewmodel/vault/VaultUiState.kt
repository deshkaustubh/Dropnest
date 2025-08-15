package tech.kaustubhdeshpande.dropnest.presentation.viewmodel.vault

import tech.kaustubhdeshpande.dropnest.domain.model.Category
import tech.kaustubhdeshpande.dropnest.domain.model.Drop

data class VaultUiState(
    val isLoading: Boolean = true,
    val categories: List<Category> = emptyList(),
    val drops: List<Drop> = emptyList(),
    val selectedCategoryId: String? = null,
    val searchQuery: String = "",
    val error: String? = null,
    val isSearchActive: Boolean = false
) {
    val hasCategories: Boolean get() = categories.isNotEmpty()
    val hasDrops: Boolean get() = drops.isNotEmpty()

    val filteredDrops: List<Drop> get() {
        return when {
            isSearchActive && searchQuery.isNotEmpty() -> drops.filter {
                it.title?.contains(searchQuery, ignoreCase = true) == true ||
                        it.text?.contains(searchQuery, ignoreCase = true) == true
            }
            selectedCategoryId != null -> drops.filter { it.categoryId == selectedCategoryId }
            else -> drops
        }.sortedWith(compareByDescending<Drop> { it.isPinned }.thenByDescending { it.timestamp })
    }
}