package tech.kaustubhdeshpande.dropnest.presentation.event

import android.net.Uri
import tech.kaustubhdeshpande.dropnest.domain.model.DropType

sealed class DropEvent {
    data class UpdateTitle(val title: String) : DropEvent()
    data class UpdateText(val text: String) : DropEvent()
    data class UpdateCategory(val categoryId: String) : DropEvent()
    data class UpdateMedia(val uri: Uri) : DropEvent()
    data class UpdateType(val type: DropType) : DropEvent()
    data class UpdateTags(val tags: List<String>) : DropEvent()
    data object TogglePin : DropEvent()
    data object Save : DropEvent()
    data object Delete : DropEvent()
}