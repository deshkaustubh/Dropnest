package tech.kaustubhdeshpande.dropnest.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Make this an open class so PreviewHomeViewModel can extend it
abstract class HomeViewModel : ViewModel() {
    abstract val uiState: StateFlow<HomeScreenState>

    // Define common methods that both real and preview implementations will have
    open fun onCategoryClick(categoryId: String) {}
    open fun onDefaultCategoryClick(categoryType: DefaultCategoryType) {}
    open fun onAddCategoryClick() {}
    open fun onCreateCategoryClick() {}
}

// The real implementation used in the app
@HiltViewModel
class HomeViewModelImpl @Inject constructor(
    // Your dependencies here
) : HomeViewModel() {
    private val _uiState = MutableStateFlow(HomeScreenState())
    override val uiState: StateFlow<HomeScreenState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            // Simulate loading from repository
            _uiState.value = HomeScreenState(isLoading = false, customCategories = emptyList())
        }
    }

    override fun onCategoryClick(categoryId: String) {
        // Navigate to category detail or perform action
    }

    override fun onDefaultCategoryClick(categoryType: DefaultCategoryType) {
        // Handle default category click
        when (categoryType) {
            DefaultCategoryType.SAVED_LINKS -> { /* Navigate to saved links */ }
            DefaultCategoryType.NOTES -> { /* Navigate to notes */ }
            DefaultCategoryType.IMAGES -> { /* Navigate to images */ }
            DefaultCategoryType.PDFS -> { /* Navigate to PDFs */ }
        }
    }

    override fun onAddCategoryClick() {
        // Navigate to add category screen or show dialog
    }

    override fun onCreateCategoryClick() {
        // Navigate to create category screen
    }
}

data class HomeScreenState(
    val isLoading: Boolean = true,
    val customCategories: List<Category> = emptyList()
)

data class Category(
    val id: String,
    val name: String,
    val color: Long,
    val icon: String? = null
)

enum class DefaultCategoryType {
    SAVED_LINKS,
    NOTES,
    IMAGES,
    PDFS
}