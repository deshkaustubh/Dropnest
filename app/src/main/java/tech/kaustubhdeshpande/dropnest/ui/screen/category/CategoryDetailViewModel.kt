package tech.kaustubhdeshpande.dropnest.ui.screen.category.detail

import android.util.Log
import android.webkit.URLUtil
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tech.kaustubhdeshpande.dropnest.domain.model.Category
import tech.kaustubhdeshpande.dropnest.domain.model.Drop
import tech.kaustubhdeshpande.dropnest.domain.model.DropType
import tech.kaustubhdeshpande.dropnest.domain.usecase.category.GetCategoryByIdUseCase
import tech.kaustubhdeshpande.dropnest.domain.usecase.drop.CreateDropUseCase
import tech.kaustubhdeshpande.dropnest.domain.usecase.drop.GetDropsByCategoryUseCase
import java.util.UUID
import javax.inject.Inject

private const val TAG = "CategoryDetailViewModel"

@HiltViewModel
class CategoryDetailViewModel @Inject constructor(
    private val getCategoryByIdUseCase: GetCategoryByIdUseCase,
    private val getDropsByCategoryUseCase: GetDropsByCategoryUseCase,
    private val createDropUseCase: CreateDropUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryDetailState())
    val uiState: StateFlow<CategoryDetailState> = _uiState.asStateFlow()

    fun loadCategory(categoryId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val category = getCategoryByIdUseCase(categoryId)
                category?.let {
                    _uiState.update { state -> state.copy(category = it) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading category", e)
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun loadDrops(categoryId: String) {
        viewModelScope.launch {
            try {
                getDropsByCategoryUseCase(categoryId).collect { drops ->
                    _uiState.update { state -> state.copy(drops = drops) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading drops", e)
            }
        }
    }

    fun updateInputText(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun sendDrop() {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank() || _uiState.value.category == null) return

        val categoryId = _uiState.value.category?.id ?: return

        // Determine if the content is a URL
        val isUrl = URLUtil.isValidUrl(text) || text.startsWith("www.")
        val dropType = if (isUrl) DropType.LINK else DropType.NOTE

        val drop = Drop(
            id = UUID.randomUUID().toString(),
            type = dropType,
            text = text,
            title = if (isUrl) "Link" else null,
            uri = if (isUrl) text else null,
            categoryId = categoryId,
            timestamp = System.currentTimeMillis(),
            isPinned = false,
            tags = emptyList()
        )

        saveDrop(drop)

        // Clear input
        _uiState.update { it.copy(inputText = "") }
    }

    fun saveDrop(drop: Drop) {
        viewModelScope.launch {
            try {
                createDropUseCase(drop)
            } catch (e: Exception) {
                Log.e(TAG, "Error saving drop", e)
            }
        }
    }

    fun saveImageDrop(uri: String) {
        val categoryId = _uiState.value.category?.id ?: return

        val drop = Drop(
            id = UUID.randomUUID().toString(),
            type = DropType.IMAGE,
            text = null,
            title = "Image",
            uri = uri,
            categoryId = categoryId,
            timestamp = System.currentTimeMillis(),
            isPinned = false,
            tags = emptyList()
        )

        saveDrop(drop)
    }

    fun savePdfDrop(uri: String) {
        val categoryId = _uiState.value.category?.id ?: return

        val drop = Drop(
            id = UUID.randomUUID().toString(),
            type = DropType.PDF,
            text = null,
            title = "PDF Document",
            uri = uri,
            categoryId = categoryId,
            timestamp = System.currentTimeMillis(),
            isPinned = false,
            tags = emptyList()
        )

        saveDrop(drop)
    }
}

data class CategoryDetailState(
    val category: Category? = null,
    val drops: List<Drop> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false
)