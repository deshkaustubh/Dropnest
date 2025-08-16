package tech.kaustubhdeshpande.dropnest.ui.screen.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PreviewHomeViewModel : HomeViewModel() {
    private val _uiState = MutableStateFlow(HomeScreenState(isLoading = false, customCategories = emptyList()))
    override val uiState: StateFlow<HomeScreenState> = _uiState

    fun setCategories(categories: List<Category>) {
        _uiState.value = HomeScreenState(isLoading = false, customCategories = categories)
    }
}