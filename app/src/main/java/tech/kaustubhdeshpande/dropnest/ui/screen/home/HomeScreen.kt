package tech.kaustubhdeshpande.dropnest.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tech.kaustubhdeshpande.dropnest.ui.theme.DropnestTheme

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onCreateCategoryClick: () -> Unit = {},
    onCategoryClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onCreateCategoryClick() },
                containerColor = MaterialTheme.colorScheme.tertiary,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add New Item",
                    tint = MaterialTheme.colorScheme.onTertiary
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header section
            HeaderSection()

            Spacer(modifier = Modifier.height(16.dp))

            // Default categories grid - updated to use onCategoryClick
            DefaultCategoriesGrid(
                onSavedLinksClick = {
                    // Navigate to category detail instead of just updating ViewModel
                    onCategoryClick("links")
                },
                onNotesClick = {
                    onCategoryClick("notes")
                },
                onImagesClick = {
                    onCategoryClick("images")
                },
                onPdfsClick = {
                    onCategoryClick("pdfs")
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Custom categories section
            CustomCategoriesSection(
                categories = uiState.customCategories,
                isLoading = uiState.isLoading,
                onCategoryClick = onCategoryClick,
                onCreateCategoryClick = onCreateCategoryClick
            )

            // Add space at the bottom to prevent FAB from covering content
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun HeaderSection(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Your Categories",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Organize your drops with custom tags and colors.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun DefaultCategoriesGrid(
    onSavedLinksClick: () -> Unit,
    onNotesClick: () -> Unit,
    onImagesClick: () -> Unit,
    onPdfsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CategoryCard(
                icon = Icons.Outlined.Bookmark,
                title = "Saved Links",
                onClick = onSavedLinksClick,
                modifier = Modifier.weight(1f)
            )
            CategoryCard(
                icon = Icons.Outlined.Notes,
                title = "Notes",
                onClick = onNotesClick,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CategoryCard(
                icon = Icons.Outlined.Image,
                title = "Images",
                onClick = onImagesClick,
                modifier = Modifier.weight(1f)
            )
            CategoryCard(
                icon = Icons.Outlined.PictureAsPdf,
                title = "PDFs",
                onClick = onPdfsClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun CategoryCard(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
            .height(90.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    val viewModel = HomeViewModelPreviewParameterProvider.createEmptyViewModel()
    DropnestTheme(darkTheme = true) {
        HomeScreen(viewModel = viewModel)
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenWithCategoriesPreview() {
    val viewModel = HomeViewModelPreviewParameterProvider.createViewModelWithCategories()
    DropnestTheme(darkTheme = true) {
        HomeScreen(viewModel = viewModel)
    }
}