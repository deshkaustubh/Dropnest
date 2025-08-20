package tech.kaustubhdeshpande.dropnest.ui.screen.categorylist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import tech.kaustubhdeshpande.dropnest.domain.model.Category
import tech.kaustubhdeshpande.dropnest.domain.usecase.category.GetCategoriesUseCase
import tech.kaustubhdeshpande.dropnest.domain.usecase.category.DeleteCategoryUseCase
import javax.inject.Inject
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.ui.graphics.vector.ImageVector
import tech.kaustubhdeshpande.dropnest.ui.component.availableCategoryIcons

@HiltViewModel
class CategoryListViewModel @Inject constructor(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase
) : ViewModel() {

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    init {
        viewModelScope.launch {
            getCategoriesUseCase().collectLatest { cats ->
                _categories.value = cats
            }
        }
    }

    fun getCategoryIconByName(iconName: String): ImageVector {
        return availableCategoryIcons.find { it.contentDescription == iconName }?.icon
            ?: Icons.Outlined.Folder
    }

    /**
     * Delete a category and its drops (cascading in DB).
     */
    fun deleteCategoryAndDrops(categoryId: String) {
        viewModelScope.launch {
            deleteCategoryUseCase(categoryId)
        }
    }
}