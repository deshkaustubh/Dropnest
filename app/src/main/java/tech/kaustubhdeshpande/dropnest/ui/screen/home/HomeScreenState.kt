package tech.kaustubhdeshpande.dropnest.ui.screen.home

import tech.kaustubhdeshpande.dropnest.domain.model.Category

data class HomeScreenState(
    val isLoading: Boolean = true,
    val customCategories: List<Category> = emptyList()
)

enum class DefaultCategoryType {
    SAVED_LINKS,
    NOTES,
    IMAGES,
    PDFS
}