package tech.kaustubhdeshpande.dropnest.presentation.viewmodel.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import tech.kaustubhdeshpande.dropnest.domain.model.Drop
import tech.kaustubhdeshpande.dropnest.domain.usecase.category.GetCategoriesUseCase
import tech.kaustubhdeshpande.dropnest.domain.usecase.drop.DeleteDropUseCase
import tech.kaustubhdeshpande.dropnest.domain.usecase.drop.GetDropsUseCase
import tech.kaustubhdeshpande.dropnest.domain.usecase.drop.GetDropsByCategoryUseCase
import tech.kaustubhdeshpande.dropnest.domain.usecase.drop.SearchDropsUseCase
import tech.kaustubhdeshpande.dropnest.presentation.event.VaultEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VaultViewModel @Inject constructor(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getDropsUseCase: GetDropsUseCase,
    private val getDropsByCategoryUseCase: GetDropsByCategoryUseCase,
    private val searchDropsUseCase: SearchDropsUseCase,
    private val deleteDropUseCase: DeleteDropUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(VaultUiState())
    val uiState: StateFlow<VaultUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
        loadAllDrops()
    }

    fun onEvent(event: VaultEvent) {
        when (event) {
            is VaultEvent.LoadCategories -> loadCategories()
            is VaultEvent.LoadAllDrops -> loadAllDrops()
            is VaultEvent.SearchDrops -> searchDrops(event.query)
            is VaultEvent.SelectCategory -> selectCategory(event.categoryId)
            is VaultEvent.DeleteDrop -> deleteDrop(event.dropId)
            is VaultEvent.ToggleDropPin -> toggleDropPin(event.drop)
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                getCategoriesUseCase().collect { categories ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            categories = categories,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        error = "Failed to load categories: ${e.localizedMessage}",
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun loadAllDrops() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, selectedCategoryId = null) }
                getDropsUseCase().collect { drops ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            drops = drops,
                            isLoading = false,
                            isSearchActive = false,
                            searchQuery = ""
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        error = "Failed to load drops: ${e.localizedMessage}",
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun selectCategory(categoryId: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, selectedCategoryId = categoryId) }
                getDropsByCategoryUseCase(categoryId).collect { drops ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            drops = drops,
                            isLoading = false,
                            isSearchActive = false,
                            searchQuery = ""
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        error = "Failed to load category drops: ${e.localizedMessage}",
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun searchDrops(query: String) {
        viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(
                        searchQuery = query,
                        isSearchActive = query.isNotEmpty()
                    )
                }

                if (query.isNotEmpty()) {
                    searchDropsUseCase(query).collect { drops ->
                        _uiState.update { currentState ->
                            currentState.copy(
                                drops = drops,
                                isLoading = false
                            )
                        }
                    }
                } else {
                    loadAllDrops()
                }
            } catch (e: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        error = "Search failed: ${e.localizedMessage}",
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun deleteDrop(dropId: String) {
        viewModelScope.launch {
            try {
                deleteDropUseCase(dropId)
                // Refresh drops after deletion
                if (_uiState.value.selectedCategoryId != null) {
                    selectCategory(_uiState.value.selectedCategoryId!!)
                } else {
                    loadAllDrops()
                }
            } catch (e: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        error = "Failed to delete drop: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    private fun toggleDropPin(drop: Drop) {
        // This would typically call an update drop use case
        // For now we'll implement a stub that would be completed with the actual use case
        viewModelScope.launch {
            // In a real implementation, we'd call updateDropUseCase with the toggled drop
            // For now we'll just refresh the view to reflect current database state
            if (_uiState.value.selectedCategoryId != null) {
                selectCategory(_uiState.value.selectedCategoryId!!)
            } else {
                loadAllDrops()
            }
        }
    }
}