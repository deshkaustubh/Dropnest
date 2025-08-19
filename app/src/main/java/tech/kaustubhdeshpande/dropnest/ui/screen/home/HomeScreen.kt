package tech.kaustubhdeshpande.dropnest.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tech.kaustubhdeshpande.dropnest.ui.theme.DropnestTheme

@OptIn(ExperimentalMaterial3Api::class)
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
        // if i had not removed this it was covering up the entire screen including the bottom system nav bar
//        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "DropNest",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                modifier = Modifier
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp)
            ) {
                // Add some spacing at the top
                item { Spacer(modifier = Modifier.height(8.dp)) }

                // Default categories grid
                item {
                    DefaultCategoriesGrid(
                        onSavedLinksClick = { onCategoryClick("links") },
                        onNotesClick = { onCategoryClick("notes") },
                        onImagesClick = { onCategoryClick("images") },
                        onPdfsClick = { onCategoryClick("pdfs") }
                    )
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }

                // Custom Categories Title
                item {
                    Text(
                        text = "Custom Categories",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                // Custom categories section
                item {
                    CustomCategoriesSection(
                        categories = uiState.customCategories,
                        isLoading = uiState.isLoading,
                        onCategoryClick = onCategoryClick,
                        onCreateCategoryClick = onCreateCategoryClick,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Space at the bottom
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }

            FloatingActionButton(
                onClick = { onCreateCategoryClick() },
                containerColor = MaterialTheme.colorScheme.tertiary,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        bottom = paddingValues.calculateBottomPadding() + 64.dp,
                        end = 16.dp
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add New Item",
                    tint = MaterialTheme.colorScheme.onTertiary
                )
            }
        }
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