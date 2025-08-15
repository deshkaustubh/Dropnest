package tech.kaustubhdeshpande.dropnest.presentation.viewmodel.drop

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tech.kaustubhdeshpande.dropnest.domain.model.Drop
import tech.kaustubhdeshpande.dropnest.domain.model.DropType
import tech.kaustubhdeshpande.dropnest.domain.usecase.category.GetCategoriesUseCase
import tech.kaustubhdeshpande.dropnest.domain.usecase.drop.CreateDropUseCase
import tech.kaustubhdeshpande.dropnest.domain.usecase.drop.DeleteDropUseCase
import tech.kaustubhdeshpande.dropnest.presentation.event.DropEvent
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class DropViewModel @Inject constructor(
    private val createDropUseCase: CreateDropUseCase,
    private val deleteDropUseCase: DeleteDropUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(DropUiState())
    val uiState: StateFlow<DropUiState> = _uiState.asStateFlow()

    init {
        loadCategories()

        // Check if we're editing an existing drop
        savedStateHandle.get<String>("dropId")?.let { dropId ->
            if (dropId.isNotEmpty()) {
                _uiState.update { it.copy(id = dropId, isEditing = true) }
                // In a real app, we would load the drop details here
            }
        }
    }

    fun onEvent(event: DropEvent) {
        when (event) {
            is DropEvent.UpdateTitle -> updateTitle(event.title)
            is DropEvent.UpdateText -> updateText(event.text)
            is DropEvent.UpdateCategory -> updateCategory(event.categoryId)
            is DropEvent.UpdateMedia -> updateMedia(event.uri)
            is DropEvent.UpdateType -> updateType(event.type)
            is DropEvent.UpdateTags -> updateTags(event.tags)
            is DropEvent.TogglePin -> togglePin()
            is DropEvent.Save -> saveDrop()
            is DropEvent.Delete -> deleteDrop()
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                getCategoriesUseCase().collect { categories ->
                    _uiState.update { currentState ->
                        // Select the first category by default if none is selected
                        val updatedState = currentState.copy(
                            availableCategories = categories,
                            isLoading = false
                        )

                        if (updatedState.categoryId.isEmpty() && categories.isNotEmpty()) {
                            updatedState.copy(categoryId = categories.first().id)
                        } else {
                            updatedState
                        }
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

    private fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    private fun updateText(text: String) {
        _uiState.update { it.copy(text = text) }
    }

    private fun updateCategory(categoryId: String) {
        _uiState.update { it.copy(categoryId = categoryId) }
    }

    private fun updateMedia(uri: Uri) {
        _uiState.update { it.copy(mediaUri = uri) }
    }

    private fun updateType(type: DropType) {
        _uiState.update { it.copy(dropType = type) }
    }

    private fun updateTags(tags: List<String>) {
        _uiState.update { it.copy(tags = tags) }
    }

    private fun togglePin() {
        _uiState.update { it.copy(isPinned = !it.isPinned) }
    }

    private fun saveDrop() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                val currentState = _uiState.value

                val drop = Drop(
                    id = currentState.id ?: UUID.randomUUID().toString(),
                    type = currentState.dropType,
                    uri = currentState.mediaUri?.toString(),
                    text = currentState.text.ifEmpty { null },
                    title = currentState.title.ifEmpty { null },
                    categoryId = currentState.categoryId,
                    isPinned = currentState.isPinned,
                    tags = currentState.tags
                )

                createDropUseCase(drop)

                _uiState.update { it.copy(isLoading = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        error = "Failed to save drop: ${e.localizedMessage}",
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun deleteDrop() {
        viewModelScope.launch {
            try {
                _uiState.value.id?.let { dropId ->
                    _uiState.update { it.copy(isLoading = true) }
                    deleteDropUseCase(dropId)
                    _uiState.update { it.copy(isLoading = false, isSaved = true) }
                }
            } catch (e: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        error = "Failed to delete drop: ${e.localizedMessage}",
                        isLoading = false
                    )
                }
            }
        }
    }
}