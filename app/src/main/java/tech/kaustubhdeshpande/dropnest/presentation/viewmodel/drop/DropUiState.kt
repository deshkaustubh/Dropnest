package tech.kaustubhdeshpande.dropnest.presentation.viewmodel.drop

import android.net.Uri
import tech.kaustubhdeshpande.dropnest.domain.model.Category
import tech.kaustubhdeshpande.dropnest.domain.model.DropType

data class DropUiState(
    val id: String? = null,
    val title: String = "",
    val text: String = "",
    val mediaUri: Uri? = null,
    val categoryId: String = "",
    val dropType: DropType = DropType.NOTE,
    val isPinned: Boolean = false,
    val tags: List<String> = emptyList(),
    val availableCategories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    val isEditing: Boolean = false
)