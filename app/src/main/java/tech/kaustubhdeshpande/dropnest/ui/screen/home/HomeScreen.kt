package tech.kaustubhdeshpande.dropnest.ui.screen.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import tech.kaustubhdeshpande.dropnest.R
import tech.kaustubhdeshpande.dropnest.ui.theme.DropnestTheme

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onAddCategoryClick() },
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
                .padding(16.dp)
        ) {
            // Header section
            HeaderSection()

            Spacer(modifier = Modifier.height(16.dp))

            // Default categories grid
            DefaultCategoriesGrid(
                onSavedLinksClick = { viewModel.onDefaultCategoryClick(DefaultCategoryType.SAVED_LINKS) },
                onNotesClick = { viewModel.onDefaultCategoryClick(DefaultCategoryType.NOTES) },
                onImagesClick = { viewModel.onDefaultCategoryClick(DefaultCategoryType.IMAGES) },
                onPdfsClick = { viewModel.onDefaultCategoryClick(DefaultCategoryType.PDFS) }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Custom categories section
            CustomCategoriesSection(
                categories = uiState.customCategories,
                isLoading = uiState.isLoading,
                onCategoryClick = { viewModel.onCategoryClick(it) },
                onCreateCategoryClick = { viewModel.onCreateCategoryClick() }
            )
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

@Composable
fun CustomCategoriesSection(
    categories: List<Category>,
    isLoading: Boolean,
    onCategoryClick: (String) -> Unit,
    onCreateCategoryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "Custom Categories",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            // Show loading indicator
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.tertiary)
            }
        } else if (categories.isEmpty()) {
            // Show empty state
            EmptyCustomCategoriesState(onCreateCategoryClick)
        } else {
            // Show grid of custom categories
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { category ->
                    CustomCategoryCard(
                        category = category,
                        onClick = { onCategoryClick(category.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyCustomCategoriesState(onCreateCategoryClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Placeholder image
        Image(
            painter = painterResource(id = R.drawable.empty_category_placeholder),
            contentDescription = "Empty categories placeholder",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No categories yet",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Create one to start organizing.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onCreateCategoryClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .height(56.dp)
                .width(240.dp)
        ) {
            Text(
                text = "Create Category",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun CustomCategoryCard(
    category: Category,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Convert the color Long to Color
    val categoryColor = Color(category.color)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
            .height(120.dp)
            .border(
                width = 1.dp,
                color = categoryColor,
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
            // For a real implementation, you might want to dynamically load icons
            // For now we'll just use a default icon
            Icon(
                imageVector = Icons.Outlined.Folder,
                contentDescription = category.name,
                tint = categoryColor,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = category.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    // Create the ViewModel outside the composable function
    val viewModel = HomeViewModelPreviewParameterProvider.createEmptyViewModel()

    DropnestTheme(darkTheme = true) {
        HomeScreen(viewModel = viewModel)
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenWithCategoriesPreview() {
    // Create the ViewModel outside the composable function
    val viewModel = HomeViewModelPreviewParameterProvider.createViewModelWithCategories()

    DropnestTheme(darkTheme = true) {
        HomeScreen(viewModel = viewModel)
    }
}