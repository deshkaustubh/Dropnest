package tech.kaustubhdeshpande.dropnest.ui.screen.home

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

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
                Category("1", "Work", 0xFF4CAF50),
                Category("2", "Personal", 0xFF2196F3),
                Category("3", "School", 0xFFFF9800),
                Category("4", "Projects", 0xFFE91E63)
            )
            val viewModel = PreviewHomeViewModel()
            viewModel.setCategories(mockCategories)
            return viewModel
        }
    }
}