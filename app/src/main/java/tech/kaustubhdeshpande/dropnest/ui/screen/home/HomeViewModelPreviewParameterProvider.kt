package tech.kaustubhdeshpande.dropnest.ui.screen.home

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import tech.kaustubhdeshpande.dropnest.domain.model.Category
import java.util.UUID

class HomeViewModelPreviewParameterProvider : PreviewParameterProvider<HomeViewModel> {
    override val values = sequenceOf(
        createEmptyViewModel(),
        createViewModelWithCategories()
    )

    companion object {
        fun createEmptyViewModel(): PreviewHomeViewModel {
            return PreviewHomeViewModel()
        }

        fun createViewModelWithCategories(): PreviewHomeViewModel {
            val mockCategories = listOf(
                Category(
                    id = UUID.randomUUID().toString(),
                    name = "Work",
                    colorHex = "#4CAF50",
                    emoji = "Work",
                    isDefault = false,
                    timestamp = System.currentTimeMillis()
                ),
                Category(
                    id = UUID.randomUUID().toString(),
                    name = "Personal",
                    colorHex = "#2196F3",
                    emoji = "Person",
                    isDefault = false,
                    timestamp = System.currentTimeMillis()
                ),
                Category(
                    id = UUID.randomUUID().toString(),
                    name = "School",
                    colorHex = "#FF9800",
                    emoji = "School",
                    isDefault = false,
                    timestamp = System.currentTimeMillis()
                ),
                Category(
                    id = UUID.randomUUID().toString(),
                    name = "Projects",
                    colorHex = "#E91E63",
                    emoji = "Code",
                    isDefault = false,
                    timestamp = System.currentTimeMillis()
                )
            )
            val viewModel = PreviewHomeViewModel()
            viewModel.setCategories(mockCategories)
            return viewModel
        }
    }
}