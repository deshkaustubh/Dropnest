package tech.kaustubhdeshpande.dropnest.ui.screen.category

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tech.kaustubhdeshpande.dropnest.domain.model.Category
import tech.kaustubhdeshpande.dropnest.domain.repository.CategoryRepository
import tech.kaustubhdeshpande.dropnest.ui.component.availableCategoryIcons
import tech.kaustubhdeshpande.dropnest.ui.component.categoryColors
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val categoryId: String? = savedStateHandle["categoryId"]

    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    init {
        if (categoryId != null) {
            loadCategory(categoryId)
        } else {
            // Set default values for new category
            _uiState.update {
                it.copy(
                    selectedColor = categoryColors.first(),
                    selectedIcon = availableCategoryIcons.first().icon
                )
            }
        }
    }

    private fun loadCategory(id: String) {
        viewModelScope.launch {
            val category = categoryRepository.getCategoryById(id)
            category?.let {
                _uiState.update { state ->
                    state.copy(
                        name = it.name,
                        selectedColor = hexToColor(it.colorHex),
                        selectedIcon = findIconByName(it.emoji),
                        isLoading = false,
                        isEditing = true
                    )
                }
            }
        }
    }

    fun onNameChange(newName: String) {
        _uiState.update { it.copy(name = newName) }
    }

    fun onColorSelected(color: Color) {
        _uiState.update { it.copy(selectedColor = color) }
    }

    fun onIconSelected(icon: ImageVector) {
        _uiState.update { it.copy(selectedIcon = icon) }
    }

    fun saveCategory(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val currentState = _uiState.value

            val category = Category(
                id = categoryId ?: UUID.randomUUID().toString(),
                name = currentState.name,
                emoji = getIconName(currentState.selectedIcon),
                colorHex = colorToHex(currentState.selectedColor),
                isDefault = false, // Custom categories are never default
                timestamp = System.currentTimeMillis() // Always update timestamp!
            )

            if (currentState.isEditing) {
                categoryRepository.updateCategory(category)
            } else {
                categoryRepository.createCategory(category)
            }

            onSuccess()
        }
    }

    // Helper methods for color conversion
    private fun colorToHex(color: Color): String {
        return String.format("#%06X", 0xFFFFFF and color.toArgb())
    }

    private fun hexToColor(colorHex: String): Color {
        return try {
            Color(android.graphics.Color.parseColor(colorHex))
        } catch (e: Exception) {
            categoryColors.first() // Default color if parsing fails
        }
    }

    // Helper methods for icon conversion
    private fun getIconName(icon: ImageVector): String {
        return availableCategoryIcons.find { it.icon == icon }?.contentDescription ?: "Folder"
    }

    private fun findIconByName(name: String): ImageVector {
        return availableCategoryIcons.find { it.contentDescription == name }?.icon ?: Icons.Outlined.Folder
    }
}

data class CategoryUiState(
    val name: String = "",
    val selectedColor: Color = Color(0xFF4CAF50),
    val selectedIcon: ImageVector = Icons.Outlined.Folder,
    val isLoading: Boolean = true,
    val isEditing: Boolean = false
)