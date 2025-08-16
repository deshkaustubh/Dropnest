package tech.kaustubhdeshpande.dropnest.ui.screen.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tech.kaustubhdeshpande.dropnest.domain.usecase.category.GetCustomCategoriesUseCase
import javax.inject.Inject

private const val TAG = "HomeViewModel"

// Base abstract class for HomeViewModel
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
    private val getCustomCategoriesUseCase: GetCustomCategoriesUseCase  // 🔄 CHANGED: Use case instead of repository
) : HomeViewModel() {
    private val _uiState = MutableStateFlow(HomeScreenState())
    override val uiState: StateFlow<HomeScreenState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading categories...")
                _uiState.update { it.copy(isLoading = true) }

                // 🔄 CHANGED: Use the use case instead of direct repository access
                getCustomCategoriesUseCase().collectLatest { categories ->
                    Log.d(TAG, "Received ${categories.size} categories")
                    _uiState.update { state ->
                        state.copy(
                            customCategories = categories,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading categories", e)
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    // Add refresh function
    fun refreshCategories() {
        loadCategories()
    }

    override fun onCategoryClick(categoryId: String) {
        // This will be handled by the navigation in the composable
        Log.d(TAG, "Category clicked: $categoryId")
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
        // This will be handled in the HomeScreen
        Log.d(TAG, "Add category button clicked")
    }

    override fun onCreateCategoryClick() {
        // Navigation will be handled in the composable
        Log.d(TAG, "Create category clicked")
    }
}