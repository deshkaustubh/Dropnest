package tech.kaustubhdeshpande.dropnest.ui.screen.category.detail

import android.app.Application
import android.net.Uri
import android.util.Log
import android.webkit.URLUtil
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
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
import tech.kaustubhdeshpande.dropnest.util.FileManager
import java.util.UUID
import javax.inject.Inject

private const val TAG = "CategoryDetailViewModel"

@HiltViewModel
class CategoryDetailViewModel @Inject constructor(
    application: Application,
    private val getCategoryByIdUseCase: GetCategoryByIdUseCase,
    private val getDropsByCategoryUseCase: GetDropsByCategoryUseCase,
    private val createDropUseCase: CreateDropUseCase
) : AndroidViewModel(application) {

    private val fileManager = FileManager(getApplication())
    private val _uiState = MutableStateFlow(CategoryDetailState())
    val uiState: StateFlow<CategoryDetailState> = _uiState.asStateFlow()

    private var loadCategoryJob : Job? = null
    private var loadDropsJob : Job? = null

    fun loadCategory(categoryId: String) {
        loadCategoryJob?.cancel()
        loadCategoryJob =  viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val category = getCategoryByIdUseCase(categoryId)
                category?.let {
                    _uiState.update { state -> state.copy(category = it) }
                }
            } catch (e: CancellationException) {
                // Don't log cancellation as error
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Error loading category", e)
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun loadDrops(categoryId: String) {
        loadDropsJob?.cancel()
        loadDropsJob = viewModelScope.launch {
            try {
                getDropsByCategoryUseCase(categoryId).collect { drops ->
                    _uiState.update { state -> state.copy(drops = drops) }
                }
            } catch (e: CancellationException) {
                // Don't log cancellation as error
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Error loading drops", e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        loadCategoryJob?.cancel()
        loadDropsJob?.cancel()
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
            tags = emptyList(),
            mimeType = if (isUrl) "text/uri-list" else "text/plain"
        )

        saveDrop(drop)

        // Clear input
        _uiState.update { it.copy(inputText = "") }
    }

    fun saveDrop(drop: Drop) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Saving drop: ${drop.id}, type: ${drop.type}")
                createDropUseCase(drop)
                Log.d(TAG, "Drop saved successfully")

                // Refresh the drops list
                loadDrops(drop.categoryId)
            } catch (e: Exception) {
                Log.e(TAG, "Error saving drop: ${e.message}", e)
            }
        }
    }

    fun saveImageDrop(sourceUriString: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Saving image from URI: $sourceUriString")
                val categoryId = _uiState.value.category?.id ?: run {
                    Log.e(TAG, "Cannot save image: Category ID is null")
                    return@launch
                }

                val sourceUri = Uri.parse(sourceUriString)
                val mimeType = fileManager.getMimeType(sourceUri)

                // Save the file to app storage
                val savedFilePath = fileManager.saveFileToAppStorage(sourceUri, "images")

                if (savedFilePath != null) {
                    val drop = Drop(
                        id = UUID.randomUUID().toString(),
                        type = DropType.IMAGE,
                        text = null,
                        title = "Image",
                        uri = savedFilePath, // Use the saved file path
                        categoryId = categoryId,
                        timestamp = System.currentTimeMillis(),
                        isPinned = false,
                        tags = emptyList(),
                        mimeType = mimeType
                    )

                    Log.d(TAG, "Created image drop with ID: ${drop.id}, path: $savedFilePath")
                    saveDrop(drop)
                } else {
                    Log.e(TAG, "Failed to save image to internal storage: savedFilePath is null")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception while saving image drop: ${e.message}", e)
            }
        }
    }

    fun savePdfDrop(sourceUriString: String) {
        saveDocumentDrop(sourceUriString) // Redirect to more general document handler
    }

    fun saveDocumentDrop(sourceUriString: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Saving document from URI: $sourceUriString")
                val categoryId = _uiState.value.category?.id ?: run {
                    Log.e(TAG, "Cannot save document: Category ID is null")
                    return@launch
                }

                val sourceUri = Uri.parse(sourceUriString)

                // Get file metadata
                val mimeType = fileManager.getMimeType(sourceUri)
                val fileName = fileManager.getFileName(sourceUri) ?: "Document"
                val documentType = fileManager.getDocumentTypeName(mimeType)

                // Save the file to app storage in "documents" directory
                val savedFilePath = fileManager.saveFileToAppStorage(sourceUri, "documents")

                if (savedFilePath != null) {
                    val drop = Drop(
                        id = UUID.randomUUID().toString(),
                        type = DropType.DOCUMENT,
                        text = null,
                        title = fileName,
                        uri = savedFilePath,
                        categoryId = categoryId,
                        timestamp = System.currentTimeMillis(),
                        isPinned = false,
                        tags = listOf(documentType), // Add document type as a tag
                        mimeType = mimeType
                    )

                    Log.d(TAG, "Created document drop with ID: ${drop.id}, path: $savedFilePath, type: $mimeType")
                    saveDrop(drop)
                } else {
                    Log.e(TAG, "Failed to save document to internal storage: savedFilePath is null")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception while saving document drop: ${e.message}", e)
            }
        }
    }
}

data class CategoryDetailState(
    val category: Category? = null,
    val drops: List<Drop> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false
)